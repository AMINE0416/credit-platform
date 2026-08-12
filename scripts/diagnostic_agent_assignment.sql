-- Diagnostic de l'assignation des agents

-- 1. Vérifier les dossiers importés
SELECT 
    d.id as dossier_id,
    c.reference_contrat,
    cl.nom || ' ' || cl.prenom as client,
    d.statut_actuel,
    d.agent_assigne_id,
    u.email as agent_email,
    u.nom as agent_nom,
    d.mois_import
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
JOIN clients cl ON c.client_id = cl.id
LEFT JOIN users u ON d.agent_assigne_id = u.id
WHERE d.mois_import = '2026-08'
ORDER BY d.id
LIMIT 10;

-- 2. Lister tous les agents disponibles
SELECT 
    u.id,
    u.email,
    u.nom,
    u.prenom,
    u.actif,
    r.nom as role
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE r.nom IN ('AGENT', 'ADMIN')
ORDER BY r.nom, u.email;

-- 3. Statistiques d'assignation
SELECT 
    CASE 
        WHEN d.agent_assigne_id IS NULL THEN 'Non assigne'
        ELSE 'Assigne'
    END as status,
    COUNT(*) as nombre
FROM dossiers d
WHERE d.mois_import = '2026-08'
GROUP BY d.agent_assigne_id IS NULL;
