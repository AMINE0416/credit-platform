package org.eqdom.creditplatform.controller;

import org.eqdom.creditplatform.dto.*;
import org.eqdom.creditplatform.model.*;
import org.eqdom.creditplatform.repository.*;
import org.eqdom.creditplatform.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/dossiers")
public class DossierController {

    @Autowired private DossierRepository dossierRepository;
    @Autowired private ActionDossierRepository actionDossierRepository;
    @Autowired private UserRepository userRepository;

    private static final Map<StatutDossier, Set<StatutDossier>> TRANSITIONS_AUTORISEES = Map.of(
            StatutDossier.NOUVEAU, Set.of(StatutDossier.EN_COURS),
            StatutDossier.EN_COURS, Set.of(StatutDossier.PROMESSE, StatutDossier.INJOIGNABLE, StatutDossier.REGULARISE),
            StatutDossier.INJOIGNABLE, Set.of(StatutDossier.EN_COURS),
            StatutDossier.PROMESSE, Set.of(StatutDossier.REGULARISE, StatutDossier.EN_COURS),
            StatutDossier.REGULARISE, Set.of(StatutDossier.CLOTURE),
            StatutDossier.CLOTURE, Set.of()
    );

    @GetMapping
    public ResponseEntity<List<DossierListItemDTO>> getDossiers(
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String ville,
            @RequestParam(required = false) Long agentId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        User connecte = currentUser.getUser();
        boolean isAdmin = connecte.getRoles().stream().anyMatch(r -> r.getNom().equals("ADMIN"));

        List<Dossier> dossiers = isAdmin
                ? dossierRepository.findAll()
                : dossierRepository.findByAgentAssigne(connecte);

        List<DossierListItemDTO> result = dossiers.stream()
                .filter(d -> statut == null || d.getStatutActuel().name().equals(statut))
                .filter(d -> ville == null || d.getCredit().getClient().getVille().equalsIgnoreCase(ville))
                .filter(d -> agentId == null || (d.getAgentAssigne() != null && d.getAgentAssigne().getId().equals(agentId)))
                .map(d -> new DossierListItemDTO(
                        d.getId(),
                        d.getCredit().getClient().getCin(),
                        d.getCredit().getClient().getPrenom() + " " + d.getCredit().getClient().getNom(),
                        d.getCredit().getReferenceContrat(),
                        d.getStatutActuel().name(),
                        d.getCredit().getClient().getVille(),
                        d.getAgentAssigne() != null
                                ? d.getAgentAssigne().getPrenom() + " " + d.getAgentAssigne().getNom()
                                : "Non assigné"
                ))
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/me")
    public ResponseEntity<List<DossierListItemDTO>> getMesDossiers(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        User connecte = currentUser.getUser();
        List<Dossier> dossiers = dossierRepository.findByAgentAssigne(connecte);

        List<DossierListItemDTO> result = dossiers.stream()
                .map(d -> new DossierListItemDTO(
                        d.getId(),
                        d.getCredit().getClient().getCin(),
                        d.getCredit().getClient().getPrenom() + " " + d.getCredit().getClient().getNom(),
                        d.getCredit().getReferenceContrat(),
                        d.getStatutActuel().name(),
                        d.getCredit().getClient().getVille(),
                        d.getAgentAssigne() != null
                                ? d.getAgentAssigne().getPrenom() + " " + d.getAgentAssigne().getNom()
                                : "Non assigné"
                ))
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DossierDetailDTO> getDossier(@PathVariable Long id) {
        try {
            Dossier dossier = dossierRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Dossier introuvable: " + id));
            
            // Force le chargement des relations avant le DTO
            dossier.getCredit();
            
            return ResponseEntity.ok(new DossierDetailDTO(dossier));
        } catch (Exception e) {
            System.err.println("Erreur getDossier " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/{id}/actions")
    public ResponseEntity<?> ajouterAction(
            @PathVariable Long id,
            @RequestBody ActionDossierRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        Dossier dossier = dossierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dossier introuvable"));

        if (dossier.getStatutActuel() == StatutDossier.CLOTURE) {
            return ResponseEntity.badRequest().body("Dossier clôturé : modification interdite");
        }

        if ("PROMESSE".equals(request.getNouveauStatut())
                && (request.getMontantPromesse() == null || request.getDatePromesse() == null)) {
            return ResponseEntity.badRequest().body("Montant et date de promesse obligatoires");
        }

        if (request.getNouveauStatut() != null) {
            StatutDossier nouveau = StatutDossier.valueOf(request.getNouveauStatut());
            if (!TRANSITIONS_AUTORISEES.get(dossier.getStatutActuel()).contains(nouveau)) {
                return ResponseEntity.badRequest().body(
                        "Transition non autorisée : " + dossier.getStatutActuel() + " → " + nouveau);
            }
        }

        ActionDossier action = new ActionDossier();
        action.setDossier(dossier);
        action.setAgent(currentUser.getUser());
        action.setNatureAction(request.getNatureAction());
        action.setMotifImpaye(request.getMotifImpaye());
        action.setCompteRendu(request.getCompteRendu());
        action.setCodeResultat(request.getCodeResultat());
        action.setRecidive(request.getRecidive());
        action.setNbreActionClient(request.getNbreActionClient());
        action.setMontantPromesse(request.getMontantPromesse());
        action.setDatePromesse(request.getDatePromesse());
        action.setNouveauTelephone(request.getNouveauTelephone());
        actionDossierRepository.save(action);

        if (request.getNouveauStatut() != null) {
            dossier.setStatutActuel(StatutDossier.valueOf(request.getNouveauStatut()));
            dossierRepository.save(dossier);
        }

        return ResponseEntity.ok().build();
    }

    // Affectation / Réaffectation en masse (module 4.5 — Admin uniquement, protégé par SecurityConfig)
    @PutMapping("/affectation")
    public ResponseEntity<?> affecter(@RequestBody AffectationRequest request) {
        try {
            User agent = userRepository.findById(request.getAgentId())
                    .orElseThrow(() -> new RuntimeException("Agent introuvable: " + request.getAgentId()));

            List<Dossier> dossiers = dossierRepository.findAllById(request.getDossierIds());
            
            if (dossiers.isEmpty()) {
                return ResponseEntity.badRequest().body("Aucun dossier trouvé");
            }
            
            for (Dossier d : dossiers) {
                // Forcer le chargement des relations
                d.getCredit();
                d.setAgentAssigne(agent);
            }
            dossierRepository.saveAll(dossiers);

            return ResponseEntity.ok(Map.of("nbAffectes", dossiers.size()));
        } catch (Exception e) {
            System.err.println("Erreur affectation: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur: " + e.getMessage());
        }
    }

    // Historique des actions d'un dossier (utile pour l'affichage détaillé)
    @GetMapping("/{id}/actions")
    public ResponseEntity<List<ActionDossier>> getActions(@PathVariable Long id) {
        return ResponseEntity.ok(actionDossierRepository.findByDossierIdOrderByDateActionDesc(id));
    }
}