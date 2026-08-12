-- ============================================================
-- Test de l'adresse de cadrage et téléphone qualifié
-- ============================================================
-- Date : 12/08/2026
-- Description : Script pour tester la fonctionnalité d'ajout
--               de l'adresse de cadrage et du téléphone qualifié
-- ============================================================

-- 1. Vérifier la structure de la table clients
-- Doit contenir la colonne adresse_cadrage
SELECT 
    column_name,
    data_type,
    character_maximum_length,
    is_nullable
FROM information_schema.columns
WHERE table_name = 'clients' 
  AND column_name = 'adresse_cadrage';

-- 2. Lister tous les clients avec leur adresse de cadrage
SELECT 
    c.id,
    c.cin,
    c.nom,
    c.prenom,
    c.telephone,
    c.adresse,
    c.ville,
    c.adresse_cadrage,
    COUNT(d.id) as nb_dossiers
FROM clients c
LEFT JOIN credits cr ON c.id = cr.client_id
LEFT JOIN dossiers d ON cr.id = d.credit_id
GROUP BY c.id, c.cin, c.nom, c.prenom, c.telephone, c.adresse, c.ville, c.adresse_cadrage
ORDER BY c.id;

-- 3. Clients avec adresse de cadrage renseignée
SELECT 
    c.id,
    c.cin,
    c.nom,
    c.prenom,
    c.adresse_cadrage,
    d.statut_actuel
FROM clients c
JOIN credits cr ON c.id = cr.client_id
JOIN dossiers d ON cr.id = d.credit_id
WHERE c.adresse_cadrage IS NOT NULL
  AND c.adresse_cadrage != ''
ORDER BY c.id;

-- 4. Dossiers en statut PROMESSE ou SUIVI_PROMESSE avec adresse de cadrage
SELECT 
    d.id as dossier_id,
    cr.reference_contrat,
    c.nom,
    c.prenom,
    c.telephone,
    c.adresse_cadrage,
    d.statut_actuel,
    d.date_promesse,
    d.montant_promesse,
    u.nom as agent_nom,
    u.prenom as agent_prenom
FROM dossiers d
JOIN credits cr ON d.credit_id = cr.id
JOIN clients c ON cr.client_id = c.id
LEFT JOIN users u ON d.agent_assigne_id = u.id
WHERE d.statut_actuel IN ('PROMESSE', 'SUIVI_PROMESSE')
ORDER BY d.date_creation DESC;

-- 5. Actions avec nouveau téléphone qualifié
SELECT 
    ad.id as action_id,
    ad.date_action,
    ad.nouveau_telephone,
    d.statut_actuel,
    cr.reference_contrat,
    c.nom,
    c.prenom,
    c.telephone as telephone_original,
    u.nom as agent_nom,
    u.prenom as agent_prenom
FROM actions_dossier ad
JOIN dossiers d ON ad.dossier_id = d.id
JOIN users u ON ad.user_id = u.id
JOIN credits cr ON d.credit_id = cr.id
JOIN clients c ON cr.client_id = c.id
WHERE ad.nouveau_telephone IS NOT NULL
  AND ad.nouveau_telephone != ''
ORDER BY ad.date_action DESC
LIMIT 20;

-- 6. Dernières actions de chaque dossier (pour export)
-- Simule ce qui sera affiché dans l'export Excel
SELECT 
    d.id as dossier_id,
    cr.reference_contrat,
    c.cin,
    c.nom,
    c.prenom,
    c.telephone,
    c.adresse_cadrage as adresse_cadrage_client,
    d.statut_actuel,
    ad.date_action as derniere_action_date,
    ad.nature_action,
    ad.code_resultat,
    ad.montant_promesse,
    ad.date_promesse,
    ad.nouveau_telephone as telephone_qualifie,
    LEFT(ad.compte_rendu, 50) as compte_rendu_extrait
FROM dossiers d
JOIN credits cr ON d.credit_id = cr.id
JOIN clients c ON cr.client_id = c.id
LEFT JOIN (
    SELECT DISTINCT ON (dossier_id) *
    FROM actions_dossier
    ORDER BY dossier_id, date_action DESC
) ad ON d.id = ad.dossier_id
ORDER BY d.date_creation DESC
LIMIT 20;

-- 7. Statistiques sur les adresses de cadrage
SELECT 
    'Total clients' as type,
    COUNT(*) as nombre
FROM clients
UNION ALL
SELECT 
    'Clients avec adresse de cadrage' as type,
    COUNT(*) as nombre
FROM clients
WHERE adresse_cadrage IS NOT NULL
  AND adresse_cadrage != ''
UNION ALL
SELECT 
    'Dossiers PROMESSE/SUIVI_PROMESSE' as type,
    COUNT(*) as nombre
FROM dossiers
WHERE statut_actuel IN ('PROMESSE', 'SUIVI_PROMESSE');

-- 8. Statistiques sur les téléphones qualifiés
SELECT 
    'Total actions' as type,
    COUNT(*) as nombre
FROM actions_dossier
UNION ALL
SELECT 
    'Actions avec téléphone qualifié' as type,
    COUNT(*) as nombre
FROM actions_dossier
WHERE nouveau_telephone IS NOT NULL
  AND nouveau_telephone != '';

-- 9. Test : Simuler une mise à jour d'adresse de cadrage
-- (À décommenter pour tester manuellement)
/*
-- Sélectionner un client pour test
SELECT id, cin, nom, prenom, adresse_cadrage
FROM clients
WHERE id = 1;

-- Mettre à jour l'adresse de cadrage
UPDATE clients
SET adresse_cadrage = '123 Avenue Mohamed V, Immeuble Najah, Appartement 5, Casablanca'
WHERE id = 1;

-- Vérifier la mise à jour
SELECT id, cin, nom, prenom, adresse_cadrage
FROM clients
WHERE id = 1;
*/

-- 10. Vérifier la cohérence des données
-- Dossiers PROMESSE/SUIVI_PROMESSE sans date ou montant
SELECT 
    d.id as dossier_id,
    cr.reference_contrat,
    c.nom,
    c.prenom,
    d.statut_actuel,
    d.date_promesse,
    d.montant_promesse,
    CASE
        WHEN d.date_promesse IS NULL THEN 'Date promesse manquante'
        WHEN d.montant_promesse IS NULL THEN 'Montant promesse manquant'
        ELSE 'OK'
    END as etat
FROM dossiers d
JOIN credits cr ON d.credit_id = cr.id
JOIN clients c ON cr.client_id = c.id
WHERE d.statut_actuel IN ('PROMESSE', 'SUIVI_PROMESSE')
  AND (d.date_promesse IS NULL OR d.montant_promesse IS NULL);

-- ============================================================
-- RÉSUMÉ DES TESTS
-- ============================================================
-- 1. ✓ Vérifier que la colonne adresse_cadrage existe
-- 2. ✓ Lister tous les clients avec leur adresse de cadrage
-- 3. ✓ Lister les clients avec adresse de cadrage renseignée
-- 4. ✓ Dossiers PROMESSE/SUIVI_PROMESSE avec adresse de cadrage
-- 5. ✓ Actions avec téléphone qualifié
-- 6. ✓ Simulation de l'export Excel (dernières actions)
-- 7. ✓ Statistiques adresses de cadrage
-- 8. ✓ Statistiques téléphones qualifiés
-- 9. ✓ Test de mise à jour manuelle
-- 10. ✓ Vérifier la cohérence des données
-- ============================================================
