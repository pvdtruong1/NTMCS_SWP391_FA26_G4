package controller;

import dal.PermissionDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import model.User;
import util.AuthUtil;
import util.PasswordUtil;

/**
 * Login screen.
 *   GET  /login -> login form (or straight to /home if already logged in)
 *   POST /login -> check email + password, start the session, go to /home
 * The session keeps two things:
 *   "loggedUser"  = the model.User who logged in
 *   "permissions" = the permission codes of that user's role (used by the menu)
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final PermissionDAO permissionDAO = new PermissionDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (AuthUtil.getLoggedUser(request) != null) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        if (email == null || password == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // 1. find the account by email, 2. it must be active, 3. password must match
        User user = userDAO.getByEmail(email.trim());
        boolean ok = user != null
                && user.isActiveStatus()
                && PasswordUtil.matches(password, user.getPasswordHash());

        if (!ok) {
            // same message for every failure, so nobody can find out which emails exist
            request.setAttribute("error", "Wrong email or password, or the account is locked.");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            return;
        }

        HttpSession session = request.getSession();
        session.setAttribute("loggedUser", user);
        session.setAttribute("user", user);
        session.setAttribute("permissions", permissionDAO.getCodesByRole(user.getRoleId()));
        response.sendRedirect(request.getContextPath() + "/home");
    }
}
