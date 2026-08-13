-- ============================================
-- SUPPRESSION RAPIDE DE L'HISTORIQUE D'ARCHIVAGE
-- Copiez et collez ces commandes dans Railway Query
-- ============================================

-- 1. Voir combien il y a d'enregistrements
SELECT 
    'AVANT SUPPRESSION' as etape,
    (SELECT COUNT(*) FROM dossier_archive) as archives,
    (SELECT COUNT(*) FROM dossier_mois_precedent) as mois_precedent,
    (SELECT COUNT(*) FROM log_archivage) as logs;

-- 2. SUPPRIMER TOUT L'HISTORIQUE D'ARCHIVAGE
DELETE FROM dossier_archive;
DELETE FROM dossier_mois_precedent;
DELETE FROM log_archivage;

-- 3. Vérifier la suppression
SELECT 
    'APRÈS SUPPRESSION' as etape,
    (SELECT COUNT(*) FROM dossier_archive) as archives,
    (SELECT COUNT(*) FROM dossier_mois_precedent) as mois_precedent,
    (SELECT COUNT(*) FROM log_archivage) as logs;

-- 4. Récupérer l'espace disque
VACUUM FULL dossier_archive;
VACUUM FULL dossier_mois_precedent;
VACUUM FULL log_archivage;

-- ✅ TERMINÉ!
SELECT '✅ Historique d''archivage supprimé avec succès!' as resultat;
