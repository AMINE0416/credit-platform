# ================================================================
# Script PowerShell : Correction de la contrainte dossiers_statut_actuel_check
# ================================================================
# Description : Exécute le script SQL de correction de la contrainte
# Usage : .\scripts\fix-statut-constraint.ps1
# ================================================================

Write-Host "`n================================================" -ForegroundColor Cyan
Write-Host "  CORRECTION CONTRAINTE STATUT DOSSIER" -ForegroundColor Cyan
Write-Host "================================================`n" -ForegroundColor Cyan

# Vérifier que le script SQL existe
$scriptPath = "scripts\fix_statut_constraint.sql"
if (-not (Test-Path $scriptPath)) {
    Write-Host "❌ Erreur : Le fichier $scriptPath n'existe pas" -ForegroundColor Red
    exit 1
}

# Demander les informations de connexion
Write-Host "📋 Informations de connexion PostgreSQL :" -ForegroundColor Yellow
$dbHost = Read-Host "Host (défaut: localhost)"
if ([string]::IsNullOrWhiteSpace($dbHost)) { $dbHost = "localhost" }

$dbPort = Read-Host "Port (défaut: 5432)"
if ([string]::IsNullOrWhiteSpace($dbPort)) { $dbPort = "5432" }

$dbName = Read-Host "Nom de la base de données (défaut: credit_platform)"
if ([string]::IsNullOrWhiteSpace($dbName)) { $dbName = "credit_platform" }

$dbUser = Read-Host "Utilisateur (défaut: postgres)"
if ([string]::IsNullOrWhiteSpace($dbUser)) { $dbUser = "postgres" }

Write-Host "`n🔄 Exécution du script de correction..." -ForegroundColor Yellow

# Construire la commande psql
$env:PGPASSWORD = Read-Host "Mot de passe" -AsSecureString | ConvertFrom-SecureString -AsPlainText

$psqlCommand = "psql -h $dbHost -p $dbPort -U $dbUser -d $dbName -f $scriptPath"

try {
    # Exécuter le script
    $result = Invoke-Expression $psqlCommand 2>&1
    
    # Afficher le résultat
    Write-Host "`n$result" -ForegroundColor White
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n✅ Correction appliquée avec succès !" -ForegroundColor Green
        Write-Host "`n📝 Prochaines étapes :" -ForegroundColor Cyan
        Write-Host "   1. Relancer l'application Spring Boot si elle est en cours d'exécution" -ForegroundColor White
        Write-Host "   2. Réessayer l'import du fichier Excel" -ForegroundColor White
        Write-Host "   3. Vérifier que les dossiers sont créés avec le statut EN_ATTENTE" -ForegroundColor White
    } else {
        Write-Host "`n❌ Erreur lors de l'exécution du script" -ForegroundColor Red
        Write-Host "Vérifiez les informations de connexion et réessayez" -ForegroundColor Yellow
    }
} catch {
    Write-Host "`n❌ Erreur : $_" -ForegroundColor Red
    Write-Host "`nAssurez-vous que psql est installé et accessible dans le PATH" -ForegroundColor Yellow
} finally {
    # Nettoyer le mot de passe de l'environnement
    Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
}

Write-Host "`n================================================`n" -ForegroundColor Cyan
