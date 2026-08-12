-- ================================================================
-- CRÉATION DU TYPE ENUM statut_dossier (VERSION SIMPLIFIÉE)
-- ================================================================
-- Description : Crée le type ENUM pour les statuts des dossiers
--               si celui-ci n'existe pas déjà
-- Version : 2.0 - Logique simplifiée
-- ================================================================

-- Vérifier si le type existe déjà
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'statut_dossier') THEN
        -- Créer le type ENUM avec les statuts simplifiés
        CREATE TYPE statut_dossier AS ENUM (
            'EN_COURS',           -- Point de départ pour tous les dossiers
            'PROMESSE',           -- Client a fait une promesse
            'INJOIGNABLE',        -- Client injoignable
            'REGULARISE',         -- Dossier régularisé
            'ALERTE',             -- Récurrent: était EN_COURS (non résolu)
            'SUIVI_PROMESSE',     -- Récurrent: avait une promesse
            'SUIVI_INJOIGNABLE',  -- Récurrent: était injoignable
            'CLOTURE'             -- Dossier clôturé
        );
        
        RAISE NOTICE '✅ Type ENUM statut_dossier créé avec succès';
    ELSE
        RAISE NOTICE 'ℹ️  Type ENUM statut_dossier existe déjà';
    END IF;
END $$;

-- Vérifier la création
SELECT 
    typname as "Type ENUM",
    array_agg(enumlabel ORDER BY enumsortorder) as "Valeurs"
FROM pg_type 
JOIN pg_enum ON pg_type.oid = pg_enum.enumtypid
WHERE typname = 'statut_dossier'
GROUP BY typname;

-- Message de confirmation
SELECT '✅ Type ENUM statut_dossier prêt à l''utilisation' AS message;
