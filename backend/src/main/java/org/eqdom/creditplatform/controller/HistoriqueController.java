package org.eqdom.creditplatform.controller;

import org.eqdom.creditplatform.model.DossierHistorique;
import org.eqdom.creditplatform.service.HistoriqueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour la gestion de l'historique des dossiers
 * Permet d'archiver les données avant chaque import mensuel
 */
@RestController
@RequestMapping("/api/historique")
@PreAuthorize("hasAuthority('ADMIN')")
public class HistoriqueController {

    @Autowired
    private HistoriqueService historiqueService;

    /**
     * Archiver tous les dossiers actuels pour un mois donné
     * À appeler AVANT chaque nouvel import
     * 
     * POST /api/historique/archiver?mois=7&annee=2024
     */
    @PostMapping("/archiver")
    public ResponseEntity<Map<String, Object>> archiverDossiers(
            @RequestParam Integer mois,
            @RequestParam Integer annee) {
        
        Map<String, Object> rapport = historiqueService.archiverDossiersPourMois(mois, annee);
        return ResponseEntity.ok(rapport);
    }

    /**
     * Récupérer la liste des périodes archivées
     * 
     * GET /api/historique/periodes
     */
    @GetMapping("/periodes")
    public ResponseEntity<List<Map<String, Object>>> getPeriodesArchivees() {
        List<Map<String, Object>> periodes = historiqueService.getPeriodesArchivees();
        return ResponseEntity.ok(periodes);
    }

    /**
     * Récupérer tous les dossiers archivés pour une période donnée
     * 
     * GET /api/historique/dossiers?mois=7&annee=2024
     */
    @GetMapping("/dossiers")
    public ResponseEntity<List<DossierHistorique>> getDossiersArchives(
            @RequestParam Integer mois,
            @RequestParam Integer annee) {
        
        List<DossierHistorique> dossiers = historiqueService.getDossiersArchives(mois, annee);
        return ResponseEntity.ok(dossiers);
    }

    /**
     * Récupérer l'historique complet d'un dossier à travers les différents mois
     * 
     * GET /api/historique/dossier/{dossierIdOrigine}
     */
    @GetMapping("/dossier/{dossierIdOrigine}")
    public ResponseEntity<List<DossierHistorique>> getHistoriqueDossier(
            @PathVariable Long dossierIdOrigine) {
        
        List<DossierHistorique> historique = historiqueService.getHistoriqueDossier(dossierIdOrigine);
        return ResponseEntity.ok(historique);
    }
}
