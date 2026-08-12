-- ===============================================
-- Migration: Ajout de la colonne score
-- Description: Ajoute le champ score à la table credits
-- Date: 2026-07-29
-- ===============================================

-- Ajouter la colonne score à la table credits
ALTER TABLE credits 
ADD COLUMN IF NOT EXISTS score DECIMAL(5,2) DEFAULT NULL;

-- Commentaire sur la colonne
COMMENT ON COLUMN credits.score IS 'Score du dossier (0-100) importé depuis le fichier Excel';

-- Vérification
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'credits' AND column_name = 'score';

-- Message de confirmation
DO $$
BEGIN
    RAISE NOTICE 'Colonne "score" ajoutée avec succès à la table "credits"';
END $$;
