package com.cts.project.vpms.security;

import com.cts.project.vpms.exceptions.JwtSignatureInvalidException;
import com.cts.project.vpms.exceptions.JwtTokenEmptyException;
import com.cts.project.vpms.exceptions.JwtTokenExpiredException;
import com.cts.project.vpms.exceptions.JwtTokenUnsupportedException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    private Key key() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key())
                .compact();
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return (String) parseClaims(token).get("role");
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith((javax.crypto.SecretKey) key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired | subject={}", e.getClaims().getSubject());
            throw new JwtTokenExpiredException();
        } catch (UnsupportedJwtException e) {
            log.warn("JWT unsupported | msg={}", e.getMessage());
            throw new JwtTokenUnsupportedException();
        } catch (SecurityException e) {
            log.warn("JWT signature invalid | msg={}", e.getMessage());
            throw new JwtSignatureInvalidException();
        } catch (IllegalArgumentException e) {
            log.warn("JWT empty or null | msg={}", e.getMessage());
            throw new JwtTokenEmptyException();
        }
    }
}