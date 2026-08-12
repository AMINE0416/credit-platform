-- ================================================================
-- SCRIPT DE TEST : IMPORT MOIS 8 AVEC DÉTECTION AUTOMATIQUE
-- ================================================================
-- Description : Simule un import du mois 8 avec :
--               - 3 dossiers récurrents (présents au mois 7)
--               - 2 nouveaux dossiers
--               La détection automatique doit appliquer les
--               bonnes règles de statut
-- ================================================================

SELECT '=== PRÉPARATION IMPORT MOIS 8 ===' as info;

-- ================================================================
-- 1. CRÉER LES NOUVEAUX CLIENTS (pour les 2 nouveaux dossiers)
-- ================================================================
-- Note: La colonne 'email' n'existe pas dans la table clients

INSERT INTO clients (cin, nom, prenom, telephone, telephone2, adresse, ville, code_agence, lib_agence) 
VALUES
('TEST_KL123456', 'NOUVEAU', 'Ahmed', '0667890123', '0767890123', '11 Rue Liberté', 'Tanger', 'TAN001', 'Agence Tanger Centre'),
('TEST_MN789012', 'NOUVELLE', 'Khadija', '0678901234', '0778901234', '22 Bd Pasteur', 'Agadir', 'AGA001', 'Agence Agadir')
ON CONFLICT (cin) DO NOTHING;

-- ================================================================
-- 2. CRÉER/METTRE À JOUR LES CRÉDITS
-- ================================================================

-- Mettre à jour les crédits récurrents (simuler évolution)
UPDATE credits SET
    nbr_impaye = 3,
    mensualite = 1500.00,
    encours = 43500.00,
    impaye_montant = 4500.00,
    score = 70.0
WHERE reference_contrat = 'DOSS-TEST-001';

UPDATE credits SET
    nbr_impaye = 1,
    mensualite = 2000.00,
    encours = 58000.00,
    impaye_montant = 2000.00,
    score = 85.0
WHERE reference_contrat = 'DOSS-TEST-002';

UPDATE credits SET
    nbr_impaye = 4,
    mensualite = 1200.00,
    encours = 34800.00,
    impaye_montant = 4800.00,
    score = 60.0
WHERE reference_contrat = 'DOSS-TEST-003';

-- Créer les nouveaux crédits
INSERT INTO credits (
    client_id, 
    reference_contrat, 
    type_credit, 
    segment, 
    nbr_impaye, 
    mensualite, 
    encours, 
    impaye_montant,
    prv_cum,
    stage,
    score
)
SELECT c.id, 'DOSS-TEST-006', 'AUTO_CLASSIQUE', 'AUTO PP', 1, 1800.00, 52000.00, 1800.00, 800.00, 'S1', 80.0
FROM clients c WHERE c.cin = 'TEST_KL123456'
ON CONFLICT (reference_contrat, client_id) DO NOTHING;

INSERT INTO credits (
    client_id, 
    reference_contrat, 
    type_credit, 
    segment, 
    nbr_impaye, 
    mensualite, 
    encours, 
    impaye_montant,
    prv_cum,
    stage,
    score
)
SELECT c.id, 'DOSS-TEST-007', 'AUTO_CLASSIQUE', 'AUTO PP', 2, 2200.00, 64000.00, 4400.00, 1200.00, 'S2', 75.0
FROM clients c WHERE c.cin = 'TEST_MN789012'
ON CONFLICT (reference_contrat, client_id) DO NOTHING;

-- ================================================================
-- 3. FONCTION POUR CALCULER LE STATUT AUTOMATIQUE
-- ================================================================

CREATE OR REPLACE FUNCTION calculer_statut_auto_mois8(ref_contrat VARCHAR) 
RETURNS TABLE(
    statut_auto VARCHAR,
    est_recurrent BOOLEAN,
    ancien_statut VARCHAR
) AS $$
DECLARE
    v_ancien_statut VARCHAR;
    v_est_recurrent BOOLEAN;
    v_statut_auto VARCHAR;
