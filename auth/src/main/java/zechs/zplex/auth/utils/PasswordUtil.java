package zechs.zplex.auth.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class PasswordUtil {

    private static final String LEGACY_HASH_ALGORITHM = "SHA-256";
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private PasswordUtil() {
    }

    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    // Verifies BCrypt hashes, falling back to the legacy unsalted SHA-256 scheme for un-migrated accounts.
    public static boolean matches(String rawPassword, String storedHash) {
        if (storedHash == null) {
            return false;
        }
        if (isBcrypt(storedHash)) {
            return ENCODER.matches(rawPassword, storedHash);
        }
        return MessageDigest.isEqual(legacyHash(rawPassword).getBytes(), storedHash.getBytes());
    }

    // Legacy hashes should be upgraded to BCrypt after a successful verification.
    public static boolean needsRehash(String storedHash) {
        return !isBcrypt(storedHash);
    }

    private static boolean isBcrypt(String hash) {
        return hash != null
                && (hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"));
    }

    private static String legacyHash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance(LEGACY_HASH_ALGORITHM);
            byte[] encodedHash = digest.digest(password.getBytes());
            return bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password: " + e.getMessage(), e);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
