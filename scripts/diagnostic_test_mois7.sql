-- ================================================================
-- SCRIPT DE DIAGNOSTIC - Test Données Mois 7
-- ================================================================
-- Ce script diagnostique pourquoi les dossiers ne sont pas créés
-- ================================================================

\echo '========================================='
\echo 'DIAGNOSTIC TEST MOIS 7'
\echo '========================================='
\echo ''

-- 1. Vérifier les clients
\echo '1. Vérification des clients de test'
\echo '---'

SELECT 
    '📋 Clients de test' as info,
    COUNT(*) as nombre
FROM clients
WHERE cin LIKE 'TEST_%';

SELECT cin, nom, prenom 
FROM clients 
WHERE cin LIKE 'TEST_%'
ORDER BY cin;

\echo ''

-- 2. Vérifier les crédits
\echo '2. Vérification des crédits de test'
\echo '---'

SELECT 
    '💳 Crédits de test' as info,
    COUNT(*) as nombre
FROM credits cr
JOIN clients cl ON cr.client_id = cl.id
WHERE cl.cin LIKE 'TEST_%';

SELECT cr.reference_contrat, cl.cin, cl.nom
FROM credits cr
JOIN clients cl ON cr.client_id = cl.id
WHERE cl.cin LIKE 'TEST_%'
ORDER BY cr.reference_contrat;

\echo ''

-- 3. Vérifier le type de la colonne statut_actuel
\echo '3. Vérification du type de statut_actuel'
\echo '---'

SELECT 
    column_name,
    data_type,
    udt_name,
    is_nullable
FROM information_schema.columns
WHERE table_name = 'dossiers' 
AND column_name IN ('statut_actuel', 'mois_import', 'est_recurrent', 'mois_precedent_statut');

\echo ''

-- 4. Vérifier si le type ENUM existe
\echo '4. Vérification du type ENUM statut_dossier'
\echo '---'

SELECT 
    CASE 
        WHEN EXISTS (SELECT 1 FROM pg_type WHERE typname = 'statut_dossier')
        THEN '✅ Type ENUM statut_dossier existe'
        ELSE '❌ Type ENUM statut_dossier n''existe pas'
    END as resultat;

-- Si le type existe, afficher ses valeurs
SELECT 
    enumlabel as valeur,
    enumsortorder as ordre
FROM pg_enum
WHERE enumtypid = (SELECT oid FROM pg_type WHERE typname = 'statut_dossier')
ORDER BY enumsortorder;

\echo ''

-- 5. Vérifier les dossiers existants
\echo '5. Vérification des dossiers existants'
\echo '---'

SELECT 
    '📁 Total dossiers' as info,
    COUNT(*) as nombre
FROM dossiers;

SELECT 
    '📁 Dossiers mois 7' as info,
    COUNT(*) as nombre
FROM dossiers
WHERE mois_import = '2024-07';

-- Afficher les dossiers du mois 7 s'ils existent
SELECT 
    d.id,
    cr.reference_contrat,
    d.statut_actuel::text as statut,
    d.mois_import,
    d.est_recurrent
FROM dossiers d
JOIN credits cr ON d.credit_id = cr.id
WHERE d.mois_import = '2024-07'
ORDER BY cr.reference_contrat;

\echo ''

-- 6. Test d'insertion simple
\echo '6. Test d''insertion simple'
\echo '---'

DO $$
DECLARE
    test_credit_id BIGINT;
    test_result TEXT;
BEGIN
    -- Récupérer un credit_id de test
    SELECT cr.id INTO test_credit_id
    FROM credits cr
    JOIN clients cl ON cr.client_id = cl.id
    WHERE cl.cin = 'TEST_AB123456'
    LIMIT 1;
    
    IF test_credit_id IS NULL THEN
        RAISE NOTICE '❌ Aucun crédit de test trouvé pour TEST_AB123456';
        RETURN;
    END IF;
    
    RAISE NOTICE '✅ Crédit de test trouvé: ID = %', test_credit_id;
    
    -- Tenter une insertion de test
    BEGIN
        INSERT INTO dossiers (credit_id, statut_actuel, date_creation, mois_import, est_recurrent)
        VALUES (test_credit_id, 'EN_COURS', CURRENT_DATE, '2024-99', false);
        
        RAISE NOTICE '✅ Insertion de test réussie';
        
        -- Nettoyer
        DELETE FROM dossiers WHERE mois_import = '2024-99';
        RAISE NOTICE '✅ Nettoyage effectué';
        
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE '❌ Erreur lors de l''insertion de test: %', SQLERRM;
    END;
END $$;

\echo ''

-- 7. Vérifier les contraintes
\echo '7. Vérification des contraintes'
\echo '---'

SELECT 
    conname as contrainte,
    contype as type,
    CASE contype
        WHEN 'p' THEN 'PRIMARY KEY'
        WHEN 'f' THEN 'FOREIGN KEY'
        WHEN 'u' THEN 'UNIQUE'
        WHEN 'c' THEN 'CHECK'
        ELSE contype::text
    END as description
FROM pg_constraint
WHERE conrelid = 'dossiers'::regclass
ORDER BY contype, conname;

\echo ''
\echo '========================================='
\echo 'FIN DU DIAGNOSTIC'
\echo '========================================='
\echo ''
\echo 'Analysez les résultats ci-dessus pour identifier le problème.'
\echo ''
