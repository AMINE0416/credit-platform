package org.eqdom.creditplatform.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eqdom.creditplatform.dto.*;
import org.eqdom.creditplatform.model.DossierArchive;
import org.eqdom.creditplatform.model.LogArchivage;
import org.eqdom.creditplatform.model.User;
import org.eqdom.creditplatform.repository.LogArchivageRepository;
import org.eqdom.creditplatform.security.CustomUserDetails;
import org.eqdom.creditplatform.service.ArchivageMensuelService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/archivage")
@PreAuthorize("hasAuthority('ADMIN')")
@Slf4j
@RequiredArgsConstructor
public class ArchivageMensuelController {

    private final ArchivageMensuelService archivageService;
    private final LogArchivageRepository logRepository;

    /**
     * POST /api/archivage/archiver
     * Archiver le mois en cours
     */
    @PostMapping("/archiver")
    public ResponseEntity<ArchivageMoisResponse> archiverMois(
        @RequestBody ArchivageMoisRequest request,
        Authentication authentication
    ) {
        log.info("Requête d'archivage pour le mois: {}", request.getMois());
        
        User user = ((CustomUserDetails) authentication.getPrincipal()).getUser();
        
        try {
            ArchivageMoisResponse response = archivageService.archiverMois(
                request.getMois(),
                request.isExporterCsv(),
                user.getId()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de l'archivage", e);
            return ResponseEntity.status(500).body(
                ArchivageMoisResponse.builder()
                    .success(false)
                    .message("Erreur: " + e.getMessage())
                    .moisArchive(request.getMois())
                    .build()
            );
        }
    }

    /**
     * DELETE /api/archivage/nettoyer/{mois}
     * Nettoyer les dossiers d'un mois (après archivage)
     */
    @DeleteMapping("/nettoyer/{mois}")
    public ResponseEntity<Map<String, Object>> nettoyerDossiers(
        @PathVariable String mois,
        Authentication authentication
    ) {
        log.info("Requête de nettoyage pour le mois: {}", mois);
        
        User user = ((CustomUserDetails) authentication.getPrincipal()).getUser();
        
        try {
            Map<String, Object> response = archivageService.nettoyerDossiersMois(mois, user.getId());
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Erreur lors du nettoyage", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Erreur: " + e.getMessage()
            ));
        }
    }

    /**
     * GET /api/archivage/mois
     * Lister tous les mois archivés
     */
    @GetMapping("/mois")
    public ResponseEntity<List<String>> getMoisArchives() {
        try {
            List<String> mois = archivageService.getMoisArchives();
            return ResponseEntity.ok(mois);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des mois", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * GET /api/archivage/statistiques/{mois}
     * Récupérer les statistiques d'un mois archivé
     */
    @GetMapping("/statistiques/{mois}")
    public ResponseEntity<StatistiquesArchiveDTO> getStatistiques(
        @PathVariable String mois
    ) {
        try {
            StatistiquesArchiveDTO stats = archivageService.getStatistiquesArchive(mois);
            
            if (stats == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des statistiques", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * GET /api/archivage/dossiers/{mois}
     * Récupérer les dossiers archivés d'un mois
     */
    @GetMapping("/dossiers/{mois}")
    public ResponseEntity<List<DossierArchive>> getDossiersArchives(
        @PathVariable String mois
    ) {
        try {
            List<DossierArchive> dossiers = archivageService.getDossiersArchives(mois);
            return ResponseEntity.ok(dossiers);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des dossiers archivés", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * GET /api/archivage/logs
     * Récupérer tous les logs d'archivage
     */
    @GetMapping("/logs")
    public ResponseEntity<List<LogArchivage>> getLogs() {
        try {
            List<LogArchivage> logs = logRepository.findAllByOrderByDateOperationDesc();
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des logs", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * GET /api/archivage/logs/{mois}
     * Récupérer les logs d'un mois spécifique
     */
    @GetMapping("/logs/{mois}")
    public ResponseEntity<List<LogArchivage>> getLogsByMois(
        @PathVariable String mois
    ) {
        try {
            List<LogArchivage> logs = logRepository.findByMoisConcerneOrderByDateOperationDesc(mois);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des logs", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * GET /api/archivage/telecharger-csv/{mois}
     * Télécharger le fichier Excel exporté lors de l'archivage d'un mois
     */
    @GetMapping("/telecharger-csv/{mois}")
    public ResponseEntity<byte[]> telechargerCsvArchive(
        @PathVariable String mois
    ) {
        try {
            List<LogArchivage> logs = logRepository.findCsvArchivageByMois(mois);

            if (logs.isEmpty() || logs.get(0).getCsvData() == null) {
                return ResponseEntity.notFound().build();
            }

            LogArchivage logEntry = logs.get(0);
            String fileName = logEntry.getNomFichierCsv() != null
                ? logEntry.getNomFichierCsv()
                : "export_archive_" + mois + ".xlsx";

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(logEntry.getCsvData());

        } catch (Exception e) {
            log.error("Erreur lors du téléchargement du CSV", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * GET /api/archivage/exports
     * Lister tous les exports disponibles (mois + nom fichier)
     */
    @GetMapping("/exports")
    public ResponseEntity<List<Map<String, Object>>> getExportsDisponibles() {
        try {
            List<LogArchivage> logs = logRepository.findCsvArchivageByMois("%");
            // On récupère tous les logs d'archivage avec CSV
            List<LogArchivage> allArchivageLogs = logRepository.findByOperation("ARCHIVAGE")
                .stream()
                .filter(l -> l.getCsvData() != null)
                .collect(Collectors.toList());

            List<Map<String, Object>> exports = allArchivageLogs.stream()
                .map(l -> Map.<String, Object>of(
                    "mois", l.getMoisConcerne(),
                    "nomFichier", l.getNomFichierCsv() != null ? l.getNomFichierCsv() : "",
                    "dateArchivage", l.getDateOperation().toString(),
                    "nbDossiers", l.getNbDossiers() != null ? l.getNbDossiers() : 0
                ))
                .collect(Collectors.toList());

            return ResponseEntity.ok(exports);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des exports", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * DELETE /api/archivage/reset-historique
     * ⚠️ DANGER: Vider complètement toutes les tables d'historique
     * Utilisé uniquement pour les tests - supprime:
     * - historique_statuts
     * - logs_archivage
     * - dossiers_archive
     * - dossiers_mois_precedent
     */
    @DeleteMapping("/reset-historique")
    public ResponseEntity<Map<String, Object>> resetHistorique(Authentication authentication) {
        log.warn("⚠️ RESET HISTORIQUE demandé par: {}", authentication.getName());
        
        try {
            Map<String, Object> result = archivageService.resetHistoriqueComplet();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Erreur lors du reset de l'historique", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Erreur: " + e.getMessage()
            ));
        }
    }
}
