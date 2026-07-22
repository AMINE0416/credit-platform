-- ============================================
-- REQUÊTES SQL UTILES - Credit Platform
-- Base de données : PostgreSQL
-- ============================================

-- ============================================
-- 1. VISUALISATION DES DONNÉES
-- ============================================

-- Tous les utilisateurs
SELECT 
    id,
    username,
    full_name,
    role,
    created_at,
    updated_at
FROM "user"
ORDER BY created_at DESC;

-- Tous les dossiers (50 derniers)
SELECT 
    id,
    numero_dossier,
    client_name,
    montant,
    status,
    affected_agent_id,
    created_at,
    updated_at
FROM dossier
ORDER BY created_at DESC
LIMIT 50;

-- Historique des actions (100 dernières)
SELECT 
    ah.id,
    ah.action_type,
    ah.old_status,
    ah.new_status,
    ah.comment,
    ah.action_date,
    u.full_name as user_name,
    d.numero_dossier
FROM action_history ah
LEFT JOIN "user" u ON ah.user_id = u.id
LEFT JOIN dossier d ON ah.dossier_id = d.id
ORDER BY ah.action_date DESC
LIMIT 100;

-- ============================================
-- 2. STATISTIQUES ET KPIs
-- ============================================

-- Nombre total de dossiers
SELECT COUNT(*) as total_dossiers FROM dossier;

-- Nombre d'utilisateurs par rôle
SELECT 
    role,
    COUNT(*) as nombre
FROM "user"
GROUP BY role;

-- Dossiers par statut
SELECT 
    status,
    COUNT(*) as nombre,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM dossier), 2) as pourcentage
FROM dossier
GROUP BY status
ORDER BY nombre DESC;

-- Montant total des dossiers
SELECT 
    status,
    COUNT(*) as nombre_dossiers,
    SUM(montant) as montant_total,
    AVG(montant) as montant_moyen
FROM dossier
GROUP BY status
ORDER BY montant_total DESC;

-- Dossiers traités aujourd'hui
SELECT 
    status,
    COUNT(*) as nombre
FROM dossier
WHERE DATE(updated_at) = CURRENT_DATE
GROUP BY status;

-- Dossiers créés cette semaine
SELECT 
    DATE(created_at) as date,
    COUNT(*) as nombre
FROM dossier
WHERE created_at >= CURRENT_DATE - INTERVAL '7 days'
GROUP BY DATE(created_at)
ORDER BY date DESC;

-- Performance des agents (nombre de dossiers traités)
SELECT 
    u.full_name as agent,
    COUNT(d.id) as dossiers_affectes,
    COUNT(CASE WHEN d.status = 'VALIDE' THEN 1 END) as valides,
    COUNT(CASE WHEN d.status = 'REJETE' THEN 1 END) as rejetes,
    COUNT(CASE WHEN d.status IN ('EN_ATTENTE', 'EN_COURS') THEN 1 END) as en_cours
FROM "user" u
LEFT JOIN dossier d ON d.affected_agent_id = u.id
WHERE u.role = 'AGENT'
GROUP BY u.id, u.full_name
ORDER BY dossiers_affectes DESC;

-- ============================================
-- 3. RECHERCHES SPÉCIFIQUES
-- ============================================

-- Chercher un dossier par numéro
SELECT * FROM dossier 
WHERE numero_dossier = 'D2024-00001';

-- Chercher un dossier par nom de client
SELECT * FROM dossier 
WHERE LOWER(client_name) LIKE LOWER('%nom%');

-- Dossiers d'un agent spécifique
SELECT 
    d.*,
    u.full_name as agent_name
FROM dossier d
LEFT JOIN "user" u ON d.affected_agent_id = u.id
WHERE u.username = 'nom_agent'
ORDER BY d.created_at DESC;

-- Dossiers sans agent affecté
SELECT * FROM dossier
WHERE affected_agent_id IS NULL
ORDER BY created_at DESC;

-- Dossiers avec montant supérieur à X
SELECT * FROM dossier
WHERE montant > 50000
ORDER BY montant DESC;

-- Dossiers en retard (date promesse dépassée)
SELECT 
    numero_dossier,
    client_name,
    status,
    date_promesse,
    (CURRENT_DATE - date_promesse) as jours_retard
FROM dossier
WHERE date_promesse < CURRENT_DATE
  AND status NOT IN ('VALIDE', 'REJETE', 'ANNULE')
ORDER BY jours_retard DESC;

-- ============================================
-- 4. JOINTURES COMPLEXES
-- ============================================

-- Vue complète d'un dossier avec agent et historique
SELECT 
    d.numero_dossier,
    d.client_name,
    d.status,
    u.full_name as agent,
    COUNT(ah.id) as nombre_actions,
    MAX(ah.action_date) as derniere_action
FROM dossier d
LEFT JOIN "user" u ON d.affected_agent_id = u.id
LEFT JOIN action_history ah ON ah.dossier_id = d.id
WHERE d.numero_dossier = 'D2024-00001'
GROUP BY d.id, d.numero_dossier, d.client_name, d.status, u.full_name;

-- Activité récente (tous types)
SELECT 
    'DOSSIER' as type,
    d.numero_dossier as reference,
    d.status as info,
    d.created_at as date
FROM dossier d
WHERE d.created_at >= CURRENT_DATE - INTERVAL '7 days'

UNION ALL

SELECT 
    'ACTION' as type,
    d.numero_dossier as reference,
    ah.action_type as info,
    ah.action_date as date
