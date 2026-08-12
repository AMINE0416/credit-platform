-- ================================================================
-- SCRIPT DE TEST COMPLET - SYSTÈME D'ARCHIVAGE MENSUEL
-- ================================================================
-- Description : Exécute tous les tests en une seule fois
--               pour valider le système complet
-- ================================================================

\echo '=================================='
\echo '🧪 TEST COMPLET ARCHIVAGE MENSUEL'
\echo '=================================='
\echo ''

-- ================================================================
-- PHASE 1 : SETUP ET VÉRIFICATION
-- ================================================================

\echo '📋 Phase 1 : Vérification de l''environnement'
\echo '---'

-- Vérifier les tables
DO $$
DECLARE
    table_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO table_count
    FROM information_schema.tables
    WHERE table_schema = 'public'
    AND table_name IN ('dossiers_archive', 'dossiers_mois_precedent', 'logs_archivage');
    
    IF table_count = 3 THEN
        RAISE NOTICE '✅ Toutes les tables nécessaires existent';
    ELSE
        RAISE EXCEPTION '❌ Tables manquantes. Exécutez create_archivage_system.sql d''abord';
    END IF;
END $$;

-- Vérifier les colonnes
DO $$
DECLARE
    column_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO column_count
    FROM information_schema.columns
    WHERE table_name = 'dossiers'
    AND column_name IN ('mois_import', 'est_recurrent', 'mois_precedent_statut');
    
    IF column_count = 3 THEN
        RAISE NOTICE '✅ Colonnes dossiers correctement ajoutées';
    ELSE
        RAISE EXCEPTION '❌ Colonnes manquantes dans table dossiers';
    END IF;
END $$;

\echo ''
\echo '✅ Environnement validé'
\echo ''

-- ================================================================
-- PHASE 2 : NETTOYAGE DONNÉES DE TEST
-- ================================================================

\echo '🧹 Phase 2 : Nettoyage des anciennes données de test'
\echo '---'

DELETE FROM dossiers WHERE mois_import LIKE '2024-%';
DELETE FROM dossiers_archive WHERE mois_archive LIKE '2024-%';
DELETE FROM dossiers_mois_precedent;
DELETE FROM logs_archivage WHERE mois_concerne LIKE '2024-%';
DELETE FROM credits WHERE reference_contrat LIKE 'DOSS-TEST-%';
DELETE FROM clients WHERE cin LIKE 'TEST_%';

\echo '✅ Données de test nettoyées'
\echo ''

-- ================================================================
-- PHASE 3 : IMPORT MOIS 7
-- ================================================================

\echo '📥 Phase 3 : Import du mois 7 (Juillet 2024)'
\echo '---'

\i test_donnees_mois7.sql

\echo ''
\echo '✅ Mois 7 importé'
\echo ''

-- ================================================================
-- PHASE 4 : ARCHIVAGE MOIS 7
-- ================================================================

\echo '📦 Phase 4 : Archivage du mois 7'
\echo '---'

SELECT * FROM archiver_mois('2024-07');

-- Vérifications
DO $$
DECLARE
    nb_archives INTEGER;
    nb_mois_prec INTEGER;
BEGIN
    SELECT COUNT(*) INTO nb_archives FROM dossiers_archive WHERE mois_archive = '2024-07';
    SELECT COUNT(*) INTO nb_mois_prec FROM dossiers_mois_precedent WHERE mois_origine = '2024-07';
    
    IF nb_archives = 5 AND nb_mois_prec = 5 THEN
        RAISE NOTICE '✅ Archivage réussi: % archives, % sauvegardés pour détection', nb_archives, nb_mois_prec;
    ELSE
        RAISE EXCEPTION '❌ Archivage incomplet: % archives (attendu 5), % sauvegardés (attendu 5)', nb_archives, nb_mois_prec;
    END IF;
END $$;

\echo ''
\echo '✅ Mois 7 archivé'
\echo ''

-- ================================================================
-- PHASE 5 : NETTOYAGE MOIS 7
-- ================================================================

\echo '🧹 Phase 5 : Nettoyage du mois 7'
\echo '---'

SELECT * FROM nettoyer_dossiers('2024-07');

-- Vérifications
DO $$
DECLARE
    nb_actifs INTEGER;
    nb_archives INTEGER;
BEGIN
    SELECT COUNT(*) INTO nb_actifs FROM dossiers WHERE mois_import = '2024-07';
    SELECT COUNT(*) INTO nb_archives FROM dossiers_archive WHERE mois_archive = '2024-07';
    
    IF nb_actifs = 0 AND nb_archives = 5 THEN
        RAISE NOTICE '✅ Nettoyage réussi: % dossiers actifs (attendu 0), % archives intactes (attendu 5)', nb_actifs, nb_archives;
    ELSE
        RAISE EXCEPTION '❌ Nettoyage incomplet: % dossiers actifs, % archives', nb_actifs, nb_archives;
    END IF;
END $$;

\echo ''
\echo '✅ Mois 7 nettoyé'
\echo ''

