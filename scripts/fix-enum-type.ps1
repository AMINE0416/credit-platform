# ================================================================
# SCRIPT DE CORRECTION - Type ENUM statut_dossier
# ================================================================

param(
    [string]$DbPassword = "",
    [string]$DbUser = "postgres",
    [string]$DbName = "credit_platform"
)

# Couleurs
function Write-Success { Write-Host $args -ForegroundColor Green }
function Write-Error { Write-Host $args -ForegroundColor Red }
function Write-Info { Write-Host $args -ForegroundColor Cyan }
function Write-Warning { Write-Host $args -ForegroundColor Yellow }

Write-Host ""
Write-Host "========================================" -ForegroundColor Magenta
Write-Host "  CORRECTION TYPE ENUM statut_dossier" -ForegroundColor Magenta
Write-Host "========================================" -ForegroundColor Magenta
Write-Host ""

# Demander le mot de passe si non fourni
if ([string]::IsNullOrEmpty($DbPassword)) {
    $securePassword = Read-Host "Mot de passe PostgreSQL" -AsSecureString
    $BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
    $DbPassword = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)
}

# Configurer l'environnement
$env:PGPASSWORD = $DbPassword
$env:PGUSER = $DbUser
$env:PGDATABASE = $DbName

Write-Info "📋 Étape 1 : Vérification de la connexion"
Write-Host "---"

$connectionTest = psql -c "SELECT version();" 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Error "❌ Impossible de se connecter à la base de données"
    Write-Host $connectionTest
    exit 1
}
Write-Success "✅ Connexion OK"
Write-Host ""

Write-Info "📋 Étape 2 : Vérification du type ENUM"
Write-Host "---"

$enumCheck = psql -t -c "SELECT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'statut_dossier');" 2>&1
$enumExists = $enumCheck.Trim()

if ($enumExists -eq "t") {
    Write-Success "✅ Type ENUM statut_dossier existe déjà"
} else {
    Write-Warning "⚠️  Type ENUM statut_dossier n'existe pas"
    Write-Host "Création du type ENUM... " -NoNewline
    
    $createEnum = psql -f "scripts\create_enum_statut.sql" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Success "✅ Créé"
    } else {
        Write-Error "❌ Erreur lors de la création"
        Write-Host $createEnum
        exit 1
    }
}
Write-Host ""

Write-Info "📋 Étape 3 : Vérification de la colonne statut_actuel"
Write-Host "---"

$columnType = psql -t -c "SELECT udt_name FROM information_schema.columns WHERE table_name = 'dossiers' AND column_name = 'statut_actuel';" 2>&1
$columnType = $columnType.Trim()

Write-Host "Type actuel de la colonne: " -NoNewline
if ($columnType -eq "statut_dossier") {
    Write-Success "statut_dossier (ENUM) ✅"
    Write-Success "✅ Aucune conversion nécessaire"
} elseif ($columnType -eq "varchar") {
    Write-Warning "varchar ⚠️"
    Write-Host ""
    Write-Warning "La colonne est de type VARCHAR. Souhaitez-vous la convertir en ENUM ?"
    Write-Host "Ceci nécessite que toutes les valeurs actuelles soient valides." -ForegroundColor Yellow
    Write-Host ""
    
    # Afficher les valeurs actuelles
    Write-Host "Valeurs actuelles dans la colonne:" -ForegroundColor Cyan
    psql -c "SELECT DISTINCT statut_actuel FROM dossiers ORDER BY statut_actuel;"
    
    Write-Host ""
    $response = Read-Host "Convertir en ENUM ? (o/N)"
    
    if ($response -eq "o" -or $response -eq "O") {
        Write-Host "Conversion en cours... " -NoNewline
        
        $convert = psql -c "ALTER TABLE dossiers ALTER COLUMN statut_actuel TYPE statut_dossier USING statut_actuel::statut_dossier;" 2>&1
        
        if ($LASTEXITCODE -eq 0) {
            Write-Success "✅ Converti"
        } else {
            Write-Error "❌ Erreur lors de la conversion"
            Write-Host $convert
            Write-Host ""
            Write-Warning "Possible raison: des valeurs dans la colonne ne correspondent pas aux valeurs de l'ENUM"
            Write-Host "Consultez CORRECTION_TYPE_ENUM.md pour plus d'informations" -ForegroundColor Yellow
            exit 1
        }
    } else {
        Write-Info "ℹ️  Conversion annulée. La colonne reste en VARCHAR."
        Write-Info "ℹ️  Les scripts s'adapteront automatiquement."
    }
} else {
    Write-Warning "⚠️  Type inconnu: $columnType"
}
Write-Host ""

Write-Info "📋 Étape 4 : Vérification finale"
Write-Host "---"

# Afficher la configuration finale
Write-Host "Configuration finale:" -ForegroundColor Cyan
psql -c "SELECT 
    'Type ENUM' as element,
    CASE WHEN EXISTS (SELECT 1 FROM pg_type WHERE typname = 'statut_dossier') 
        THEN '✅ Existe' 
        ELSE '❌ N''existe pas' 
    END as statut
UNION ALL
SELECT 
    'Colonne statut_actuel',
    udt_name
FROM information_schema.columns 
WHERE table_name = 'dossiers' AND column_name = 'statut_actuel';"

Write-Host ""

Write-Host "========================================" -ForegroundColor Magenta
Write-Host "  ✅ CORRECTION TERMINÉE" -ForegroundColor Magenta
Write-Host "========================================" -ForegroundColor Magenta
Write-Host ""

Write-Success "🚀 Vous pouvez maintenant exécuter les tests:"
Write-Host ""
Write-Host "  .\scripts\test-complet-automatique.ps1" -ForegroundColor White
Write-Host ""
Write-Host "Ou consulter le guide complet:" -ForegroundColor Cyan
Write-Host "  PLAN_TEST_FINAL_SANS_EMAIL.md" -ForegroundColor White
Write-Host ""

# Nettoyer
Remove-Item Env:\PGPASSWORD -ErrorAction SilentlyContinue
Remove-Item Env:\PGUSER -ErrorAction SilentlyContinue
Remove-Item Env:\PGDATABASE -ErrorAction SilentlyContinue
