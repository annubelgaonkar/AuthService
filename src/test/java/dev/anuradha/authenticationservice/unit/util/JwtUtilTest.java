package dev.anuradha.authenticationservice.unit.util;

import dev.anuradha.authenticationservice.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JwtUtilTest {
    private JwtUtil jwtUtil;

    private final String rawSecret = "my-test-secret-my-test-secret-123456";  // Must be at least 32 bytes
    private final long expiration = 60;  // 1 minute for testing

    @BeforeEach
    void setup() {
        jwtUtil = new JwtUtil();
        setField(jwtUtil, "SECRET_KEY", Base64.getEncoder().encodeToString(rawSecret.getBytes()));
        setField(jwtUtil, "expiration", expiration);

        jwtUtil.init();  // Simulate @PostConstruct
    }

    @Test
    void testGenerateAndExtractToken() {
        String email = "test@example.com";
        String role = "USER";

        String token = jwtUtil.generateToken(email, role);

        assertNotNull(token);

        assertEquals(email, jwtUtil.extractUsername(token));
        assertEquals(role, jwtUtil.extractRole(token));
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    void testIsTokenExpired_falseForNewToken() {
        String token = jwtUtil.generateToken("john@example.com", "USER");
        assertFalse(jwtUtil.isTokenExpired(token));
    }
    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field " + fieldName, e);
        }
    }
}
