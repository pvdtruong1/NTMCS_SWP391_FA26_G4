package controller;

import dal.AuditLogDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import model.User;
import util.ApiUtil;
import util.FormUtil;
import util.Json;
import util.PasswordUtil;

/**
 * JSON version of UserServlet (UC-06). The rules are the same as UserServlet.
 *   POST /api/users  action=create | update | status
 *     create : full_name, email, password, role_id, desk_id (optional)
 *     update : id, full_name, role_id, desk_id (optional), password (optional = keep the old one)
 *     status : id, active (true = unlock, false = lock)
 */
@WebServlet(name = "ApiUserServlet", urlPatterns = {"/api/users"})
public class ApiUserServlet extends HttpServlet {

    private static final String PERMISSION = "USER_MANAGE";

    private final UserDAO userDAO = new UserDAO();
    private final AuditLogDAO auditDAO = new AuditLogDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User admin = ApiUtil.requirePermission(request, response, PERMISSION);
        if (admin == null) {
            return;
        }
        String action = request.getParameter("action");

        if ("create".equals(action)) {
            createUser(request, response, admin);
        } else if ("update".equals(action)) {
            updateUser(request, response, admin);
        } else if ("status".equals(action)) {
            changeActive(request, response, admin);
        } else {
            ApiUtil.error(response, 400, "Unknown action.");
        }
    }

    // ---------------------------------------------------------------- create
    private void createUser(HttpServletRequest request, HttpServletResponse response, User admin)
            throws IOException {
        User user = readForm(request);
        String password = FormUtil.text(request, "password");

        String error = null;
        if (user.getFullName().isEmpty()) {
            error = "Full name is required.";
        } else if (!user.getEmail().contains("@")) {
            error = "Email is not valid.";
        } else if (password.length() < 6) {
            error = "Password must be at least 6 characters.";
        } else if (user.getRoleId() <= 0) {
            error = "Please choose a role.";
        } else if (userDAO.emailExists(user.getEmail())) {
            error = "This email is already used.";
        }
        if (error != null) {
            ApiUtil.error(response, 400, error);
            return;
        }

        user.setPasswordHash(PasswordUtil.hash(password));
        user.setActiveStatus(true);
        if (!userDAO.insert(user)) {
            ApiUtil.error(response, 500, "Could not save the account. Please try again.");
            return;
        }

        User saved = userDAO.getByEmail(user.getEmail());
        Integer savedId = (saved == null) ? null : saved.getUserId();
        auditDAO.log(admin.getUserId(), "CREATE_USER", "USER", savedId,
                "Created account " + user.getEmail());
        ApiUtil.send(response, 200, Json.obj("ok", true, "user_id", savedId));
    }

    // ---------------------------------------------------------------- update
    private void updateUser(HttpServletRequest request, HttpServletResponse response, User admin)
            throws IOException {
        User form = readForm(request);
        User old = userDAO.getById(form.getUserId());
        if (old == null) {
            ApiUtil.error(response, 404, "Account not found.");
            return;
        }
        String newPassword = FormUtil.text(request, "password");   // blank = keep old password

        String error = null;
        if (form.getFullName().isEmpty()) {
            error = "Full name is required.";
        } else if (form.getRoleId() <= 0) {
            error = "Please choose a role.";
        } else if (!newPassword.isEmpty() && newPassword.length() < 6) {
            error = "New password must be at least 6 characters.";
        } else if (old.getUserId() == admin.getUserId() && form.getRoleId() != old.getRoleId()) {
            error = "You cannot change your own role.";
        }
        if (error != null) {
            ApiUtil.error(response, 400, error);
            return;
        }

        old.setFullName(form.getFullName());
        old.setRoleId(form.getRoleId());
        old.setDeskId(form.getDeskId());
        boolean ok = userDAO.update(old);
        if (ok && !newPassword.isEmpty()) {
            ok = userDAO.updatePassword(old.getUserId(), PasswordUtil.hash(newPassword));
        }
        if (!ok) {
            ApiUtil.error(response, 500, "Could not save the changes. Please try again.");
            return;
        }

        String details = "Updated account " + old.getEmail();
        if (!newPassword.isEmpty()) {
            details += " (password reset)";
        }
        auditDAO.log(admin.getUserId(), "UPDATE_USER", "USER", old.getUserId(), details);
        ApiUtil.send(response, 200, Json.obj("ok", true));
    }

    // ------------------------------------------------------------ lock/unlock
    private void changeActive(HttpServletRequest request, HttpServletResponse response, User admin)
            throws IOException {
        int id = FormUtil.toInt(request.getParameter("id"));
        boolean active = "true".equals(request.getParameter("active"));

        if (id == admin.getUserId()) {
            ApiUtil.error(response, 400, "You cannot lock your own account.");
            return;
        }
        User target = userDAO.getById(id);
        if (target == null) {
            ApiUtil.error(response, 404, "Account not found.");
            return;
        }
        if (!userDAO.setActive(id, active)) {
            ApiUtil.error(response, 500, "Could not change the account. Please try again.");
            return;
        }
        String action = active ? "UNLOCK_USER" : "LOCK_USER";
        String word = active ? "Unlocked" : "Locked";
        auditDAO.log(admin.getUserId(), action, "USER", id, word + " account " + target.getEmail());
        ApiUtil.send(response, 200, Json.obj("ok", true));
    }

    /** Reads id, full_name, email, role_id, desk_id from the request into a User. */
    private User readForm(HttpServletRequest request) {
        User u = new User();
        u.setUserId(FormUtil.toInt(request.getParameter("id")));
        u.setFullName(FormUtil.text(request, "full_name"));
        u.setEmail(FormUtil.text(request, "email"));
        u.setRoleId(FormUtil.toInt(request.getParameter("role_id")));
        String desk = FormUtil.text(request, "desk_id");
        if (!desk.isEmpty()) {
            u.setDeskId(FormUtil.toInt(desk));
        }
        return u;
    }
}
