package com.digistack.bank.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * PasswordUtil — P01 v2
 *
 * Utility class for SHA-256 password hashing with a salt.
 *
 * How hashing works here:
 *   1. Take the salt (a fixed random string per user, stored in DB).
 *   2. Concatenate: salt + password.
 *   3. Run SHA-256 on the combined string.
 *   4. Convert the resulting bytes to a hex string.
 *   5. Store that hex string as password_hash in the users table.
 *
 * On login: hash(storedSalt + enteredPassword) == storedHash?
 *   → Yes: correct password.
 *   → No:  wrong password.
 *
 * This class is used by:
 *   - SeedUsers.java  (sets correct hashes in the DB at setup)
 *   - LoginServlet.java (verifies password at login time)
 */
public class PasswordUtil {

    // Private constructor — this class is never instantiated.
    // All methods are static utilities.
    private PasswordUtil() {}

    /**
     * Hashes a password with the given salt using SHA-256.
     *
     * @param salt     The per-user salt stored in users.password_salt
     * @param password The plain-text password entered by the user
     * @return         Hex-encoded SHA-256 hash string (64 characters)
     * @throws RuntimeException if SHA-256 is not available (never
     *                          happens on any standard JVM)
     */
    public static String hash(String salt, String password) {
        try {
            // MessageDigest is Java's built-in cryptographic hash engine.
            // "SHA-256" tells it which algorithm to use.
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Combine salt + password into one string, then get its bytes.
            // UTF-8 encoding ensures consistent byte representation
            // across all platforms and JVM versions.
            String combined = salt + password;
            byte[] hashBytes = digest.digest(combined.getBytes("UTF-8"));

            // Convert the raw bytes to a readable hex string.
            // Each byte becomes two hex characters (e.g. 0x0F → "0f").
            StringBuilder hexBuilder = new StringBuilder();
            for (byte b : hashBytes) {
                // 0xFF masks to treat the byte as unsigned.
                // 0x100 ensures the result is always two digits
                // (e.g. byte value 5 → "05", not "5").
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexBuilder.append('0');
                }
                hexBuilder.append(hex);
            }

            return hexBuilder.toString();

        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is required by the Java specification — this
            // exception will never actually be thrown.
            throw new RuntimeException(
                "SHA-256 algorithm not available on this JVM.", e);
        } catch (java.io.UnsupportedEncodingException e) {
            // UTF-8 is required by the Java specification — same as above.
            throw new RuntimeException(
                "UTF-8 encoding not available on this JVM.", e);
        }
    }

    /**
     * Verifies a plain-text password against a stored salt and hash.
     *
     * @param salt           The per-user salt from users.password_salt
     * @param enteredPassword The plain-text password the user typed
     * @param storedHash     The hash from users.password_hash
     * @return               true if the password is correct, false otherwise
     */
    public static boolean verify(String salt,
                                  String enteredPassword,
                                  String storedHash) {
        String computedHash = hash(salt, enteredPassword);
        // Use equals() for string comparison — never == for strings in Java.
        return computedHash.equals(storedHash);
    }
}