package zechs.zplex.auth.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtil {

    private static final String HASH_ALGORITHM = "SHA-256";

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
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
