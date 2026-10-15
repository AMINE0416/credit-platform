-- ============================================
-- SCRIPT PGADMIN - SUPPRESSION DES ARCHIVES
-- À copier-coller directement dans pgAdmin
-- ============================================

-- ÉTAPE 1: Vérifier ce qui sera supprimé
SELECT 
    mois,
    annee,
    COUNT(*) as nb_dossiers_archives,
    MIN(date_archivage) as date_creation,
    MAX(date_archivage) as derniere_mise_a_jour
FROM dossier_historique 
GROUP BY mois, annee
ORDER BY annee DESC, mois DESC;

-- ÉTAPE 2: Supprimer TOUTES les archives et afficher le nombre
-- Sélectionnez et exécutez cette requête pour supprimer
WITH deleted AS (
    DELETE FROM dossier_historique 
    RETURNING id
)
SELECT COUNT(*) as "Archives supprimées" FROM deleted;

-- ÉTAPE 3: Vérifier que la table est vide
SELECT COUNT(*) as "Archives restantes" FROM dossier_historique;

-- ÉTAPE 4: Libérer l'espace disque (optionnel mais recommandé)
VACUUM FULL dossier_historique;
ANALYZE dossier_historique;
