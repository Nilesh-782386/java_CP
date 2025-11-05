package com.smartheal.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {
    private static final int ROUNDS = 12;

    /**
     * Hash a password using BCrypt
     * @param plainPassword The plain text password
     * @return The hashed password
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(ROUNDS));
    }

    /**
     * Verify a password against a hash
     * @param plainPassword The plain text password to verify
     * @param hashedPassword The hashed password to verify against
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            System.err.println("Error verifying password: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if a password meets minimum requirements
     * @param password The password to check
     * @return true if password meets requirements
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        // At least 6 characters, contains at least one letter and one number
        return password.matches(".*[a-zA-Z].*") && password.matches(".*[0-9].*");
    }
}

