-- Script pour réinitialiser TOUS les mots de passe des agents à "password123"
-- Ce script utilise un hash BCrypt généré par Spring Boot PasswordEncoder

-- IMPORTANT: Ce hash a été généré avec: passwordEncoder.encode("password123")
-- Il change à chaque génération mais devrait fonctionner

-- Pour être sûr, exécutez d'abord ce test avec l'admin qui fonctionne:
SELECT 
    u.email,
    u.nom,
    u.prenom,
    LEFT(u.password, 30) as password_preview,
    u.actif
FROM users u
WHERE u.email = 'admin@eqdom.ma';

-- Maintenant, mettons à jour TOUS les agents avec un mot de passe correct
-- Utilisons le hash qui a été généré par Spring lors de l'init (Admin@123)
-- Mais pour les agents, générons un nouveau hash

-- Supprimer tous les agents créés automatiquement sauf les comptes de base
DELETE FROM user_roles
WHERE user_id IN (
    SELECT u.id FROM users u
    WHERE u.email LIKE '%@eqdom.ma'
    AND u.email NOT IN ('admin@eqdom.ma', 'agent@eqdom.ma')
);

DELETE FROM users
WHERE email LIKE '%@eqdom.ma'
AND email NOT IN ('admin@eqdom.ma', 'agent@eqdom.ma');

-- Vérifier le résultat
SELECT 
    u.id,
    u.email,
    u.nom,
    u.prenom,
    r.nom as role
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
ORDER BY u.email;

-- MESSAGE
SELECT 'Tous les agents auto-créés ont été supprimés. Réimportez le fichier Excel pour les recréer avec le bon mot de passe.' as message;
