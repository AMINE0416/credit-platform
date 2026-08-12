-- Script de vérification de l'état des promesses
-- Date: 2026-08-11

\echo '========================================='
\echo 'VÉRIFICATION DES PROMESSES'
\echo '========================================='
\echo ''

-- 1. Dossiers avec statut PROMESSE ou SUIVI_PROMESSE
\echo '1️⃣  Dossiers en PROMESSE ou SUIVI_PROMESSE:'
\echo ''
SELECT 
    d.id,
    c.reference_contrat,
    d.statut_actuel,
    d.date_promesse,
    d.montant_promesse,
    CASE 
        WHEN d.date_promesse IS NOT NULL THEN '✅ OUI'
        ELSE '❌ NON'
    END as promesse_au_niveau_dossier
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
WHERE d.statut_actuel IN ('PROMESSE', 'SUIVI_PROMESSE')
ORDER BY d.id;

\echo ''
\echo '2️⃣  Actions avec promesse pour ces dossiers:'
\echo ''
SELECT 
    d.id as dossier_id,
    c.reference_contrat,
    a.id as action_id,
    a.date_action,
    a.statut_avant,
    a.statut_apres,
    a.date_promesse,
    a.montant_promesse
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
JOIN actions_dossier a ON a.dossier_id = d.id
WHERE d.statut_actuel IN ('PROMESSE', 'SUIVI_PROMESSE')
  AND (a.date_promesse IS NOT NULL OR a.montant_promesse IS NOT NULL)
ORDER BY d.id, a.date_action DESC;

\echo ''
\echo '3️⃣  Statistiques globales:'
\echo ''
SELECT 
    'Total dossiers PROMESSE' as type,
    COUNT(*) as nombre
FROM dossiers 
WHERE statut_actuel = 'PROMESSE'
UNION ALL
SELECT 
    'Total dossiers SUIVI_PROMESSE' as type,
    COUNT(*) as nombre
FROM dossiers 
WHERE statut_actuel = 'SUIVI_PROMESSE'
UNION ALL
SELECT 
    'Dossiers avec promesse au niveau dossier' as type,
    COUNT(*) as nombre
FROM dossiers 
WHERE date_promesse IS NOT NULL
UNION ALL
SELECT 
    'Actions avec promesse' as type,
    COUNT(*) as nombre
FROM actions_dossier 
WHERE date_promesse IS NOT NULL;

\echo ''
\echo '4️⃣  Dossiers NÉCESSITANT une migration (ont des promesses dans actions mais pas au niveau dossier):'
\echo ''
SELECT 
    d.id,
    c.reference_contrat,
    d.statut_actuel,
    COUNT(a.id) as nb_actions_avec_promesse,
    MAX(a.date_promesse) as derniere_date_promesse,
    MAX(a.montant_promesse) as dernier_montant_promesse
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
JOIN actions_dossier a ON a.dossier_id = d.id
WHERE d.statut_actuel IN ('PROMESSE', 'SUIVI_PROMESSE')
  AND a.date_promesse IS NOT NULL
  AND d.date_promesse IS NULL  -- Pas encore de promesse au niveau dossier
GROUP BY d.id, c.reference_contrat, d.statut_actuel
ORDER BY d.id;

\echo ''
\echo '5️⃣  État de la table temporaire (dossiers_mois_precedent):'
\echo ''
SELECT 
    numero_dossier,
    statut_final,
    date_promesse,
    montant_promesse,
    mois_origine
FROM dossiers_mois_precedent
ORDER BY numero_dossier;

\echo ''
\echo '========================================='
\echo 'FIN DE LA VÉRIFICATION'
\echo '========================================='
