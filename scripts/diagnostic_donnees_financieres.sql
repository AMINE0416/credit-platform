-- Script de diagnostic pour les données financières
-- Ce script vérifie si les colonnes mensualite, encours, impaye_montant et prv_cum contiennent des données

-- 1. Vérifier la structure de la table credits
SELECT 
    column_name,
    data_type,
    is_nullable
FROM information_schema.columns
WHERE table_name = 'credits'
AND column_name IN ('mensualite', 'encours', 'impaye_montant', 'prv_cum')
ORDER BY ordinal_position;

-- 2. Compter les enregistrements avec des valeurs NULL ou 0
SELECT 
    COUNT(*) as total_credits,
    COUNT(mensualite) as mensualite_non_null,
    COUNT(encours) as encours_non_null,
    COUNT(impaye_montant) as impaye_montant_non_null,
    COUNT(prv_cum) as prv_cum_non_null,
    SUM(CASE WHEN mensualite IS NULL OR mensualite = 0 THEN 1 ELSE 0 END) as mensualite_vide,
    SUM(CASE WHEN encours IS NULL OR encours = 0 THEN 1 ELSE 0 END) as encours_vide,
    SUM(CASE WHEN impaye_montant IS NULL OR impaye_montant = 0 THEN 1 ELSE 0 END) as impaye_vide,
    SUM(CASE WHEN prv_cum IS NULL OR prv_cum = 0 THEN 1 ELSE 0 END) as prv_cum_vide
FROM credits;

-- 3. Afficher quelques exemples de données
SELECT 
    id,
    reference_contrat,
    mensualite,
    encours,
    impaye_montant,
    prv_cum,
    nbr_impaye
FROM credits
LIMIT 10;

-- 4. Afficher les statistiques des valeurs
SELECT 
    MIN(mensualite) as mensualite_min,
    MAX(mensualite) as mensualite_max,
    AVG(mensualite) as mensualite_avg,
    MIN(encours) as encours_min,
    MAX(encours) as encours_max,
    AVG(encours) as encours_avg,
    MIN(impaye_montant) as impaye_min,
    MAX(impaye_montant) as impaye_max,
    AVG(impaye_montant) as impaye_avg,
    MIN(prv_cum) as prv_cum_min,
    MAX(prv_cum) as prv_cum_max,
    AVG(prv_cum) as prv_cum_avg
FROM credits
WHERE mensualite IS NOT NULL;
