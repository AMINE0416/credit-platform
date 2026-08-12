-- Migration pour ajouter les champs de promesse
-- Date de création: 2024

-- Ajout des colonnes date_promesse et montant_promesse à la table dossiers
ALTER TABLE dossiers 
ADD COLUMN IF NOT EXISTS date_promesse DATE,
ADD COLUMN IF NOT EXISTS montant_promesse NUMERIC(15, 2);

-- Ajout de commentaires pour la documentation
COMMENT ON COLUMN dossiers.date_promesse IS 'Date de la promesse de paiement (pour statuts PROMESSE et SUIVI_PROMESSE)';
COMMENT ON COLUMN dossiers.montant_promesse IS 'Montant promis par le client (pour statuts PROMESSE et SUIVI_PROMESSE)';

-- Vérification
SELECT 
    column_name, 
    data_type, 
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'dossiers' 
  AND column_name IN ('date_promesse', 'montant_promesse')
ORDER BY ordinal_position;
