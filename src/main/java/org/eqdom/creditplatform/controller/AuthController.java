package org.eqdom.creditplatform.controller;

import org.eqdom.creditplatform.dto.LoginRequest;
import org.eqdom.creditplatform.dto.LoginResponse;
import org.eqdom.creditplatform.model.User;
import org.eqdom.creditplatform.repository.UserRepository;
import org.eqdom.creditplatform.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Authentifier l'utilisateur
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            
            // Récupérer l'utilisateur
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
            
            // Vérifier si l'utilisateur est actif
            if (!user.isActif()) {
                return ResponseEntity.status(403).body("Compte désactivé");
            }
            
            // Générer le token
            List<String> roles = user.getRoles().stream().map(r -> r.getNom()).toList();
            String token = jwtUtil.generateToken(user.getEmail(), roles);

            return ResponseEntity.ok(new LoginResponse(token, user.getNom(), user.getPrenom(), roles));
            
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Email ou mot de passe incorrect");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur lors de l'authentification: " + e.getMessage());
        }
    }
}