-- ═══════════════════════════════════════════════════════════════════════════
-- Script de vérification rapide - Informations de Promesse
-- Exécution : psql -U user -d database -f verifier_promesse.sql
-- ═══════════════════════════════════════════════════════════════════════════

\echo '═══════════════════════════════════════════════════════════════════════════'
\echo '  VÉRIFICATION RAPIDE - INFORMATIONS DE PROMESSE'
\echo '═══════════════════════════════════════════════════════════════════════════'
\echo ''

-- ───────────────────────────────────────────────────────────────────────────
-- 1. VÉRIFICATION DE LA STRUCTURE
-- ───────────────────────────────────────────────────────────────────────────
\echo '1️⃣  Vérification des colonnes dans la table dossiers...'
\echo ''

DO $$
DECLARE
    col_date_exists BOOLEAN;
    col_montant_exists BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'dossiers' 
          AND column_name = 'date_promesse'
    ) INTO col_date_exists;
    
    SELECT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'dossiers' 
          AND column_name = 'montant_promesse'
    ) INTO col_montant_exists;
    
    IF col_date_exists AND col_montant_exists THEN
        RAISE NOTICE '✅ Les colonnes date_promesse et montant_promesse existent';
    ELSIF col_date_exists THEN
        RAISE WARNING '⚠️  Seulement date_promesse existe, montant_promesse manquant';
    ELSIF col_montant_exists THEN
        RAISE WARNING '⚠️  Seulement montant_promesse existe, date_promesse manquant';
    ELSE
        RAISE WARNING '❌ Aucune des colonnes n''existe - La migration n''a pas été appliquée';
    END IF;
END $$;

\echo ''

-- ───────────────────────────────────────────────────────────────────────────
-- 2. STATISTIQUES GLOBALES
-- ───────────────────────────────────────────────────────────────────────────
\echo '2️⃣  Statistiques des dossiers avec statut PROMESSE...'
\echo ''

SELECT 
    '📊 Statistiques Promesses' as titre,
    COUNT(*) FILTER (WHERE statut_actuel = 'PROMESSE') as "Total PROMESSE",
    COUNT(*) FILTER (WHERE statut_actuel = 'SUIVI_PROMESSE') as "Total SUIVI_PROMESSE",
    COUNT(*) FILTER (
        WHERE statut_actuel IN ('PROMESSE', 'SUIVI_PROMESSE')
          AND date_promesse IS NOT NULL
          AND montant_promesse IS NOT NULL
    ) as "Avec infos complètes",
    COUNT(*) FILTER (
        WHERE statut_actuel IN ('PROMESSE', 'SUIVI_PROMESSE')
          AND (date_promesse IS NULL OR montant_promesse IS NULL)
    ) as "Avec infos manquantes"
FROM dossiers;

\echo ''

-- ───────────────────────────────────────────────────────────────────────────
-- 3. MONTANTS TOTAUX
-- ───────────────────────────────────────────────────────────────────────────
\echo '3️⃣  Montants promis...'
\echo ''

SELECT 
    '💰 Montants' as titre,
    COUNT(*) as "Nombre de promesses",
    COALESCE(SUM(montant_promesse), 0) as "Montant total (MAD)",
    COALESCE(ROUND(AVG(montant_promesse), 2), 0) as "Montant moyen (MAD)",
    COALESCE(MIN(montant_promesse), 0) as "Montant min (MAD)",
    COALESCE(MAX(montant_promesse), 0) as "Montant max (MAD)"
FROM dossiers
WHERE statut_actuel IN ('PROMESSE', 'SUIVI_PROMESSE')
  AND montant_promesse IS NOT NULL;

\echo ''

-- ───────────────────────────────────────────────────────────────────────────
-- 4. ÉCHÉANCES
-- ───────────────────────────────────────────────────────────────────────────
\echo '4️⃣  État des échéances...'
\echo ''

SELECT 
    '📅 Échéances' as titre,
    COUNT(*) FILTER (WHERE date_promesse < CURRENT_DATE) as "🔴 Échues",
    COUNT(*) FILTER (WHERE date_promesse = CURRENT_DATE) as "🟠 Aujourd'hui",
    COUNT(*) FILTER (
        WHERE date_promesse > CURRENT_DATE 
          AND date_promesse <= CURRENT_DATE + INTERVAL '7 days'
    ) as "🟡 Cette semaine",
    COUNT(*) FILTER (WHERE date_promesse > CURRENT_DATE + INTERVAL '7 days') as "🟢 Ultérieures"
FROM dossiers
WHERE statut_actuel IN ('PROMESSE', 'SUIVI_PROMESSE')
  AND date_promesse IS NOT NULL;

\echo ''

-- ───────────────────────────────────────────────────────────────────────────
-- 5. EXEMPLES DE DOSSIERS
-- ───────────────────────────────────────────────────────────────────────────
\echo '5️⃣  Exemples de dossiers avec promesse (5 derniers)...'
\echo ''

SELECT 
    d.id as "ID",
    d.statut_actuel as "Statut",
    c.reference_contrat as "Contrat",
    d.date_promesse as "Date Promesse",
    d.montant_promesse as "Montant (MAD)",
    CASE 
        WHEN d.date_promesse < CURRENT_DATE THEN '🔴 Échue'
        WHEN d.date_promesse = CURRENT_DATE THEN '🟠 Aujourd''hui'
        WHEN d.date_promesse <= CURRENT_DATE + 7 THEN '🟡 Proche'
        ELSE '🟢 OK'
    END as "État"
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
WHERE d.statut_actuel IN ('PROMESSE', 'SUIVI_PROMESSE')
ORDER BY d.date_creation DESC
LIMIT 5;

