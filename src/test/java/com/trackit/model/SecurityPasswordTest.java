package com.trackit.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Security & Cryptographic Hashing Verification (REQ-QA-02)")
class SecurityPasswordTest {

    @Test
    @DisplayName("Should generate unique cryptographic salt and different hashes for same password")
    void testSaltedHashingUniqueness() {
        String password = "SecretPassword123";
        String salt1 = User.generateSalt();
        String salt2 = User.generateSalt();

        assertNotNull(salt1);
        assertNotNull(salt2);
        assertNotEquals(salt1, salt2, "Cryptographic salts must be unique");

        String hash1 = User.hashPasswordWithSalt(password, salt1);
        String hash2 = User.hashPasswordWithSalt(password, salt2);

        assertNotNull(hash1);
        assertNotNull(hash2);
        assertNotEquals(hash1, hash2, "Identical passwords with different salts must produce distinct hashes");
    }

    @Test
    @DisplayName("Should verify user password correctly using stored salt")
    void testUserPasswordVerification() {
        String salt = User.generateSalt();
        String hash = User.hashPasswordWithSalt("SecurePass2026", salt);
        User user = new User("shafiq", hash, "shafiq@test.com", salt);

        assertTrue(user.verifyPassword("SecurePass2026"), "Correct password must verify successfully");
        assertFalse(user.verifyPassword("WrongPassword"), "Incorrect password must be rejected");
        assertFalse(user.verifyPassword(null), "Null password must be rejected safely");
    }

    @Test
    @DisplayName("Audit (REQ-QA-02): Zero plaintext password fields exist in User entity")
    void testZeroPlaintextStorage() {
        for (Field field : User.class.getDeclaredFields()) {
            String fieldName = field.getName().toLowerCase();
            if (fieldName.contains("password")) {
                assertTrue(fieldName.contains("hash"), 
                        "Field '" + field.getName() + "' must only store hashes, never plaintext passwords");
            }
        }
    }
}
