package util;

import jakarta.servlet.http.HttpServletRequest;

/** Small helpers for reading form fields. */
public class FormUtil {

    /** Trimmed text of a parameter, never null. */
    public static String text(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return (value == null) ? "" : value.trim();
    }

    /** Text to number. Returns 0 if it is empty or not a valid number. */
    public static int toInt(String text) {
        if (text == null) {
            return 0;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
