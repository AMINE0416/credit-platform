-- ⚡ TEST RAPIDE APRÈS IMPORT ⚡
-- Exécuter ce script dans pgAdmin ou psql après chaque import

-- 1️⃣ VÉRIFICATION RAPIDE
SELECT 
    '🔍 VÉRIFICATION RAPIDE' as test,
    COUNT(*) as total_credits,
    COUNT(mensualite) as mensualite_non_null,
    SUM(CASE WHEN mensualite > 0 THEN 1 ELSE 0 END) as mensualite_non_zero,
    ROUND(AVG(mensualite), 2) as mensualite_moyenne
FROM credits;

-- 2️⃣ AFFICHER 5 EXEMPLES
SELECT 
    '📊 EXEMPLES DE DONNÉES' as test;
    
SELECT 
    reference_contrat as "Référence",
    mensualite as "Mensualité",
    encours as "Encours",
    impaye_montant as "Montant Impayé",
    prv_cum as "Provision"
FROM credits
ORDER BY id
LIMIT 5;

-- 3️⃣ DÉTECTER LES PROBLÈMES
SELECT 
    '⚠️ DIAGNOSTIC' as test;

SELECT 
    CASE 
        WHEN COUNT(*) = 0 THEN '❌ AUCUN CRÉDIT EN BASE'
        WHEN COUNT(mensualite) = 0 THEN '❌ TOUTES LES MENSUALITÉS SONT NULL'
        WHEN SUM(CASE WHEN mensualite > 0 THEN 1 ELSE 0 END) = 0 THEN '❌ TOUTES LES MENSUALITÉS = 0'
        WHEN SUM(CASE WHEN mensualite > 0 THEN 1 ELSE 0 END) > 0 THEN '✅ DONNÉES OK !'
        ELSE '⚠️ ÉTAT INCONNU'
    END as diagnostic
FROM credits;

-- 4️⃣ VÉRIFIER UN DOSSIER SPÉCIFIQUE
SELECT 
    '🎯 DOSSIER #1' as test;

SELECT 
    d.id as dossier_id,
    cl.cin,
    c.reference_contrat,
    c.mensualite,
    c.encours,
    c.impaye_montant,
    c.prv_cum,
    CASE 
        WHEN c.mensualite IS NULL OR c.mensualite = 0 THEN '❌'
        ELSE '✅'
    END as status
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
JOIN clients cl ON c.client_id = cl.id
WHERE d.id = 1;
