-- ============================================
-- FIX: Ajouter SUIVI_INJOIGNABLE à l'enum
-- ============================================
-- Ce script ajoute le statut SUIVI_INJOIGNABLE
-- s'il n'existe pas déjà dans l'enum
-- ============================================

-- Vérifier l'état actuel de l'enum
SELECT 'État actuel de l''enum statut_dossier:' AS info;
SELECT enumlabel as statut_disponible
FROM pg_enum e
JOIN pg_type t ON e.enumtypid = t.oid
WHERE t.typname = 'statut_dossier'
ORDER BY e.enumsortorder;

-- Ajouter SUIVI_INJOIGNABLE si manquant
DO $$
BEGIN
    -- Vérifier si le statut existe déjà
    IF NOT EXISTS (
        SELECT 1 FROM pg_enum e
        JOIN pg_type t ON e.enumtypid = t.oid
        WHERE t.typname = 'statut_dossier'
        AND e.enumlabel = 'SUIVI_INJOIGNABLE'
    ) THEN
        -- Ajouter le statut
        ALTER TYPE statut_dossier ADD VALUE 'SUIVI_INJOIGNABLE';
        RAISE NOTICE '✅ Statut SUIVI_INJOIGNABLE ajouté avec succès';
    ELSE
        RAISE NOTICE 'ℹ️  Statut SUIVI_INJOIGNABLE existe déjà';
    END IF;
END $$;

-- Afficher l'état final
SELECT 'État final de l''enum statut_dossier:' AS info;
SELECT enumlabel as statut_disponible
FROM pg_enum e
JOIN pg_type t ON e.enumtypid = t.oid
WHERE t.typname = 'statut_dossier'
ORDER BY e.enumsortorder;

-- Vérifier que tous les statuts requis existent
DO $$
DECLARE
    statuts_requis TEXT[] := ARRAY['EN_COURS', 'PROMESSE', 'INJOIGNABLE', 'REGULARISE', 
                                    'ALERTE', 'SUIVI_PROMESSE', 'SUIVI_INJOIGNABLE', 'CLOTURE'];
    statut TEXT;
    manquants TEXT[] := ARRAY[]::TEXT[];
BEGIN
    FOREACH statut IN ARRAY statuts_requis
    LOOP
        IF NOT EXISTS (
            SELECT 1 FROM pg_enum e
            JOIN pg_type t ON e.enumtypid = t.oid
            WHERE t.typname = 'statut_dossier'
            AND e.enumlabel = statut
        ) THEN
            manquants := array_append(manquants, statut);
        END IF;
    END LOOP;
    
    IF array_length(manquants, 1) > 0 THEN
        RAISE WARNING '⚠️  Statuts manquants: %', array_to_string(manquants, ', ');
    ELSE
        RAISE NOTICE '✅ Tous les statuts requis sont présents';
    END IF;
END $$;
