-- Script d'optimisation des index pour améliorer les performances

-- ============================================
-- Index sur les colonnes de recherche fréquentes
-- ============================================

-- Dossier
CREATE INDEX IF NOT EXISTS idx_dossier_statut ON dossier(statut);
CREATE INDEX IF NOT EXISTS idx_dossier_agent ON dossier(agent_id);
CREATE INDEX IF NOT EXISTS idx_dossier_numero_contrat ON dossier(numero_contrat);
CREATE INDEX IF NOT EXISTS idx_dossier_created_at ON dossier(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_dossier_updated_at ON dossier(updated_at DESC);

-- Client
CREATE INDEX IF NOT EXISTS idx_client_dossier ON client(dossier_id);
CREATE INDEX IF NOT EXISTS idx_client_cin ON client(cin);

-- Promesse
CREATE INDEX IF NOT EXISTS idx_promesse_dossier ON promesse(dossier_id);
CREATE INDEX IF NOT EXISTS idx_promesse_date ON promesse(date_promesse);
CREATE INDEX IF NOT EXISTS idx_promesse_statut ON promesse(statut);

-- Historique
CREATE INDEX IF NOT EXISTS idx_historique_dossier ON historique_dossier(dossier_id);
CREATE INDEX IF NOT EXISTS idx_historique_date ON historique_dossier(date_modification DESC);

-- Log Archivage
CREATE INDEX IF NOT EXISTS idx_log_archivage_dossier ON log_archivage(dossier_id);
CREATE INDEX IF NOT EXISTS idx_log_archivage_date ON log_archivage(date_archivage DESC);

-- Utilisateurs
CREATE INDEX IF NOT EXISTS idx_utilisateur_username ON utilisateur(username);
CREATE INDEX IF NOT EXISTS idx_utilisateur_role ON utilisateur(role);

-- ============================================
-- Index composites pour les requêtes complexes
-- ============================================

-- Recherche par statut et agent
CREATE INDEX IF NOT EXISTS idx_dossier_statut_agent ON dossier(statut, agent_id);

-- Recherche par date et statut
CREATE INDEX IF NOT EXISTS idx_dossier_date_statut ON dossier(created_at DESC, statut);

-- Recherche de promesses par date et statut
CREATE INDEX IF NOT EXISTS idx_promesse_date_statut ON promesse(date_promesse, statut);

-- Historique par dossier et date
CREATE INDEX IF NOT EXISTS idx_historique_dossier_date ON historique_dossier(dossier_id, date_modification DESC);

-- ============================================
-- Index pour les recherches textuelles (PostgreSQL)
-- ============================================

-- Recherche sur nom et prénom des clients
CREATE INDEX IF NOT EXISTS idx_client_nom_prenom_tsvector 
ON client USING gin(to_tsvector('french', coalesce(nom, '') || ' ' || coalesce(prenom, '')));

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
-- Vérification des index créés
-- ============================================

SELECT 
    schemaname AS schema,
    tablename AS table,
    indexname AS index_name,
    indexdef AS definition
FROM pg_indexes 
WHERE schemaname = 'public'
ORDER BY tablename, indexname;

-- ============================================
-- Statistiques sur l'utilisation des tables
-- ============================================

SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size,
    n_live_tup AS rows
FROM pg_stat_user_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

PRINT 'Index d''optimisation créés avec succès!';
