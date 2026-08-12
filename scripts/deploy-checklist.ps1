# Script de vérification pré-déploiement

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "   Checklist de Déploiement - Credit Platform" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""

$allChecksPass = $true

# Test 1: Vérifier que Git est à jour
Write-Host "[1/8] Vérification du statut Git..." -ForegroundColor Yellow
$gitStatus = git status --porcelain
if ($gitStatus) {
    Write-Host "⚠ Il y a des modifications non committées" -ForegroundColor Yellow
    Write-Host "Voulez-vous les committer maintenant? (O/N)" -ForegroundColor Yellow
    $response = Read-Host
    if ($response -eq "O" -or $response -eq "o") {
        $message = Read-Host "Message du commit"
        git add .
        git commit -m "$message"
        Write-Host "✓ Modifications committées" -ForegroundColor Green
    } else {
        Write-Host "⚠ Déploiement sans commit" -ForegroundColor Yellow
    }
} else {
    Write-Host "✓ Repository à jour" -ForegroundColor Green
}

# Test 2: Vérifier le build backend
Write-Host "`n[2/8] Test du build backend..." -ForegroundColor Yellow
$backendBuild = & .\mvnw clean package -DskipTests 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Backend build réussi" -ForegroundColor Green
} else {
    Write-Host "✗ Backend build échoué" -ForegroundColor Red
    Write-Host "Erreur: $backendBuild" -ForegroundColor Red
    $allChecksPass = $false
}

# Test 3: Vérifier le build frontend
Write-Host "`n[3/8] Test du build frontend..." -ForegroundColor Yellow
Set-Location frontend
$frontendBuild = npm run build 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Frontend build réussi" -ForegroundColor Green
} else {
    Write-Host "✗ Frontend build échoué" -ForegroundColor Red
    Write-Host "Erreur: $frontendBuild" -ForegroundColor Red
    $allChecksPass = $false
}
Set-Location ..

# Test 4: Vérifier les variables d'environnement backend
Write-Host "`n[4/8] Vérification des variables d'environnement backend..." -ForegroundColor Yellow
$envFile = Get-Content .env -ErrorAction SilentlyContinue
$requiredVars = @("DATABASE_URL", "JWT_SECRET", "CORS_ALLOWED_ORIGINS")
$missingVars = @()

foreach ($var in $requiredVars) {
    if (-not ($envFile -match "^$var=.+")) {
        $missingVars += $var
    }
}

if ($missingVars.Count -eq 0) {
    Write-Host "✓ Variables d'environnement backend OK" -ForegroundColor Green
} else {
    Write-Host "⚠ Variables manquantes: $($missingVars -join ', ')" -ForegroundColor Yellow
    Write-Host "  Assurez-vous qu'elles sont définies dans Railway" -ForegroundColor Gray
}

# Test 5: Vérifier les variables d'environnement frontend
Write-Host "`n[5/8] Vérification des variables d'environnement frontend..." -ForegroundColor Yellow
$frontendEnv = Get-Content frontend\.env.production -ErrorAction SilentlyContinue
if ($frontendEnv -match "^VITE_API_URL=.+") {
    Write-Host "✓ Variables d'environnement frontend OK" -ForegroundColor Green
} else {
    Write-Host "⚠ VITE_API_URL manquante" -ForegroundColor Yellow
    Write-Host "  Assurez-vous qu'elle est définie dans Vercel" -ForegroundColor Gray
}

# Test 6: Vérifier la taille du build frontend
Write-Host "`n[6/8] Vérification de la taille du build..." -ForegroundColor Yellow
if (Test-Path "frontend\dist") {
    $distSize = (Get-ChildItem frontend\dist -Recurse | Measure-Object -Property Length -Sum).Sum / 1MB
    $distSize = [math]::Round($distSize, 2)
    Write-Host "Taille du build: $distSize MB" -ForegroundColor Cyan
    
    if ($distSize -lt 5) {
        Write-Host "✓ Taille optimale (<5MB)" -ForegroundColor Green
    } elseif ($distSize -lt 10) {
        Write-Host "✓ Taille acceptable (<10MB)" -ForegroundColor Green
    } else {
        Write-Host "⚠ Build volumineux (>10MB)" -ForegroundColor Yellow
    }
} else {
    Write-Host "⚠ Dossier dist non trouvé" -ForegroundColor Yellow
}

# Test 7: Vérifier les fichiers critiques
Write-Host "`n[7/8] Vérification des fichiers critiques..." -ForegroundColor Yellow
$criticalFiles = @(
    "Dockerfile",
    "pom.xml",
    "frontend\package.json",
    "frontend\vite.config.ts",
    "src\main\resources\application-prod.properties"
)

$missingFiles = @()
foreach ($file in $criticalFiles) {
    if (-not (Test-Path $file)) {
        $missingFiles += $file
    }
}

if ($missingFiles.Count -eq 0) {
    Write-Host "✓ Tous les fichiers critiques présents" -ForegroundColor Green
} else {
    Write-Host "✗ Fichiers manquants: $($missingFiles -join ', ')" -ForegroundColor Red
    $allChecksPass = $false
}

# Test 8: Vérifier la connexion Internet
Write-Host "`n[8/8] Vérification de la connexion..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "https://www.google.com" -UseBasicParsing -TimeoutSec 5
    Write-Host "✓ Connexion Internet OK" -ForegroundColor Green
} catch {
    Write-Host "✗ Pas de connexion Internet" -ForegroundColor Red
    $allChecksPass = $false
}

# Résumé
Write-Host "`n==================================================" -ForegroundColor Cyan
Write-Host "              RÉSUMÉ" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan

if ($allChecksPass) {
    Write-Host "✓ Tous les tests sont passés!" -ForegroundColor Green
    Write-Host "`nVous pouvez déployer:" -ForegroundColor Green
    Write-Host "  1. git push origin main" -ForegroundColor Gray
    Write-Host "  2. Attendre le déploiement automatique" -ForegroundColor Gray
    Write-Host "  3. Exécuter .\scripts\test-performance.ps1" -ForegroundColor Gray
} else {
    Write-Host "✗ Certains tests ont échoué" -ForegroundColor Red
    Write-Host "Veuillez corriger les erreurs avant de déployer" -ForegroundColor Yellow
}

Write-Host "`n==================================================" -ForegroundColor Cyan

# Demander si on veut pousser
if ($allChecksPass) {
    Write-Host "`nVoulez-vous pousser les modifications maintenant? (O/N)" -ForegroundColor Yellow
    $pushResponse = Read-Host
    if ($pushResponse -eq "O" -or $pushResponse -eq "o") {
        Write-Host "`nPush en cours..." -ForegroundColor Cyan
        git push origin main
        Write-Host "✓ Push terminé!" -ForegroundColor Green
        Write-Host "`nSuivez le déploiement sur:" -ForegroundColor Cyan
        Write-Host "  - Railway: https://railway.app/dashboard" -ForegroundColor Gray
        Write-Host "  - Vercel: https://vercel.com/dashboard" -ForegroundColor Gray
    }
}

Write-Host "`nAppuyez sur une touche pour terminer..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
