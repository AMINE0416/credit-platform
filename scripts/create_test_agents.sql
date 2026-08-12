-- Création des agents de test pour l'assignation automatique

BEGIN;

-- 1. Créer le rôle AGENT s'il n'existe pas
INSERT INTO roles (nom) 
VALUES ('AGENT')
ON CONFLICT (nom) DO NOTHING;

-- 2. Créer plusieurs agents de test
INSERT INTO users (email, password, nom, prenom, actif)
VALUES 
    ('agent1@eqdom.ma', '$2a$10$xvPZ.xJH8QvwPOvHPqLQr.EYJZr7CdJOcW3d7.VPEw7GJZP0kXqHS', 'Alami', 'Hassan', true),
    ('agent2@eqdom.ma', '$2a$10$xvPZ.xJH8QvwPOvHPqLQr.EYJZr7CdJOcW3d7.VPEw7GJZP0kXqHS', 'Bennani', 'Fatima', true),
    ('agent3@eqdom.ma', '$2a$10$xvPZ.xJH8QvwPOvHPqLQr.EYJZr7CdJOcW3d7.VPEw7GJZP0kXqHS', 'Haouza', 'Mohammed', true),
    ('agent@eqdom.ma', '$2a$10$xvPZ.xJH8QvwPOvHPqLQr.EYJZr7CdJOcW3d7.VPEw7GJZP0kXqHS', 'Agent', 'Test', true)
ON CONFLICT (email) DO NOTHING;

-- 3. Assigner le rôle AGENT à ces utilisateurs
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
CROSS JOIN roles r
WHERE u.email IN ('agent1@eqdom.ma', 'agent2@eqdom.ma', 'agent3@eqdom.ma', 'agent@eqdom.ma')
  AND r.nom = 'AGENT'
ON CONFLICT DO NOTHING;

COMMIT;

-- Vérification
SELECT 
    u.id,
    u.email,
    u.nom,
    u.prenom,
    r.nom as role
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE r.nom = 'AGENT'
ORDER BY u.email;

SELECT '✅ Agents créés avec succès. Mot de passe pour tous: password123' as message;
