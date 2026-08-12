-- ================================================================
-- SCRIPT DE TEST : NETTOYAGE MOIS 7
-- ================================================================
-- Description : Nettoie les dossiers du mois 7 après archivage
-- ================================================================

-- Afficher l'état AVANT nettoyage
SELECT '=== ÉTAT AVANT NETTOYAGE ===' as info;

SELECT 
    'Dossiers actifs (mois 7)' as table_name,
    COUNT(*) as nombre
FROM dossiers
WHERE mois_import = '2024-07';

SELECT 
    'Dossiers archivés (mois 7)' as table_name,
    COUNT(*) as nombre,
    '✅ Doivent être > 0 pour nettoyer' as validation
FROM dossiers_archive
WHERE mois_archive = '2024-07';

-- ================================================================
-- EXÉCUTER LE NETTOYAGE
-- ================================================================

SELECT '=== DÉBUT NETTOYAGE MOIS 7 ===' as info;

-- Utiliser la fonction de nettoyage
SELECT * FROM nettoyer_dossiers('2024-07');

-- ================================================================
-- VÉRIFICATIONS APRÈS NETTOYAGE
-- ================================================================

SELECT '=== ÉTAT APRÈS NETTOYAGE ===' as info;

-- 1. Vérifier que les dossiers sont supprimés
SELECT 
    '✅ Dossiers actifs (mois 7)' as verif,
    COUNT(*) as nombre,
    'Devrait être 0' as attendu
FROM dossiers
WHERE mois_import = '2024-07';

-- 2. Vérifier que l'archive est intacte
SELECT 
    '✅ Dossiers archivés (mois 7)' as verif,
    COUNT(*) as nombre,
    'Devrait être intact (5 dossiers)' as attendu
FROM dossiers_archive
WHERE mois_archive = '2024-07';

-- 3. Vérifier que la table de détection est intacte
SELECT 
    '✅ Table détection récurrents' as verif,
    COUNT(*) as nombre,
    'Devrait être intact (5 entrées)' as attendu
FROM dossiers_mois_precedent
WHERE mois_origine = '2024-07';

-- 4. Vérifier le log de nettoyage
SELECT 
    '📝 Log nettoyage' as titre,
    la.operation,
    la.mois_concerne,
    la.nb_dossiers as dossiers_supprimes,
    la.statut,
    la.date_operation
FROM logs_archivage la
WHERE la.mois_concerne = '2024-07' AND la.operation = 'NETTOYAGE'
ORDER BY la.date_operation DESC
LIMIT 1;

-- 5. Tableau récapitulatif
SELECT 
    '📊 RÉCAPITULATIF FINAL' as titre,
    'Mois 7' as periode,
    (SELECT COUNT(*) FROM dossiers WHERE mois_import = '2024-07') as actifs,
    (SELECT COUNT(*) FROM dossiers_archive WHERE mois_archive = '2024-07') as archives,
    (SELECT COUNT(*) FROM dossiers_mois_precedent WHERE mois_origine = '2024-07') as detection;

-- ================================================================
-- PRÊT POUR IMPORT MOIS 8
-- ================================================================

SELECT 
    '✅ SYSTÈME PRÊT' as statut,
    'La base est prête pour importer le mois 8 avec détection automatique' as message;
