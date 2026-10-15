package org.eqdom.creditplatform.controller;

import jakarta.validation.Valid;
import org.eqdom.creditplatform.dto.*;
import org.eqdom.creditplatform.exception.BusinessException;
import org.eqdom.creditplatform.exception.UserNotFoundException;
import org.eqdom.creditplatform.model.Role;
import org.eqdom.creditplatform.model.User;
import org.eqdom.creditplatform.repository.DossierRepository;
import org.eqdom.creditplatform.repository.RoleRepository;
import org.eqdom.creditplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * Contrôleur REST pour la gestion des utilisateurs.
 * Les opérations admin sont protégées par @PreAuthorize.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private DossierRepository dossierRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAll() {
        List<UserDTO> result = userRepository.findAll().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody CreateUserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Un compte existe déjà avec cet email");
        }

        Role role = roleRepository.findByNom(request.getRole())
                .orElseThrow(() -> new BusinessException("Rôle invalide: " + request.getRole()));

        User user = new User();
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActif(true);
        user.setRoles(new java.util.HashSet<>(Set.of(role)));
        userRepository.save(user);

        return ResponseEntity.ok(toDto(user));
    }

    /**
     * Mise à jour du profil utilisateur (nom, prénom, email).
     * N'autorise PAS la modification du rôle pour prévenir le mass assignment.
     * Accessible uniquement aux administrateurs.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody UpdateUserProfileRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable: " + id));

        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setEmail(request.getEmail());

        userRepository.save(user);
        return ResponseEntity.ok(toDto(user));
    }

    /**
     * Modification du rôle d'un utilisateur.
     * Endpoint séparé, sécurisé explicitement pour les administrateurs UNIQUEMENT.
     * Prévient l'auto-promotion et le mass assignment sur les rôles.
     */
    @PutMapping("/{id}/role")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody UpdateUserRoleRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable: " + id));

        if (request.getRole() == null || request.getRole().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Le rôle ne peut pas être vide");
        }

        // Protection : empêcher de retirer le rôle ADMIN de l'utilisateur principal
        if (id == 1 && !"ADMIN".equals(request.getRole())) {
            return ResponseEntity.badRequest()
                    .body("Impossible de modifier le rôle de l'administrateur principal (ID=1). Cela pourrait vous faire perdre l'accès.");
        }

        Role role = roleRepository.findByNom(request.getRole())
                .orElseThrow(() -> new BusinessException("Rôle invalide: " + request.getRole()));
        
        user.setRoles(new java.util.HashSet<>(Set.of(role)));
        userRepository.save(user);
        
        return ResponseEntity.ok(toDto(user));
    }

    @PutMapping("/{id}/activation")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> toggleActivation(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable: " + id));
        user.setActif(!user.isActif());
        userRepository.save(user);
        return ResponseEntity.ok(toDto(user));
    }

    /**
     * Réinitialisation du mot de passe par un administrateur.
     * Ne nécessite PAS l'ancien mot de passe (cas d'usage : utilisateur a oublié son mot de passe).
     * Réservé aux administrateurs uniquement.
     */
    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> resetPassword(@PathVariable Long id, @RequestBody ResetPasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable: " + id));
        
        if (request.getNouveauMotDePasse() == null || request.getNouveauMotDePasse().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Le nouveau mot de passe ne peut pas être vide");
        }
        
        user.setPassword(passwordEncoder.encode(request.getNouveauMotDePasse()));
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    /**
     * Changement de mot de passe par l'utilisateur lui-même.
     * Nécessite la vérification de l'ancien mot de passe pour sécurité.
     * Accessible à tout utilisateur authentifié pour son propre compte.
     */
    @PutMapping("/{id}/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id, 
            @Valid @RequestBody ChangePasswordRequest request,
            org.springframework.security.core.Authentication authentication) {
        
        // Vérifier que l'utilisateur ne modifie que son propre mot de passe
        String currentUserEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));
        
        if (!currentUser.getId().equals(id)) {
            return ResponseEntity.status(403)
                    .body("Vous ne pouvez modifier que votre propre mot de passe");
        }
        
        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(request.getAncienMotDePasse(), currentUser.getPassword())) {
            return ResponseEntity.badRequest().body("Ancien mot de passe incorrect");
        }
        
        // Vérifier que le nouveau mot de passe n'est pas vide
        if (request.getNouveauMotDePasse() == null || request.getNouveauMotDePasse().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Le nouveau mot de passe ne peut pas être vide");
        }
        
        // Vérifier que le nouveau mot de passe est différent de l'ancien
        if (request.getAncienMotDePasse().equals(request.getNouveauMotDePasse())) {
            return ResponseEntity.badRequest()
                    .body("Le nouveau mot de passe doit être différent de l'ancien");
        }
        
        // Mettre à jour le mot de passe
        currentUser.setPassword(passwordEncoder.encode(request.getNouveauMotDePasse()));
        userRepository.save(currentUser);
        
        return ResponseEntity.ok().body("Mot de passe modifié avec succès");
    }

    private UserDTO toDto(User user) {
        long nbDossiers = dossierRepository.countByAgentAssigne(user);
        List<String> roles = user.getRoles().stream().map(Role::getNom).toList();
        return new UserDTO(user.getId(), user.getNom(), user.getPrenom(), user.getEmail(),
                user.isActif(), roles, nbDossiers);
    }
}