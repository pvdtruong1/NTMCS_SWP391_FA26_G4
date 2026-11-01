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
import java.util.ArrayList;
import java.util.Set;
import model.User;
import util.ApiUtil;
import util.FormUtil;
import util.Json;
import util.PasswordUtil;

/**
 * JSON version of LoginServlet / LogoutServlet, for the React front end.
 *   POST /api/auth/login   (email, password)
 *   POST /api/auth/logout
 *   GET  /api/auth/me      who is logged in now
 * The check is the same as LoginServlet: the account exists, is active, and the password matches.
 */
@WebServlet(name = "ApiAuthServlet", urlPatterns = {"/api/auth/*"})
public class ApiAuthServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final PermissionDAO permissionDAO = new PermissionDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String path = request.getPathInfo();

        if ("/login".equals(path)) {
            login(request, response);
        } else if ("/logout".equals(path)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            ApiUtil.send(response, 200, Json.obj("ok", true));
        } else {
            ApiUtil.error(response, 404, "Unknown address.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!"/me".equals(request.getPathInfo())) {
            ApiUtil.error(response, 404, "Unknown address.");
            return;
        }
        User user = ApiUtil.requireLogin(request, response);
        if (user == null) {
            return;
        }
        // read the permissions again, so a change in the RBAC matrix shows up
        Set<String> codes = permissionDAO.getCodesByRole(user.getRoleId());
        request.getSession().setAttribute("permissions", codes);
        ApiUtil.send(response, 200, Json.obj(
                "user", ApiUtil.userJson(user),
                "permissions", new ArrayList<>(codes)));
    }

    private void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = FormUtil.text(request, "email");
        String password = request.getParameter("password");
        if (password == null) {
            password = "";
        }

        // 1. find the account by email, 2. it must be active, 3. password must match
        User user = userDAO.getByEmail(email);
        boolean ok = user != null
                && user.isActiveStatus()
                && PasswordUtil.matches(password, user.getPasswordHash());
        if (!ok) {
            // same message for every failure, so nobody can find out which emails exist
            ApiUtil.error(response, 401, "Wrong email or password, or the account is locked.");
            return;
        }

        Set<String> codes = permissionDAO.getCodesByRole(user.getRoleId());
        HttpSession session = request.getSession();
        session.setAttribute("loggedUser", user);
        session.setAttribute("permissions", codes);
        ApiUtil.send(response, 200, Json.obj(
                "user", ApiUtil.userJson(user),
                "permissions", new ArrayList<>(codes)));
    }
}
