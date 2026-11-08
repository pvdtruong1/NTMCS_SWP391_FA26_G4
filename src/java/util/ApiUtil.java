package util;

import dal.PermissionDAO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Map;
import model.User;

/**
 * Helpers shared by the /api servlets (the JSON side used by the React front end).
 * The /api servlets use the same session ("loggedUser") and the same permission checks
 * as the JSP servlets, so both sides always agree on who is logged in.
 */
public class ApiUtil {

    /** Sends a JSON body with an HTTP status. */
    public static void send(HttpServletResponse response, int status, Object body) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write(Json.toJson(body));
    }

    /** Sends {"error": "message"} with an HTTP status. The front end shows the message. */
    public static void error(HttpServletResponse response, int status, String message) throws IOException {
        send(response, status, Json.obj("error", message));
    }

    /** The logged-in user, or null after sending 401 (not logged in). */
    public static User requireLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = AuthUtil.getLoggedUser(request);
        if (user == null) {
            error(response, 401, "Not logged in.");
            return null;
        }
        return user;
    }

    /** The logged-in user if their role has the permission, otherwise null after sending 401 or 403. */
    public static User requirePermission(HttpServletRequest request, HttpServletResponse response,
            String permissionCode) throws IOException {
        User user = requireLogin(request, response);
        if (user == null) {
            return null;
        }
        if (!new PermissionDAO().roleHasPermission(user.getRoleId(), permissionCode)) {
            error(response, 403, "You do not have permission for this action.");
            return null;
        }
        return user;
    }

    /** A user as JSON. The password hash is NEVER sent to the browser. */
    public static Map<String, Object> userJson(User u) {
        return Json.obj(
                "user_id", u.getUserId(),
                "full_name", u.getFullName(),
                "email", u.getEmail(),
                "role_id", u.getRoleId(),
                "role_name", u.getRoleName(),
                "active_status", u.isActiveStatus(),
                "desk_id", u.getDeskId());
    }

    /** A timestamp as "2026-10-12 08:30:00", the format the front end shows. */
    public static String timeText(Timestamp time) {
        if (time == null) {
            return null;
        }
        String text = time.toString();
        return text.length() >= 19 ? text.substring(0, 19) : text;
    }
}
