-- ========================================
-- CORRECTION AFFECTATION AUTOMATIQUE
-- ========================================
-- Ce script crée les agents et affecte les dossiers manuellement
-- si l'import automatique n'a pas fonctionné
-- ========================================

-- ÉTAPE 1 : Créer les agents (basé sur les villes des dossiers)
-- ========================================

-- Vérifier d'abord que le rôle AGENT existe
SELECT id, nom FROM roles WHERE nom = 'AGENT';
-- Si la requête ci-dessus ne retourne rien, créer le rôle :
-- INSERT INTO roles (nom) VALUES ('AGENT');

-- Créer 3 agents pour FES
INSERT INTO users (email, nom, prenom, password, actif)
SELECT 'agent1.fes@eqdom.ma', 'Agent1', 'Fes', '$2a$10$YourHashedPasswordHere', true
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'agent1.fes@eqdom.ma');

INSERT INTO users (email, nom, prenom, password, actif)
SELECT 'agent2.fes@eqdom.ma', 'Agent2', 'Fes', '$2a$10$YourHashedPasswordHere', true
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'agent2.fes@eqdom.ma');

INSERT INTO users (email, nom, prenom, password, actif)
SELECT 'agent3.fes@eqdom.ma', 'Agent3', 'Fes', '$2a$10$YourHashedPasswordHere', true
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'agent3.fes@eqdom.ma');

-- Assigner le rôle AGENT
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email IN ('agent1.fes@eqdom.ma', 'agent2.fes@eqdom.ma', 'agent3.fes@eqdom.ma')
  AND r.nom = 'AGENT'
  AND NOT EXISTS (
      SELECT 1 FROM user_roles ur2 
      WHERE ur2.user_id = u.id AND ur2.role_id = r.id
  );


-- ÉTAPE 2 : Affectation équitable des dossiers
-- ========================================

-- Méthode 1 : Affectation Round-Robin (répartition équitable)
-- On affecte les dossiers à tour de rôle aux agents

-- Pour FES : répartir les dossiers de FES entre les 3 agents
WITH agents_fes AS (
    SELECT u.id, u.email, ROW_NUMBER() OVER (ORDER BY u.id) as agent_num
    FROM users u
    JOIN user_roles ur ON u.id = ur.user_id
    JOIN roles r ON ur.role_id = r.id
    WHERE r.nom = 'AGENT' 
      AND u.email LIKE '%fes%'
),
dossiers_fes AS (
    SELECT d.id, ROW_NUMBER() OVER (ORDER BY d.id) as dossier_num
    FROM dossiers d
    JOIN credits cr ON d.credit_id = cr.id
    JOIN clients cl ON cr.client_id = cl.id
    WHERE UPPER(cl.ville) = 'FES'
      AND d.agent_assigne_id IS NULL
)
UPDATE dossiers d
SET 
    agent_assigne_id = (
        SELECT a.id 
        FROM agents_fes a
        WHERE a.agent_num = ((df.dossier_num - 1) % (SELECT COUNT(*) FROM agents_fes)) + 1
    ),
    statut_actuel = 'EN_COURS'
FROM dossiers_fes df
WHERE d.id = df.id;


-- ÉTAPE 3 : Vérification
-- ========================================

-- Afficher la répartition finale
SELECT 
    u.email as Agent,
    cl.ville as Ville,
    COUNT(d.id) as Nb_Dossiers,
    SUM(cr.encours) as Encours_Total
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
JOIN dossiers d ON d.agent_assigne_id = u.id
JOIN credits cr ON d.credit_id = cr.id
JOIN clients cl ON cr.client_id = cl.id
WHERE r.nom = 'AGENT'
GROUP BY u.email, cl.ville
ORDER BY u.email, cl.ville;

-- Afficher les dossiers non affectés
SELECT 
    d.id,
    cr.reference_contrat,
    cl.ville,
    d.statut_actuel
FROM dossiers d
JOIN credits cr ON d.credit_id = cr.id
JOIN clients cl ON cr.client_id = cl.id
WHERE d.agent_assigne_id IS NULL;
