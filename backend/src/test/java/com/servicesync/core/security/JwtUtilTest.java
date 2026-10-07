package com.servicesync.core.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String SECRET = "test-secret-key-that-is-long-enough-for-hmac";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, 3600000); // 1 hour expiration
    }

    @Test
    void testGenerateAndValidateToken() {
        String token = jwtUtil.generateToken("admin@example.com", "ADMIN", 1L);
        assertNotNull(token);

        DecodedJWT decodedJWT = jwtUtil.validateTokenAndGetDecodedJWT(token);
        assertEquals("admin@example.com", decodedJWT.getSubject());
        assertEquals("ADMIN", decodedJWT.getClaim("role").asString());
        assertEquals("servicesync", decodedJWT.getIssuer());
    }

    @Test
    void testInvalidTokenThrowsException() {
        JwtUtil otherJwtUtil = new JwtUtil("different-secret-key", 3600000);
        String token = otherJwtUtil.generateToken("admin@example.com", "ADMIN", 1L);

        assertThrows(JWTVerificationException.class, () -> {
            jwtUtil.validateTokenAndGetDecodedJWT(token);
        });
    }

    @Test
    void testExpiredTokenThrowsException() throws InterruptedException {
        JwtUtil shortLivedJwtUtil = new JwtUtil(SECRET, 1); // 1 ms expiration
        String token = shortLivedJwtUtil.generateToken("admin@example.com", "ADMIN", 1L);
        
        Thread.sleep(10); // Wait for expiration

        assertThrows(JWTVerificationException.class, () -> {
            shortLivedJwtUtil.validateTokenAndGetDecodedJWT(token);
        });
    }
}
