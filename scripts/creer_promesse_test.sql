-- Script pour créer une promesse de test
-- Date: 2026-08-11
-- Ce script crée une promesse pour le dossier SUIVI_PROMESSE de Khadija Lahlou

BEGIN;

\echo '================================================'
\echo 'CRÉATION DE PROMESSE DE TEST'
\echo '================================================'
\echo ''

-- 1. Afficher le dossier cible
\echo '1️⃣  Dossier cible (C22J0016 - KHADIJA LAHLOU):'
\echo ''
SELECT 
    d.id as dossier_id,
    c.reference_contrat,
    cl.cin,
    cl.nom || ' ' || cl.prenom as client,
    d.statut_actuel
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
JOIN clients cl ON c.client_id = cl.id
WHERE c.reference_contrat = 'C22J0016';

-- 2. Mettre à jour le dossier avec une promesse
\echo ''
\echo '2️⃣  Ajout de la promesse au dossier:'
\echo ''
UPDATE dossiers d
SET 
    date_promesse = '2026-08-20',
    montant_promesse = 3000.00
FROM credits c
WHERE d.credit_id = c.id
  AND c.reference_contrat = 'C22J0016'
RETURNING 
    d.id,
    c.reference_contrat,
    d.statut_actuel,
    d.date_promesse,
    d.montant_promesse;

-- 3. Vérifier le résultat
\echo ''
\echo '3️⃣  Vérification du résultat:'
\echo ''
SELECT 
    d.id,
    c.reference_contrat,
    cl.nom || ' ' || cl.prenom as client,
    d.statut_actuel,
    d.date_promesse,
    d.montant_promesse,
    CASE 
        WHEN d.date_promesse IS NOT NULL THEN '✅ Promesse ajoutée'
        ELSE '❌ Pas de promesse'
    END as statut
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
JOIN clients cl ON c.client_id = cl.id
WHERE c.reference_contrat = 'C22J0016';

COMMIT;

\echo ''
\echo '================================================'
\echo '✅ PROMESSE CRÉÉE AVEC SUCCÈS'
\echo '================================================'
\echo ''
\echo 'Vous pouvez maintenant:'
\echo '  1. Actualiser la page du dossier dans le navigateur'
\echo '  2. Vous devriez voir l encadré avec:'
\echo '     - Montant: 3000.00 MAD'
\echo '     - Date: 20 août 2026'
\echo ''
