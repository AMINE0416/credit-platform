-- Diagnostic détaillé des promesses
-- Date: 2026-08-11

\echo '================================================'
\echo 'DIAGNOSTIC DÉTAILLÉ DES PROMESSES'
\echo '================================================'
\echo ''

-- 1. Tous les dossiers avec leur statut actuel
\echo '1️⃣  TOUS LES DOSSIERS:'
\echo ''
SELECT 
    d.id,
    c.reference_contrat,
    cl.cin,
    cl.nom || ' ' || cl.prenom as client,
    d.statut_actuel,
    d.date_promesse as promesse_dossier_date,
    d.montant_promesse as promesse_dossier_montant
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
JOIN clients cl ON c.client_id = cl.id
ORDER BY d.id;

\echo ''
\echo '2️⃣  TOUTES LES ACTIONS POUR CES DOSSIERS:'
\echo ''
SELECT 
    d.id as dossier_id,
    c.reference_contrat,
    a.id as action_id,
    a.date_action,
    a.statut_avant,
    a.statut_apres,
    a.date_promesse,
    a.montant_promesse,
    a.compte_rendu
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
LEFT JOIN actions_dossier a ON a.dossier_id = d.id
ORDER BY d.id, a.date_action DESC;

\echo ''
\echo '3️⃣  RÉSUMÉ PAR STATUT:'
\echo ''
SELECT 
    statut_actuel,
    COUNT(*) as nombre_dossiers
FROM dossiers
GROUP BY statut_actuel
ORDER BY statut_actuel;

\echo ''
\echo '4️⃣  DOSSIERS AVEC ACTIONS:'
\echo ''
SELECT 
    d.statut_actuel,
    COUNT(DISTINCT d.id) as nb_dossiers,
    COUNT(a.id) as nb_actions_total,
    SUM(CASE WHEN a.date_promesse IS NOT NULL THEN 1 ELSE 0 END) as nb_actions_avec_promesse
FROM dossiers d
LEFT JOIN actions_dossier a ON a.dossier_id = d.id
GROUP BY d.statut_actuel
ORDER BY d.statut_actuel;

\echo ''
\echo '5️⃣  STRUCTURE DE LA TABLE DOSSIERS:'
\echo ''
SELECT column_name, data_type, is_nullable
FROM information_schema.columns 
WHERE table_name = 'dossiers' 
  AND column_name IN ('date_promesse', 'montant_promesse')
ORDER BY ordinal_position;

\echo ''
\echo '6️⃣  STRUCTURE DE LA TABLE ACTIONS_DOSSIER:'
\echo ''
SELECT column_name, data_type, is_nullable
FROM information_schema.columns 
WHERE table_name = 'actions_dossier' 
  AND column_name IN ('date_promesse', 'montant_promesse')
ORDER BY ordinal_position;

\echo ''
\echo '================================================'
\echo 'FIN DU DIAGNOSTIC'
\echo '================================================'
