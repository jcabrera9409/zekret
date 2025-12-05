package com.zekret.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.zekret.model.User;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class JWTServiceImplTest {

    @Inject
    JWTServiceImpl jwtService;

    private User testUser;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setEnabled(true);
    }

    @Test
    void testGenerateToken_Success() {
        // Act
        String token = jwtService.generateToken(testUser);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains(".")); // JWT format has dots separating parts
        
        // JWT tokens have 3 parts separated by dots
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT should have 3 parts: header, payload, signature");
    }

    @Test
    void testGenerateToken_NotNull() {
        // Act
        String token = jwtService.generateToken(testUser);

        // Assert
        assertNotNull(token);
    }

    @Test
    void testGenerateToken_DifferentTokensForSameUser() {
        // Act - Generate two tokens for the same user
        String token1 = jwtService.generateToken(testUser);
        
        // Wait a moment to ensure different timestamps
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String token2 = jwtService.generateToken(testUser);

        // Assert - Tokens should be different even for the same user
        // because they have different timestamps
        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2, "Tokens generated at different times should be different");
    }

    @Test
    void testGenerateToken_DifferentUsers() {
        // Arrange
        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@example.com");
        user2.setUsername("user2");
        user2.setEnabled(true);

        // Act
        String token1 = jwtService.generateToken(testUser);
        String token2 = jwtService.generateToken(user2);

        // Assert
        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2, "Tokens for different users should be different");
    }

    @Test
    void testGenerateToken_ValidJWTFormat() {
        // Act
        String token = jwtService.generateToken(testUser);

        // Assert
        assertNotNull(token);
        
        // JWT format validation
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
        
        // Each part should be non-empty
        for (String part : parts) {
            assertFalse(part.isEmpty());
        }
    }

    @Test
    void testGenerateToken_WithDifferentUserEmails() {
        // Arrange
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("user1@test.com");
        user1.setUsername("user1");

        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@test.com");
        user2.setUsername("user2");

        // Act
        String token1 = jwtService.generateToken(user1);
        String token2 = jwtService.generateToken(user2);

        // Assert
        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2);
    }

    @Test
    void testGenerateToken_ConsistentFormat() {
        // Act - Generate multiple tokens
        String token1 = jwtService.generateToken(testUser);
        
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setEmail("another@test.com");
        anotherUser.setUsername("another");
        
        String token2 = jwtService.generateToken(anotherUser);

        // Assert - All tokens should have the same format
        assertEquals(3, token1.split("\\.").length);
        assertEquals(3, token2.split("\\.").length);
    }

    @Test
    void testGenerateToken_TokenIsNotEmpty() {
        // Act
        String token = jwtService.generateToken(testUser);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.length() > 50, "JWT token should be reasonably long");
    }

    @Test
    void testGenerateToken_MultipleCallsSucceed() {
        // Act & Assert - Should be able to generate multiple tokens without errors
        for (int i = 0; i < 5; i++) {
            String token = jwtService.generateToken(testUser);
            assertNotNull(token);
            assertFalse(token.isEmpty());
        }
    }

    @Test
    void testGenerateToken_WithDisabledUser() {
        // Arrange
        User disabledUser = new User();
        disabledUser.setId(3L);
        disabledUser.setEmail("disabled@test.com");
        disabledUser.setUsername("disabled");
        disabledUser.setEnabled(false);

        // Act - Should still generate token (authorization is checked elsewhere)
        String token = jwtService.generateToken(disabledUser);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testGenerateToken_WithLongEmail() {
        // Arrange
        User userWithLongEmail = new User();
        userWithLongEmail.setId(4L);
        userWithLongEmail.setEmail("verylongemailaddress.with.many.dots@verylongdomainname.example.com");
        userWithLongEmail.setUsername("longuser");

        // Act
        String token = jwtService.generateToken(userWithLongEmail);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void testGenerateToken_WithSpecialCharactersInEmail() {
        // Arrange
        User userWithSpecialChars = new User();
        userWithSpecialChars.setId(5L);
        userWithSpecialChars.setEmail("user+tag@example.com");
        userWithSpecialChars.setUsername("specialuser");

        // Act
        String token = jwtService.generateToken(userWithSpecialChars);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }
}
