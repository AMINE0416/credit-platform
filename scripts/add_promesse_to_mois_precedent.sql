-- Migration pour ajouter les champs de promesse à la table dossiers_mois_precedent
-- Date de création: 2026-08-11
-- Objectif: Préserver les informations de promesse lors de l'archivage mensuel

-- Ajout des colonnes date_promesse et montant_promesse à la table dossiers_mois_precedent
ALTER TABLE dossiers_mois_precedent 
ADD COLUMN IF NOT EXISTS date_promesse DATE,
ADD COLUMN IF NOT EXISTS montant_promesse NUMERIC(15, 2);

-- Ajout de commentaires pour la documentation
COMMENT ON COLUMN dossiers_mois_precedent.date_promesse IS 'Date de la promesse de paiement préservée pour le prochain import';
COMMENT ON COLUMN dossiers_mois_precedent.montant_promesse IS 'Montant promis préservé pour le prochain import';

-- Vérification
SELECT 
    column_name, 
    data_type, 
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'dossiers_mois_precedent' 
  AND column_name IN ('date_promesse', 'montant_promesse')
ORDER BY ordinal_position;

-- Afficher la structure complète de la table
\d dossiers_mois_precedent
