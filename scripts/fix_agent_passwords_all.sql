-- Script pour réinitialiser TOUS les mots de passe des agents
-- Mot de passe: password123
-- Hash BCrypt: $2a$10$xvPZ.xJH8QvwPOvHPqLQr.EYJZr7CdJOcW3d7.VPEw7GJZP0kXqHS

-- 1. Afficher tous les agents actuels
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
WHERE r.nom = 'AGENT'
ORDER BY u.email;

-- 2. Mettre à jour le mot de passe de TOUS les agents
UPDATE users
SET password = '$2a$10$xvPZ.xJH8QvwPOvHPqLQr.EYJZr7CdJOcW3d7.VPEw7GJZP0kXqHS'
WHERE id IN (
    SELECT u.id
    FROM users u
    JOIN user_roles ur ON u.id = ur.user_id
    JOIN roles r ON ur.role_id = r.id
    WHERE r.nom = 'AGENT'
);

-- 3. Vérifier le résultat
SELECT 
    u.id,
    u.email,
    u.nom,
    u.prenom,
    u.password,
    u.actif
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE r.nom = 'AGENT'
ORDER BY u.email;
