package org.eqdom.creditplatform.controller;

import jakarta.validation.Valid;
import org.eqdom.creditplatform.dto.*;
import org.eqdom.creditplatform.model.*;
import org.eqdom.creditplatform.security.CustomUserDetails;
import org.eqdom.creditplatform.service.DossierService;
import org.eqdom.creditplatform.service.ExcelExportService;
import org.eqdom.creditplatform.service.DossierSuppressionService;
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

/**
 * Contrôleur REST pour la gestion des dossiers.
 * La logique métier est déléguée à DossierService.
 */
@RestController
@RequestMapping("/api/dossiers")
public class DossierController {

    @Autowired private DossierService dossierService;
    @Autowired private ExcelExportService excelExportService;
    @Autowired private DossierSuppressionService dossierSuppressionService;

    @GetMapping
    public ResponseEntity<List<DossierListItemDTO>> getDossiers(
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String ville,
            @RequestParam(required = false) Long agentId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        List<DossierListItemDTO> result = dossierService.getDossiers(
                statut, ville, agentId, currentUser.getUser());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/me")
    public ResponseEntity<List<DossierListItemDTO>> getMesDossiers(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        List<DossierListItemDTO> result = dossierService.getMesDossiers(currentUser.getUser());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DossierDetailDTO> getDossier(@PathVariable Long id) {
        DossierDetailDTO detail = dossierService.getDossierDetail(id);
        return ResponseEntity.ok(detail);
    }
    
    /**
     * Endpoint pour récupérer les transitions autorisées et les champs requis
     * pour un dossier selon son statut actuel
     */
    @GetMapping("/{id}/transitions")
    public ResponseEntity<TransitionInfo> getTransitionsAutorisees(
            @PathVariable Long id,
            @RequestParam(required = false) String nouveauStatut) {

        TransitionInfo info = dossierService.getTransitionsAutorisees(id, nouveauStatut);
        return ResponseEntity.ok(info);
    }

    @PostMapping("/{id}/actions")
    public ResponseEntity<Map<String, Object>> ajouterAction(
            @PathVariable Long id,
            @Valid @RequestBody ActionDossierRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        Map<String, Object> result = dossierService.ajouterAction(
                id, request, currentUser.getUser());
        return ResponseEntity.ok(result);
    }

    // Affectation / Réaffectation en masse (module 4.5 — Admin uniquement, protégé par SecurityConfig)
    @PutMapping("/affectation")
    public ResponseEntity<Map<String, Object>> affecter(@RequestBody AffectationRequest request) {
        Map<String, Object> result = dossierService.affecterDossiers(
                request.getDossierIds(), request.getAgentId());
        return ResponseEntity.ok(result);
    }

    // Historique des actions d'un dossier (utile pour l'affichage détaillé)
    @GetMapping("/{id}/actions")
    public ResponseEntity<List<ActionDossier>> getActions(@PathVariable Long id) {
        List<ActionDossier> actions = dossierService.getActions(id);
        return ResponseEntity.ok(actions);
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

    // Suppression de dossiers (ADMIN uniquement, protégé par SecurityConfig)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> supprimerDossier(@PathVariable Long id) {
        dossierSuppressionService.supprimerDossier(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Dossier supprimé"));
    }
}