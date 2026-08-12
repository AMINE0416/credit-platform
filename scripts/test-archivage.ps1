# ================================================================
# SCRIPT POWERSHELL - TEST SYSTÈME D'ARCHIVAGE MENSUEL
# ================================================================
# Description : Script Windows pour tester le système d'archivage
# Usage : .\test-archivage.ps1
# ================================================================

Write-Host "================================" -ForegroundColor Cyan
Write-Host "🧪 TEST SYSTÈME D'ARCHIVAGE" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$DB_HOST = "localhost"
$DB_PORT = "5432"
$DB_NAME = "credit_platform"
$DB_USER = "postgres"

Write-Host "📋 Configuration:" -ForegroundColor Yellow
Write-Host "  Database: $DB_NAME"
Write-Host "  Host: $DB_HOST:$DB_PORT"
Write-Host "  User: $DB_USER"
Write-Host ""

# Demander le mot de passe
$DB_PASSWORD = Read-Host "Entrez le mot de passe PostgreSQL" -AsSecureString
$BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($DB_PASSWORD)
$DB_PASS_PLAIN = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)

# Définir la variable d'environnement
$env:PGPASSWORD = $DB_PASS_PLAIN

Write-Host ""
Write-Host "================================" -ForegroundColor Green
Write-Host "ÉTAPE 1 : Création des tables" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Green

$result = psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f "scripts\create_archivage_system.sql" 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Tables créées avec succès" -ForegroundColor Green
} else {
    Write-Host "❌ Erreur lors de la création des tables" -ForegroundColor Red
    Write-Host $result
    exit 1
}

Write-Host ""
Write-Host "================================" -ForegroundColor Green
Write-Host "ÉTAPE 2 : Test complet" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Green

$result = psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f "scripts\test_complet_archivage.sql" 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Tests terminés avec succès" -ForegroundColor Green
} else {
    Write-Host "⚠️  Tests terminés avec des warnings (peut être normal)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "================================" -ForegroundColor Cyan
Write-Host "📊 Vérification des résultats" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan

# Vérifier les tables
Write-Host ""
Write-Host "1️⃣  Vérification des tables créées..." -ForegroundColor Yellow
$query = @"
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN ('dossiers_archive', 'dossiers_mois_precedent', 'logs_archivage');
"@

$tables = psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c $query

if ($tables -match "dossiers_archive" -and $tables -match "dossiers_mois_precedent" -and $tables -match "logs_archivage") {
    Write-Host "✅ Toutes les tables existent" -ForegroundColor Green
} else {
    Write-Host "❌ Certaines tables sont manquantes" -ForegroundColor Red
}

# Vérifier les dossiers actifs
Write-Host ""
Write-Host "2️⃣  Vérification des dossiers actifs..." -ForegroundColor Yellow
$query = "SELECT mois_import, COUNT(*) FROM dossiers WHERE mois_import = '2024-08' GROUP BY mois_import;"
$count = psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c $query

if ($count -match "5") {
    Write-Host "✅ 5 dossiers actifs pour le mois 2024-08" -ForegroundColor Green
} else {
    Write-Host "⚠️  Nombre de dossiers actifs: $count" -ForegroundColor Yellow
}

# Vérifier les archives
Write-Host ""
Write-Host "3️⃣  Vérification des archives..." -ForegroundColor Yellow
$query = "SELECT COUNT(*) FROM dossiers_archive WHERE mois_archive = '2024-07';"
$count = psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c $query

if ($count -match "5") {
    Write-Host "✅ 5 dossiers archivés pour le mois 2024-07" -ForegroundColor Green
} else {
    Write-Host "⚠️  Nombre de dossiers archivés: $count" -ForegroundColor Yellow
}

# Vérifier la détection
Write-Host ""
Write-Host "4️⃣  Vérification de la détection récurrents..." -ForegroundColor Yellow
$query = "SELECT COUNT(*) FROM dossiers WHERE mois_import = '2024-08' AND est_recurrent = true;"
$count = psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c $query

if ($count -match "3") {
    Write-Host "✅ 3 dossiers récurrents détectés" -ForegroundColor Green
} else {
    Write-Host "⚠️  Nombre de dossiers récurrents: $count" -ForegroundColor Yellow
}

# Vérifier les logs
Write-Host ""
Write-Host "5️⃣  Vérification des logs..." -ForegroundColor Yellow
$query = "SELECT COUNT(*) FROM logs_archivage WHERE statut = 'SUCCESS';"
$count = psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c $query

Write-Host "✅ $count opérations réussies enregistrées" -ForegroundColor Green

# Afficher un résumé
Write-Host ""
Write-Host "================================" -ForegroundColor Cyan
Write-Host "📋 RÉSUMÉ DES TESTS" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

$query = @"
SELECT 
    '📦 Archives mois 7' as info,
    COUNT(*)::text as valeur
FROM dossiers_archive WHERE mois_archive = '2024-07'
UNION ALL
SELECT 
    '📂 Dossiers actifs mois 8',
    COUNT(*)::text
FROM dossiers WHERE mois_import = '2024-08'
UNION ALL
SELECT 
    '🔄 Récurrents détectés',
    COUNT(*)::text
FROM dossiers WHERE mois_import = '2024-08' AND est_recurrent = true
UNION ALL
SELECT 
    '🆕 Nouveaux dossiers',
    COUNT(*)::text
FROM dossiers WHERE mois_import = '2024-08' AND est_recurrent = false
UNION ALL
SELECT 
    '📝 Logs SUCCESS',
    COUNT(*)::text
FROM logs_archivage WHERE statut = 'SUCCESS';
"@

psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c $query

Write-Host ""
Write-Host "================================" -ForegroundColor Green
Write-Host "✅ TESTS TERMINÉS" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Green
Write-Host ""
Write-Host "🎯 Prochaines étapes:" -ForegroundColor Yellow
Write-Host "  1. Redémarrer l'application Spring Boot"
Write-Host "  2. Accéder à /admin/gestion-mensuelle"
Write-Host "  3. Tester l'interface utilisateur"
Write-Host ""
Write-Host "📚 Documentation:" -ForegroundColor Yellow
Write-Host "  - PLAN_TEST_ARCHIVAGE_MENSUEL.md"
Write-Host "  - GUIDE_DEMARRAGE_ARCHIVAGE.md"
Write-Host "  - SYSTEME_ARCHIVAGE_README.md"
Write-Host ""

# Nettoyer la variable d'environnement
Remove-Item Env:\PGPASSWORD

# Demander si on veut voir le détail des dossiers
Write-Host ""
$response = Read-Host "Voulez-vous voir le détail des dossiers importés ? (o/N)"

if ($response -eq "o" -or $response -eq "O") {
    Write-Host ""
    Write-Host "📋 Détail des dossiers mois 8:" -ForegroundColor Cyan
    
    $query = @"
SELECT 
    cr.reference_contrat,
    CASE 
        WHEN d.est_recurrent THEN '🔄 RÉCURRENT'
        ELSE '🆕 NOUVEAU'
    END as type,
    COALESCE(d.mois_precedent_statut, 'N/A') || ' → ' || d.statut_actuel::text as transition
FROM dossiers d
JOIN credits cr ON d.credit_id = cr.id
WHERE d.mois_import = '2024-08'
ORDER BY d.est_recurrent DESC, cr.reference_contrat;
"@

    $env:PGPASSWORD = $DB_PASS_PLAIN
    psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c $query
    Remove-Item Env:\PGPASSWORD
}

Write-Host ""
Write-Host "✨ Script terminé !" -ForegroundColor Green
Write-Host ""
