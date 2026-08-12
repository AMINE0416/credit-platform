-- Script pour vérifier les statuts des dossiers

-- 1. Afficher tous les dossiers avec leur statut
SELECT 
    d.id,
    c.cin,
    cl.prenom || ' ' || cl.nom as nom_complet,
    c.reference_contrat,
    d.statut_actuel as statut,
    cl.ville,
    u.prenom || ' ' || u.nom as agent_assigne,
    c.score
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
JOIN clients cl ON c.client_id = cl.id
LEFT JOIN users u ON d.agent_assigne_id = u.id
ORDER BY d.id
LIMIT 20;

-- 2. Compter les dossiers par statut
SELECT 
    COALESCE(d.statut_actuel::text, 'NULL') as statut,
    COUNT(*) as nombre
FROM dossiers d
GROUP BY d.statut_actuel
ORDER BY nombre DESC;

-- 3. Vérifier les dossiers avec statut NULL
SELECT 
    d.id,
    c.reference_contrat,
    d.statut_actuel,
    d.created_at
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
WHERE d.statut_actuel IS NULL
LIMIT 10;

-- 4. Si des dossiers ont statut NULL, les mettre à EN_COURS par défaut
UPDATE dossiers
SET statut_actuel = 'EN_COURS'
WHERE statut_actuel IS NULL;

-- 5. Vérifier le résultat
SELECT 'Dossiers mis à jour: ' || COUNT(*) as message
FROM dossiers
WHERE statut_actuel = 'EN_COURS';
