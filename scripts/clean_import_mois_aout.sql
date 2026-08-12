-- Nettoyage complet pour réimport du mois d'août 2026

BEGIN;

-- Étape 1 : Supprimer les dossiers du mois
DELETE FROM dossiers WHERE mois_import = '2026-08';

-- Étape 2 : Supprimer les crédits orphelins
DELETE FROM credits WHERE id NOT IN (SELECT DISTINCT credit_id FROM dossiers WHERE credit_id IS NOT NULL);

-- Étape 3 : Supprimer les clients orphelins
DELETE FROM clients WHERE id NOT IN (SELECT DISTINCT client_id FROM credits WHERE client_id IS NOT NULL);

-- Étape 4 : Nettoyer les logs d'archivage du mois
DELETE FROM logs_archivage WHERE mois_concerne = '2026-08';

COMMIT;

-- Vérification finale
SELECT 'Nettoyage termine - Vous pouvez reimporter' as message;
