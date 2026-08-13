-- ========================================
-- SCRIPT DE CORRECTION APRÈS TRUNCATE CASCADE
-- ========================================
-- Ce script répare les séquences et contraintes après un TRUNCATE CASCADE

-- 1. DIAGNOSTIC : Vérifier l'état des tables et séquences
\echo '=== DIAGNOSTIC ==='

-- Vérifier les séquences de dossiers_archive
SELECT 
    schemaname, 
    sequencename, 
    last_value, 
    is_called 
FROM pg_sequences 
WHERE sequencename LIKE '%dossiers_archive%';

-- Vérifier la structure de la table
\d dossiers_archive

-- Compter les enregistrements
SELECT COUNT(*) as nb_archives FROM dossiers_archive;

-- 2. RÉINITIALISER LES SÉQUENCES
\echo ''
\echo '=== RÉINITIALISATION DES SÉQUENCES ==='

-- Réinitialiser la séquence de la clé primaire
-- (Remplacer 'id' si le nom de la colonne est différent)
SELECT setval(
    pg_get_serial_sequence('dossiers_archive', 'id'), 
    COALESCE(MAX(id), 0) + 1, 
    false
) 
FROM dossiers_archive;

\echo 'Séquence réinitialisée ✓'

-- 3. VÉRIFIER LES CONTRAINTES
\echo ''
\echo '=== VÉRIFICATION DES CONTRAINTES ==='

SELECT 
    conname as constraint_name,
    contype as type,
    pg_get_constraintdef(oid) as definition
FROM pg_constraint
WHERE conrelid = 'dossiers_archive'::regclass
ORDER BY contype;

-- 4. SI LA TABLE A DES FOREIGN KEYS VERS D'AUTRES TABLES
-- Vérifier si les tables référencées existent encore
\echo ''
\echo '=== VÉRIFICATION DES TABLES LIÉES ==='

-- Vérifier si la table users existe
SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'users') as users_exists;

-- Vérifier si la table dossiers existe
SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'dossiers') as dossiers_exists;

-- 5. TEST D'INSERTION
\echo ''
\echo '=== TEST D'INSERTION ==='

-- Tester une insertion fictive (sera annulée)
BEGIN;

-- Essayer d'insérer un enregistrement de test
INSERT INTO dossiers_archive (
    numero_dossier,
    statut_final,
    date_archivage,
    date_creation,
    date_derniere_action
) VALUES (
    'TEST-TRUNCATE-FIX',
    'EN_COURS',
    NOW(),
    NOW(),
    NOW()
);

SELECT * FROM dossiers_archive WHERE numero_dossier = 'TEST-TRUNCATE-FIX';

ROLLBACK;

\echo ''
\echo '✓ Test d''insertion réussi (annulé)'
\echo ''
\echo '=== FIN DU DIAGNOSTIC ==='
\echo ''
\echo 'Si aucune erreur ci-dessus, l''import devrait fonctionner maintenant.'
\echo 'Si problème persiste, exécutez le script de recréation complet.'
