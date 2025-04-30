package dev.anuradha.authenticationservice.controller;

import dev.anuradha.authenticationservice.dto.LoginRequestDTO;
import dev.anuradha.authenticationservice.dto.AuthResponseDTO;
import dev.anuradha.authenticationservice.dto.RegisterRequestDTO;
import dev.anuradha.authenticationservice.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("/auth")
public class AuthController {


    private final AuthService authService;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponseDTO register(@Valid @RequestBody RegisterRequestDTO request) {
        try{
            String token = authService.registerUser(request.getUsername(), request.getEmail(), request.getPassword());
            return new AuthResponseDTO(token, "Registration successful");
        }catch (ResponseStatusException ex){
            throw ex;
        }
        catch (Exception ex){
            // Log unexpected exceptions
            log.error("Unexpected error during registration", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred");
        }
    }

    @PostMapping("/login")
    public AuthResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        String token = authService.loginUser(request.getEmail(), request.getPassword());
        return new AuthResponseDTO(token, "Login successful");
    }

}
