package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public final class PasswordUtil {

    private PasswordUtil() {
    }

    public static boolean verifyPassword(
            String inputPassword,
            String storedPassword) {

        if (inputPassword == null || storedPassword == null) {
            return false;
        }

        if (storedPassword.isEmpty()) {
            return false;
        }

        // Trường hợp password_hash là SHA-256.
        if (storedPassword.matches("(?i)[0-9a-f]{64}")) {

            String inputHash = sha256(inputPassword);

            return constantTimeEquals(
                    inputHash,
                    storedPassword.toLowerCase(Locale.ROOT)
            );
        }

        // Tương thích với tài khoản phát triển đang lưu mật khẩu dạng chữ thường.
        return constantTimeEquals(inputPassword, storedPassword);
    }

    public static String sha256(String value) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder result = new StringBuilder();

            for (byte b : hash) {
                result.append(String.format("%02x", b & 0xff));
            }

            return result.toString();

        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                    "Không thể thực hiện SHA-256.",
                    ex
            );
        }
    }

    private static boolean constantTimeEquals(
            String first,
            String second) {

        return MessageDigest.isEqual(
                first.getBytes(StandardCharsets.UTF_8),
                second.getBytes(StandardCharsets.UTF_8)
        );
    }
}