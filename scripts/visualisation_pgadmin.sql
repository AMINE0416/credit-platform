-- ========================================
-- REQUÊTES POSTGRESQL POUR PGADMIN
-- Plateforme de Crédit EQDOM
-- ========================================

-- ========================================
-- 1. TABLES ET DONNÉES DE BASE
-- ========================================

-- Voir tous les utilisateurs
SELECT * FROM users ORDER BY id;

-- Voir tous les rôles
SELECT * FROM roles ORDER BY id;

-- Voir la table de liaison users-roles
SELECT * FROM user_roles ORDER BY user_id;

-- Voir les utilisateurs avec leurs rôles
SELECT 
    u.id, 
    u.nom, 
    u.prenom, 
    u.email, 
    u.actif, 
    r.nom as role
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
ORDER BY u.id;

-- Compter les utilisateurs par rôle
SELECT 
    r.nom as role, 
    COUNT(*) as nombre_utilisateurs
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
GROUP BY r.nom;

-- ========================================
-- 2. DOSSIERS - VUE D'ENSEMBLE
-- ========================================

-- Voir tous les dossiers (si des dossiers existent)
SELECT * FROM dossiers ORDER BY id;

-- Compter les dossiers par statut
SELECT 
    statut_actuel,
    COUNT(*) as nombre_dossiers
FROM dossiers
GROUP BY statut_actuel
ORDER BY 
    CASE statut_actuel
        WHEN 'NOUVEAU' THEN 1
        WHEN 'EN_COURS' THEN 2
        WHEN 'INJOIGNABLE' THEN 3
        WHEN 'PROMESSE' THEN 4
        WHEN 'REGULARISE' THEN 5
        WHEN 'CLOTURE' THEN 6
        ELSE 7
    END;

-- ========================================
-- 3. CLIENTS ET CRÉDITS
-- ========================================

-- Voir tous les clients (si des clients existent)
SELECT * FROM clients ORDER BY id;

-- Voir tous les crédits
SELECT * FROM credits ORDER BY id;

-- Vue complète : Dossiers avec clients et crédits
SELECT 
    d.id as dossier_id,
    d.statut_actuel,
    cl.cin,
    CONCAT(cl.prenom, ' ', cl.nom) as nom_complet,
    cl.telephone,
    cl.ville,
    cr.reference_contrat,
    cr.nbr_impaye,
    cr.impaye_montant,
    CONCAT(u.prenom, ' ', u.nom) as agent_assigne,
    d.date_creation
FROM dossiers d
INNER JOIN credits cr ON d.credit_id = cr.id
INNER JOIN clients cl ON cr.client_id = cl.id
LEFT JOIN users u ON d.agent_assigne_id = u.id
ORDER BY d.statut_actuel, d.id;

-- ========================================
-- 4. ACTIONS SUR DOSSIERS
-- ========================================

-- Voir toutes les actions
SELECT * FROM actions_dossier ORDER BY date_action DESC LIMIT 50;

-- Actions avec détails utilisateur et dossier
SELECT 
    a.id,
    a.date_action,
    CONCAT(u.prenom, ' ', u.nom) as agent,
    d.id as dossier_id,
    d.statut_actuel,
    a.nature_action,
    a.code_resultat,
    a.montant_promesse,
    a.date_promesse,
    LEFT(a.compte_rendu, 100) as compte_rendu_extrait
FROM actions_dossier a
INNER JOIN users u ON a.user_id = u.id
INNER JOIN dossiers d ON a.dossier_id = d.id
ORDER BY a.date_action DESC
LIMIT 50;

-- ========================================
-- 5. HISTORIQUE D'UN DOSSIER SPÉCIFIQUE
-- ========================================

-- Remplacer <ID_DOSSIER> par l'ID réel (ex: 85, 91)
SELECT 
    a.id as action_id,
    a.date_action,
    CONCAT(u.prenom, ' ', u.nom) as agent,
    a.nature_action,
    a.motif_impaye,
    a.code_resultat,
    a.recidive,
    a.nbre_action_client,
    a.montant_promesse,
    a.date_promesse,
    a.nouveau_telephone,
    a.compte_rendu
FROM actions_dossier a
INNER JOIN users u ON a.user_id = u.id
WHERE a.dossier_id = 85
ORDER BY a.date_action DESC;

-- ========================================
-- 6. DOSSIERS PAR STATUT
-- ========================================

-- Dossiers NOUVEAU (non assignés)
SELECT 
    d.id,
    cl.cin,
    CONCAT(cl.prenom, ' ', cl.nom) as client,
    cr.reference_contrat,
    cr.impaye_montant,
    d.date_creation
