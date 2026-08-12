-- ═══════════════════════════════════════════════════════════════════════════
-- Script de test complet pour les informations de promesse
-- ═══════════════════════════════════════════════════════════════════════════

-- ───────────────────────────────────────────────────────────────────────────
-- 1. VÉRIFICATION DE LA MIGRATION
-- ───────────────────────────────────────────────────────────────────────────
\echo '=== 1. Vérification des colonnes ajoutées ==='
SELECT 
    column_name, 
    data_type, 
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'dossiers' 
  AND column_name IN ('date_promesse', 'montant_promesse')
ORDER BY ordinal_position;

-- ───────────────────────────────────────────────────────────────────────────
-- 2. ÉTAT ACTUEL DES DOSSIERS AVEC PROMESSE
-- ───────────────────────────────────────────────────────────────────────────
\echo ''
\echo '=== 2. Dossiers avec statut PROMESSE ou SUIVI_PROMESSE ==='
SELECT 
    d.id as dossier_id,
    d.statut_actuel,
    d.date_promesse,
    d.montant_promesse,
    c.reference_contrat,
    cl.nom || ' ' || cl.prenom as client_nom,
    d.date_creation
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
JOIN clients cl ON c.client_id = cl.id
WHERE d.statut_actuel IN ('PROMESSE', 'SUIVI_PROMESSE')
ORDER BY d.date_creation DESC;

-- ───────────────────────────────────────────────────────────────────────────
-- 3. ACTIONS AVEC PROMESSE (pour comparaison)
-- ───────────────────────────────────────────────────────────────────────────
\echo ''
\echo '=== 3. Actions contenant des promesses ==='
SELECT 
    ad.id as action_id,
    ad.dossier_id,
    ad.date_promesse as action_date_promesse,
    ad.montant_promesse as action_montant_promesse,
    ad.date_action,
    u.prenom || ' ' || u.nom as agent_nom,
    ad.compte_rendu
FROM actions_dossier ad
JOIN users u ON ad.agent_id = u.id
WHERE ad.montant_promesse IS NOT NULL 
  AND ad.date_promesse IS NOT NULL
ORDER BY ad.date_action DESC
LIMIT 10;

-- ───────────────────────────────────────────────────────────────────────────
-- 4. COMPARAISON DOSSIERS vs ACTIONS
-- ───────────────────────────────────────────────────────────────────────────
\echo ''
\echo '=== 4. Comparaison entre dossiers et actions (promesses) ==='
SELECT 
    d.id as dossier_id,
    d.statut_actuel,
    d.date_promesse as dossier_date_promesse,
    d.montant_promesse as dossier_montant_promesse,
    ad.date_promesse as action_date_promesse,
    ad.montant_promesse as action_montant_promesse,
    ad.date_action as date_derniere_action,
    CASE 
        WHEN d.date_promesse IS NULL AND ad.date_promesse IS NOT NULL THEN '⚠️ Manque au niveau dossier'
        WHEN d.date_promesse IS NOT NULL AND ad.date_promesse IS NULL THEN '✓ OK (nouveau système)'
        WHEN d.date_promesse = ad.date_promesse AND d.montant_promesse = ad.montant_promesse THEN '✓ Cohérent'
        ELSE '⚠️ Différence détectée'
    END as statut_coherence
FROM dossiers d
LEFT JOIN LATERAL (
    SELECT 
        date_promesse, 
        montant_promesse, 
        date_action
    FROM actions_dossier
    WHERE dossier_id = d.id
      AND date_promesse IS NOT NULL
      AND montant_promesse IS NOT NULL
    ORDER BY date_action DESC
    LIMIT 1
) ad ON TRUE
WHERE d.statut_actuel IN ('PROMESSE', 'SUIVI_PROMESSE')
ORDER BY d.id;

-- ───────────────────────────────────────────────────────────────────────────
-- 5. TEST : CRÉATION D'UN DOSSIER DE TEST AVEC PROMESSE
-- ───────────────────────────────────────────────────────────────────────────
\echo ''
\echo '=== 5. Création d''un dossier de test ==='

-- Vérifier si un client de test existe
DO $$
DECLARE
    v_client_id BIGINT;
    v_credit_id BIGINT;
    v_dossier_id BIGINT;
    v_agent_id BIGINT;
