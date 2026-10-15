package org.eqdom.creditplatform.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eqdom.creditplatform.dto.ArchivageMoisResponse;
import org.eqdom.creditplatform.dto.StatistiquesArchiveDTO;
import org.eqdom.creditplatform.model.*;
import org.eqdom.creditplatform.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArchivageMensuelService {

    private final DossierRepository dossierRepository;
    private final DossierArchiveRepository archiveRepository;
    private final DossierMoisPrecedentRepository moisPrecedentRepository;
    private final LogArchivageRepository logArchivageRepository;
    private final ExcelExportService excelExportService;
    private final HistoriqueService historiqueService;
    
    private static final DateTimeFormatter MOIS_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    /**
     * Archiver tous les dossiers d'un mois donné
     * Étapes:
     * 1. Export CSV (optionnel)
     * 2. Copie vers dossiers_archive
     * 3. Sauvegarde dans dossiers_mois_precedent
     */
    @Transactional
    public ArchivageMoisResponse archiverMois(String mois, boolean exporterCsv, Long userId) {
        log.info("Début archivage du mois: {}", mois);

        try {
            // Vérifier si déjà archivé
            if (archiveRepository.existsByMoisArchive(mois)) {
                return ArchivageMoisResponse.builder()
                    .success(false)
                    .message("Le mois " + mois + " est déjà archivé")
                    .moisArchive(mois)
                    .build();
            }

            // 1. Récupérer tous les dossiers du mois
            List<Dossier> dossiers = dossierRepository.findByMoisImport(mois);
            
            if (dossiers.isEmpty()) {
                return ArchivageMoisResponse.builder()
                    .success(false)
                    .message("Aucun dossier trouvé pour le mois " + mois)
                    .moisArchive(mois)
                    .build();
            }

            log.info("Nombre de dossiers à archiver: {}", dossiers.size());

            // 2. Générer le fichier Excel/CSV de l'état actuel
            byte[] csvBytes = null;
            String exportFileName = null;
            if (exporterCsv) {
                try {
                    log.info("Génération de l'export Excel pour le mois {}", mois);
                    csvBytes = excelExportService.genererExportComplet();
                    exportFileName = "export_archive_" + mois + ".xlsx";
                    log.info("Export Excel généré: {} octets", csvBytes.length);
                } catch (IOException e) {
                    log.warn("Impossible de générer l'export Excel: {}", e.getMessage());
                    // On continue l'archivage même si l'export échoue
                }
            }

            // 3. Copier vers dossiers_archive
            List<DossierArchive> archives = dossiers.stream()
                .map(d -> creerArchive(d, mois))
                .collect(Collectors.toList());
            
            archiveRepository.saveAll(archives);
            log.info("Dossiers copiés dans archive: {}", archives.size());

            // 4. Sauvegarder dans dossiers_mois_precedent pour détection
            List<DossierMoisPrecedent> precedents = dossiers.stream()
                .map(d -> creerMoisPrecedent(d, mois))
                .collect(Collectors.toList());
            
            moisPrecedentRepository.saveAll(precedents);
            log.info("Dossiers sauvegardés pour détection: {}", precedents.size());

            // 5. Logger l'opération avec le CSV intégré
            LogArchivage logEntry = LogArchivage.builder()
                .operation("ARCHIVAGE")
                .moisConcerne(mois)
                .nbDossiers(dossiers.size())
                .utilisateurId(userId)
                .details(exportFileName != null ? "Export Excel: " + exportFileName : "Sans export Excel")
                .statut("SUCCESS")
                .csvData(csvBytes)
                .nomFichierCsv(exportFileName)
                .build();
            logArchivageRepository.save(logEntry);

            return ArchivageMoisResponse.builder()
                .success(true)
                .message("Archivage du mois " + mois + " réussi")
                .moisArchive(mois)
                .nbDossiersArchives((long) archives.size())
                .nbDossiersSauvegardes((long) precedents.size())
                .exportId(logEntry.getId())
                .exportFileName(exportFileName)
                .build();

        } catch (Exception e) {
            log.error("Erreur lors de l'archivage du mois " + mois, e);
            
            logArchivageRepository.save(LogArchivage.builder()
                .operation("ARCHIVAGE")
                .moisConcerne(mois)
                .utilisateurId(userId)
                .details("Erreur: " + e.getMessage())
                .statut("ERROR")
                .build());

            throw new RuntimeException("Erreur archivage: " + e.getMessage(), e);
        }
    }

    /**
     * Nettoyer les dossiers d'un mois (après archivage)
     */
    @Transactional
    public Map<String, Object> nettoyerDossiersMois(String mois, Long userId) {
        log.info("Début nettoyage des dossiers du mois: {}", mois);

        // Vérifier que l'archivage a été fait
        if (!archiveRepository.existsByMoisArchive(mois)) {
            throw new IllegalStateException(
                "Impossible de nettoyer: le mois " + mois + " n'a pas été archivé"
            );
        }

        // Récupérer les dossiers à supprimer
        List<Dossier> dossiers = dossierRepository.findByMoisImport(mois);
        long count = dossiers.size();
        
        // Supprimer via JPA pour déclencher les cascades (suppression automatique des actions)
        dossierRepository.deleteAll(dossiers);
        
        log.info("Dossiers supprimés avec leurs actions: {}", count);

        // Logger
        logArchivageRepository.save(LogArchivage.builder()
            .operation("NETTOYAGE")
            .moisConcerne(mois)
            .nbDossiers((int) count)
            .utilisateurId(userId)
            .statut("SUCCESS")
            .build());

        return Map.of(
            "success", true,
            "message", count + " dossiers supprimés du mois " + mois,
            "nbSupprimes", count
        );
    }

    /**
     * Récupérer les statistiques d'un mois archivé
     */
    public StatistiquesArchiveDTO getStatistiquesArchive(String mois) {
        List<DossierArchive> archives = archiveRepository.findByMoisArchiveOrderByDateArchivageDesc(mois);
        
        if (archives.isEmpty()) {
            return null;
        }

        // Répartition par statut
        Map<String, Long> repartition = archives.stream()
            .collect(Collectors.groupingBy(
                DossierArchive::getStatutFinal,
                Collectors.counting()
            ));

        // Total dû
        var totalDu = archives.stream()
            .map(DossierArchive::getTotalDu)
            .filter(java.util.Objects::nonNull)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        // Date archivage
        LocalDateTime dateArchivage = archives.get(0).getDateArchivage();

        return StatistiquesArchiveDTO.builder()
            .moisArchive(mois)
            .totalDossiers((long) archives.size())
            .repartitionStatuts(repartition)
            .totalDuMensuel(totalDu)
            .dateArchivage(dateArchivage)
            .build();
    }

    /**
     * Lister tous les mois archivés
     */
    public List<String> getMoisArchives() {
        return archiveRepository.findDistinctMoisArchive();
    }

    /**
     * Récupérer les dossiers archivés d'un mois
     */
    public List<DossierArchive> getDossiersArchives(String mois) {
        return archiveRepository.findByMoisArchiveOrderByDateArchivageDesc(mois);
    }

    /**
     * ⚠️ DANGER: Réinitialiser complètement tout l'historique
     * Vide toutes les tables d'historique et d'archivage
     * Utilisé uniquement pour les tests
     */
    @Transactional
    public Map<String, Object> resetHistoriqueComplet() {
        log.warn("⚠️ RESET HISTORIQUE COMPLET - Suppression de toutes les données d'historique");
        
        try {
            // Compter avant suppression
            long nbHistoriqueStatuts = historiqueService.countAll();
            long nbLogs = logArchivageRepository.count();
            long nbArchives = archiveRepository.count();
            long nbMoisPrecedent = moisPrecedentRepository.count();
            
            // Supprimer toutes les données
            historiqueService.deleteAll();
            logArchivageRepository.deleteAll();
            archiveRepository.deleteAll();
            moisPrecedentRepository.deleteAllDossiers();
            
            log.info("✅ Reset historique terminé:");
            log.info("   - {} historiques de statuts supprimés", nbHistoriqueStatuts);
            log.info("   - {} logs supprimés", nbLogs);
            log.info("   - {} archives supprimées", nbArchives);
            log.info("   - {} dossiers mois précédent supprimés", nbMoisPrecedent);
            
            return Map.of(
                "success", true,
                "message", "Historique réinitialisé avec succès",
                "supprime", Map.of(
                    "historique_statuts", nbHistoriqueStatuts,
                    "logs_archivage", nbLogs,
                    "dossiers_archive", nbArchives,
                    "dossiers_mois_precedent", nbMoisPrecedent
                )
            );
            
        } catch (Exception e) {
            log.error("Erreur lors du reset de l'historique", e);
            throw new RuntimeException("Erreur lors du reset: " + e.getMessage(), e);
        }
    }

    // ===== MÉTHODES PRIVÉES =====

    private DossierArchive creerArchive(Dossier dossier, String mois) {
        DossierArchive archive = new DossierArchive();
        
        // Copier les données du crédit
        Credit credit = dossier.getCredit();
        Client client = credit.getClient();
        
        archive.setNumeroDossier(credit.getReferenceContrat());
        archive.setNomClient(client.getNom() + " " + client.getPrenom());
        archive.setTelephone(client.getTelephone());
        // archive.setEmail(client.getEmail()); // Client n'a pas d'email
        archive.setMontantPret(credit.getEncours());
        archive.setMontantEcheance(credit.getMensualite());
        archive.setNombreEcheancesImpayees(credit.getNbrImpaye());
        archive.setTotalDu(credit.getImpayeMontant());
        archive.setScorePriorite(credit.getScore() != null ? credit.getScore().intValue() : 0);
        
        // Statut et agent
        archive.setStatut(dossier.getStatutActuel().name());
        archive.setStatutFinal(dossier.getStatutActuel().name());
        archive.setAgentId(dossier.getAgentAssigne() != null ? dossier.getAgentAssigne().getId() : null);
        
        // Dates
        archive.setDateDerniereAction(LocalDateTime.now());
        
        // Métadonnées archivage
        archive.setMoisImport(mois);
        archive.setMoisArchive(mois);
        archive.setDateArchivage(LocalDateTime.now());
        
        return archive;
    }

    private DossierMoisPrecedent creerMoisPrecedent(Dossier dossier, String mois) {
        DossierMoisPrecedent precedent = new DossierMoisPrecedent();
        precedent.setNumeroDossier(dossier.getCredit().getReferenceContrat());
        
        // Transformer le statut selon la nouvelle logique pour le prochain import
        String statutActuel = dossier.getStatutActuel().name();
        String statutPourProchainMois = transformerStatutPourArchivage(statutActuel);
        
        precedent.setStatutFinal(statutPourProchainMois);
        precedent.setMoisOrigine(mois);
        precedent.setDateSauvegarde(LocalDateTime.now());
        
        // PRÉSERVER les informations de promesse pour les dossiers PROMESSE et SUIVI_PROMESSE
        if (dossier.getDatePromesse() != null) {
            precedent.setDatePromesse(dossier.getDatePromesse());
            log.debug("  📅 Promesse sauvegardée pour prochain mois - Date: {}", dossier.getDatePromesse());
        }
        if (dossier.getMontantPromesse() != null) {
            precedent.setMontantPromesse(java.math.BigDecimal.valueOf(dossier.getMontantPromesse()));
            log.debug("  💰 Promesse sauvegardée pour prochain mois - Montant: {}", dossier.getMontantPromesse());
        }
        
        log.debug("Archivage: {} (statut actuel) → {} (statut pour prochain mois)", 
            statutActuel, statutPourProchainMois);
        
        return precedent;
    }
    
    /**
     * Transformer le statut lors de l'archivage pour le prochain import
     * Nouvelle logique :
     * - EN_COURS → ALERTE (dossier non résolu qui revient)
     * - PROMESSE → SUIVI_PROMESSE (suivi de promesse)
     * - INJOIGNABLE → SUIVI_INJOIGNABLE (suivi d'injoignable)
     * - REGULARISE → REGULARISE (reste tel quel, pourrait être clôturé)
     * - Les statuts de suivi restent tels quels s'ils reviennent
     */
    private String transformerStatutPourArchivage(String statutActuel) {
        return switch (statutActuel.toUpperCase()) {
            case "EN_COURS" -> "ALERTE";
            case "PROMESSE" -> "SUIVI_PROMESSE";
            case "INJOIGNABLE" -> "SUIVI_INJOIGNABLE";
            case "REGULARISE", "ALERTE", "SUIVI_PROMESSE", "SUIVI_INJOIGNABLE", "CLOTURE" -> statutActuel;
            default -> "ALERTE";  // Par défaut, créer une alerte
        };
    }
}
