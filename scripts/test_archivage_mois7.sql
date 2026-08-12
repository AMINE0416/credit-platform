-- ================================================================
-- SCRIPT DE TEST : ARCHIVAGE MOIS 7
-- ================================================================
-- Description : Teste l'archivage du mois 7 en utilisant les
--               fonctions PostgreSQL créées
-- ================================================================

-- Afficher l'état AVANT archivage
SELECT '=== ÉTAT AVANT ARCHIVAGE ===' as info;

SELECT 
    'Dossiers actifs (mois 7)' as table_name,
    COUNT(*) as nombre
FROM dossiers
WHERE mois_import = '2024-07';

SELECT 
    'Dossiers déjà archivés (mois 7)' as table_name,
    COUNT(*) as nombre
FROM dossiers_archive
WHERE mois_archive = '2024-07';

SELECT 
    'Table mois précédent' as table_name,
    COUNT(*) as nombre
FROM dossiers_mois_precedent;

-- ================================================================
-- EXÉCUTER L'ARCHIVAGE
-- ================================================================

SELECT '=== DÉBUT ARCHIVAGE MOIS 7 ===' as info;

-- Utiliser la fonction d'archivage
SELECT * FROM archiver_mois('2024-07');

-- ================================================================
-- VÉRIFICATIONS APRÈS ARCHIVAGE
-- ================================================================

SELECT '=== ÉTAT APRÈS ARCHIVAGE ===' as info;

-- 1. Vérifier que les dossiers sont dans l'archive
SELECT 
    '✅ Dossiers archivés' as verif,
    COUNT(*) as nombre
FROM dossiers_archive
WHERE mois_archive = '2024-07';

-- 2. Vérifier que les dossiers mois précédent sont sauvegardés
SELECT 
    '✅ Dossiers sauvegardés pour détection' as verif,
    COUNT(*) as nombre
FROM dossiers_mois_precedent
WHERE mois_origine = '2024-07';

-- 3. Détail des dossiers archivés
SELECT 
    '📋 Détail archives' as titre,
    da.numero_dossier,
    da.nom_client,
    da.statut_final,
    da.total_du,
    da.date_archivage
FROM dossiers_archive da
WHERE da.mois_archive = '2024-07'
ORDER BY da.numero_dossier;

-- 4. Détail table mois précédent (pour détection future)
SELECT 
    '🔍 Table détection récurrents' as titre,
    dmp.numero_dossier,
    dmp.statut_final,
    dmp.mois_origine
FROM dossiers_mois_precedent dmp
ORDER BY dmp.numero_dossier;

-- 5. Vérifier les logs
SELECT 
    '📝 Logs archivage' as titre,
    la.operation,
    la.mois_concerne,
    la.nb_dossiers,
    la.statut,
    la.date_operation,
    la.details
FROM logs_archivage la
WHERE la.mois_concerne = '2024-07'
ORDER BY la.date_operation DESC;

-- 6. Statistiques de l'archive
SELECT 
    '📊 Statistiques archive mois 7' as titre,
    mois_archive,
    total_dossiers,
    contactes,
    promesses,
    injoignables,
    en_attente
FROM v_stats_archivage
WHERE mois_archive = '2024-07';

-- ================================================================
-- NOTE : Les dossiers sont TOUJOURS dans la table dossiers
--        Le nettoyage se fait dans une étape séparée
-- ================================================================

SELECT 
    '⚠️  IMPORTANT' as note,
    'Les dossiers sont archivés mais toujours présents dans la table dossiers' as message,
    'Utilisez le script test_nettoyage_mois7.sql pour les supprimer' as action;

SELECT 
    'Dossiers encore actifs (mois 7)' as table_name,
    COUNT(*) as nombre,
    'Normal après archivage, avant nettoyage' as statut
FROM dossiers
WHERE mois_import = '2024-07';
