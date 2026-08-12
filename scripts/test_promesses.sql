-- Script de vérification des promesses de paiement

-- 1. Vérifier les dossiers avec statut SUIVI_PROMESSE
SELECT 
    d.id as dossier_id,
    d.statut_actuel,
    c.reference_contrat,
    cl.nom || ' ' || cl.prenom as client,
    d.mois_precedent_statut
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
JOIN clients cl ON c.client_id = cl.id
WHERE d.statut_actuel = 'SUIVI_PROMESSE';

-- 2. Vérifier les actions avec promesses pour ces dossiers
SELECT 
    ad.id as action_id,
    ad.dossier_id,
    ad.date_action,
    ad.montant_promesse,
    ad.date_promesse,
    ad.compte_rendu,
    ad.nature_action,
    ad.code_resultat
FROM actions_dossier ad
WHERE ad.montant_promesse IS NOT NULL 
  AND ad.date_promesse IS NOT NULL
ORDER BY ad.date_action DESC;

-- 3. Vérifier si les dossiers SUIVI_PROMESSE ont des promesses
SELECT 
    d.id as dossier_id,
    d.statut_actuel,
    c.reference_contrat,
    COUNT(ad.id) as nb_actions,
    COUNT(CASE WHEN ad.montant_promesse IS NOT NULL THEN 1 END) as nb_promesses,
    MAX(ad.date_action) as derniere_action,
    MAX(CASE WHEN ad.montant_promesse IS NOT NULL THEN ad.date_action END) as derniere_promesse
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
LEFT JOIN actions_dossier ad ON ad.dossier_id = d.id
WHERE d.statut_actuel = 'SUIVI_PROMESSE'
GROUP BY d.id, d.statut_actuel, c.reference_contrat;

-- 4. Détail des dernières promesses par dossier SUIVI_PROMESSE
SELECT 
    d.id as dossier_id,
    c.reference_contrat,
    ad.montant_promesse,
    ad.date_promesse,
    ad.date_action,
    ad.compte_rendu
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
JOIN actions_dossier ad ON ad.dossier_id = d.id
WHERE d.statut_actuel = 'SUIVI_PROMESSE'
  AND ad.montant_promesse IS NOT NULL
  AND ad.date_promesse IS NOT NULL
ORDER BY d.id, ad.date_action DESC;
