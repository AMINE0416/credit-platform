-- ========================================
-- DIAGNOSTIC AFFECTATION AUTOMATIQUE
-- ========================================

-- 1. Vérifier les dossiers créés
SELECT 
    'DOSSIERS' as Type,
    COUNT(*) as Total,
    SUM(CASE WHEN agent_assigne_id IS NULL THEN 1 ELSE 0 END) as Sans_Agent,
    SUM(CASE WHEN agent_assigne_id IS NOT NULL THEN 1 ELSE 0 END) as Avec_Agent
FROM dossiers;

-- 2. Vérifier les statuts des dossiers
SELECT 
    statut_actuel,
    COUNT(*) as Nombre,
    SUM(CASE WHEN agent_assigne_id IS NULL THEN 1 ELSE 0 END) as Sans_Agent
FROM dossiers
GROUP BY statut_actuel;

-- 3. Vérifier les agents créés
SELECT 
    'AGENTS' as Type,
    COUNT(*) as Total
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE r.nom = 'AGENT';

-- 4. Liste des agents
SELECT 
    u.id,
    u.email,
    u.nom,
    u.prenom,
    u.actif,
    COUNT(d.id) as Nb_Dossiers
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
LEFT JOIN dossiers d ON d.agent_assigne_id = u.id
WHERE r.nom = 'AGENT'
GROUP BY u.id, u.email, u.nom, u.prenom, u.actif
ORDER BY u.email;

-- 5. Liste des dossiers avec leurs détails
SELECT 
    d.id as Dossier_ID,
    cr.reference_contrat,
    cl.cin,
    CONCAT(cl.prenom, ' ', cl.nom) as Client,
    cl.ville,
    d.statut_actuel,
    u.email as Agent_Email,
    CONCAT(u.prenom, ' ', u.nom) as Agent_Nom
FROM dossiers d
JOIN credits cr ON d.credit_id = cr.id
JOIN clients cl ON cr.client_id = cl.id
LEFT JOIN users u ON d.agent_assigne_id = u.id
ORDER BY d.id;
