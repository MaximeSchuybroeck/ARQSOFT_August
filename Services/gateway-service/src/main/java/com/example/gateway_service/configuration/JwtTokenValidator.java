package com.example.gateway_service.configuration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;

@Component
public class JwtTokenValidator {

    private final JwtConfig jwtConfig;

    @Autowired
    public JwtTokenValidator(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    public void validateToken(String token) {
        String secret = jwtConfig.getSecret();
        byte[] secretBytes = Base64.getDecoder().decode(secret); // because your key is base64 encoded

        Key key = Keys.hmacShaKeyFor(secretBytes);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Optional: You can access user info here
        String username = claims.getSubject();
        System.out.println("Authenticated user: " + username);
    }
}