package util;

import java.util.Collections;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import model.User;

public final class AuthUtil {

    private AuthUtil() {
    }

    public static User getLoggedUser(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        Object value = session.getAttribute("loggedUser");

        if (value instanceof User) {
            return (User) value;
        }

        return null;
    }

    public static boolean isLoggedIn(HttpServletRequest request) {
        return getLoggedUser(request) != null;
    }

    public static Set<String> getPermissions(
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return Collections.emptySet();
        }

        Object value = session.getAttribute("permissions");

        if (!(value instanceof Set<?>)) {
            return Collections.emptySet();
        }

        @SuppressWarnings("unchecked")
        Set<String> permissions = (Set<String>) value;

        return permissions;
    }

    public static boolean hasPermission(
            HttpServletRequest request,
            String permissionCode) {

        if (permissionCode == null || permissionCode.trim().isEmpty()) {
            return false;
        }

        return getPermissions(request).contains(permissionCode.trim());
    }
}