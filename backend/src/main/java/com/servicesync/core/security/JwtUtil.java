package com.servicesync.core.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    private final String secret;
    private final long expirationMs;
    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expirationMs:86400000}") long expirationMs) {
        this.secret = secret;
        this.expirationMs = expirationMs;
        this.algorithm = Algorithm.HMAC256(secret);
        this.verifier = JWT.require(algorithm)
                .withIssuer("servicesync")
                .build();
    }

    public String generateToken(String email, String role, Long userId) {
        return JWT.create()
                .withIssuer("servicesync")
                .withSubject(email)
                .withClaim("role", role)
                .withClaim("userId", userId)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationMs))
                .sign(algorithm);
    }

    public DecodedJWT validateTokenAndGetDecodedJWT(String token) throws JWTVerificationException {
        return verifier.verify(token);
    }
}
