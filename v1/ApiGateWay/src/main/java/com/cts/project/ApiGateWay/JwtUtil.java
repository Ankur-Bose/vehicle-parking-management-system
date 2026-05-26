package com.cts.project.ApiGateWay;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    private Key key() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Gateway — JWT expired");
        } catch (UnsupportedJwtException e) {
            log.warn("Gateway — JWT unsupported");
        } catch (MalformedJwtException e) {
            log.warn("Gateway — JWT malformed");
        } catch (io.jsonwebtoken.security.SecurityException e) {
            log.warn("Gateway — JWT signature invalid");
        } catch (IllegalArgumentException e) {
            log.warn("Gateway — JWT empty or null");
        }
        return false;
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return (String) parseClaims(token).get("role");
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}