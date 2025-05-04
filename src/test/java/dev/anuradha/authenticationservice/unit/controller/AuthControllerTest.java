    package dev.anuradha.authenticationservice.unit.controller;

    import com.fasterxml.jackson.databind.ObjectMapper;
    import dev.anuradha.authenticationservice.controller.AuthController;
    import dev.anuradha.authenticationservice.dto.LoginRequestDTO;
    import dev.anuradha.authenticationservice.dto.RegisterRequestDTO;
    import dev.anuradha.authenticationservice.model.RoleName;
    import dev.anuradha.authenticationservice.security.SecurityConfig;
    import dev.anuradha.authenticationservice.service.AuthService;
    import org.junit.jupiter.api.Test;
    import org.mockito.Mockito;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
    import org.springframework.boot.test.mock.mockito.MockBean;
    import org.springframework.context.annotation.Import;
    import org.springframework.http.MediaType;
    import org.springframework.test.web.servlet.MockMvc;

    import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
    import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
    import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
    import static org.hamcrest.Matchers.is;
    import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
    import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
    import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

    @WebMvcTest(controllers = AuthController.class)
    @Import(SecurityConfig.class)
    class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private AuthService authService;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        void testRegister_success() throws Exception {
            RegisterRequestDTO request = new RegisterRequestDTO();
            request.setUsername("john");
            request.setEmail("john@example.com");
            request.setPassword("password123");
            request.setRole(RoleName.USER);

            String token = "mocked-jwt-token";

            Mockito.when(authService.registerUser("john", "john@example.com", "password123", RoleName.USER))
                    .thenReturn(token);

            mockMvc.perform(post("/auth/register")
                             .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token", is(token)))
                    .andExpect(jsonPath("$.message", is("Registration successful")));
        }

        @Test
        void testLogin_success() throws Exception {
            LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
            loginRequestDTO.setEmail("test@example.com");
            loginRequestDTO.setPassword("password123");

            Mockito.when(authService.loginUser("test@example.com", "password123"))
                    .thenReturn("fake-jwt-token");
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                    .andExpect(jsonPath("$.message").value("Login successful"));
        }
        @Test
        void testRegister_unexpectedException_shouldReturn500() throws Exception {
            RegisterRequestDTO requestDTO = new RegisterRequestDTO();
            requestDTO.setUsername("errorUser");
            requestDTO.setEmail("error@example.com");
            requestDTO.setPassword("pass123");

            Mockito.when(authService.registerUser(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenThrow(new RuntimeException("Something went wrong"));
            mockMvc.perform(post("/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string("Unexpected error occurred"));
        }
    }
