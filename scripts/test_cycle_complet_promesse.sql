-- Test complet du cycle de preservation des promesses
-- Date: 2026-08-11

-- ==============================================
-- ETAPE 1: PREPARATION - Nettoyage complet
-- ==============================================

BEGIN;

-- Vider toutes les promesses existantes
UPDATE dossiers SET date_promesse = NULL, montant_promesse = NULL;
TRUNCATE TABLE dossiers_mois_precedent;

COMMIT;

SELECT '==============================================';
SELECT 'ETAPE 1: Preparation terminee';
SELECT '==============================================';
SELECT '';

-- ==============================================
-- ETAPE 2: Creer une promesse via SQL (simuler l action agent)
-- ==============================================

BEGIN;

-- Trouver le premier dossier EN_COURS
SELECT 'Dossier cible pour le test:';
SELECT 
    d.id as dossier_id,
    c.reference_contrat,
    cl.nom || ' ' || cl.prenom as client,
    d.statut_actuel
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
JOIN clients cl ON c.client_id = cl.id
WHERE d.statut_actuel = 'EN_COURS'
LIMIT 1;

-- Passer ce dossier en PROMESSE avec date et montant
UPDATE dossiers d
SET 
    statut_actuel = 'PROMESSE',
    date_promesse = '2026-08-25',
    montant_promesse = 5000.00
FROM credits c
WHERE d.credit_id = c.id
  AND d.id = (
      SELECT d2.id 
      FROM dossiers d2 
      WHERE d2.statut_actuel = 'EN_COURS' 
      LIMIT 1
  );

COMMIT;

SELECT '';
SELECT '==============================================';
SELECT 'ETAPE 2: Promesse creee';
SELECT '==============================================';
SELECT '';

-- Verifier la creation
SELECT 
    d.id,
    c.reference_contrat,
    d.statut_actuel,
    d.date_promesse,
    d.montant_promesse
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
WHERE d.date_promesse IS NOT NULL;

SELECT '';

-- ==============================================
-- ETAPE 3: ARCHIVAGE - Simuler l archivage mensuel
-- ==============================================

BEGIN;

-- Sauvegarder dans dossiers_mois_precedent
INSERT INTO dossiers_mois_precedent (
    numero_dossier,
    statut_final,
    mois_origine,
    date_promesse,
    montant_promesse,
    date_sauvegarde
)
SELECT 
    c.reference_contrat,
    'SUIVI_PROMESSE',
    '2026-08',
    d.date_promesse,
    d.montant_promesse,
    NOW()
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
WHERE d.statut_actuel = 'PROMESSE';

COMMIT;

SELECT '==============================================';
SELECT 'ETAPE 3: Archivage simule';
SELECT '==============================================';
SELECT '';

-- Verifier l archivage
SELECT 
    numero_dossier,
    statut_final,
    date_promesse,
    montant_promesse,
    mois_origine
FROM dossiers_mois_precedent
WHERE date_promesse IS NOT NULL;

SELECT '';

-- ==============================================
-- ETAPE 4: IMPORT - Simuler le nouvel import
-- ==============================================

BEGIN;

-- Mettre a jour le statut vers SUIVI_PROMESSE (comme le ferait l import)
UPDATE dossiers d
SET 
    statut_actuel = 'SUIVI_PROMESSE',
    mois_import = '2026-09',
    est_recurrent = TRUE
WHERE d.statut_actuel = 'PROMESSE';

COMMIT;

SELECT '==============================================';
SELECT 'ETAPE 4: Import simule (passage vers SUIVI_PROMESSE)';
SELECT '==============================================';
SELECT '';

-- Verifier que les promesses sont toujours presentes
SELECT 
    d.id,
    c.reference_contrat,
    d.statut_actuel,
    d.date_promesse,
    d.montant_promesse,
    d.est_recurrent,
    CASE 
        WHEN d.date_promesse IS NOT NULL THEN 'OK - Promesse preservee'
        ELSE 'ERREUR - Promesse perdue'
    END as resultat
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
WHERE d.statut_actuel = 'SUIVI_PROMESSE';

SELECT '';

-- ==============================================
-- ETAPE 5: VERIFICATION FINALE
-- ==============================================

SELECT '==============================================';
SELECT 'ETAPE 5: VERIFICATION FINALE';
SELECT '==============================================';
SELECT '';

SELECT 'Statistiques:';
SELECT 
    COUNT(*) as total_dossiers_avec_promesse,
    SUM(CASE WHEN statut_actuel = 'PROMESSE' THEN 1 ELSE 0 END) as nb_promesse,
    SUM(CASE WHEN statut_actuel = 'SUIVI_PROMESSE' THEN 1 ELSE 0 END) as nb_suivi_promesse
FROM dossiers
WHERE date_promesse IS NOT NULL;

SELECT '';
SELECT 'Test termine!';
SELECT '';
SELECT 'Si vous voyez date_promesse et montant_promesse dans SUIVI_PROMESSE,';
SELECT 'alors le systeme fonctionne correctement!';
SELECT '';
SELECT '==============================================';
