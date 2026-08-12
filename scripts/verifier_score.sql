-- ===============================================
-- Vérifier que le score est bien enregistré
-- ===============================================

-- 1. Vérifier que la colonne existe
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'credits' AND column_name = 'score';

-- 2. Compter les dossiers avec et sans score
SELECT 
    COUNT(*) as total_dossiers,
    COUNT(c.score) as avec_score,
    COUNT(*) - COUNT(c.score) as sans_score
FROM credits c;

-- 3. Afficher les 10 premiers dossiers avec leur score
SELECT 
    c.reference_contrat,
    cl.nom,
    cl.prenom,
    c.score,
    d.statut_actuel
FROM credits c
JOIN clients cl ON c.client_id = cl.id
JOIN dossiers d ON d.credit_id = c.id
LIMIT 10;

-- 4. Afficher les scores distincts pour voir les valeurs
SELECT DISTINCT score
FROM credits
WHERE score IS NOT NULL
ORDER BY score DESC
LIMIT 20;