BEGIN
    -- Vérifier si le dossier existe dans le mois précédent
    SELECT dmp.statut_final INTO v_ancien_statut
    FROM dossiers_mois_precedent dmp
    WHERE dmp.numero_dossier = ref_contrat;
    
    v_est_recurrent := (v_ancien_statut IS NOT NULL);
    
    IF NOT v_est_recurrent THEN
        -- Nouveau dossier
        v_statut_auto := 'EN_COURS';
    ELSE
        -- Dossier récurrent - appliquer la logique métier
        v_statut_auto := CASE v_ancien_statut
            WHEN 'CONTACTE' THEN 'RELANCE'
            WHEN 'PROMESSE' THEN 'SUIVI_PROMESSE'
            WHEN 'INJOIGNABLE' THEN 'INJOIGNABLE'
            WHEN 'EN_ATTENTE' THEN 'EN_ATTENTE'
            WHEN 'EN_COURS' THEN 'EN_COURS'
            ELSE 'EN_COURS'
        END;
    END IF;
    
    RETURN QUERY SELECT v_statut_auto, v_est_recurrent, v_ancien_statut;
END;
$$ LANGUAGE plpgsql;

-- ================================================================
-- 4. PRÉVISUALISER LA DÉTECTION AUTOMATIQUE
-- ================================================================

SELECT '📋 PRÉVISUALISATION DÉTECTION AUTOMATIQUE' as titre;

SELECT 
    cr.reference_contrat,
    cl.nom || ' ' || cl.prenom as client,
    dmp.statut_final as statut_mois7,
    calc.statut_auto as statut_mois8_prevu,
    CASE 
        WHEN calc.est_recurrent THEN '🔄 RÉCURRENT'
        ELSE '🆕 NOUVEAU'
    END as type,
    CASE 
        WHEN calc.est_recurrent THEN 
            '✅ ' || dmp.statut_final || ' → ' || calc.statut_auto
        ELSE 
            '✅ Nouveau → ' || calc.statut_auto
    END as transition
FROM credits cr
JOIN clients cl ON cr.client_id = cl.id
CROSS JOIN LATERAL calculer_statut_auto_mois8(cr.reference_contrat) calc
LEFT JOIN dossiers_mois_precedent dmp ON dmp.numero_dossier = cr.reference_contrat
WHERE cr.reference_contrat IN ('DOSS-TEST-001', 'DOSS-TEST-002', 'DOSS-TEST-003', 'DOSS-TEST-006', 'DOSS-TEST-007')
ORDER BY calc.est_recurrent DESC, cr.reference_contrat;

-- ================================================================
-- 5. CRÉER LES DOSSIERS MOIS 8 AVEC DÉTECTION
-- ================================================================

SELECT '=== IMPORT DOSSIERS MOIS 8 ===' as info;

INSERT INTO dossiers (
    credit_id, 
    statut_actuel, 
    date_creation, 
    mois_import, 
    est_recurrent,
    mois_precedent_statut
)
SELECT 
    cr.id,
    calc.statut_auto,
    CURRENT_DATE,
    '2024-08',
    calc.est_recurrent,
    calc.ancien_statut
FROM credits cr
CROSS JOIN LATERAL calculer_statut_auto_mois8(cr.reference_contrat) calc
WHERE cr.reference_contrat IN ('DOSS-TEST-001', 'DOSS-TEST-002', 'DOSS-TEST-003', 'DOSS-TEST-006', 'DOSS-TEST-007')
ON CONFLICT DO NOTHING;

-- ================================================================
-- 6. VÉRIFICATIONS POST-IMPORT
-- ================================================================

SELECT '=== VÉRIFICATIONS POST-IMPORT ===' as info;

