package org.eqdom.creditplatform.controller;

import org.eqdom.creditplatform.dto.ActionDossierRequest;
import org.eqdom.creditplatform.dto.DossierDetailDTO;
import org.eqdom.creditplatform.dto.DossierListItemDTO;
import org.eqdom.creditplatform.model.*;
import org.eqdom.creditplatform.repository.ActionDossierRepository;
import org.eqdom.creditplatform.repository.DossierRepository;
import org.eqdom.creditplatform.repository.UserRepository;
import org.eqdom.creditplatform.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dossiers")
public class DossierController {

    @Autowired private DossierRepository dossierRepository;
    @Autowired private ActionDossierRepository actionDossierRepository;
    @Autowired private UserRepository userRepository;

    // Liste des dossiers assignés à l'agent connecté
    @GetMapping
    public ResponseEntity<List<DossierListItemDTO>> getMesDossiers(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        User agent = currentUser.getUser();
        boolean isAdmin = agent.getRoles().stream().anyMatch(r -> r.getNom().equals("ADMIN"));

        List<Dossier> dossiers = isAdmin
                ? dossierRepository.findAll()
                : dossierRepository.findByAgentAssigne(agent);

        List<DossierListItemDTO> result = dossiers.stream().map(d -> new DossierListItemDTO(
                d.getId(),
                d.getCredit().getClient().getCin(),
                d.getCredit().getClient().getPrenom() + " " + d.getCredit().getClient().getNom(),
                d.getCredit().getReferenceContrat(),
                d.getStatutActuel().name(),
                d.getCredit().getClient().getVille()
        )).toList();

        return ResponseEntity.ok(result);
    }

    // Fiche dossier détaillée
    @GetMapping("/{id}")
    public ResponseEntity<DossierDetailDTO> getDossier(@PathVariable Long id) {
        Dossier dossier = dossierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dossier introuvable"));
        return ResponseEntity.ok(new DossierDetailDTO(dossier));
    }

    // Enregistrer une action + changement de statut
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

        // Règle métier : PROMESSE exige montant + date
        if ("PROMESSE".equals(request.getNouveauStatut())
                && (request.getMontantPromesse() == null || request.getDatePromesse() == null)) {
            return ResponseEntity.badRequest().body("Montant et date de promesse obligatoires");
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
}