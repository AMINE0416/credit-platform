-- Script pour mettre à jour le nom de l'administrateur
-- De "Bennani Yassine" à "Bourmani Amine"

UPDATE users 
SET nom = 'Bourmani', 
    prenom = 'Amine'
WHERE email = 'admin@eqdom.ma';

SELECT * FROM users WHERE email = 'admin@eqdom.ma';