FROM dossiers d
INNER JOIN credits cr ON d.credit_id = cr.id
INNER JOIN clients cl ON cr.client_id = cl.id
WHERE d.statut_actuel = 'NOUVEAU'
ORDER BY d.date_creation DESC;

-- Dossiers EN_COURS
SELECT 
    d.id,
    cl.cin,
    CONCAT(cl.prenom, ' ', cl.nom) as client,
    cr.reference_contrat,
    CONCAT(u.prenom, ' ', u.nom) as agent,
    (SELECT COUNT(*) FROM actions_dossier WHERE dossier_id = d.id) as nb_actions,
    d.date_creation
FROM dossiers d
INNER JOIN credits cr ON d.credit_id = cr.id
INNER JOIN clients cl ON cr.client_id = cl.id
LEFT JOIN users u ON d.agent_assigne_id = u.id
WHERE d.statut_actuel = 'EN_COURS'
ORDER BY d.date_creation DESC;

-- Dossiers avec PROMESSE
SELECT 
    d.id,
    cl.cin,
    CONCAT(cl.prenom, ' ', cl.nom) as client,
    cr.reference_contrat,
    CONCAT(u.prenom, ' ', u.nom) as agent,
    a.montant_promesse,
    a.date_promesse,
    (a.date_promesse - CURRENT_DATE) as jours_restants,
    a.date_action as date_enregistrement
FROM dossiers d
INNER JOIN credits cr ON d.credit_id = cr.id
INNER JOIN clients cl ON cr.client_id = cl.id
LEFT JOIN users u ON d.agent_assigne_id = u.id
INNER JOIN actions_dossier a ON d.id = a.dossier_id
WHERE d.statut_actuel = 'PROMESSE'
  AND a.montant_promesse IS NOT NULL
ORDER BY a.date_promesse ASC;

-- Dossiers INJOIGNABLE
SELECT 
    d.id,
    cl.cin,
    CONCAT(cl.prenom, ' ', cl.nom) as client,
    cl.telephone,
    cl.telephone2,
    cr.reference_contrat,
    CONCAT(u.prenom, ' ', u.nom) as agent,
    (SELECT COUNT(*) FROM actions_dossier WHERE dossier_id = d.id) as nb_tentatives,
    (SELECT MAX(date_action) FROM actions_dossier WHERE dossier_id = d.id) as derniere_tentative
FROM dossiers d
INNER JOIN credits cr ON d.credit_id = cr.id
INNER JOIN clients cl ON cr.client_id = cl.id
LEFT JOIN users u ON d.agent_assigne_id = u.id
WHERE d.statut_actuel = 'INJOIGNABLE'
ORDER BY derniere_tentative DESC;

-- Dossiers REGULARISE
SELECT 
    d.id,
    cl.cin,
    CONCAT(cl.prenom, ' ', cl.nom) as client,
    cr.reference_contrat,
    cr.impaye_montant as montant_initial,
    (SELECT MAX(date_action) FROM actions_dossier WHERE dossier_id = d.id) as date_regularisation,
    (SELECT MAX(date_action) FROM actions_dossier WHERE dossier_id = d.id) - d.date_creation as duree_recouvrement_jours
FROM dossiers d
INNER JOIN credits cr ON d.credit_id = cr.id
INNER JOIN clients cl ON cr.client_id = cl.id
WHERE d.statut_actuel = 'REGULARISE'
ORDER BY date_regularisation DESC;

-- ========================================
-- 7. STATISTIQUES
-- ========================================

-- Nombre d'actions par agent
SELECT 
    CONCAT(u.prenom, ' ', u.nom) as agent,
    COUNT(*) as total_actions,
    COUNT(DISTINCT a.dossier_id) as dossiers_traites,
    SUM(CASE WHEN d.statut_actuel = 'REGULARISE' THEN 1 ELSE 0 END) as dossiers_regularises,
    SUM(CASE WHEN a.montant_promesse IS NOT NULL THEN 1 ELSE 0 END) as promesses_obtenues
FROM actions_dossier a
INNER JOIN users u ON a.user_id = u.id
INNER JOIN dossiers d ON a.dossier_id = d.id
GROUP BY u.id, u.prenom, u.nom
ORDER BY total_actions DESC;

-- Nature d'actions les plus fréquentes
SELECT 
    a.nature_action,
    COUNT(*) as nombre_actions,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM actions_dossier), 2) as pourcentage
FROM actions_dossier a
GROUP BY a.nature_action
ORDER BY nombre_actions DESC;

