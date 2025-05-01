package dev.anuradha.authenticationservice.service;

import dev.anuradha.authenticationservice.exception.InvalidCredentialsException;
import dev.anuradha.authenticationservice.model.Role;
import dev.anuradha.authenticationservice.model.RoleName;
import dev.anuradha.authenticationservice.model.User;
import dev.anuradha.authenticationservice.repository.RoleRepository;
import dev.anuradha.authenticationservice.repository.UserRepository;
import dev.anuradha.authenticationservice.security.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Set;


@Service
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;



    public String registerUser(String username, String email, String password, RoleName roleName) {
        if (userRepository.findByEmail(email) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists: " + email);
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        Role role = roleRepository.findByName(roleName);

        if (role == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found: " + roleName);
        }

        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles); // Assign roles

        // Save the user in the database
        userRepository.save(user);

        return jwtUtil.generateToken(email, role.getName().name());
    }

    public String loginUser(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password.");
        }

        // Extract the first role (assuming one role per user)
        Role role = user.getRoles().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("User has no roles"));

        return jwtUtil.generateToken(email, role.getName().name());  // ✅ Include role
    }
}
