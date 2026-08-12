-- Ajouter le statut SUIVI_INJOIGNABLE manquant dans l'enum
-- Ce statut est nécessaire pour l'archivage mensuel

-- Ajouter SUIVI_INJOIGNABLE à l'enum statut_dossier_enum
ALTER TYPE statut_dossier_enum ADD VALUE IF NOT EXISTS 'SUIVI_INJOIGNABLE';

-- Vérifier que tous les statuts nécessaires existent
DO $$
BEGIN
    -- Vérifier la présence des statuts de suivi
    IF NOT EXISTS (
        SELECT 1 FROM pg_enum e
        JOIN pg_type t ON e.enumtypid = t.oid
        WHERE t.typname = 'statut_dossier_enum'
        AND e.enumlabel = 'SUIVI_INJOIGNABLE'
    ) THEN
        RAISE EXCEPTION 'SUIVI_INJOIGNABLE n''a pas pu être ajouté';
    END IF;
    
    RAISE NOTICE 'Statut SUIVI_INJOIGNABLE ajouté avec succès';
END $$;

-- Afficher tous les statuts disponibles
SELECT enumlabel as statut_disponible
FROM pg_enum e
JOIN pg_type t ON e.enumtypid = t.oid
WHERE t.typname = 'statut_dossier_enum'
ORDER BY e.enumsortorder;
