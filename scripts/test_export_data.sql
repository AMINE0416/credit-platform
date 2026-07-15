-- Script de test pour vérifier les données qui seront exportées en Excel
-- À exécuter avant de tester la fonctionnalité d'export

-- ===========================================================
-- FEUILLE 1 : État actuel - Tous les dossiers
-- ===========================================================

SELECT 
    NOW() AS 'Date extraction',
    cr.reference_contrat AS 'Référence contrat',
    cl.cin AS 'CIN',
    cl.nom AS 'Nom client',
    cl.prenom AS 'Prénom client',
    cl.telephone AS 'Téléphone',
    d.statut_actuel AS 'Statut actuel',
    CONCAT(u.prenom, ' ', u.nom) AS 'Agent assigné',
    d.date_creation AS 'Date création',
    cr.nbr_impaye AS 'Nb impayés',
    cr.impaye_montant AS 'Montant impayé',
    cr.mensualite AS 'Mensualité',
    cr.encours AS 'Encours'
FROM dossiers d
INNER JOIN credits cr ON d.credit_id = cr.id
INNER JOIN clients cl ON cr.client_id = cl.id
LEFT JOIN users u ON d.agent_assigne_id = u.id
ORDER BY d.id;

-- Statistiques
SELECT 
    'Total dossiers' AS Métrique, 
    COUNT(*) AS Valeur
FROM dossiers
UNION ALL
SELECT 
    'Par statut - NOUVEAU', 
    COUNT(*)
FROM dossiers WHERE statut_actuel = 'NOUVEAU'
UNION ALL
SELECT 
    'Par statut - EN_COURS', 
    COUNT(*)
FROM dossiers WHERE statut_actuel = 'EN_COURS'
UNION ALL
SELECT 
    'Par statut - PROMESSE', 
    COUNT(*)
FROM dossiers WHERE statut_actuel = 'PROMESSE'
UNION ALL
SELECT 
    'Par statut - INJOIGNABLE', 
    COUNT(*)
FROM dossiers WHERE statut_actuel = 'INJOIGNABLE'
UNION ALL
SELECT 
    'Par statut - REGULARISE', 
    COUNT(*)
FROM dossiers WHERE statut_actuel = 'REGULARISE'
UNION ALL
SELECT 
    'Par statut - CLOTURE', 
    COUNT(*)
FROM dossiers WHERE statut_actuel = 'CLOTURE';

-- ===========================================================
-- FEUILLE 2 : Historique statuts - Toutes les transitions
-- ===========================================================

SELECT 
    cr.reference_contrat AS 'Référence contrat',
    cl.nom AS 'Nom client',
    cl.prenom AS 'Prénom client',
    CASE 
        WHEN a.montant_promesse IS NOT NULL AND a.date_promesse IS NOT NULL THEN 'EN_COURS → PROMESSE'
        WHEN a.recidive = 'PREMIERE_FOIS' THEN 'EN_COURS → INJOIGNABLE'
        WHEN a.recidive = 'RECIDIVE' THEN 'INJOIGNABLE → INJOIGNABLE'
        ELSE CONCAT('→ ', d.statut_actuel)
    END AS 'Transition',
    CONCAT(u.prenom, ' ', u.nom) AS 'Agent',
    DATE_FORMAT(a.date_action, '%d/%m/%Y %H:%i') AS 'Date/Heure',
    a.nature_action AS 'Nature action',
    a.code_resultat AS 'Code résultat',
    a.motif_impaye AS 'Motif impayé',
    a.recidive AS 'Récidive',
    a.montant_promesse AS 'Montant promesse',
    DATE_FORMAT(a.date_promesse, '%d/%m/%Y') AS 'Date promesse',
    LEFT(a.compte_rendu, 100) AS 'Compte rendu (extrait)'
FROM actions_dossier a
INNER JOIN dossiers d ON a.dossier_id = d.id
INNER JOIN credits cr ON d.credit_id = cr.id
INNER JOIN clients cl ON cr.client_id = cl.id
INNER JOIN users u ON a.user_id = u.id
ORDER BY a.date_action ASC;

-- Statistiques sur les actions
SELECT 
    'Total actions' AS Métrique, 
    COUNT(*) AS Valeur
FROM actions_dossier
UNION ALL
SELECT 
    'Actions avec promesse', 
    COUNT(*)
FROM actions_dossier 
WHERE montant_promesse IS NOT NULL
UNION ALL
SELECT 
    'Actions injoignable (1ère fois)', 
    COUNT(*)
FROM actions_dossier 
WHERE recidive = 'PREMIERE_FOIS'
UNION ALL
SELECT 
    'Actions injoignable (récidive)', 
    COUNT(*)
FROM actions_dossier 
WHERE recidive = 'RECIDIVE';

-- ===========================================================
-- Vérifications de cohérence
-- ===========================================================

-- Vérifier les dossiers sans agent assigné
SELECT 
    COUNT(*) AS 'Dossiers sans agent',
    GROUP_CONCAT(cr.reference_contrat SEPARATOR ', ') AS 'Références'
FROM dossiers d
INNER JOIN credits cr ON d.credit_id = cr.id
WHERE d.agent_assigne_id IS NULL;

-- Vérifier les actions orphelines (dossiers supprimés)
SELECT 
    COUNT(*) AS 'Actions sans dossier'
FROM actions_dossier a
LEFT JOIN dossiers d ON a.dossier_id = d.id
WHERE d.id IS NULL;

-- Top 5 agents par nombre d'actions
SELECT 
    CONCAT(u.prenom, ' ', u.nom) AS Agent,
    COUNT(*) AS 'Nb actions'
FROM actions_dossier a
INNER JOIN users u ON a.user_id = u.id
GROUP BY u.id, u.prenom, u.nom
ORDER BY COUNT(*) DESC
LIMIT 5;

-- Distribution des transitions par type (approximation)
SELECT 
    CASE 
        WHEN a.montant_promesse IS NOT NULL THEN 'EN_COURS → PROMESSE'
        WHEN a.recidive = 'PREMIERE_FOIS' THEN 'EN_COURS → INJOIGNABLE'
        WHEN a.recidive = 'RECIDIVE' THEN 'INJOIGNABLE → INJOIGNABLE'
        ELSE 'Autre'
    END AS 'Type transition',
    COUNT(*) AS 'Nombre'
FROM actions_dossier a
GROUP BY 
    CASE 
        WHEN a.montant_promesse IS NOT NULL THEN 'EN_COURS → PROMESSE'
        WHEN a.recidive = 'PREMIERE_FOIS' THEN 'EN_COURS → INJOIGNABLE'
        WHEN a.recidive = 'RECIDIVE' THEN 'INJOIGNABLE → INJOIGNABLE'
        ELSE 'Autre'
    END
ORDER BY COUNT(*) DESC;

-- ===========================================================
-- Test de performance : Estimation du nombre de lignes
-- ===========================================================

SELECT 
    'Feuille État actuel' AS Feuille,
    COUNT(*) AS 'Nb lignes (hors header)'
FROM dossiers
UNION ALL
SELECT 
    'Feuille Historique statuts',
    COUNT(*)
FROM actions_dossier;
