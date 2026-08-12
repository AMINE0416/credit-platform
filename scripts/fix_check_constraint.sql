-- ============================================
-- FIX: Corriger la contrainte CHECK pour inclure SUIVI_INJOIGNABLE
-- ============================================

-- 1. Voir la contrainte actuelle
SELECT conname, pg_get_constraintdef(oid)
FROM pg_constraint
WHERE conname = 'dossiers_statut_actuel_check';

-- 2. Supprimer l'ancienne contrainte
ALTER TABLE dossiers DROP CONSTRAINT IF EXISTS dossiers_statut_actuel_check;

-- 3. Recréer la contrainte avec TOUS les statuts
ALTER TABLE dossiers ADD CONSTRAINT dossiers_statut_actuel_check
CHECK (statut_actuel::text = ANY (ARRAY[
    'EN_COURS'::text,
    'PROMESSE'::text,
    'INJOIGNABLE'::text,
    'REGULARISE'::text,
    'ALERTE'::text,
    'SUIVI_PROMESSE'::text,
    'SUIVI_INJOIGNABLE'::text,
    'CLOTURE'::text
]));

-- 4. Vérifier la nouvelle contrainte
SELECT conname, pg_get_constraintdef(oid)
FROM pg_constraint
WHERE conname = 'dossiers_statut_actuel_check';

-- 5. Faire pareil pour les tables d'archive
ALTER TABLE dossiers_archive DROP CONSTRAINT IF EXISTS dossiers_archive_statut_final_check;
ALTER TABLE dossiers_archive ADD CONSTRAINT dossiers_archive_statut_final_check
CHECK (statut_final::text = ANY (ARRAY[
    'EN_COURS'::text,
    'PROMESSE'::text,
    'INJOIGNABLE'::text,
    'REGULARISE'::text,
    'ALERTE'::text,
    'SUIVI_PROMESSE'::text,
    'SUIVI_INJOIGNABLE'::text,
    'CLOTURE'::text
]));

ALTER TABLE dossiers_mois_precedent DROP CONSTRAINT IF EXISTS dossiers_mois_precedent_statut_final_check;
ALTER TABLE dossiers_mois_precedent ADD CONSTRAINT dossiers_mois_precedent_statut_final_check
CHECK (statut_final::text = ANY (ARRAY[
    'EN_COURS'::text,
    'PROMESSE'::text,
    'INJOIGNABLE'::text,
    'REGULARISE'::text,
    'ALERTE'::text,
    'SUIVI_PROMESSE'::text,
    'SUIVI_INJOIGNABLE'::text,
    'CLOTURE'::text
]));

SELECT '✅ Contraintes CHECK mises à jour avec SUIVI_INJOIGNABLE' AS resultat;
