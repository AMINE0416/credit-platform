package org.eqdom.creditplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eqdom.creditplatform.dto.LoginRequest;
import org.eqdom.creditplatform.model.Role;
import org.eqdom.creditplatform.model.User;
import org.eqdom.creditplatform.repository.UserRepository;
import org.eqdom.creditplatform.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires pour AuthController.
 * Utilise MockMvc en mode standalone (sans contexte Spring complet)
 * car Spring Boot 4.x a retiré @WebMvcTest/@MockBean du module test-autoconfigure.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController — Tests unitaires (MockMvc Standalone)")
class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private User userActif;
    private User userDesactive;

    @BeforeEach
    void setUp() {
        // MockMvc standalone : charge uniquement AuthController, sans filtre Spring Security
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();

        Role roleAgent = new Role("AGENT");

        userActif = new User();
        userActif.setNom("Alami");
        userActif.setPrenom("Hassan");
        userActif.setEmail("hassan.alami@eqdom.ma");
        userActif.setPassword("$2a$10$encryptedPassword");
        userActif.setActif(true);
        userActif.setRoles(Set.of(roleAgent));

        userDesactive = new User();
        userDesactive.setNom("Benali");
        userDesactive.setPrenom("Sara");
        userDesactive.setEmail("sara.benali@eqdom.ma");
        userDesactive.setPassword("$2a$10$encryptedPassword");
        userDesactive.setActif(false);
        userDesactive.setRoles(Set.of(roleAgent));
    }

    // ──────────────────────────────────────────────────
    // POST /api/auth/login — 200 OK
    // ──────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/login → 200 OK avec token si identifiants valides")
    void login_withValidCredentials_shouldReturn200WithToken() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("hassan.alami@eqdom.ma"))
                .thenReturn(Optional.of(userActif));
        when(jwtUtil.generateToken("hassan.alami@eqdom.ma", List.of("AGENT")))
                .thenReturn("eyJhbGciOiJIUzI1NiJ9.test.token");

        LoginRequest request = new LoginRequest();
        request.setEmail("hassan.alami@eqdom.ma");
        request.setPassword("Password1!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("eyJhbGciOiJIUzI1NiJ9.test.token"))
                .andExpect(jsonPath("$.nom").value("Alami"))
                .andExpect(jsonPath("$.prenom").value("Hassan"));
    }

    // ──────────────────────────────────────────────────
    // POST /api/auth/login — 401 Unauthorized
    // ──────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/login → 401 si mot de passe incorrect")
    void login_withBadCredentials_shouldReturn401() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Identifiants invalides"));

        LoginRequest request = new LoginRequest();
        request.setEmail("hassan.alami@eqdom.ma");
        request.setPassword("mauvais_mdp");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Email ou mot de passe incorrect"));
    }

    // ──────────────────────────────────────────────────
    // POST /api/auth/login — 403 compte désactivé
    // ──────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/login → 403 si le compte est désactivé")
    void login_withInactiveAccount_shouldReturn403() throws Exception {
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail("sara.benali@eqdom.ma"))
                .thenReturn(Optional.of(userDesactive));

        LoginRequest request = new LoginRequest();
        request.setEmail("sara.benali@eqdom.ma");
        request.setPassword("Password1!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Compte désactivé"));
    }

    // ──────────────────────────────────────────────────
    // Tests via appel direct (sans HTTP, sans MockMvc)
    // ──────────────────────────────────────────────────

    @Test
    @DisplayName("login() → appelle authenticationManager.authenticate() exactement 1 fois")
    void login_shouldCallAuthenticationManagerOnce() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad"));

        LoginRequest request = new LoginRequest();
        request.setEmail("test@eqdom.ma");
        request.setPassword("wrong");

        ResponseEntity<?> response = authController.login(request);

        assertThat(response.getStatusCode().value()).isEqualTo(401);
        verify(authenticationManager, times(1)).authenticate(any());
    }

    @Test
    @DisplayName("login() → NE génère PAS de token si compte désactivé")
    void login_withInactiveAccount_shouldNotGenerateToken() {
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail("sara.benali@eqdom.ma"))
                .thenReturn(Optional.of(userDesactive));

        LoginRequest request = new LoginRequest();
        request.setEmail("sara.benali@eqdom.ma");
        request.setPassword("Password1!");

        authController.login(request);

        verify(jwtUtil, never()).generateToken(any(), any());
    }

    @Test
    @DisplayName("login() → retourne 500 si userRepository lève une exception inattendue")
    void login_withUnexpectedException_shouldReturn500() {
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail(any())).thenThrow(new RuntimeException("DB error"));

        LoginRequest request = new LoginRequest();
        request.setEmail("test@eqdom.ma");
        request.setPassword("Password1!");

        ResponseEntity<?> response = authController.login(request);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
    }
}
