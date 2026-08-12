package org.eqdom.creditplatform.controller;

import org.eqdom.creditplatform.dto.LoginRequest;
import org.eqdom.creditplatform.dto.LoginResponse;
import org.eqdom.creditplatform.exception.UserNotFoundException;
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
            System.out.println("=== TENTATIVE DE CONNEXION ===");
            System.out.println("Email: " + request.getEmail());
            System.out.println("Password (premiers 5 chars): " + request.getPassword().substring(0, Math.min(5, request.getPassword().length())) + "...");
            
            // Vérifier que l'utilisateur existe d'abord
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable: " + request.getEmail()));
            
            System.out.println("Utilisateur trouvé: " + user.getNom() + " " + user.getPrenom());
            System.out.println("Actif: " + user.isActif());
            System.out.println("Hash dans DB (premiers 20 chars): " + user.getPassword().substring(0, Math.min(20, user.getPassword().length())) + "...");
            System.out.println("Nombre de rôles: " + (user.getRoles() != null ? user.getRoles().size() : "NULL"));
            
            if (user.getRoles() != null) {
                user.getRoles().forEach(r -> System.out.println("  - Rôle: " + r.getNom()));
            }
            
            // Authentifier l'utilisateur
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            
            System.out.println("Authentification réussie !");
            
            // Vérifier si l'utilisateur est actif
            if (!user.isActif()) {
                return ResponseEntity.status(403).body("Compte désactivé");
            }
            
            // Générer le token
            List<String> roles = user.getRoles().stream().map(r -> r.getNom()).toList();
            String token = jwtUtil.generateToken(user.getEmail(), roles);

            return ResponseEntity.ok(new LoginResponse(token, user.getNom(), user.getPrenom(), roles));
            
        } catch (BadCredentialsException e) {
            System.out.println("❌ Erreur: Identifiants incorrects");
            e.printStackTrace();
            return ResponseEntity.status(401).body("Email ou mot de passe incorrect");
        } catch (Exception e) {
            System.out.println("❌ Erreur inattendue: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur lors de l'authentification: " + e.getMessage());
        }
    }
}