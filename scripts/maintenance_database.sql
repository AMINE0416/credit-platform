-- Script de maintenance de la base de données

-- ============================================
-- Nettoyage des données obsolètes
-- ============================================

-- Supprimer les logs d'archivage de plus de 6 mois
DELETE FROM log_archivage 
WHERE date_archivage < NOW() - INTERVAL '6 months';

-- Nettoyer l'historique trop ancien (garder 2 ans)
DELETE FROM historique_dossier 
WHERE date_modification < NOW() - INTERVAL '2 years';

-- ============================================
-- VACUUM pour récupérer l'espace disque
-- ============================================

VACUUM FULL ANALYZE dossier;
VACUUM FULL ANALYZE client;
VACUUM FULL ANALYZE promesse;
VACUUM FULL ANALYZE historique_dossier;
VACUUM FULL ANALYZE log_archivage;
VACUUM FULL ANALYZE utilisateur;

-- ============================================
-- Réindexation pour optimiser les performances
-- ============================================

REINDEX TABLE dossier;
REINDEX TABLE client;
REINDEX TABLE promesse;
REINDEX TABLE historique_dossier;
REINDEX TABLE log_archivage;
REINDEX TABLE utilisateur;

-- ============================================
-- Mise à jour des statistiques
-- ============================================

ANALYZE dossier;
ANALYZE client;
ANALYZE promesse;
ANALYZE historique_dossier;
ANALYZE log_archivage;
ANALYZE utilisateur;

-- ============================================
-- Vérification de la santé de la base
-- ============================================

-- Tables les plus volumineuses
SELECT 
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS total_size,
    pg_size_pretty(pg_relation_size(schemaname||'.'||tablename)) AS table_size,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename) - pg_relation_size(schemaname||'.'||tablename)) AS indexes_size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- Index non utilisés (à supprimer potentiellement)
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan as scans,
    pg_size_pretty(pg_relation_size(indexrelid)) as size
FROM pg_stat_user_indexes
WHERE idx_scan = 0
AND schemaname = 'public'
ORDER BY pg_relation_size(indexrelid) DESC;

-- Nombre de lignes par table
SELECT 
    schemaname,
    tablename,
    n_live_tup AS row_count
FROM pg_stat_user_tables
WHERE schemaname = 'public'
ORDER BY n_live_tup DESC;

PRINT 'Maintenance de la base de données terminée!';
