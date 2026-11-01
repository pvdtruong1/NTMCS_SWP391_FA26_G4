package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Password helper (SHA-256 is built into Java, no extra jar needed).
 * To switch to BCrypt later, only hash() and matches() have to change.
 */
public class PasswordUtil {

    public static String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Does the typed password match what is stored in the database?
     *  - Accounts created by our User screen store a SHA-256 hash (64 hex characters).
     *  - The group's seed data stores the password as plain text (e.g. 123).
     * Both kinds are accepted, so seeded and newly created accounts can log in.
     */
    public static boolean matches(String typedPassword, String storedValue) {
        if (typedPassword == null || storedValue == null) {
            return false;
        }
        if (looksLikeHash(storedValue)) {
            return hash(typedPassword).equals(storedValue);
        }
        return typedPassword.equals(storedValue);   // plain text from the seed data
    }

    private static boolean looksLikeHash(String value) {
        return value.length() == 64 && value.matches("[0-9a-f]+");
    }
}
