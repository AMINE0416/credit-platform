# ===============================================
# Script : Compiler et Démarrer l'Application
# ===============================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Compilation et Démarrage" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Se placer dans le répertoire du projet
Set-Location -Path "c:\Users\asus\Documents\credit-platform"

Write-Host "📁 Répertoire actuel : $(Get-Location)" -ForegroundColor Yellow
Write-Host ""

# Étape 1 : Clean et Install
Write-Host "🧹 Étape 1/2 : Nettoyage et compilation..." -ForegroundColor Green
Write-Host "Commande : mvn clean install -DskipTests" -ForegroundColor Gray
Write-Host ""

mvn clean install -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "❌ ERREUR lors de la compilation !" -ForegroundColor Red
    Write-Host "Vérifiez les erreurs ci-dessus." -ForegroundColor Red
    Write-Host ""
    Read-Host "Appuyez sur Entrée pour quitter"
    exit 1
}

Write-Host ""
Write-Host "✅ Compilation réussie !" -ForegroundColor Green
Write-Host ""

# Étape 2 : Démarrer l'application
Write-Host "🚀 Étape 2/2 : Démarrage de l'application..." -ForegroundColor Green
Write-Host "Commande : mvn spring-boot:run" -ForegroundColor Gray
Write-Host ""
Write-Host "⏳ L'application va démarrer..." -ForegroundColor Yellow
Write-Host "   Pour arrêter, appuyez sur Ctrl+C" -ForegroundColor Yellow
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

mvn spring-boot:run
