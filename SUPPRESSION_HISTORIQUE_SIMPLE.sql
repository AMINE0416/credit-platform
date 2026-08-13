-- ============================================
-- SUPPRESSION DE L'HISTORIQUE EXISTANT
-- Pour la base de données de production Railway
-- ============================================

-- 1. Voir combien il y a d'enregistrements dans l'historique
SELECT 
    'Historique actuel' as info,
    COUNT(*) as nombre_lignes
FROM historique_dossier;

-- 2. SUPPRIMER TOUT L'HISTORIQUE DES DOSSIERS
-- ⚠️ Cette commande va supprimer toutes les modifications enregistrées
DELETE FROM historique_dossier;

-- 3. Vérifier la suppression
SELECT 
    'Après suppression' as info,
    COUNT(*) as nombre_lignes
FROM historique_dossier;

-- 4. Récupérer l'espace disque
VACUUM FULL historique_dossier;

-- 5. Mettre à jour les statistiques
ANALYZE historique_dossier;

-- ✅ TERMINÉ!
SELECT '✅ Historique supprimé avec succès!' as resultat;
