package controller;

import dal.AuditLogDAO;
import dal.DeskDAO;
import dal.RoleDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import model.User;
import util.AuthUtil;
import util.PasswordUtil;

/**
 * UC-06 Manage User Account (SCR-5.6).
 *   GET  /users                 -> list of accounts
 *   GET  /users?action=new      -> empty form
 *   GET  /users?action=edit&id= -> form filled with one account
 *   POST /users  action=create | update | lock | unlock
 */
@WebServlet(name = "UserServlet", urlPatterns = {"/users"})
public class UserServlet extends HttpServlet {

    private static final String PERMISSION = "USER_MANAGE";

    private final UserDAO userDAO = new UserDAO();
    private final RoleDAO roleDAO = new RoleDAO();
    private final DeskDAO deskDAO = new DeskDAO();
    private final AuditLogDAO auditDAO = new AuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!AuthUtil.check(request, response, PERMISSION)) {
            return;
        }
        String action = request.getParameter("action");

        if ("new".equals(action)) {
            showForm(request, response, new User());
        } else if ("edit".equals(action)) {
            User user = userDAO.getById(toInt(request.getParameter("id")));
            if (user == null) {
                response.sendRedirect(request.getContextPath() + "/users");
                return;
            }
            showForm(request, response, user);
        } else {
            request.setAttribute("userList", userDAO.getAll());
            request.getRequestDispatcher("/WEB-INF/views/user-list.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!AuthUtil.check(request, response, PERMISSION)) {
            return;
        }
        String action = request.getParameter("action");

        if ("create".equals(action)) {
            createUser(request, response);
        } else if ("update".equals(action)) {
            updateUser(request, response);
        } else if ("lock".equals(action)) {
            changeActive(request, response, false);
        } else if ("unlock".equals(action)) {
            changeActive(request, response, true);
        } else {
            response.sendRedirect(request.getContextPath() + "/users");
        }
    }

    // ---------------------------------------------------------------- create
    private void createUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User admin = AuthUtil.getLoggedUser(request);
        User user = readForm(request);
        String password = text(request, "password");

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
            request.setAttribute("error", error);
            showForm(request, response, user);
            return;
        }

        user.setPasswordHash(PasswordUtil.hash(password));
        user.setActiveStatus(true);
        if (!userDAO.insert(user)) {
            request.setAttribute("error", "Could not save the account. Please try again.");
            showForm(request, response, user);
            return;
        }

        User saved = userDAO.getByEmail(user.getEmail());
        Integer savedId = (saved == null) ? null : saved.getUserId();
        auditDAO.log(admin.getUserId(), "CREATE_USER", "USER", savedId,
                "Created account " + user.getEmail());
        response.sendRedirect(request.getContextPath() + "/users?msg=created");
    }

    // ---------------------------------------------------------------- update
    private void updateUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User admin = AuthUtil.getLoggedUser(request);
        User form = readForm(request);
        User old = userDAO.getById(form.getUserId());
        if (old == null) {
            response.sendRedirect(request.getContextPath() + "/users");
            return;
        }
        String newPassword = text(request, "password");   // blank = keep old password

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
            form.setEmail(old.getEmail());   // email is not editable, keep showing it
            request.setAttribute("error", error);
            showForm(request, response, form);
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
            form.setEmail(old.getEmail());
            request.setAttribute("error", "Could not save the changes. Please try again.");
            showForm(request, response, form);
            return;
        }

        String details = "Updated account " + old.getEmail();
        if (!newPassword.isEmpty()) {
            details += " (password reset)";
        }
        auditDAO.log(admin.getUserId(), "UPDATE_USER", "USER", old.getUserId(), details);
        response.sendRedirect(request.getContextPath() + "/users?msg=updated");
    }

    // ------------------------------------------------------------ lock/unlock
    private void changeActive(HttpServletRequest request, HttpServletResponse response, boolean active)
            throws IOException {
        User admin = AuthUtil.getLoggedUser(request);
        int id = toInt(request.getParameter("id"));

        if (id == admin.getUserId()) {
            response.sendRedirect(request.getContextPath() + "/users?msg=selflock");
            return;
        }
        User target = userDAO.getById(id);
        if (target != null && userDAO.setActive(id, active)) {
            String action = active ? "UNLOCK_USER" : "LOCK_USER";
            String word = active ? "Unlocked" : "Locked";
            auditDAO.log(admin.getUserId(), action, "USER", id, word + " account " + target.getEmail());
        }
        response.sendRedirect(request.getContextPath() + "/users?msg=" + (active ? "unlocked" : "locked"));
    }

    // ---------------------------------------------------------------- helpers
    private void showForm(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        request.setAttribute("editUser", user);
        request.setAttribute("roleList", roleDAO.getAll());
        request.setAttribute("deskList", deskDAO.getAll());
        request.getRequestDispatcher("/WEB-INF/views/user-form.jsp").forward(request, response);
    }

    /** Reads id, fullName, email, roleId, deskId from the form into a User. */
    private User readForm(HttpServletRequest request) {
        User u = new User();
        u.setUserId(toInt(request.getParameter("id")));
        u.setFullName(text(request, "fullName"));
        u.setEmail(text(request, "email"));
        u.setRoleId(toInt(request.getParameter("roleId")));
        String desk = text(request, "deskId");
        if (!desk.isEmpty()) {
            u.setDeskId(toInt(desk));
        }
        return u;
    }

    /** Trimmed text of a parameter, never null. */
    private String text(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return (value == null) ? "" : value.trim();
    }

    /** Text to number. Returns 0 if it is not a valid number. */
    private int toInt(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
