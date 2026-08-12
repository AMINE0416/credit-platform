# ================================================================
# SCRIPT DE TEST AUTOMATIQUE COMPLET
# Système d'Archivage Mensuel
# ================================================================

param(
    [string]$DbPassword = "",
    [string]$DbUser = "postgres",
    [string]$DbName = "credit_platform",
    [string]$DbHost = "localhost",
    [int]$DbPort = 5432
)

# Couleurs pour l'affichage
function Write-Success { Write-Host $args -ForegroundColor Green }
function Write-Error { Write-Host $args -ForegroundColor Red }
function Write-Info { Write-Host $args -ForegroundColor Cyan }
function Write-Warning { Write-Host $args -ForegroundColor Yellow }

# Bannière
Write-Host ""
Write-Host "========================================" -ForegroundColor Magenta
Write-Host "  TEST AUTOMATIQUE - ARCHIVAGE MENSUEL" -ForegroundColor Magenta
Write-Host "========================================" -ForegroundColor Magenta
Write-Host ""

# Vérifier le mot de passe
if ([string]::IsNullOrEmpty($DbPassword)) {
    $securePassword = Read-Host "Mot de passe PostgreSQL" -AsSecureString
    $BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
    $DbPassword = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)
}

# Définir les variables d'environnement
$env:PGPASSWORD = $DbPassword
$env:PGUSER = $DbUser
$env:PGDATABASE = $DbName
$env:PGHOST = $DbHost
$env:PGPORT = $DbPort

# Fonction pour exécuter une commande SQL
function Invoke-SqlCommand {
    param(
        [string]$Query,
        [string]$File = ""
    )
    
    try {
        if ($File) {
            psql -f $File 2>&1
        } else {
            psql -c $Query 2>&1
        }
        
        if ($LASTEXITCODE -eq 0) {
            return $true
        } else {
            return $false
        }
    } catch {
        Write-Error "Erreur SQL: $_"
        return $false
    }
}

# ================================================================
# PHASE 1 : VÉRIFICATION ENVIRONNEMENT
# ================================================================

Write-Info "📋 Phase 1 : Vérification de l'environnement"
Write-Host "---"

# Vérifier PostgreSQL
Write-Host "Vérification PostgreSQL... " -NoNewline
$pgVersion = psql --version 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Success "✅ OK ($pgVersion)"
} else {
    Write-Error "❌ PostgreSQL non trouvé"
    exit 1
}

# Vérifier la connexion à la base
Write-Host "Connexion à la base '$DbName'... " -NoNewline
$result = Invoke-SqlCommand -Query "SELECT version();"
if ($result) {
    Write-Success "✅ Connecté"
} else {
    Write-Error "❌ Impossible de se connecter"
    exit 1
}

Write-Host ""

# ================================================================
# PHASE 2 : CRÉATION DES TABLES
# ================================================================

Write-Info "🏗️  Phase 2 : Création des tables SQL"
Write-Host "---"

Write-Host "Exécution du script create_archivage_system.sql... " -NoNewline
$result = Invoke-SqlCommand -File "scripts\create_archivage_system.sql"
if ($result) {
    Write-Success "✅ Tables créées"
} else {
    Write-Warning "⚠️  Erreur (les tables existent peut-être déjà)"
}

# Vérifier les tables
Write-Host "Vérification des tables... " -NoNewline
$tableCheck = psql -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name IN ('dossiers_archive', 'dossiers_mois_precedent', 'logs_archivage');" 2>&1
$tableCount = [int]($tableCheck.Trim())

if ($tableCount -eq 3) {
    Write-Success "✅ 3 tables présentes"
} else {
    Write-Error "❌ Tables manquantes (trouvé: $tableCount/3)"
    exit 1
}

Write-Host ""

# ================================================================
# PHASE 3 : TEST MAVEN
# ================================================================

Write-Info "☕ Phase 3 : Test compilation Maven"
Write-Host "---"

Write-Host "Compilation du projet... "
$mvnOutput = mvn clean compile -DskipTests 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Success "✅ BUILD SUCCESS"
} else {
    Write-Error "❌ Erreur de compilation"
    Write-Host $mvnOutput
    exit 1
}

Write-Host ""

# ================================================================
# PHASE 4 : TESTS SQL COMPLETS
# ================================================================

Write-Info "🧪 Phase 4 : Tests SQL complets"
Write-Host "---"

Write-Host "Exécution du test complet d'archivage... "
$result = Invoke-SqlCommand -File "scripts\test_complet_archivage.sql"

if ($result) {
    Write-Success "✅ Tests SQL réussis"
} else {
    Write-Warning "⚠️  Certains tests ont échoué"
}

Write-Host ""

# ================================================================
# PHASE 5 : VÉRIFICATIONS FINALES
# ================================================================

Write-Info "📊 Phase 5 : Vérifications finales"
Write-Host "---"

# Compter les archives mois 7
$archives = psql -t -c "SELECT COUNT(*) FROM dossiers_archive WHERE mois_archive = '2024-07';" 2>&1
$archivesCount = [int]($archives.Trim())
Write-Host "Archives mois 7: " -NoNewline
if ($archivesCount -eq 5) {
    Write-Success "✅ $archivesCount (attendu: 5)"
} else {
    Write-Error "❌ $archivesCount (attendu: 5)"
}

