-- ============================================
-- Script: Nettoyage de l'historique des mois archivés
-- Description: Supprime les données d'archivage de la base de production
-- ⚠️ ATTENTION: Cette opération est IRRÉVERSIBLE!
-- ============================================

-- Afficher un résumé avant suppression
SELECT 
    'RÉSUMÉ AVANT SUPPRESSION' as info,
    (SELECT COUNT(*) FROM dossier_archive) as dossiers_archives,
    (SELECT COUNT(*) FROM dossier_mois_precedent) as dossiers_mois_precedent,
    (SELECT COUNT(*) FROM log_archivage) as logs_archivage,
    (SELECT COUNT(*) FROM historique_dossier) as historique_total;

-- ============================================
-- OPTION 1: Supprimer TOUT l'historique d'archivage
-- ============================================

-- Supprimer les dossiers archivés
DELETE FROM dossier_archive;

-- Supprimer les dossiers du mois précédent
DELETE FROM dossier_mois_precedent;

-- Supprimer les logs d'archivage
DELETE FROM log_archivage;

-- Supprimer l'historique des dossiers (optionnel - À DÉCOMMENTER SI NÉCESSAIRE)
-- DELETE FROM historique_dossier;

-- ============================================
-- OPTION 2: Supprimer uniquement les archives d'un mois spécifique
-- ============================================

-- Décommentez et modifiez la date selon vos besoins:

-- DELETE FROM dossier_archive 
-- WHERE DATE_TRUNC('month', date_archivage) = '2024-07-01'::date;

-- DELETE FROM dossier_mois_precedent 
-- WHERE DATE_TRUNC('month', date_archivage) = '2024-07-01'::date;

-- DELETE FROM log_archivage 
-- WHERE DATE_TRUNC('month', date_archivage) = '2024-07-01'::date;

-- ============================================
-- OPTION 3: Supprimer les archives de plus de X mois
-- ============================================

-- Supprimer les archives de plus de 6 mois (décommenter si besoin):

-- DELETE FROM dossier_archive 
-- WHERE date_archivage < NOW() - INTERVAL '6 months';

-- DELETE FROM dossier_mois_precedent 
-- WHERE date_archivage < NOW() - INTERVAL '6 months';

-- DELETE FROM log_archivage 
-- WHERE date_archivage < NOW() - INTERVAL '6 months';

-- ============================================
-- Vérification après suppression
-- ============================================

SELECT 
    'RÉSUMÉ APRÈS SUPPRESSION' as info,
    (SELECT COUNT(*) FROM dossier_archive) as dossiers_archives,
    (SELECT COUNT(*) FROM dossier_mois_precedent) as dossiers_mois_precedent,
    (SELECT COUNT(*) FROM log_archivage) as logs_archivage,
    (SELECT COUNT(*) FROM historique_dossier) as historique_total;

-- ============================================
-- Optimisation après suppression (RECOMMANDÉ)
-- ============================================

-- Récupérer l'espace disque
VACUUM FULL dossier_archive;
VACUUM FULL dossier_mois_precedent;
VACUUM FULL log_archivage;
VACUUM FULL historique_dossier;

-- Mettre à jour les statistiques
ANALYZE dossier_archive;
ANALYZE dossier_mois_precedent;
ANALYZE log_archivage;
ANALYZE historique_dossier;

SELECT 'Nettoyage terminé avec succès!' as resultat;