\echo ''

-- ───────────────────────────────────────────────────────────────────────────
-- 6. COHÉRENCE DES DONNÉES
-- ───────────────────────────────────────────────────────────────────────────
\echo '6️⃣  Vérification de cohérence...'
\echo ''

DO $$
DECLARE
    dossiers_incoherents INTEGER;
    dossiers_valides INTEGER;
BEGIN
    -- Compter les dossiers avec statut PROMESSE/SUIVI mais sans infos
    SELECT COUNT(*) 
    INTO dossiers_incoherents
    FROM dossiers
    WHERE statut_actuel IN ('PROMESSE', 'SUIVI_PROMESSE')
      AND (date_promesse IS NULL OR montant_promesse IS NULL);
    
    -- Compter les dossiers valides
    SELECT COUNT(*) 
    INTO dossiers_valides
    FROM dossiers
    WHERE statut_actuel IN ('PROMESSE', 'SUIVI_PROMESSE')
      AND date_promesse IS NOT NULL 
      AND montant_promesse IS NOT NULL;
    
    RAISE NOTICE '';
    RAISE NOTICE '═══════════════════════════════════════════';
    RAISE NOTICE '  RÉSULTAT DE LA VÉRIFICATION';
    RAISE NOTICE '═══════════════════════════════════════════';
    RAISE NOTICE '';
    
    IF dossiers_incoherents = 0 THEN
        RAISE NOTICE '✅ Tous les dossiers PROMESSE/SUIVI_PROMESSE ont des informations complètes';
        RAISE NOTICE '   Dossiers valides: %', dossiers_valides;
    ELSE
        RAISE WARNING '⚠️  % dossier(s) PROMESSE/SUIVI_PROMESSE sans informations complètes', dossiers_incoherents;
        RAISE NOTICE '   Dossiers valides: %', dossiers_valides;
        RAISE NOTICE '   → Ces dossiers ont été créés avant l''implémentation';
        RAISE NOTICE '   → Ou les informations n''ont pas été saisies correctement';
    END IF;
    
    RAISE NOTICE '';
END $$;

-- ───────────────────────────────────────────────────────────────────────────
-- 7. DOSSIERS NÉCESSITANT ATTENTION
-- ───────────────────────────────────────────────────────────────────────────
\echo ''
\echo '7️⃣  Dossiers nécessitant une attention (promesses échues)...'
\echo ''

SELECT 
    d.id as "ID Dossier",
    c.reference_contrat as "Contrat",
    cl.nom || ' ' || cl.prenom as "Client",
    cl.telephone as "Téléphone",
    d.date_promesse as "Date Promesse",
    d.montant_promesse as "Montant (MAD)",
    CURRENT_DATE - d.date_promesse as "Jours de retard"
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
JOIN clients cl ON c.client_id = cl.id
WHERE d.statut_actuel IN ('PROMESSE', 'SUIVI_PROMESSE')
  AND d.date_promesse < CURRENT_DATE
ORDER BY d.date_promesse ASC
LIMIT 10;

-- ───────────────────────────────────────────────────────────────────────────
-- 8. RECOMMANDATIONS
-- ───────────────────────────────────────────────────────────────────────────
\echo ''
\echo '═══════════════════════════════════════════════════════════════════════════'
\echo '  RECOMMANDATIONS'
\echo '═══════════════════════════════════════════════════════════════════════════'
\echo ''

DO $$
DECLARE
    echues INTEGER;
    cette_semaine INTEGER;
BEGIN
    SELECT COUNT(*) INTO echues
    FROM dossiers
    WHERE statut_actuel IN ('PROMESSE', 'SUIVI_PROMESSE')
      AND date_promesse < CURRENT_DATE;
    
    SELECT COUNT(*) INTO cette_semaine
    FROM dossiers
    WHERE statut_actuel IN ('PROMESSE', 'SUIVI_PROMESSE')
      AND date_promesse > CURRENT_DATE
      AND date_promesse <= CURRENT_DATE + 7;
    
    IF echues > 0 THEN
        RAISE NOTICE '⚠️  URGENT: % promesse(s) échue(s) nécessitent un suivi immédiat', echues;
    END IF;
    
    IF cette_semaine > 0 THEN
        RAISE NOTICE '📌 INFO: % promesse(s) arrivent à échéance cette semaine', cette_semaine;
    END IF;
    
    IF echues = 0 AND cette_semaine = 0 THEN
        RAISE NOTICE '✅ Aucune promesse échue ou proche de l''échéance';
    END IF;
    
    RAISE NOTICE '';
    RAISE NOTICE '💡 ACTIONS RECOMMANDÉES:';
    RAISE NOTICE '   1. Contacter les clients avec promesses échues';
    RAISE NOTICE '   2. Planifier le suivi des promesses de la semaine';
    RAISE NOTICE '   3. Mettre à jour les statuts selon les paiements reçus';
    RAISE NOTICE '';
END $$;

\echo '═══════════════════════════════════════════════════════════════════════════'
\echo '  FIN DE LA VÉRIFICATION'
\echo '═══════════════════════════════════════════════════════════════════════════'
\echo ''
\echo 'Pour plus d''informations, consultez:'
\echo '  • GUIDE_RAPIDE_PROMESSE.md'
\echo '  • RESUME_PROMESSE.md'
\echo ''
