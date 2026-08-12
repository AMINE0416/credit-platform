-- ================================================================
-- CORRECTION DE LA CONTRAINTE dossiers_statut_actuel_check
-- ================================================================
-- Description : Met à jour la contrainte de vérification pour autoriser
--               toutes les valeurs de l'enum StatutDossier incluant EN_ATTENTE
-- ================================================================

-- Étape 1 : Supprimer l'ancienne contrainte
DO $$ 
BEGIN
    IF EXISTS (
        SELECT 1 
        FROM pg_constraint 
        WHERE conname = 'dossiers_statut_actuel_check'
    ) THEN
        ALTER TABLE dossiers DROP CONSTRAINT dossiers_statut_actuel_check;
        RAISE NOTICE '✅ Ancienne contrainte dossiers_statut_actuel_check supprimée';
    ELSE
        RAISE NOTICE 'ℹ️  Contrainte dossiers_statut_actuel_check n''existe pas';
    END IF;
END $$;

-- Étape 2 : Créer la nouvelle contrainte avec toutes les valeurs autorisées
ALTER TABLE dossiers 
ADD CONSTRAINT dossiers_statut_actuel_check 
CHECK (statut_actuel IN (
    'EN_COURS',
    'PROMESSE',
    'INJOIGNABLE',
    'REGULARISE',
    'CLOTURE',
    'EN_ATTENTE',
    'CONTACTE',
    'RELANCE',
    'SUIVI_PROMESSE'
));

-- Message de confirmation
SELECT '✅ Contrainte dossiers_statut_actuel_check mise à jour avec succès' AS message;

-- Vérification : Afficher les valeurs autorisées
SELECT 
    conname AS "Nom de la contrainte",
    pg_get_constraintdef(oid) AS "Définition"
FROM pg_constraint
WHERE conname = 'dossiers_statut_actuel_check';
