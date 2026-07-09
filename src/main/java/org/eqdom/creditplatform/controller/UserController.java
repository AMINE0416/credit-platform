package org.eqdom.creditplatform.controller;

import org.eqdom.creditplatform.dto.*;
import org.eqdom.creditplatform.model.Role;
import org.eqdom.creditplatform.model.User;
import org.eqdom.creditplatform.repository.DossierRepository;
import org.eqdom.creditplatform.repository.RoleRepository;
import org.eqdom.creditplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private DossierRepository dossierRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAll() {
        List<UserDTO> result = userRepository.findAll().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateUserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Un compte existe déjà avec cet email");
        }

        Role role = roleRepository.findByNom(request.getRole())
                .orElseThrow(() -> new RuntimeException("Rôle invalide"));

        User user = new User();
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActif(true);
        user.setRoles(Set.of(role));
        userRepository.save(user);

        return ResponseEntity.ok(toDto(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setEmail(request.getEmail());

        if (request.getRole() != null) {
            Role role = roleRepository.findByNom(request.getRole())
                    .orElseThrow(() -> new RuntimeException("Rôle invalide"));
            user.setRoles(Set.of(role));
        }

        userRepository.save(user);
        return ResponseEntity.ok(toDto(user));
    }

    @PutMapping("/{id}/activation")
    public ResponseEntity<?> toggleActivation(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        user.setActif(!user.isActif());
        userRepository.save(user);
        return ResponseEntity.ok(toDto(user));
    }

    @PutMapping("/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Long id, @RequestBody ResetPasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        user.setPassword(passwordEncoder.encode(request.getNouveauMotDePasse()));
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    private UserDTO toDto(User user) {
        long nbDossiers = dossierRepository.findByAgentAssigne(user).size();
        List<String> roles = user.getRoles().stream().map(Role::getNom).toList();
        return new UserDTO(user.getId(), user.getNom(), user.getPrenom(), user.getEmail(),
                user.isActif(), roles, nbDossiers);
    }
}