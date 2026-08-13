# ========================================
# SCRIPT DE CORRECTION APRÈS TRUNCATE
# ========================================

Write-Host "=== CORRECTION APRÈS TRUNCATE CASCADE ===" -ForegroundColor Cyan
Write-Host ""

# Charger les variables d'environnement
if (Test-Path ".env") {
    Get-Content ".env" | ForEach-Object {
        if ($_ -match "^([^#][^=]+)=(.*)$") {
            $key = $matches[1].Trim()
            $value = $matches[2].Trim()
            [Environment]::SetEnvironmentVariable($key, $value, "Process")
        }
    }
    Write-Host "✓ Variables d'environnement chargées" -ForegroundColor Green
} else {
    Write-Host "⚠ Fichier .env introuvable" -ForegroundColor Yellow
}

$DATABASE_URL = $env:DATABASE_URL

if (-not $DATABASE_URL) {
    Write-Host "❌ DATABASE_URL introuvable" -ForegroundColor Red
    Write-Host "Veuillez définir DATABASE_URL dans .env" -ForegroundColor Yellow
    exit 1
}

Write-Host "✓ DATABASE_URL trouvée" -ForegroundColor Green
Write-Host ""

# Exécuter le script de diagnostic
Write-Host "Exécution du diagnostic et correction..." -ForegroundColor Cyan
& psql $DATABASE_URL -f "scripts\fix_after_truncate.sql"

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✓ Correction terminée avec succès" -ForegroundColor Green
    Write-Host ""
    Write-Host "Vous pouvez maintenant réessayer l'import." -ForegroundColor Cyan
} else {
    Write-Host ""
    Write-Host "❌ Erreur lors de la correction" -ForegroundColor Red
    Write-Host "Code d'erreur: $LASTEXITCODE" -ForegroundColor Yellow
    exit 1
}