FROM action_history ah
JOIN dossier d ON ah.dossier_id = d.id
WHERE ah.action_date >= CURRENT_DATE - INTERVAL '7 days'

ORDER BY date DESC
LIMIT 100;

-- ============================================
-- 5. VÉRIFICATIONS ET VALIDATION
-- ============================================

-- Vérifier l'intégrité des données
-- Dossiers avec agent inexistant
SELECT d.* FROM dossier d
LEFT JOIN "user" u ON d.affected_agent_id = u.id
WHERE d.affected_agent_id IS NOT NULL
  AND u.id IS NULL;

-- Actions avec utilisateur inexistant
SELECT ah.* FROM action_history ah
LEFT JOIN "user" u ON ah.user_id = u.id
WHERE u.id IS NULL;

-- Dossiers sans historique
SELECT d.* FROM dossier d
LEFT JOIN action_history ah ON ah.dossier_id = d.id
WHERE ah.id IS NULL
  AND d.status != 'EN_ATTENTE';

-- Vérifier les doublons de numéro de dossier
SELECT 
    numero_dossier,
    COUNT(*) as occurences
FROM dossier
GROUP BY numero_dossier
HAVING COUNT(*) > 1;

-- ============================================
-- 6. MODIFICATIONS DE DONNÉES
-- ⚠️ ATTENTION : À utiliser avec précaution !
-- ============================================

-- Affecter un dossier à un agent
-- UPDATE dossier 
-- SET affected_agent_id = (SELECT id FROM "user" WHERE username = 'agent1')
-- WHERE numero_dossier = 'D2024-00001';

-- Changer le statut d'un dossier
-- UPDATE dossier 
-- SET status = 'EN_COURS', updated_at = CURRENT_TIMESTAMP
-- WHERE numero_dossier = 'D2024-00001';

-- Supprimer un dossier (CASCADE supprime aussi l'historique)
-- DELETE FROM dossier WHERE numero_dossier = 'D2024-00001';

-- ============================================
-- 7. MAINTENANCE ET NETTOYAGE
-- ⚠️ DÉVELOPPEMENT LOCAL UNIQUEMENT !
-- ============================================

-- Voir la taille de la base de données
SELECT 
    pg_size_pretty(pg_database_size('credit_platform')) as taille_totale;

-- Voir la taille de chaque table
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- Analyser les performances d'une table
ANALYZE dossier;
ANALYZE "user";
ANALYZE action_history;

-- ⚠️ RESET COMPLET (LOCAL SEULEMENT) ⚠️
-- TRUNCATE TABLE action_history CASCADE;
-- TRUNCATE TABLE dossier CASCADE;
-- DELETE FROM "user" WHERE username != 'admin';

-- Réinitialiser les séquences
-- ALTER SEQUENCE dossier_id_seq RESTART WITH 1;
-- ALTER SEQUENCE user_id_seq RESTART WITH 2;
-- ALTER SEQUENCE action_history_id_seq RESTART WITH 1;

-- ============================================
-- 8. EXPORT DE DONNÉES
-- ============================================

-- Créer une vue pour export Excel
CREATE OR REPLACE VIEW v_export_dossiers AS
SELECT 
    d.numero_dossier,
    d.client_name,
    d.montant,
    d.status,
    d.date_promesse,
    u.full_name as agent,
    d.created_at,
    d.updated_at,
    COUNT(ah.id) as nombre_actions
FROM dossier d
LEFT JOIN "user" u ON d.affected_agent_id = u.id
LEFT JOIN action_history ah ON ah.dossier_id = d.id
GROUP BY d.id, d.numero_dossier, d.client_name, d.montant, d.status, 
         d.date_promesse, u.full_name, d.created_at, d.updated_at;

-- Utiliser la vue
SELECT * FROM v_export_dossiers;

-- ============================================
-- 9. REQUÊTES DE DEBUG
-- ============================================

-- Voir les connexions actives
SELECT 
    pid,
    usename,
    application_name,
    client_addr,
    state,
    query_start,
    state_change
FROM pg_stat_activity
WHERE datname = 'credit_platform'
ORDER BY query_start DESC;

-- Voir les index de la base
SELECT 
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE schemaname = 'public'
ORDER BY tablename, indexname;

-- Voir les contraintes
SELECT 
    tc.table_name,
    tc.constraint_name,
    tc.constraint_type,
    kcu.column_name
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu 
    ON tc.constraint_name = kcu.constraint_name
WHERE tc.table_schema = 'public'
ORDER BY tc.table_name, tc.constraint_type;

-- ============================================
-- 10. SAUVEGARDES ET RESTAURATION
-- ============================================

-- Ces commandes s'exécutent dans le terminal, pas dans pgAdmin

-- Sauvegarder la base
-- pg_dump -U postgres -d credit_platform -F c -f backup_credit_platform.backup

-- Sauvegarder en SQL
-- pg_dump -U postgres -d credit_platform > backup_credit_platform.sql

-- Restaurer depuis backup
-- pg_restore -U postgres -d credit_platform backup_credit_platform.backup

-- Restaurer depuis SQL
-- psql -U postgres -d credit_platform < backup_credit_platform.sql

-- ============================================
-- FIN DU FICHIER
-- ============================================

-- Notes :
-- 1. Les requêtes commentées (--) sont à décommenter avec précaution
-- 2. Toujours tester sur base locale avant production
-- 3. Faire des backups avant modifications importantes
-- 4. Utiliser LIMIT pour éviter de charger trop de données
