-- =====================================================
-- TEST URGENT : Vérifier si le score est en base
-- =====================================================

-- 1. Vérifier si la colonne score existe
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'credits' AND column_name = 'score';

-- 2. Compter combien de dossiers ont un score
SELECT 
    COUNT(*) as total_dossiers,
    COUNT(score) as avec_score,
    COUNT(*) - COUNT(score) as sans_score,
    ROUND(COUNT(score)::numeric / NULLIF(COUNT(*), 0) * 100, 2) as pourcentage_avec_score
FROM credits;

-- 3. Afficher 10 dossiers avec leur score (NULL ou valeur)
SELECT 
    reference_contrat,
    COALESCE(score::text, 'NULL') as score_value
FROM credits
ORDER BY id
LIMIT 10;

-- 4. Si des scores existent, les afficher
SELECT 
    reference_contrat,
    score
FROM credits
WHERE score IS NOT NULL
ORDER BY score DESC
LIMIT 10;
