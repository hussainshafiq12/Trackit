package com.trackit.model;

import com.trackit.exception.ValidationException;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Represents a user in the system with Salted SHA-256 cryptographic password hashing.
 * Strictly adheres to REQ-QA-02 (Security - Confidentiality & Integrity).
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String username;
    private String passwordHash;
    private String salt;
    private String email;
    private LocalDateTime registeredDate;

    public User(String username, String passwordHash, String email) {
        this(username, passwordHash, email, generateSalt());
    }

    public User(String username, String passwordHash, String email, String salt) {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username cannot be empty");
        }
        if (email == null || !email.contains("@")) {
            throw new ValidationException("Valid email containing '@' is required");
        }
        this.username = username.trim();
        this.passwordHash = passwordHash;
        this.email = email.trim();
        this.salt = (salt != null && !salt.trim().isEmpty()) ? salt.trim() : generateSalt();
        this.registeredDate = LocalDateTime.now();
    }

    /**
     * Generates a secure, 16-byte cryptographic salt encoded as hexadecimal.
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        return bytesToHex(saltBytes);
    }

    /**
     * Cryptographically hashes password with salt using SHA-256 (REQ-QA-02).
     */
    public static String hashPasswordWithSalt(String password, String salt) {
        if (password == null || password.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters");
        }
        if (salt == null || salt.trim().isEmpty()) {
            throw new ValidationException("Salt cannot be null or empty");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Backward-compatible hashing using default salt.
     */
    public static String hashPassword(String password) {
        return hashPasswordWithSalt(password, "STATIC_TRACKIT_SALT");
    }

    public boolean verifyPassword(String rawPassword) {
        if (rawPassword == null || passwordHash == null) {
            return false;
        }
        String computedHash = hashPasswordWithSalt(rawPassword, this.salt != null ? this.salt : "STATIC_TRACKIT_SALT");
        return this.passwordHash.equals(computedHash);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getRegisteredDate() {
        return registeredDate;
    }

    public void setRegisteredDate(LocalDateTime registeredDate) {
        this.registeredDate = registeredDate;
    }
}