BEGIN
    -- Trouver ou créer un client de test
    SELECT id INTO v_client_id 
    FROM clients 
    WHERE cin = 'TEST_PROMESSE_001' 
    LIMIT 1;
    
    IF v_client_id IS NULL THEN
        INSERT INTO clients (
            cin, nom, prenom, telephone, ville, 
            adresse, code_agence, lib_agence
        ) VALUES (
            'TEST_PROMESSE_001', 
            'TEST', 
            'Promesse', 
            '0612345678', 
            'Casablanca',
            'Adresse Test',
            'AG001',
            'Agence Test'
        ) RETURNING id INTO v_client_id;
        
        RAISE NOTICE 'Client de test créé avec ID: %', v_client_id;
    END IF;
    
    -- Créer un crédit de test
    INSERT INTO credits (
        client_id, reference_contrat, type_credit, segment,
        nbr_impaye, mensualite, encours, impaye_montant,
        score
    ) VALUES (
        v_client_id,
        'PROM-TEST-' || EXTRACT(EPOCH FROM NOW())::BIGINT,
        'AUTO',
        'PARTICULIER',
        2,
        2500.00,
        75000.00,
        5000.00,
        75.5
    ) RETURNING id INTO v_credit_id;
    
    RAISE NOTICE 'Crédit de test créé avec ID: %', v_credit_id;
    
    -- Créer un dossier avec statut PROMESSE et informations de promesse
    INSERT INTO dossiers (
        credit_id, 
        statut_actuel, 
        date_creation,
        date_promesse,
        montant_promesse,
        mois_import
    ) VALUES (
        v_credit_id,
        'PROMESSE',
        CURRENT_DATE,
        CURRENT_DATE + INTERVAL '7 days',
        5000.00,
        TO_CHAR(CURRENT_DATE, 'YYYY-MM')
    ) RETURNING id INTO v_dossier_id;
    
    RAISE NOTICE 'Dossier de test créé avec ID: % (PROMESSE)', v_dossier_id;
    
    -- Trouver un agent pour l'action
    SELECT id INTO v_agent_id
    FROM users u
    JOIN user_roles ur ON u.id = ur.user_id
    JOIN roles r ON ur.role_id = r.id
    WHERE r.nom = 'AGENT'
    LIMIT 1;
    
    -- Créer une action correspondante
    IF v_agent_id IS NOT NULL THEN
        INSERT INTO actions_dossier (
            dossier_id,
            agent_id,
            nature_action,
            compte_rendu,
            code_resultat,
            recidive,
            nbre_action_client,
            montant_promesse,
            date_promesse,
            date_action
        ) VALUES (
            v_dossier_id,
            v_agent_id,
            'APPEL_TELEPHONIQUE',
            'Client a promis de payer 5000 MAD dans 7 jours',
            'PTP',
            'NON',
            1,
            5000.00,
            CURRENT_DATE + INTERVAL '7 days',
            CURRENT_TIMESTAMP
        );
        
        RAISE NOTICE 'Action de test créée pour le dossier';
    END IF;
    
    RAISE NOTICE '✓ Dossier de test créé avec succès';
    RAISE NOTICE 'Dossier ID: %, Date promesse: %, Montant: 5000.00 MAD', 
                 v_dossier_id, 
                 TO_CHAR(CURRENT_DATE + INTERVAL '7 days', 'YYYY-MM-DD');
END $$;

-- ───────────────────────────────────────────────────────────────────────────
-- 6. VÉRIFICATION DU DOSSIER DE TEST
-- ───────────────────────────────────────────────────────────────────────────
\echo ''
\echo '=== 6. Vérification du dossier de test créé ==='
SELECT 
    d.id as dossier_id,
    d.statut_actuel,
    d.date_promesse,
    d.montant_promesse,
    c.reference_contrat,
    cl.nom || ' ' || cl.prenom as client_nom,
    d.date_creation
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
JOIN clients cl ON c.client_id = cl.id
WHERE cl.cin = 'TEST_PROMESSE_001'
ORDER BY d.date_creation DESC
LIMIT 1;

-- ───────────────────────────────────────────────────────────────────────────
-- 7. STATISTIQUES GLOBALES
-- ───────────────────────────────────────────────────────────────────────────
\echo ''
\echo '=== 7. Statistiques globales ==='
SELECT 
    COUNT(*) FILTER (WHERE statut_actuel = 'PROMESSE') as total_promesse,
    COUNT(*) FILTER (WHERE statut_actuel = 'SUIVI_PROMESSE') as total_suivi_promesse,
    COUNT(*) FILTER (WHERE date_promesse IS NOT NULL) as avec_date_promesse,
    COUNT(*) FILTER (WHERE montant_promesse IS NOT NULL) as avec_montant_promesse,
    COUNT(*) FILTER (
        WHERE (statut_actuel IN ('PROMESSE', 'SUIVI_PROMESSE'))
          AND date_promesse IS NOT NULL 
          AND montant_promesse IS NOT NULL
    ) as promesses_completes,
    COUNT(*) FILTER (
        WHERE (statut_actuel IN ('PROMESSE', 'SUIVI_PROMESSE'))
          AND (date_promesse IS NULL OR montant_promesse IS NULL)
    ) as promesses_incompletes
FROM dossiers;

-- ───────────────────────────────────────────────────────────────────────────
-- 8. PROMESSES ARRIVANT À ÉCHÉANCE
-- ───────────────────────────────────────────────────────────────────────────
\echo ''
\echo '=== 8. Promesses arrivant à échéance ==='
SELECT 
    d.id as dossier_id,
    d.statut_actuel,
    c.reference_contrat,
    cl.nom || ' ' || cl.prenom as client_nom,
    cl.telephone,
    d.date_promesse,
    d.montant_promesse,
    CASE 
        WHEN d.date_promesse < CURRENT_DATE THEN '🔴 Échue'
        WHEN d.date_promesse = CURRENT_DATE THEN '🟠 Aujourd''hui'
        WHEN d.date_promesse <= CURRENT_DATE + INTERVAL '7 days' THEN '🟡 Cette semaine'
        ELSE '🟢 Ultérieur'
    END as statut_echeance,
    d.date_promesse - CURRENT_DATE as jours_restants
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
JOIN clients cl ON c.client_id = cl.id
WHERE d.statut_actuel IN ('PROMESSE', 'SUIVI_PROMESSE')
  AND d.date_promesse IS NOT NULL
ORDER BY d.date_promesse ASC;

-- ───────────────────────────────────────────────────────────────────────────
-- 9. NETTOYAGE (OPTIONNEL)
-- ───────────────────────────────────────────────────────────────────────────
\echo ''
\echo '=== 9. Pour nettoyer le dossier de test, exécutez : ==='
\echo 'DELETE FROM dossiers WHERE credit_id IN (SELECT id FROM credits WHERE client_id IN (SELECT id FROM clients WHERE cin = ''TEST_PROMESSE_001''));'
\echo 'DELETE FROM credits WHERE client_id IN (SELECT id FROM clients WHERE cin = ''TEST_PROMESSE_001'');'
\echo 'DELETE FROM clients WHERE cin = ''TEST_PROMESSE_001'';'

\echo ''
\echo '✓ Test complet terminé !'
