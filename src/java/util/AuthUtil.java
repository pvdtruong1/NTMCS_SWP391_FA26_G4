package util;

import dal.PermissionDAO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import model.User;

/**
 * Every protected servlet starts with:
 *     if (!AuthUtil.check(request, response, "SOME_PERMISSION")) return;
 */
public class AuthUtil {

    /** The logged-in user, or null if nobody is logged in. */
    public static User getLoggedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute("loggedUser");
    }

    /**
     * true  = user is logged in and has the permission, the servlet may continue.
     * false = a redirect (login) or an error (403) was already sent, the servlet must stop.
     */
    public static boolean check(HttpServletRequest request, HttpServletResponse response,
            String permissionCode) throws IOException {
        User user = getLoggedUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        PermissionDAO permissionDAO = new PermissionDAO();
        if (!permissionDAO.roleHasPermission(user.getRoleId(), permissionCode)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "You do not have permission to open this page.");
            return false;
        }
        return true;
    }
}
