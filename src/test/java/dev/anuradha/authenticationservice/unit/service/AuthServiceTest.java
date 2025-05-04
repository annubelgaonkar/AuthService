package dev.anuradha.authenticationservice.unit.service;

import dev.anuradha.authenticationservice.exception.InvalidCredentialsException;
import dev.anuradha.authenticationservice.model.RoleName;
import dev.anuradha.authenticationservice.repository.RoleRepository;
import dev.anuradha.authenticationservice.repository.UserRepository;
import dev.anuradha.authenticationservice.security.JwtUtil;
import dev.anuradha.authenticationservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import dev.anuradha.authenticationservice.model.Role;
import dev.anuradha.authenticationservice.model.User;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerUser_success() {
        String email = "test@example.com";
        String username = "testUser";
        String password = "password";
        String encodedPassword = "encodedPassword";

        Role role = new Role();
        role.setName(RoleName.USER);

        when(userRepository.findByEmail(email)).thenReturn(null);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(roleRepository.findByName(RoleName.USER)).thenReturn(role);
        when(jwtUtil.generateToken(email, "USER")).thenReturn("jwtToken");

        String token = authService.registerUser(username, email, password, RoleName.USER);

        assertEquals("jwtToken", token);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void loginUser_noRoles_shouldThrowException() {
        String email = "nobody@example.com";
        String hashedPassword = "hashed";

        User user = new User();
        user.setEmail(email);
        user.setPassword(hashedPassword);
        user.setRoles(new HashSet<>()); // Empty roles

        when(userRepository.findByEmail(email)).thenReturn(user);
        when(passwordEncoder.matches("password", hashedPassword)).thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> authService.loginUser(email, "password"));
    }

    @Test
    void registerUser_userAlreadyExists_shouldThrowConflict() {
        when(userRepository.findByEmail("existing@example.com")).thenReturn(new User());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authService.registerUser("user", "existing@example.com", "pass", RoleName.USER));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }
    @Test
    void registerUser_roleNotFound_shouldThrowNotFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(null);
        when(roleRepository.findByName(RoleName.ADMIN)).thenReturn(null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authService.registerUser("user", "test@example.com", "pass", RoleName.ADMIN));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void loginUser_success_shouldReturnToken() {
        String email = "user@example.com";
        String rawPassword = "123";
        String hashedPassword = "hashed";
        String token = "jwt-token";

        Role role = new Role(1L, RoleName.USER);
        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = new User();
        user.setEmail(email);
        user.setPassword(hashedPassword);
        user.setRoles(roles);

        when(userRepository.findByEmail(email)).thenReturn(user);
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(true);
        when(jwtUtil.generateToken(email, "USER")).thenReturn(token);

        String result = authService.loginUser(email, rawPassword);

        assertEquals(token, result);
    }

    @Test
    void loginUser_invalidCredentials_shouldThrow() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(null);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.loginUser("notfound@example.com", "pass"));
    }

}
