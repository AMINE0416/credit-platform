-- Exécuter ce script directement dans pgAdmin ou votre client PostgreSQL

-- 1. Vérifier les statuts actuels
SELECT 'Statuts actuels:' AS info;
SELECT enumlabel 
FROM pg_enum e
JOIN pg_type t ON e.enumtypid = t.oid
WHERE t.typname = 'statut_dossier'
ORDER BY e.enumsortorder;

-- 2. Ajouter SUIVI_INJOIGNABLE si manquant
ALTER TYPE statut_dossier ADD VALUE IF NOT EXISTS 'SUIVI_INJOIGNABLE';

-- 3. Vérifier que c'est ajouté
SELECT 'Après ajout:' AS info;
SELECT enumlabel 
FROM pg_enum e
JOIN pg_type t ON e.enumtypid = t.oid
WHERE t.typname = 'statut_dossier'
ORDER BY e.enumsortorder;
