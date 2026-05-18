package com.example.MpFitness.Security;

import com.example.MpFitness.Model.Cliente;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;
import java.security.SecureRandom;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtUtils {

    private static final int MIN_SECRET_LENGTH = 32;
    private static final String RESET_TOKEN_PURPOSE = "PASSWORD_RESET";

    private final SecretKey secretKey;
    private final long expirationTime;
    private final long resetExpirationTime;

    public JwtUtils(
            @Value("${security.jwt.secret:}") String secret,
            @Value("${security.jwt.expiration-ms}") long expirationTime,
            @Value("${security.jwt.reset-expiration-ms:900000}") long resetExpirationTime,
            Environment environment) {
        secret = resolveSecret(secret, environment);
        if (Objects.isNull(secret) || secret.isBlank()) {
            throw new IllegalStateException("security.jwt.secret must be configured");
        }
        if (secret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException("security.jwt.secret must have at least 32 characters");
        }

        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationTime = expirationTime;
        this.resetExpirationTime = resetExpirationTime;
    }

    public String generateToken(Cliente cliente) {
        return Jwts.builder()
                .setSubject(cliente.getEmail())
                .claim("id", cliente.getId())
                .claim("nome", cliente.getNome())
                .claim("role", cliente.getRole().name())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generatePasswordResetToken(Cliente cliente) {
        return Jwts.builder()
                .setSubject(cliente.getEmail())
                .claim("id", cliente.getId())
                .claim("purpose", RESET_TOKEN_PURPOSE)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + resetExpirationTime))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractId(String token) {
        return extractAllClaims(token).get("id", Long.class);
    }

    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    public boolean validateToken(String token) {
        return !isTokenExpired(token);
    }

    public boolean validatePasswordResetToken(String token) {
        Claims claims = extractAllClaims(token);
        return !claims.getExpiration().before(new Date())
                && RESET_TOKEN_PURPOSE.equals(claims.get("purpose", String.class));
    }

    public String extractEmailFromPasswordResetToken(String token) {
        Claims claims = extractAllClaims(token);
        String purpose = claims.get("purpose", String.class);
        if (!RESET_TOKEN_PURPOSE.equals(purpose)) {
            throw new IllegalArgumentException("Token de reset inválido");
        }
        return claims.getSubject();
    }

    private String resolveSecret(String secret, Environment environment) {
        if (!Objects.isNull(secret) && !secret.isBlank()) {
            return secret;
        }

        if (environment.acceptsProfiles(Profiles.of("prod", "production"))) {
            throw new IllegalStateException("security.jwt.secret must be configured in production");
        }

        byte[] keyBytes = new byte[32];
        new SecureRandom().nextBytes(keyBytes);
        String generatedSecret = Base64.getEncoder().encodeToString(keyBytes);
        log.warn("JWT_SECRET not configured. Using a temporary development secret for this boot.");
        return generatedSecret;
    }
}
