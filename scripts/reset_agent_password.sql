-- Réinitialiser le mot de passe d'agent3.fes@eqdom.ma à "password123"

BEGIN;

UPDATE users
SET password = '$2a$10$xvPZ.xJH8QvwPOvHPqLQr.EYJZr7CdJOcW3d7.VPEw7GJZP0kXqHS'
WHERE email = 'agent3.fes@eqdom.ma';

COMMIT;

SELECT email, nom, prenom, password, actif
FROM users
WHERE email = 'agent3.fes@eqdom.ma';

SELECT 'Mot de passe reinitialise: password123' as message;
