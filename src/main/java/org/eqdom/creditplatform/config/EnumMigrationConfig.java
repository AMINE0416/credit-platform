package org.eqdom.creditplatform.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * Configuration pour garantir que tous les statuts nécessaires existent dans l'enum PostgreSQL
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class EnumMigrationConfig {

    private final JdbcTemplate jdbcTemplate;

    @Bean
    public CommandLineRunner verifyEnumStatuts() {
        return args -> {
            log.info("🔍 Vérification des statuts dans l'enum statut_dossier...");

            try {
                // Récupérer les statuts actuellement dans l'enum
                List<String> statutsActuels = jdbcTemplate.queryForList(
                    "SELECT enumlabel FROM pg_enum e " +
                    "JOIN pg_type t ON e.enumtypid = t.oid " +
                    "WHERE t.typname = 'statut_dossier' " +
                    "ORDER BY e.enumsortorder",
                    String.class
                );

                log.info("📋 Statuts actuels dans l'enum: {}", statutsActuels);

                // Statuts requis par l'application
                String[] statutsRequis = {
                    "EN_COURS", "PROMESSE", "INJOIGNABLE", "REGULARISE",
                    "ALERTE", "SUIVI_PROMESSE", "SUIVI_INJOIGNABLE", "CLOTURE"
                };

                // Vérifier et ajouter les statuts manquants
                boolean ajouts = false;
                for (String statut : statutsRequis) {
                    if (!statutsActuels.contains(statut)) {
                        log.warn("⚠️  Statut manquant: {}", statut);
                        try {
                            jdbcTemplate.execute(
                                "ALTER TYPE statut_dossier ADD VALUE IF NOT EXISTS '" + statut + "'"
                            );
                            log.info("✅ Statut {} ajouté avec succès", statut);
                            ajouts = true;
                        } catch (Exception e) {
                            log.error("❌ Erreur lors de l'ajout du statut {}: {}", statut, e.getMessage());
                        }
                    }
                }

                if (ajouts) {
                    // Recharger la liste après ajouts
                    statutsActuels = jdbcTemplate.queryForList(
                        "SELECT enumlabel FROM pg_enum e " +
                        "JOIN pg_type t ON e.enumtypid = t.oid " +
                        "WHERE t.typname = 'statut_dossier' " +
                        "ORDER BY e.enumsortorder",
                        String.class
                    );
                    log.info("📋 Statuts après ajout: {}", statutsActuels);
                }

                // Vérifier que tous les statuts requis sont présents
                boolean allPresent = true;
                for (String statut : statutsRequis) {
                    if (!statutsActuels.contains(statut)) {
                        log.error("❌ Statut requis toujours manquant: {}", statut);
                        allPresent = false;
                    }
                }

                if (allPresent) {
                    log.info("✅ Tous les statuts requis sont présents dans l'enum");
                } else {
                    log.error("❌ Certains statuts requis sont toujours manquants - l'application pourrait rencontrer des erreurs");
                }

                // Vérifier et corriger les contraintes CHECK sur les tables
                log.info("🔍 Vérification des contraintes CHECK sur les tables...");
                corrigerContraintesCheck();

            } catch (Exception e) {
                log.error("❌ Erreur lors de la vérification des statuts enum: {}", e.getMessage(), e);
            }
        };
    }

    /**
     * Corriger les contraintes CHECK qui peuvent avoir une liste hardcodée de valeurs
     */
    private void corrigerContraintesCheck() {
        try {
            String[] statuts = {
                "'EN_COURS'::text", "'PROMESSE'::text", "'INJOIGNABLE'::text", 
                "'REGULARISE'::text", "'ALERTE'::text", "'SUIVI_PROMESSE'::text", 
                "'SUIVI_INJOIGNABLE'::text", "'CLOTURE'::text"
            };
            String statutsArray = String.join(", ", statuts);

            // Table dossiers
            jdbcTemplate.execute("ALTER TABLE dossiers DROP CONSTRAINT IF EXISTS dossiers_statut_actuel_check");
            jdbcTemplate.execute(
                "ALTER TABLE dossiers ADD CONSTRAINT dossiers_statut_actuel_check " +
                "CHECK (statut_actuel::text = ANY (ARRAY[" + statutsArray + "]))"
            );
            log.info("✅ Contrainte CHECK sur dossiers.statut_actuel mise à jour");

            // Table dossiers_archive
            jdbcTemplate.execute("ALTER TABLE dossiers_archive DROP CONSTRAINT IF EXISTS dossiers_archive_statut_final_check");
            jdbcTemplate.execute(
                "ALTER TABLE dossiers_archive ADD CONSTRAINT dossiers_archive_statut_final_check " +
                "CHECK (statut_final::text = ANY (ARRAY[" + statutsArray + "]))"
            );
            log.info("✅ Contrainte CHECK sur dossiers_archive.statut_final mise à jour");

            // Table dossiers_mois_precedent
            jdbcTemplate.execute("ALTER TABLE dossiers_mois_precedent DROP CONSTRAINT IF EXISTS dossiers_mois_precedent_statut_final_check");
            jdbcTemplate.execute(
                "ALTER TABLE dossiers_mois_precedent ADD CONSTRAINT dossiers_mois_precedent_statut_final_check " +
                "CHECK (statut_final::text = ANY (ARRAY[" + statutsArray + "]))"
            );
            log.info("✅ Contrainte CHECK sur dossiers_mois_precedent.statut_final mise à jour");

            log.info("✅ Toutes les contraintes CHECK ont été mises à jour avec SUIVI_INJOIGNABLE");

        } catch (Exception e) {
            log.error("❌ Erreur lors de la mise à jour des contraintes CHECK: {}", e.getMessage(), e);
        }
    }
}
