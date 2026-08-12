-- Script pour vérifier et corriger les rôles des agents
-- Vérifie que tous les agents ont bien le rôle AGENT assigné

-- 1. Afficher tous les utilisateurs sans rôles
SELECT 
    u.id,
    u.email,
    u.nom,
    u.prenom,
    u.actif,
    COUNT(ur.role_id) as nombre_roles
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
GROUP BY u.id, u.email, u.nom, u.prenom, u.actif
HAVING COUNT(ur.role_id) = 0;

-- 2. Assigner le rôle AGENT à tous les utilisateurs qui n'ont pas de rôles
-- (sauf si c'est l'admin)
INSERT INTO user_roles (user_id, role_id)
SELECT 
    u.id,
    (SELECT id FROM roles WHERE nom = 'AGENT' LIMIT 1)
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
WHERE ur.role_id IS NULL
AND u.email != 'admin@eqdom.ma'
AND NOT EXISTS (
    SELECT 1 FROM user_roles WHERE user_id = u.id
);

-- 3. Vérifier le résultat
SELECT 
    u.id,
    u.email,
    u.nom,
    u.prenom,
    u.actif,
    r.nom as role
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
ORDER BY u.id;
