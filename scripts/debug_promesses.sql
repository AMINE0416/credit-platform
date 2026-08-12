-- ============================================================
-- Script de diagnostic pour les promesses de paiement
-- ============================================================

-- 1. Vérifier les dossiers avec statut SUIVI_PROMESSE
SELECT 
    id,
    statut_actuel,
    agent_assigne_id,
    credit_id
FROM dossiers
WHERE statut_actuel = 'SUIVI_PROMESSE'
ORDER BY id;

-- 2. Vérifier les actions avec promesses (montant ET date remplis)
SELECT 
    ad.id,
    ad.dossier_id,
    ad.montant_promesse,
    ad.date_promesse,
    ad.compte_rendu,
    ad.date_action,
    d.statut_actuel
FROM actions_dossier ad
JOIN dossiers d ON ad.dossier_id = d.id
WHERE ad.montant_promesse IS NOT NULL 
  AND ad.date_promesse IS NOT NULL
ORDER BY ad.date_action DESC;

-- 3. Vérifier spécifiquement les promesses pour les dossiers SUIVI_PROMESSE
SELECT 
    d.id as dossier_id,
    d.statut_actuel,
    ad.id as action_id,
    ad.montant_promesse,
    ad.date_promesse,
    ad.compte_rendu,
    ad.date_action
FROM dossiers d
LEFT JOIN actions_dossier ad ON ad.dossier_id = d.id 
    AND ad.montant_promesse IS NOT NULL 
    AND ad.date_promesse IS NOT NULL
WHERE d.statut_actuel = 'SUIVI_PROMESSE'
ORDER BY d.id, ad.date_action DESC;

-- 4. Compter les dossiers et actions
SELECT 
    'Dossiers SUIVI_PROMESSE' as type,
    COUNT(*) as count
FROM dossiers
WHERE statut_actuel = 'SUIVI_PROMESSE'
UNION ALL
SELECT 
    'Actions avec promesse' as type,
    COUNT(*) as count
FROM actions_dossier
WHERE montant_promesse IS NOT NULL 
  AND date_promesse IS NOT NULL;
