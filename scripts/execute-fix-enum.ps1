# Script pour exécuter le fix de l'enum SUIVI_INJOIGNABLE
# Utilise psql pour se connecter à la base de données locale

$env:PGPASSWORD = "postgres123"
$dbName = "credit_platform"
$dbUser = "postgres"
$dbHost = "localhost"
$scriptPath = "fix_enum_suivi_injoignable.sql"

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "FIX ENUM SUIVI_INJOIGNABLE" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# Vérifier que psql est installé
try {
    $psqlVersion = psql --version
    Write-Host "✅ psql trouvé: $psqlVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ ERREUR: psql n'est pas installé ou pas dans le PATH" -ForegroundColor Red
    Write-Host "Installez PostgreSQL client ou ajoutez-le au PATH" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "Connexion à la base de données..." -ForegroundColor Yellow
Write-Host "  Database: $dbName" -ForegroundColor Gray
Write-Host "  User: $dbUser" -ForegroundColor Gray
Write-Host "  Host: $dbHost" -ForegroundColor Gray
Write-Host ""

# Exécuter le script SQL
try {
    psql -h $dbHost -U $dbUser -d $dbName -f $scriptPath
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "======================================" -ForegroundColor Green
        Write-Host "✅ FIX APPLIQUÉ AVEC SUCCÈS" -ForegroundColor Green
        Write-Host "======================================" -ForegroundColor Green
        Write-Host ""
        Write-Host "Vous pouvez maintenant relancer l'import Excel" -ForegroundColor Cyan
    } else {
        Write-Host ""
        Write-Host "❌ ERREUR lors de l'exécution du script" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host ""
    Write-Host "❌ ERREUR: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
} finally {
    # Nettoyer la variable d'environnement du mot de passe
    Remove-Item Env:\PGPASSWORD -ErrorAction SilentlyContinue
}
