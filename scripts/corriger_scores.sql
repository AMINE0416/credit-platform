-- ============================================
-- CORRECTION DES SCORES
-- Diviser par 100 si > 100
-- ============================================

-- 1. Diagnostic : Voir les scores actuels
SELECT 
    COUNT(*) as total_dossiers,
    COUNT(CASE WHEN c.score > 100 THEN 1 END) as scores_anormaux,
    MIN(c.score) as score_min,
    MAX(c.score) as score_max,
    AVG(c.score) as score_moyen
FROM dossier d
JOIN credit c ON d.credit_id = c.id
WHERE c.score IS NOT NULL;

-- 2. Voir quelques exemples de scores anormaux
SELECT 
    d.id,
    cl.cin,
    cl.nom,
    cl.prenom,
    c.score as score_actuel,
    ROUND(c.score / 100, 2) as score_corrige
FROM dossier d
JOIN client cl ON d.client_id = cl.id
JOIN credit c ON d.credit_id = c.id
WHERE c.score IS NOT NULL 
  AND c.score > 100
LIMIT 10;

-- 3. CORRECTION : Diviser tous les scores > 100 par 100
UPDATE credit c
SET score = ROUND(score / 100, 2)
WHERE score > 100;

-- 4. Vérification après correction
SELECT 
    COUNT(*) as total_dossiers,
    COUNT(CASE WHEN c.score > 100 THEN 1 END) as scores_encore_anormaux,
    MIN(c.score) as score_min,
    MAX(c.score) as score_max,
    AVG(c.score) as score_moyen
FROM dossier d
JOIN credit c ON d.credit_id = c.id
WHERE c.score IS NOT NULL;
