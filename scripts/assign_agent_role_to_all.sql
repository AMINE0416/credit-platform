-- Assigner le rôle AGENT à tous les utilisateurs qui n'ont pas de rôle

BEGIN;

-- Afficher les utilisateurs sans rôle
SELECT u.id, u.email, u.nom, u.prenom
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
WHERE ur.user_id IS NULL
  AND u.id > 1  -- Exclure l'admin
ORDER BY u.id;

-- Assigner le rôle AGENT à tous les utilisateurs sans rôle
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, 2  -- 2 = ID du rôle AGENT
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
WHERE ur.user_id IS NULL
  AND u.id > 1;  -- Exclure l'admin

-- Réinitialiser le mot de passe à password123 pour tous ces agents
UPDATE users
SET password = '$2a$10$xvPZ.xJH8QvwPOvHPqLQr.EYJZr7CdJOcW3d7.VPEw7GJZP0kXqHS'
WHERE id IN (
    SELECT u.id
    FROM users u
    WHERE u.id > 1
);

COMMIT;

-- Vérification
SELECT u.id, u.email, u.nom, u.prenom, r.nom as role
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE u.id > 1
ORDER BY u.id
LIMIT 10;

SELECT 'Role AGENT assigne et mot de passe reinitialise: password123' as message;