# Compter les dossiers mois 7 (devrait être 0)
$dossiers7 = psql -t -c "SELECT COUNT(*) FROM dossiers WHERE mois_import = '2024-07';" 2>&1
$dossiers7Count = [int]($dossiers7.Trim())
Write-Host "Dossiers actifs mois 7: " -NoNewline
if ($dossiers7Count -eq 0) {
    Write-Success "✅ $dossiers7Count (attendu: 0)"
} else {
    Write-Error "❌ $dossiers7Count (attendu: 0)"
}

# Compter les dossiers mois 8
$dossiers8 = psql -t -c "SELECT COUNT(*) FROM dossiers WHERE mois_import = '2024-08';" 2>&1
$dossiers8Count = [int]($dossiers8.Trim())
Write-Host "Dossiers actifs mois 8: " -NoNewline
if ($dossiers8Count -eq 5) {
    Write-Success "✅ $dossiers8Count (attendu: 5)"
} else {
    Write-Error "❌ $dossiers8Count (attendu: 5)"
}

# Compter les récurrents mois 8
$recurrents = psql -t -c "SELECT COUNT(*) FROM dossiers WHERE mois_import = '2024-08' AND est_recurrent = true;" 2>&1
$recurrentsCount = [int]($recurrents.Trim())
Write-Host "Récurrents mois 8: " -NoNewline
if ($recurrentsCount -eq 3) {
    Write-Success "✅ $recurrentsCount (attendu: 3)"
} else {
    Write-Error "❌ $recurrentsCount (attendu: 3)"
}

# Compter les nouveaux mois 8
$nouveaux = psql -t -c "SELECT COUNT(*) FROM dossiers WHERE mois_import = '2024-08' AND est_recurrent = false;" 2>&1
$nouveauxCount = [int]($nouveaux.Trim())
Write-Host "Nouveaux mois 8: " -NoNewline
if ($nouveauxCount -eq 2) {
    Write-Success "✅ $nouveauxCount (attendu: 2)"
} else {
    Write-Error "❌ $nouveauxCount (attendu: 2)"
}

# Compter les logs SUCCESS
$logs = psql -t -c "SELECT COUNT(*) FROM logs_archivage WHERE statut = 'SUCCESS';" 2>&1
$logsCount = [int]($logs.Trim())
Write-Host "Logs SUCCESS: " -NoNewline
if ($logsCount -ge 3) {
    Write-Success "✅ $logsCount (attendu: 3+)"
} else {
    Write-Error "❌ $logsCount (attendu: 3+)"
}

Write-Host ""

# ================================================================
# PHASE 6 : AFFICHAGE DES TRANSITIONS
# ================================================================

Write-Info "📈 Phase 6 : Détails des transitions"
Write-Host "---"

Write-Host ""
psql -c "SELECT 
    cr.reference_contrat,
    CASE WHEN d.est_recurrent THEN '🔄 RÉCURRENT' ELSE '🆕 NOUVEAU' END as type,
    COALESCE(d.mois_precedent_statut, 'N/A') as ancien_statut,
    d.statut_actuel::text as nouveau_statut
FROM dossiers d
JOIN credits cr ON d.credit_id = cr.id
WHERE d.mois_import = '2024-08'
ORDER BY d.est_recurrent DESC, cr.reference_contrat;"

Write-Host ""

# ================================================================
# RÉSULTAT FINAL
# ================================================================

Write-Host "========================================" -ForegroundColor Magenta
Write-Host "  📊 RÉSULTAT FINAL" -ForegroundColor Magenta
Write-Host "========================================" -ForegroundColor Magenta
Write-Host ""

$totalTests = 6
$testsReussis = 0

if ($archivesCount -eq 5) { $testsReussis++ }
if ($dossiers7Count -eq 0) { $testsReussis++ }
if ($dossiers8Count -eq 5) { $testsReussis++ }
if ($recurrentsCount -eq 3) { $testsReussis++ }
if ($nouveauxCount -eq 2) { $testsReussis++ }
if ($logsCount -ge 3) { $testsReussis++ }

Write-Host "Tests réussis: $testsReussis/$totalTests" -ForegroundColor $(if ($testsReussis -eq $totalTests) { "Green" } else { "Yellow" })

if ($testsReussis -eq $totalTests) {
    Write-Host ""
    Write-Success "✅ TOUS LES TESTS SONT PASSÉS !"
    Write-Host ""
    Write-Success "🚀 Le système d'archivage est opérationnel"
    Write-Host ""
    Write-Info "📋 Prochaines étapes:"
    Write-Host "  1. Démarrer l'application: mvn spring-boot:run"
    Write-Host "  2. Tester les APIs REST (voir PLAN_TEST_FINAL_SANS_EMAIL.md)"
    Write-Host "  3. Tester l'interface web /admin/gestion-mensuelle"
    Write-Host ""
} else {
    Write-Host ""
    Write-Warning "⚠️  Certains tests ont échoué"
    Write-Host ""
    Write-Info "Vérifiez les erreurs ci-dessus et consultez PLAN_TEST_FINAL_SANS_EMAIL.md"
    Write-Host ""
}

# Nettoyer les variables d'environnement
Remove-Item Env:\PGPASSWORD -ErrorAction SilentlyContinue
Remove-Item Env:\PGUSER -ErrorAction SilentlyContinue
Remove-Item Env:\PGDATABASE -ErrorAction SilentlyContinue
Remove-Item Env:\PGHOST -ErrorAction SilentlyContinue
Remove-Item Env:\PGPORT -ErrorAction SilentlyContinue

Write-Host "========================================" -ForegroundColor Magenta
Write-Host ""
