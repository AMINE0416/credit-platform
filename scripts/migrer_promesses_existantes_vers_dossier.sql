-- Migration des promesses existantes depuis les actions vers le niveau dossier
-- Date: 2026-08-11
-- Objectif: Migrer les informations de promesse des actions vers les dossiers
--           pour les dossiers en statut PROMESSE ou SUIVI_PROMESSE

BEGIN;

-- Étape 1: Afficher les dossiers qui vont être mis à jour
SELECT 
    d.id as dossier_id,
    c.reference_contrat,
    d.statut_actuel,
    d.date_promesse as date_promesse_actuelle,
    d.montant_promesse as montant_promesse_actuel,
    a.date_promesse as date_promesse_action,
    a.montant_promesse as montant_promesse_action,
    a.date_action
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
JOIN actions_dossier a ON a.dossier_id = d.id
WHERE d.statut_actuel IN ('PROMESSE', 'SUIVI_PROMESSE')
  AND a.date_promesse IS NOT NULL
  AND a.montant_promesse IS NOT NULL
  AND d.date_promesse IS NULL  -- Dossier n'a pas encore de promesse
ORDER BY d.id, a.date_action DESC;

-- Étape 2: Mettre à jour les dossiers avec la dernière promesse trouvée
-- On utilise une sous-requête pour récupérer la dernière action avec promesse pour chaque dossier
UPDATE dossiers d
SET 
    date_promesse = (
        SELECT a.date_promesse 
        FROM actions_dossier a 
        WHERE a.dossier_id = d.id 
          AND a.date_promesse IS NOT NULL 
        ORDER BY a.date_action DESC 
        LIMIT 1
    ),
    montant_promesse = (
        SELECT a.montant_promesse 
        FROM actions_dossier a 
        WHERE a.dossier_id = d.id 
          AND a.montant_promesse IS NOT NULL 
        ORDER BY a.date_action DESC 
        LIMIT 1
    )
WHERE d.statut_actuel IN ('PROMESSE', 'SUIVI_PROMESSE')
  AND d.date_promesse IS NULL
  AND EXISTS (
      SELECT 1 
      FROM actions_dossier a 
      WHERE a.dossier_id = d.id 
        AND a.date_promesse IS NOT NULL 
        AND a.montant_promesse IS NOT NULL
  );

-- Étape 3: Afficher le résultat de la migration
SELECT 
    COUNT(*) as nombre_dossiers_migres
FROM dossiers 
WHERE statut_actuel IN ('PROMESSE', 'SUIVI_PROMESSE')
  AND date_promesse IS NOT NULL;

-- Étape 4: Afficher les dossiers migrés
SELECT 
    d.id,
    c.reference_contrat,
    d.statut_actuel,
    d.date_promesse,
    d.montant_promesse
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
WHERE d.statut_actuel IN ('PROMESSE', 'SUIVI_PROMESSE')
  AND d.date_promesse IS NOT NULL
ORDER BY d.id;

COMMIT;

-- Statistiques finales
SELECT 
    'Dossiers PROMESSE avec promesse migrée' as type,
    COUNT(*) as nombre
FROM dossiers 
WHERE statut_actuel = 'PROMESSE' 
  AND date_promesse IS NOT NULL
UNION ALL
SELECT 
    'Dossiers SUIVI_PROMESSE avec promesse migrée' as type,
    COUNT(*) as nombre
FROM dossiers 
WHERE statut_actuel = 'SUIVI_PROMESSE' 
  AND date_promesse IS NOT NULL
UNION ALL
SELECT 
    'Total dossiers avec promesse' as type,
    COUNT(*) as nombre
FROM dossiers 
WHERE date_promesse IS NOT NULL;