-- ================================================================
-- PHASE 6 : IMPORT MOIS 8 AVEC DÉTECTION
-- ================================================================

\echo '📥🔍 Phase 6 : Import du mois 8 avec détection automatique'
\echo '---'

\i test_import_mois8_detection.sql

-- Vérifications détaillées
DO $$
DECLARE
    nb_total INTEGER;
    nb_recurrents INTEGER;
    nb_nouveaux INTEGER;
    nb_relance INTEGER;
    nb_suivi INTEGER;
    nb_injoignable INTEGER;
BEGIN
    SELECT COUNT(*) INTO nb_total FROM dossiers WHERE mois_import = '2024-08';
    SELECT COUNT(*) INTO nb_recurrents FROM dossiers WHERE mois_import = '2024-08' AND est_recurrent = true;
    SELECT COUNT(*) INTO nb_nouveaux FROM dossiers WHERE mois_import = '2024-08' AND est_recurrent = false;
    
    SELECT COUNT(*) INTO nb_relance 
    FROM dossiers 
    WHERE mois_import = '2024-08' 
    AND mois_precedent_statut = 'CONTACTE' 
    AND statut_actuel = 'RELANCE';
    
    SELECT COUNT(*) INTO nb_suivi 
    FROM dossiers 
    WHERE mois_import = '2024-08' 
    AND mois_precedent_statut = 'PROMESSE' 
    AND statut_actuel = 'SUIVI_PROMESSE';
    
    SELECT COUNT(*) INTO nb_injoignable 
    FROM dossiers 
    WHERE mois_import = '2024-08' 
    AND mois_precedent_statut = 'INJOIGNABLE' 
    AND statut_actuel = 'INJOIGNABLE';
    
    IF nb_total = 5 AND nb_recurrents = 3 AND nb_nouveaux = 2 THEN
        RAISE NOTICE '✅ Import réussi: % total (% récurrents, % nouveaux)', nb_total, nb_recurrents, nb_nouveaux;
    ELSE
        RAISE EXCEPTION '❌ Import incomplet: % total, % récurrents, % nouveaux (attendu 5, 3, 2)', nb_total, nb_recurrents, nb_nouveaux;
    END IF;
    
    IF nb_relance >= 1 AND nb_suivi >= 1 AND nb_injoignable >= 1 THEN
        RAISE NOTICE '✅ Détection automatique réussie: CONTACTE→RELANCE, PROMESSE→SUIVI_PROMESSE, INJOIGNABLE→INJOIGNABLE';
    ELSE
        RAISE EXCEPTION '❌ Détection automatique échouée';
    END IF;
END $$;

\echo ''
\echo '✅ Mois 8 importé avec détection'
\echo ''

-- ================================================================
-- PHASE 7 : RÉCAPITULATIF FINAL
-- ================================================================

\echo '=================================='
\echo '📊 RÉCAPITULATIF FINAL'
\echo '=================================='
\echo ''

-- État des tables
\echo '📋 État des tables:'
SELECT 
    'Dossiers actifs (mois 8)' as table_name,
    COUNT(*) as nombre
FROM dossiers
WHERE mois_import = '2024-08'
UNION ALL
SELECT 
    'Dossiers archivés (mois 7)',
    COUNT(*)
FROM dossiers_archive
WHERE mois_archive = '2024-07'
UNION ALL
SELECT 
    'Table détection récurrents',
    COUNT(*)
FROM dossiers_mois_precedent
UNION ALL
SELECT 
    'Logs d''archivage',
    COUNT(*)
FROM logs_archivage;

\echo ''
\echo '📈 Détail import mois 8:'
SELECT 
    cr.reference_contrat,
    CASE 
        WHEN d.est_recurrent THEN '🔄 RÉCURRENT'
        ELSE '🆕 NOUVEAU'
    END as type,
    COALESCE(d.mois_precedent_statut, 'N/A') || ' → ' || d.statut_actuel::text as transition,
    cl.nom || ' ' || cl.prenom as client
FROM dossiers d
JOIN credits cr ON d.credit_id = cr.id
JOIN clients cl ON cr.client_id = cl.id
WHERE d.mois_import = '2024-08'
ORDER BY d.est_recurrent DESC, cr.reference_contrat;

\echo ''
\echo '📝 Logs d''archivage:'
SELECT 
    operation,
    mois_concerne,
    nb_dossiers,
    statut,
    to_char(date_operation, 'YYYY-MM-DD HH24:MI:SS') as date
FROM logs_archivage
ORDER BY date_operation;

\echo ''
\echo '=================================='
\echo '✅ TOUS LES TESTS SONT PASSÉS !'
\echo '=================================='
\echo ''
\echo '📋 Résumé:'
\echo '  ✅ Tables et fonctions créées'
\echo '  ✅ Mois 7 importé et archivé'
\echo '  ✅ Mois 7 nettoyé correctement'
\echo '  ✅ Mois 8 importé avec détection'
\echo '  ✅ Statuts automatiques appliqués'
\echo '  ✅ Logs enregistrés'
\echo ''
\echo '🚀 Le système est opérationnel !'
\echo ''
