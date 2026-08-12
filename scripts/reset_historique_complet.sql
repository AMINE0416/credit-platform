-- ═══════════════════════════════════════════════════════════════════
-- RESET COMPLET HISTORIQUE ET ARCHIVAGE
-- ═══════════════════════════════════════════════════════════════════
-- Ce script vide toutes les tables d'historique pour repartir de zéro
-- Utilisez-le pour les tests depuis le début
-- ═══════════════════════════════════════════════════════════════════

\echo '🗑️  Début du nettoyage de l''historique...'

-- 1. Supprimer les historiques de statuts
\echo '📝 Suppression des historiques de statuts...'
DELETE FROM historique_statuts;
\echo '   ✅ Historiques de statuts supprimés'

-- 2. Supprimer les logs d'archivage
\echo '📋 Suppression des logs d''archivage...'
DELETE FROM logs_archivage;
\echo '   ✅ Logs d''archivage supprimés'

-- 3. Supprimer les archives
\echo '📦 Suppression des dossiers archivés...'
DELETE FROM dossiers_archive;
\echo '   ✅ Dossiers archivés supprimés'

-- 4. Supprimer les dossiers du mois précédent (pour détection récurrents)
\echo '🔄 Suppression de la table dossiers_mois_precedent...'
DELETE FROM dossiers_mois_precedent;
\echo '   ✅ Dossiers mois précédent supprimés'

-- 5. Vérification des suppressions
\echo ''
\echo '📊 Vérification des suppressions:'
SELECT 
    'historique_statuts' as table_name, 
    COUNT(*) as nb_lignes 
FROM historique_statuts
UNION ALL
SELECT 
    'logs_archivage' as table_name, 
    COUNT(*) as nb_lignes 
FROM logs_archivage
UNION ALL
SELECT 
    'dossiers_archive' as table_name, 
    COUNT(*) as nb_lignes 
FROM dossiers_archive
UNION ALL
SELECT 
    'dossiers_mois_precedent' as table_name, 
    COUNT(*) as nb_lignes 
FROM dossiers_mois_precedent;

\echo ''
\echo '✅ Nettoyage terminé! Toutes les tables d''historique sont vides.'
\echo '📌 Les données courantes (dossiers, actions, etc.) sont conservées.'
\echo '🚀 Vous pouvez maintenant recommencer vos tests depuis le début.'