-- Vue d'ensemble
SELECT 
    '📊 VUE D\'ENSEMBLE MOIS 8' as titre,
    cr.reference_contrat,
    cl.nom || ' ' || cl.prenom as client,
    d.statut_actuel as statut_mois8,
    d.mois_precedent_statut as statut_mois7,
    d.est_recurrent,
    CASE 
        WHEN d.est_recurrent THEN '🔄 RÉCURRENT'
        ELSE '🆕 NOUVEAU'
    END as type,
    CASE 
        WHEN d.est_recurrent THEN 
            d.mois_precedent_statut || ' → ' || d.statut_actuel::text
        ELSE 
            'N/A → ' || d.statut_actuel::text
    END as transition
FROM dossiers d
JOIN credits cr ON d.credit_id = cr.id
JOIN clients cl ON cr.client_id = cl.id
WHERE d.mois_import = '2024-08'
ORDER BY d.est_recurrent DESC, cr.reference_contrat;

-- Statistiques globales
SELECT 
    '📈 STATISTIQUES IMPORT MOIS 8' as categorie,
    COUNT(*) as nombre,
    'Total dossiers' as description
FROM dossiers WHERE mois_import = '2024-08'
UNION ALL
SELECT '🔄', COUNT(*), 'Dossiers récurrents'
FROM dossiers WHERE mois_import = '2024-08' AND est_recurrent = true
UNION ALL
SELECT '🆕', COUNT(*), 'Nouveaux dossiers'
FROM dossiers WHERE mois_import = '2024-08' AND est_recurrent = false;

-- Répartition des statuts pour récurrents
SELECT 
    '🔄 TRANSITIONS RÉCURRENTS' as titre,
    d.mois_precedent_statut as mois7,
    d.statut_actuel::text as mois8,
    COUNT(*) as nombre,
    '✅' as validation
FROM dossiers d
WHERE d.mois_import = '2024-08' AND d.est_recurrent = true
GROUP BY d.mois_precedent_statut, d.statut_actuel;

-- Validation des règles métier
SELECT 
    '✅ VALIDATION RÈGLES MÉTIER' as test,
    CASE 
        WHEN COUNT(*) FILTER (WHERE mois_precedent_statut = 'CONTACTE' AND statut_actuel::text != 'RELANCE') > 0 
        THEN '❌ ERREUR: CONTACTE devrait devenir RELANCE'
        WHEN COUNT(*) FILTER (WHERE mois_precedent_statut = 'PROMESSE' AND statut_actuel::text != 'SUIVI_PROMESSE') > 0
        THEN '❌ ERREUR: PROMESSE devrait devenir SUIVI_PROMESSE'
        WHEN COUNT(*) FILTER (WHERE mois_precedent_statut = 'INJOIGNABLE' AND statut_actuel::text != 'INJOIGNABLE') > 0
        THEN '❌ ERREUR: INJOIGNABLE devrait rester INJOIGNABLE'
        WHEN COUNT(*) FILTER (WHERE est_recurrent = false AND statut_actuel::text != 'EN_COURS') > 0
        THEN '❌ ERREUR: Nouveaux dossiers devraient être EN_COURS'
        ELSE '✅ TOUTES LES RÈGLES SONT RESPECTÉES'
    END as resultat
FROM dossiers
WHERE mois_import = '2024-08';

-- ================================================================
-- RÉCAPITULATIF FINAL
-- ================================================================

SELECT 
    '✅ IMPORT MOIS 8 TERMINÉ' as statut,
    (SELECT COUNT(*) FROM dossiers WHERE mois_import = '2024-08') as total_dossiers,
    (SELECT COUNT(*) FROM dossiers WHERE mois_import = '2024-08' AND est_recurrent = true) as recurrents,
    (SELECT COUNT(*) FROM dossiers WHERE mois_import = '2024-08' AND est_recurrent = false) as nouveaux;

SELECT 
    '📋 DÉTAIL ATTENDU' as info,
    '3 récurrents (DOSS-TEST-001, 002, 003)' as recurrents,
    '2 nouveaux (DOSS-TEST-006, 007)' as nouveaux,
    'CONTACTE→RELANCE, PROMESSE→SUIVI_PROMESSE, INJOIGNABLE→INJOIGNABLE' as transitions;
