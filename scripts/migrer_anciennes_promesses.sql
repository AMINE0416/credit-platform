-- ═══════════════════════════════════════════════════════════════════════════
-- Migration des anciennes promesses vers les nouveaux champs du dossier
-- ═══════════════════════════════════════════════════════════════════════════

-- Mettre à jour tous les dossiers PROMESSE et SUIVI_PROMESSE 
-- en copiant les infos depuis leur dernière action avec promesse

UPDATE dossiers d
SET 
    date_promesse = derniere_action.date_promesse,
    montant_promesse = derniere_action.montant_promesse
FROM (
    SELECT DISTINCT ON (ad.dossier_id)
        ad.dossier_id,
        ad.date_promesse,
        ad.montant_promesse
    FROM actions_dossier ad
    WHERE ad.date_promesse IS NOT NULL 
      AND ad.montant_promesse IS NOT NULL
    ORDER BY ad.dossier_id, ad.date_action DESC
) AS derniere_action
WHERE d.id = derniere_action.dossier_id
  AND d.statut_actuel IN ('PROMESSE', 'SUIVI_PROMESSE')
  AND (d.date_promesse IS NULL OR d.montant_promesse IS NULL);

-- Vérifier le résultat
SELECT 
    d.id,
    d.statut_actuel,
    d.date_promesse,
    d.montant_promesse,
    c.reference_contrat
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
WHERE d.statut_actuel IN ('PROMESSE', 'SUIVI_PROMESSE')
ORDER BY d.id;
