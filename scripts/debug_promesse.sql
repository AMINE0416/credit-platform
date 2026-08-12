-- Vérifier les données d'un dossier spécifique
-- Remplacer 123 par l'ID de votre dossier

SELECT 
    d.id,
    d.statut_actuel,
    d.date_promesse,
    d.montant_promesse,
    c.reference_contrat
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
WHERE d.id = 123;  -- REMPLACER PAR VOTRE ID

-- Voir toutes les actions du dossier
SELECT 
    id,
    date_action,
    nature_action,
    date_promesse,
    montant_promesse,
    compte_rendu
FROM actions_dossier
WHERE dossier_id = 123  -- REMPLACER PAR VOTRE ID
ORDER BY date_action DESC;