-- Vue synthétique quotidienne
SELECT 
    'Total dossiers' as indicateur,
    COUNT(*) as valeur
FROM dossiers
UNION ALL
SELECT 
    'Dossiers EN_COURS' as indicateur,
    COUNT(*) as valeur
FROM dossiers WHERE statut_actuel = 'EN_COURS'
UNION ALL
SELECT 
    'Promesses actives' as indicateur,
    COUNT(*) as valeur
FROM dossiers WHERE statut_actuel = 'PROMESSE'
UNION ALL
SELECT 
    'Dossiers régularisés' as indicateur,
    COUNT(*) as valeur
FROM dossiers WHERE statut_actuel = 'REGULARISE';

-- ========================================
-- 8. ALERTES
-- ========================================

-- Promesses arrivant à échéance (J-3)
SELECT 
    d.id as dossier_id,
    cl.cin,
    CONCAT(cl.prenom, ' ', cl.nom) as client,
    cl.telephone,
    cr.reference_contrat,
    a.montant_promesse,
    a.date_promesse,
    (a.date_promesse - CURRENT_DATE) as jours_restants,
    CONCAT(u.prenom, ' ', u.nom) as agent
FROM dossiers d
INNER JOIN actions_dossier a ON d.id = a.dossier_id
INNER JOIN credits cr ON d.credit_id = cr.id
INNER JOIN clients cl ON cr.client_id = cl.id
LEFT JOIN users u ON d.agent_assigne_id = u.id
WHERE d.statut_actuel = 'PROMESSE'
  AND a.montant_promesse IS NOT NULL
  AND a.date_promesse BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '3 days'
ORDER BY a.date_promesse ASC;

-- Promesses dépassées non traitées
SELECT 
    d.id as dossier_id,
    cl.cin,
    CONCAT(cl.prenom, ' ', cl.nom) as client,
    cl.telephone,
    cr.reference_contrat,
    a.montant_promesse,
    a.date_promesse,
    (CURRENT_DATE - a.date_promesse) as jours_retard,
    CONCAT(u.prenom, ' ', u.nom) as agent
FROM dossiers d
INNER JOIN actions_dossier a ON d.id = a.dossier_id
INNER JOIN credits cr ON d.credit_id = cr.id
INNER JOIN clients cl ON cr.client_id = cl.id
LEFT JOIN users u ON d.agent_assigne_id = u.id
WHERE d.statut_actuel = 'PROMESSE'
  AND a.montant_promesse IS NOT NULL
  AND a.date_promesse < CURRENT_DATE
ORDER BY jours_retard DESC;

-- ========================================
-- 9. TRANSITIONS D'ÉTATS
-- ========================================

-- Voir toutes les transitions configurées
SELECT * FROM transitions_etat ORDER BY etat_depart, etat_arrivee;

-- ========================================
-- 10. VÉRIFICATIONS SYSTÈME
-- ========================================

-- Vérifier la structure de toutes les tables
SELECT 
    table_name,
    column_name,
    data_type,
    is_nullable
FROM information_schema.columns
WHERE table_schema = 'public'
ORDER BY table_name, ordinal_position;

-- Compter les enregistrements dans chaque table
SELECT 'users' as table_name, COUNT(*) as nb_enregistrements FROM users
UNION ALL
SELECT 'roles', COUNT(*) FROM roles
UNION ALL
SELECT 'user_roles', COUNT(*) FROM user_roles
UNION ALL
SELECT 'clients', COUNT(*) FROM clients
UNION ALL
SELECT 'credits', COUNT(*) FROM credits
UNION ALL
SELECT 'dossiers', COUNT(*) FROM dossiers
UNION ALL
SELECT 'actions_dossier', COUNT(*) FROM actions_dossier
UNION ALL
SELECT 'transitions_etat', COUNT(*) FROM transitions_etat;

-- ========================================
-- NOTES D'UTILISATION
-- ========================================
-- 
-- Pour utiliser ces requêtes dans pgAdmin:
-- 1. Ouvrez pgAdmin
-- 2. Naviguez vers votre base de données credit_platform
-- 3. Cliquez sur "Query Tool" (outil de requête)
-- 4. Copiez-collez la requête souhaitée
-- 5. Cliquez sur le bouton "Execute" (▶) ou appuyez sur F5
-- 
-- Astuces:
-- - Sélectionnez uniquement la requête que vous voulez exécuter
-- - Utilisez Ctrl+/ pour commenter/décommenter des lignes
-- - Les résultats apparaissent dans l'onglet "Data Output"
-- - Vous pouvez exporter les résultats en cliquant sur "Download as CSV"
--
-- ========================================
