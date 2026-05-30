package com.example.RbacTaskManager.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil
{
    private static final String SECRET = "rbac-task-manager-secret-key-super-secure-256bit!";
    private static final long EXPIRATION_MS = 86400000; // 24 hours

    private SecretKey getKey()
    {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generateToken(String username, String role)
    {
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(getKey())
                .compact();
    }

    public String extractUsername(String token)
    {
        return getClaims(token).getSubject();
    }

    public String extractRole(String token)
    {
        return getClaims(token).get("role", String.class);
    }

    public boolean isTokenValid(String token)
    {
        try
        {
            getClaims(token);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private Claims getClaims(String token)
    {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}