-- ============================================
-- VÉRIFICATION DES NOUVEAUX STATUTS
-- ============================================
-- Script pour vérifier que la migration est correcte
-- ============================================

-- 1. Vérifier l'enum PostgreSQL
SELECT '=== 1. ENUM STATUT_DOSSIER ===' AS section;
SELECT 
    enumlabel AS statut_disponible,
    enumsortorder AS ordre
FROM pg_enum
JOIN pg_type ON pg_enum.enumtypid = pg_type.oid
WHERE pg_type.typname = 'statut_dossier'
ORDER BY enumsortorder;

-- 2. Vérifier la répartition des statuts actuels
SELECT '=== 2. RÉPARTITION DES STATUTS ===' AS section;
SELECT 
    statut_actuel,
    COUNT(*) AS nombre_dossiers,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER (), 2) AS pourcentage
FROM dossiers
GROUP BY statut_actuel
ORDER BY nombre_dossiers DESC;

-- 3. Vérifier qu'aucun ancien statut n'existe
SELECT '=== 3. VÉRIFICATION ANCIENS STATUTS ===' AS section;
SELECT 
    CASE 
        WHEN EXISTS (SELECT 1 FROM dossiers WHERE statut_actuel::TEXT IN ('EN_ATTENTE', 'CONTACTE', 'RELANCE'))
        THEN '❌ ERREUR: Anciens statuts détectés!'
        ELSE '✅ OK: Aucun ancien statut trouvé'
    END AS verification;

-- 4. Lister les dossiers EN_COURS (doivent être tous les nouveaux)
SELECT '=== 4. DOSSIERS EN_COURS ===' AS section;
SELECT 
    d.id,
    c.reference_contrat,
    cl.nom || ' ' || cl.prenom AS client,
    d.statut_actuel,
    d.est_recurrent,
    d.mois_import,
    u.email AS agent
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
JOIN clients cl ON c.client_id = cl.id
LEFT JOIN users u ON d.agent_assigne_id = u.id
WHERE d.statut_actuel = 'EN_COURS'
ORDER BY d.id
LIMIT 10;

-- 5. Vérifier les dossiers de suivi
SELECT '=== 5. DOSSIERS DE SUIVI ===' AS section;
SELECT 
    statut_actuel,
    est_recurrent,
    COUNT(*) AS nombre
FROM dossiers
WHERE statut_actuel IN ('ALERTE', 'SUIVI_PROMESSE', 'SUIVI_INJOIGNABLE')
GROUP BY statut_actuel, est_recurrent;

-- 6. Vérifier le dernier import
SELECT '=== 6. DERNIER IMPORT ===' AS section;
SELECT 
    mois_import,
    COUNT(*) AS total_dossiers,
    COUNT(CASE WHEN est_recurrent THEN 1 END) AS recurrents,
    COUNT(CASE WHEN NOT est_recurrent THEN 1 END) AS nouveaux,
    COUNT(CASE WHEN statut_actuel = 'EN_COURS' THEN 1 END) AS en_cours,
    COUNT(CASE WHEN statut_actuel = 'ALERTE' THEN 1 END) AS alertes,
    COUNT(CASE WHEN statut_actuel = 'SUIVI_PROMESSE' THEN 1 END) AS suivi_promesse,
    COUNT(CASE WHEN statut_actuel = 'SUIVI_INJOIGNABLE' THEN 1 END) AS suivi_injoignable
FROM dossiers
GROUP BY mois_import
ORDER BY mois_import DESC
LIMIT 3;

-- 7. Résumé final
SELECT '=== 7. RÉSUMÉ ===' AS section;
SELECT 
    COUNT(*) AS total_dossiers,
    COUNT(DISTINCT statut_actuel) AS nb_statuts_differents,
    COUNT(CASE WHEN statut_actuel = 'EN_COURS' THEN 1 END) AS en_cours,
    COUNT(CASE WHEN statut_actuel = 'PROMESSE' THEN 1 END) AS promesse,
    COUNT(CASE WHEN statut_actuel = 'INJOIGNABLE' THEN 1 END) AS injoignable,
    COUNT(CASE WHEN statut_actuel = 'REGULARISE' THEN 1 END) AS regularise,
    COUNT(CASE WHEN statut_actuel = 'ALERTE' THEN 1 END) AS alerte,
    COUNT(CASE WHEN statut_actuel = 'SUIVI_PROMESSE' THEN 1 END) AS suivi_promesse,
    COUNT(CASE WHEN statut_actuel = 'SUIVI_INJOIGNABLE' THEN 1 END) AS suivi_injoignable,
    COUNT(CASE WHEN statut_actuel = 'CLOTURE' THEN 1 END) AS cloture
FROM dossiers;

SELECT '✅ Vérification terminée' AS message;
