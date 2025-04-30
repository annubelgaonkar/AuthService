package dev.anuradha.authenticationservice.security;


import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.SignatureAlgorithm;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import io.jsonwebtoken.Jwts;


@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expiration}")
    private long expiration;

    private Key key;

    @PostConstruct
    public void init() {
        // Generate a secure HMAC-SHA256 key with a proper length of 256 bits (32 bytes)
        if (SECRET_KEY != null && !SECRET_KEY.isEmpty()) {
            byte[] decodedKey = Base64.getDecoder().decode(SECRET_KEY);
            this.key = Keys.hmacShaKeyFor(decodedKey);  // Use the correct key length
        } else {
            // Fallback to generating a key with the correct length if SECRET_KEY is not provided
            this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);  // Generates a 256-bit key
        }
    }

    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)  // Set the username (subject)
                .claim("role", role)  // Store the role as a claim
                .setIssuedAt(new Date())  // Set the issue date
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))  // Set expiration time
                .signWith(key, SignatureAlgorithm.HS256)  // Use the 'key' initialized in @PostConstruct
                .compact();  // Build the JWT token
    }

    public String extractUsername(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token.replace("Bearer ", ""))
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            // Log the error and/or rethrow a custom exception
            return null;
        }
    }

    public String extractRole(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)  // Use the 'key' to validate the token
                .build()
                .parseClaimsJws(token.replace("Bearer ", ""))  // Remove "Bearer " prefix if present
                .getBody()
                .get("role", String.class);  // Get the "role" claim
    }

    // Utility method to check if token is expired
    public boolean isTokenExpired(String token) {
        try {
            Date expirationDate = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token.replace("Bearer ", ""))
                    .getBody()
                    .getExpiration();
            return expirationDate.before(new Date());
        } catch (Exception e) {
            return false;  // Return false if any error occurs (e.g., invalid token format)
        }
    }
}
