-- Script de test rapide pour vérifier les données financières
-- Exécuter dans pgAdmin ou psql après l'import

-- 1. Statistiques globales
SELECT 
    '=== STATISTIQUES GLOBALES ===' as titre;
    
SELECT 
    COUNT(*) as total_credits,
    COUNT(mensualite) as avec_mensualite,
    COUNT(encours) as avec_encours,
    COUNT(impaye_montant) as avec_impaye,
    COUNT(prv_cum) as avec_provision,
    ROUND(AVG(mensualite), 2) as mensualite_moyenne,
    ROUND(AVG(encours), 2) as encours_moyen,
    ROUND(AVG(impaye_montant), 2) as impaye_moyen
FROM credits;

-- 2. Afficher 10 exemples
SELECT 
    '=== 10 EXEMPLES ===' as titre;

SELECT 
    c.reference_contrat as "Référence",
    cl.cin as "CIN",
    cl.nom || ' ' || cl.prenom as "Client",
    c.nbr_impaye as "Nb Impayés",
    c.mensualite as "Mensualité",
    c.encours as "Encours",
    c.impaye_montant as "Montant Impayé",
    c.prv_cum as "Provision Cumulée",
    c.score as "Score"
FROM credits c
JOIN clients cl ON c.client_id = cl.id
ORDER BY c.id
LIMIT 10;

-- 3. Compter les valeurs nulles ou zéro
SELECT 
    '=== VALEURS NULLES OU ZÉRO ===' as titre;

SELECT 
    SUM(CASE WHEN mensualite IS NULL OR mensualite = 0 THEN 1 ELSE 0 END) as mensualite_vide,
    SUM(CASE WHEN encours IS NULL OR encours = 0 THEN 1 ELSE 0 END) as encours_vide,
    SUM(CASE WHEN impaye_montant IS NULL OR impaye_montant = 0 THEN 1 ELSE 0 END) as impaye_vide,
    SUM(CASE WHEN prv_cum IS NULL OR prv_cum = 0 THEN 1 ELSE 0 END) as prv_cum_vide,
    SUM(CASE WHEN score IS NULL THEN 1 ELSE 0 END) as score_null
FROM credits;

-- 4. Valeurs min/max pour détecter les anomalies
SELECT 
    '=== MIN/MAX ===' as titre;

SELECT 
    MIN(mensualite) as mensualite_min,
    MAX(mensualite) as mensualite_max,
    MIN(encours) as encours_min,
    MAX(encours) as encours_max,
    MIN(impaye_montant) as impaye_min,
    MAX(impaye_montant) as impaye_max,
    MIN(prv_cum) as prv_cum_min,
    MAX(prv_cum) as prv_cum_max
FROM credits
WHERE mensualite > 0;

-- 5. Vérifier un dossier spécifique (utilisé dans le frontend)
SELECT 
    '=== DÉTAIL DOSSIER #1 ===' as titre;

SELECT 
    d.id as dossier_id,
    d.statut_actuel as statut,
    cl.cin,
    cl.nom,
    cl.prenom,
    c.reference_contrat,
    c.mensualite,
    c.encours,
    c.impaye_montant,
    c.prv_cum,
    c.score
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
JOIN clients cl ON c.client_id = cl.id
WHERE d.id = 1;

-- 6. Compter par statut
SELECT 
    '=== DOSSIERS PAR STATUT ===' as titre;

SELECT 
    d.statut_actuel,
    COUNT(*) as nombre,
    ROUND(AVG(c.mensualite), 2) as mensualite_moyenne,
    ROUND(SUM(c.impaye_montant), 2) as total_impayes
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
GROUP BY d.statut_actuel
ORDER BY nombre DESC;

-- 7. Crédits sans données financières (problématiques)
SELECT 
    '=== CRÉDITS SANS DONNÉES FINANCIÈRES ===' as titre;

SELECT 
    c.id,
    c.reference_contrat,
    cl.cin,
    cl.nom || ' ' || cl.prenom as client,
    c.mensualite,
    c.encours,
    c.impaye_montant,
    c.prv_cum
FROM credits c
JOIN clients cl ON c.client_id = cl.id
WHERE (c.mensualite IS NULL OR c.mensualite = 0)
  AND (c.encours IS NULL OR c.encours = 0)
  AND (c.impaye_montant IS NULL OR c.impaye_montant = 0)
LIMIT 20;
