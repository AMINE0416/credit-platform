-- ============================================
-- MIGRATION: SIMPLIFICATION DES STATUTS
-- ============================================
-- Nouvelle logique:
-- - Supprimer: EN_ATTENTE, CONTACTE, RELANCE
-- - Garder: EN_COURS (point de départ), PROMESSE, INJOIGNABLE, REGULARISE, CLOTURE
-- - Ajouter: ALERTE, SUIVI_PROMESSE, SUIVI_INJOIGNABLE
--
-- Date: 2026-08-05
-- ============================================

BEGIN;

-- 1. SAUVEGARDER L'ÉTAT ACTUEL
CREATE TABLE IF NOT EXISTS backup_statuts_migration AS
SELECT 
    d.id,
    d.statut_actuel AS ancien_statut,
    c.reference_contrat,
    cl.nom || ' ' || cl.prenom AS client
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
JOIN clients cl ON c.client_id = cl.id;

SELECT COUNT(*) AS "Dossiers sauvegardés" FROM backup_statuts_migration;

-- 2. MAPPER LES ANCIENS STATUTS VERS LES NOUVEAUX
-- Logique de migration:
-- EN_ATTENTE → EN_COURS (tous commencent en cours)
-- CONTACTE → EN_COURS
-- RELANCE → ALERTE (dossiers à surveiller)
-- EN_COURS → EN_COURS (pas de changement)
-- PROMESSE → PROMESSE (pas de changement)
-- INJOIGNABLE → INJOIGNABLE (pas de changement)
-- SUIVI_PROMESSE → SUIVI_PROMESSE (pas de changement)
-- REGULARISE → REGULARISE (pas de changement)
-- CLOTURE → CLOTURE (pas de changement)

UPDATE dossiers
SET statut_actuel = 'EN_COURS'
WHERE statut_actuel IN ('EN_ATTENTE', 'CONTACTE');

UPDATE dossiers
SET statut_actuel = 'ALERTE'
WHERE statut_actuel = 'RELANCE';

-- 3. VÉRIFIER L'ÉTAT APRÈS MIGRATION
SELECT 
    statut_actuel,
    COUNT(*) AS nombre,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER (), 2) AS pourcentage
FROM dossiers
GROUP BY statut_actuel
ORDER BY nombre DESC;

-- 4. METTRE À JOUR LES DOSSIERS ARCHIVÉS (historique)
-- Pour préserver l'historique tout en permettant les requêtes
UPDATE dossiers_archive
SET statut_final = 'EN_COURS'
WHERE statut_final IN ('EN_ATTENTE', 'CONTACTE');

UPDATE dossiers_archive
SET statut_final = 'ALERTE'
WHERE statut_final = 'RELANCE';

-- 5. METTRE À JOUR LES DOSSIERS DU MOIS PRÉCÉDENT (pour la détection)
UPDATE dossiers_mois_precedent
SET statut_final = 'EN_COURS'
WHERE statut_final IN ('EN_ATTENTE', 'CONTACTE');

UPDATE dossiers_mois_precedent
SET statut_final = 'ALERTE'
WHERE statut_final = 'RELANCE';

-- 6. RECRÉER L'ENUM AVEC LES NOUVEAUX STATUTS
-- Attention: Cette opération nécessite que la colonne ne contienne plus les anciens statuts

-- D'abord, convertir la colonne en VARCHAR temporairement
ALTER TABLE dossiers ALTER COLUMN statut_actuel TYPE VARCHAR(50);
ALTER TABLE dossiers_archive ALTER COLUMN statut_final TYPE VARCHAR(50);
ALTER TABLE dossiers_mois_precedent ALTER COLUMN statut_final TYPE VARCHAR(50);

-- Supprimer l'ancien type ENUM
DROP TYPE IF EXISTS statut_dossier CASCADE;

-- Créer le nouveau type ENUM avec les statuts simplifiés
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

-- Reconvertir les colonnes en utilisant le nouveau type ENUM
ALTER TABLE dossiers 
    ALTER COLUMN statut_actuel TYPE statut_dossier 
    USING statut_actuel::statut_dossier;

ALTER TABLE dossiers_archive 
    ALTER COLUMN statut_final TYPE statut_dossier 
    USING statut_final::statut_dossier;

ALTER TABLE dossiers_mois_precedent 
    ALTER COLUMN statut_final TYPE statut_dossier 
    USING statut_final::statut_dossier;

-- 7. CRÉER UN RAPPORT DE MIGRATION
SELECT 
    'MIGRATION TERMINÉE' AS statut,
    COUNT(DISTINCT d.id) AS total_dossiers_migres,
    COUNT(DISTINCT CASE WHEN d.statut_actuel = 'EN_COURS' THEN d.id END) AS en_cours,
    COUNT(DISTINCT CASE WHEN d.statut_actuel = 'PROMESSE' THEN d.id END) AS promesse,
    COUNT(DISTINCT CASE WHEN d.statut_actuel = 'INJOIGNABLE' THEN d.id END) AS injoignable,
    COUNT(DISTINCT CASE WHEN d.statut_actuel = 'ALERTE' THEN d.id END) AS alerte,
    COUNT(DISTINCT CASE WHEN d.statut_actuel = 'SUIVI_PROMESSE' THEN d.id END) AS suivi_promesse,
    COUNT(DISTINCT CASE WHEN d.statut_actuel = 'SUIVI_INJOIGNABLE' THEN d.id END) AS suivi_injoignable,
    COUNT(DISTINCT CASE WHEN d.statut_actuel = 'REGULARISE' THEN d.id END) AS regularise,
    COUNT(DISTINCT CASE WHEN d.statut_actuel = 'CLOTURE' THEN d.id END) AS cloture
FROM dossiers d;

-- 8. COMPARER AVANT/APRÈS
SELECT 
    'Avant migration' AS periode,
    ancien_statut AS statut,
    COUNT(*) AS nombre
FROM backup_statuts_migration
GROUP BY ancien_statut

UNION ALL

SELECT 
    'Après migration' AS periode,
    d.statut_actuel::TEXT AS statut,
    COUNT(*) AS nombre
FROM dossiers d
GROUP BY d.statut_actuel
ORDER BY periode, nombre DESC;

COMMIT;

-- ============================================
-- NOTES:
-- - Garder la table backup_statuts_migration pour référence
-- - Tous les dossiers EN_ATTENTE et CONTACTE deviennent EN_COURS
-- - Les RELANCE deviennent ALERTE (à surveiller)
-- - Import Excel: tous les nouveaux dossiers commencent EN_COURS
-- - Archivage: EN_COURS → ALERTE, PROMESSE → SUIVI_PROMESSE, etc.
-- ============================================
