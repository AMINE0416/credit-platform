package org.eqdom.creditplatform.controller;

import org.eqdom.creditplatform.dto.*;
import org.eqdom.creditplatform.model.*;
import org.eqdom.creditplatform.repository.*;
import org.eqdom.creditplatform.security.CustomUserDetails;
import org.eqdom.creditplatform.service.ExcelExportService;
import org.eqdom.creditplatform.service.TransitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/dossiers")
public class DossierController {

    @Autowired private DossierRepository dossierRepository;
    @Autowired private ActionDossierRepository actionDossierRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private TransitionService transitionService;
    @Autowired private ExcelExportService excelExportService;

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
                // Les agents ne voient pas les dossiers NOUVEAU (non assignés logiquement)
                .filter(d -> isAdmin || d.getStatutActuel() != StatutDossier.NOUVEAU)
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
                // Exclure les dossiers NOUVEAU (ne devraient pas exister pour un agent, mais par sécurité)
                .filter(d -> d.getStatutActuel() != StatutDossier.NOUVEAU)
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
            
            // Calculer le nombre d'actions pour ce dossier
            long nbActions = actionDossierRepository.countByDossierId(id);
            
            return ResponseEntity.ok(new DossierDetailDTO(dossier, nbActions));
        } catch (Exception e) {
            System.err.println("Erreur getDossier " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Endpoint pour récupérer les transitions autorisées et les champs requis
     * pour un dossier selon son statut actuel
     */
    @GetMapping("/{id}/transitions")
    public ResponseEntity<?> getTransitionsAutorisees(
            @PathVariable Long id,
            @RequestParam(required = false) String nouveauStatut) {
        try {
            Dossier dossier = dossierRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Dossier introuvable: " + id));
            
            StatutDossier statutActuel = dossier.getStatutActuel();
            StatutDossier nouveau = null;
            
            if (nouveauStatut != null && !nouveauStatut.trim().isEmpty()) {
                try {
                    nouveau = StatutDossier.valueOf(nouveauStatut);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body("Statut invalide: " + nouveauStatut);
                }
            }
            
            TransitionInfo info = transitionService.getTransitionInfo(statutActuel, nouveau);
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            System.err.println("Erreur getTransitionsAutorisees " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/actions")
    public ResponseEntity<?> ajouterAction(
            @PathVariable Long id,
            @RequestBody ActionDossierRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        try {
            // Log détaillé de la requête reçue
            System.out.println("=== DÉBUT ajouterAction pour dossier " + id + " ===");
            System.out.println("Request received: " + 
                "natureAction=" + request.getNatureActionStr() + 
                ", motifImpaye=" + request.getMotifImpayeStr() +
                ", codeResultat=" + request.getCodeResultatStr() +
                ", recidive=" + request.getRecidiveStr() +
                ", compteRendu=" + (request.getCompteRendu() != null ? request.getCompteRendu().substring(0, Math.min(30, request.getCompteRendu().length())) + "..." : "null") +
                ", nouveauStatut=" + request.getNouveauStatut());

            if (currentUser == null) {
                System.err.println("ERROR: currentUser is NULL");
                return ResponseEntity.status(401).body("Non authentifié");
            }

            User currentUserEntity = currentUser.getUser();
            if (currentUserEntity == null) {
                System.err.println("ERROR: currentUserEntity is NULL");
                return ResponseEntity.status(401).body("Utilisateur introuvable");
            }

            System.out.println("DEBUG: currentUser ID = " + currentUserEntity.getId() + ", email = " + currentUserEntity.getEmail());

            Dossier dossier = dossierRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Dossier introuvable: " + id));

            System.out.println("DEBUG: dossier trouvé, statut actuel = " + dossier.getStatutActuel());
            System.out.println("DEBUG: dossier.agentAssigne = " + (dossier.getAgentAssigne() != null ? dossier.getAgentAssigne().getId() : "NULL"));

            boolean isAdmin = currentUserEntity.getRoles().stream()
                    .anyMatch(r -> r.getNom().equals("ADMIN"));

            System.out.println("DEBUG: isAdmin = " + isAdmin);

            // Vérification d'accès: ADMIN peut accéder à tous, AGENT seulement aux dossiers assignés
            if (!isAdmin) {
                if (dossier.getAgentAssigne() == null) {
                    System.err.println("DEBUG: Dossier non assigné");
                    return ResponseEntity.status(403).body("Dossier non assigné: vous n'avez pas accès");
                }
                if (!currentUserEntity.getId().equals(dossier.getAgentAssigne().getId())) {
                    System.err.println("DEBUG: User ID " + currentUserEntity.getId() + " != Agent ID " + dossier.getAgentAssigne().getId());
                    return ResponseEntity.status(403).body("Accès refusé: vous n'êtes pas assigné à ce dossier");
                }
            }

            if (dossier.getStatutActuel() == StatutDossier.CLOTURE) {
                System.err.println("VALIDATION ERROR: Dossier clôturé");
                return ResponseEntity.badRequest().body("Dossier clôturé : modification interdite");
            }

            // VALIDATION DU NOUVEAU STATUT
            if (request.getNouveauStatut() == null || request.getNouveauStatut().trim().isEmpty()) {
                System.err.println("VALIDATION ERROR: nouveauStatut is NULL or empty");
                return ResponseEntity.badRequest().body("Le nouveau statut est obligatoire");
            }
            
            StatutDossier nouveauStatut;
            try {
                nouveauStatut = StatutDossier.valueOf(request.getNouveauStatut());
                System.out.println("DEBUG: nouveauStatut = " + nouveauStatut);
            } catch (IllegalArgumentException e) {
                System.err.println("VALIDATION ERROR: Statut invalide - " + request.getNouveauStatut());
                return ResponseEntity.badRequest().body("Statut invalide: " + request.getNouveauStatut());
            }
            
            // Vérifier que la transition est autorisée
            System.out.println("DEBUG: Vérification transition " + dossier.getStatutActuel() + " → " + nouveauStatut);
            if (!transitionService.isTransitionAutorisee(dossier.getStatutActuel(), nouveauStatut)) {
                System.err.println("VALIDATION ERROR: Transition non autorisée");
                return ResponseEntity.badRequest().body(
                        "Transition non autorisée : " + dossier.getStatutActuel() + " → " + nouveauStatut);
            }
            
            // VALIDATION CONDITIONNELLE DES CHAMPS selon la transition
            System.out.println("DEBUG: Validation des champs obligatoires...");
            String erreurValidation = transitionService.validerChampsObligatoires(
                    dossier.getStatutActuel(),
                    nouveauStatut,
                    request.getNatureActionStr(),
                    request.getMotifImpayeStr(),
                    request.getCompteRendu(),
                    request.getCodeResultatStr(),
                    request.getRecidiveStr(),
                    request.getMontantPromesse(),
                    request.getDatePromesse()
            );
            
            if (erreurValidation != null) {
                System.err.println("VALIDATION ERROR: " + erreurValidation);
                return ResponseEntity.badRequest().body(erreurValidation);
            }
            
            System.out.println("DEBUG: Validation OK, création de l'action...");

            ActionDossier action = new ActionDossier();
            action.setDossier(dossier);
            action.setAgent(currentUserEntity);
            action.setNatureAction(request.getNatureAction());
            action.setMotifImpaye(request.getMotifImpaye());
            action.setCompteRendu(request.getCompteRendu());
            action.setCodeResultat(request.getCodeResultat());
            action.setRecidive(request.getRecidive());
            action.setMontantPromesse(request.getMontantPromesse());
            action.setDatePromesse(request.getDatePromesse());
            action.setNouveauTelephone(request.getNouveauTelephone());
            
            // Calculer automatiquement le nombre d'actions (ne pas utiliser la valeur du frontend)
            System.out.println("DEBUG: Calcul du nombre d'actions...");
            long nbActionsExistantes = actionDossierRepository.countByDossierId(id);
            System.out.println("DEBUG: nbActionsExistantes = " + nbActionsExistantes);
            action.setNbreActionClient((int) (nbActionsExistantes + 1));
            
            System.out.println("DEBUG: Sauvegarde de l'action...");
            actionDossierRepository.save(action);

            // Mettre à jour le statut du dossier
            System.out.println("DEBUG: Mise à jour du statut du dossier vers " + nouveauStatut);
            dossier.setStatutActuel(nouveauStatut);
            dossierRepository.save(dossier);

            System.out.println("DEBUG: Action saved successfully with ID=" + action.getId());
            System.out.println("=== FIN ajouterAction - SUCCESS ===");
            return ResponseEntity.ok(Map.of("success", true, "actionId", action.getId()));
        } catch (IllegalArgumentException e) {
            System.err.println("ERROR IllegalArgumentException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Valeur enum invalide: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("ERROR Exception in ajouterAction: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur lors de l'enregistrement: " + e.getMessage());
        }
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
            
            int nbPassesEnCours = 0;
            for (Dossier d : dossiers) {
                // Forcer le chargement des relations
                d.getCredit();
                d.setAgentAssigne(agent);
                
                // Passer automatiquement de NOUVEAU à EN_COURS lors de l'affectation
                if (d.getStatutActuel() == StatutDossier.NOUVEAU) {
                    d.setStatutActuel(StatutDossier.EN_COURS);
                    nbPassesEnCours++;
                    System.out.println("Dossier " + d.getId() + " passé de NOUVEAU à EN_COURS lors de l'affectation");
                }
            }
            dossierRepository.saveAll(dossiers);

            String message = dossiers.size() + " dossier(s) affecté(s)";
            if (nbPassesEnCours > 0) {
                message += " (" + nbPassesEnCours + " passé(s) EN_COURS automatiquement)";
            }
            
            return ResponseEntity.ok(Map.of(
                "nbAffectes", dossiers.size(),
                "nbPassesEnCours", nbPassesEnCours,
                "message", message
            ));
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

    /**
     * Export Excel complet avec:
     * - Feuille "État actuel": snapshot de tous les dossiers
     * - Feuille "Historique statuts": chronologie complète des transitions
     * 
     * Accessible uniquement aux ADMIN (protégé par SecurityConfig)
     */
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel() {
        try {
            byte[] excelData = excelExportService.genererExportComplet();
            
            // Nom de fichier avec timestamp
            String filename = "export_dossiers_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + 
                ".xlsx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(excelData.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
                    
        } catch (Exception e) {
            System.err.println("Erreur lors de l'export Excel: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}