-- ========================================
-- SCRIPT DE RÉINITIALISATION DES DOSSIERS
-- ========================================
-- Ce script supprime TOUS les dossiers, crédits, clients et agents
-- MAIS garde l'utilisateur ADMIN
-- ========================================

-- ÉTAPE 1 : Afficher ce qui va être supprimé
-- ========================================
SELECT 'AVANT SUPPRESSION' as Etape;

SELECT 'Actions Dossiers' as Table_Name, COUNT(*) as Nb_Lignes FROM action_dossiers
UNION ALL
SELECT 'Dossiers' as Table_Name, COUNT(*) as Nb_Lignes FROM dossiers
UNION ALL
SELECT 'Crédits' as Table_Name, COUNT(*) as Nb_Lignes FROM credits
UNION ALL
SELECT 'Clients' as Table_Name, COUNT(*) as Nb_Lignes FROM clients
UNION ALL
SELECT 'Agents (users avec rôle AGENT)' as Table_Name, COUNT(*) as Nb_Lignes 
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE r.nom = 'AGENT';


-- ÉTAPE 2 : Supprimer les données dans l'ordre (respect des contraintes FK)
-- ========================================

-- 2.1 Supprimer les actions des dossiers
DELETE FROM action_dossiers;

-- 2.2 Supprimer les dossiers
DELETE FROM dossiers;

-- 2.3 Supprimer les crédits
DELETE FROM credits;

-- 2.4 Supprimer les clients
DELETE FROM clients;

-- 2.5 Supprimer les agents (users avec rôle AGENT uniquement, PAS l'admin)
DELETE FROM user_roles 
WHERE user_id IN (
    SELECT u.id 
    FROM users u
    JOIN user_roles ur ON u.id = ur.user_id
    JOIN roles r ON ur.role_id = r.id
    WHERE r.nom = 'AGENT'
);

DELETE FROM users 
WHERE id IN (
    SELECT u.id 
    FROM users u
    LEFT JOIN user_roles ur ON u.id = ur.user_id
    WHERE ur.user_id IS NULL
);


-- ÉTAPE 3 : Réinitialiser les séquences (auto-increment)
-- ========================================
-- Note: Cela dépend de votre base de données (PostgreSQL, MySQL, H2)

-- Pour PostgreSQL:
-- ALTER SEQUENCE clients_id_seq RESTART WITH 1;
-- ALTER SEQUENCE credits_id_seq RESTART WITH 1;
-- ALTER SEQUENCE dossiers_id_seq RESTART WITH 1;
-- ALTER SEQUENCE action_dossiers_id_seq RESTART WITH 1;
-- ALTER SEQUENCE users_id_seq RESTART WITH 1;

-- Pour MySQL:
-- ALTER TABLE clients AUTO_INCREMENT = 1;
-- ALTER TABLE credits AUTO_INCREMENT = 1;
-- ALTER TABLE dossiers AUTO_INCREMENT = 1;
-- ALTER TABLE action_dossiers AUTO_INCREMENT = 1;
-- ALTER TABLE users AUTO_INCREMENT = 1;

-- Pour H2 (base de données en mémoire):
-- Les séquences se réinitialisent automatiquement


-- ÉTAPE 4 : Vérification après suppression
-- ========================================
SELECT 'APRÈS SUPPRESSION' as Etape;

SELECT 'Actions Dossiers' as Table_Name, COUNT(*) as Nb_Lignes FROM action_dossiers
UNION ALL
SELECT 'Dossiers' as Table_Name, COUNT(*) as Nb_Lignes FROM dossiers
UNION ALL
SELECT 'Crédits' as Table_Name, COUNT(*) as Nb_Lignes FROM credits
UNION ALL
SELECT 'Clients' as Table_Name, COUNT(*) as Nb_Lignes FROM clients
UNION ALL
SELECT 'Users' as Table_Name, COUNT(*) as Nb_Lignes FROM users;

-- Vérifier que l'admin existe toujours
SELECT 
    u.id,
    u.email,
    u.nom,
    u.prenom,
    r.nom as role
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE r.nom = 'ADMIN';

-- ÉTAPE 5 : La base est maintenant prête pour un nouvel import
-- ========================================
SELECT '✅ Base de données nettoyée - Prête pour un nouvel import' as Message;
