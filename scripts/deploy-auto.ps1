# Script de Déploiement Automatisé Complet
# Credit Platform - Déploiement avec Optimisations

param(
    [switch]$SkipTests,
    [switch]$SkipDBOptimization,
    [switch]$Force
)

$ErrorActionPreference = "Stop"

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "   Déploiement Automatisé - Credit Platform" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""

# Variables
$startTime = Get-Date
$deploymentSuccess = $true

# Fonction pour afficher les étapes
function Write-Step {
    param($step, $message)
    Write-Host "`n[$step] $message" -ForegroundColor Yellow
}

function Write-Success {
    param($message)
    Write-Host "✓ $message" -ForegroundColor Green
}

function Write-Error {
    param($message)
    Write-Host "✗ $message" -ForegroundColor Red
}

function Write-Warning {
    param($message)
    Write-Host "⚠ $message" -ForegroundColor Yellow
}

# ====================================
# ÉTAPE 1: Vérifications préliminaires
# ====================================
Write-Step "1/10" "Vérifications préliminaires..."

# Vérifier Git
if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    Write-Error "Git n'est pas installé"
    exit 1
}
Write-Success "Git installé"

# Vérifier Maven
if (-not (Test-Path "mvnw")) {
    Write-Error "Maven wrapper non trouvé"
    exit 1
}
Write-Success "Maven wrapper trouvé"

# Vérifier Node
if (-not (Get-Command npm -ErrorAction SilentlyContinue)) {
    Write-Error "Node.js/npm n'est pas installé"
    exit 1
}
Write-Success "Node.js/npm installé"

# ====================================
# ÉTAPE 2: Vérifier le statut Git
# ====================================
Write-Step "2/10" "Vérification du statut Git..."

$gitStatus = git status --porcelain
if ($gitStatus) {
    Write-Warning "Modifications non committées détectées"
    if (-not $Force) {
        $commit = Read-Host "Voulez-vous committer maintenant? (O/N)"
        if ($commit -eq "O" -or $commit -eq "o") {
            $message = Read-Host "Message du commit"
            git add .
            git commit -m "$message"
            Write-Success "Modifications committées"
        } else {
            Write-Warning "Déploiement annulé"
            exit 0
        }
    }
} else {
    Write-Success "Repository à jour"
}

# ====================================
# ÉTAPE 3: Build Backend
# ====================================
Write-Step "3/10" "Build du backend..."

try {
    if ($SkipTests) {
        & .\mvnw clean package -DskipTests | Out-Null
    } else {
        & .\mvnw clean package | Out-Null
    }
    
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Backend build réussi"
    } else {
        throw "Build échoué"
    }
} catch {
    Write-Error "Erreur lors du build backend: $_"
    $deploymentSuccess = $false
    exit 1
}

# ====================================
# ÉTAPE 4: Build Frontend
# ====================================
Write-Step "4/10" "Build du frontend..."

Push-Location frontend
try {
    npm run build | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Frontend build réussi"
        
        # Vérifier la taille du build
        $distSize = (Get-ChildItem dist -Recurse | Measure-Object -Property Length -Sum).Sum / 1MB
        $distSize = [math]::Round($distSize, 2)
        Write-Host "  Taille du build: $distSize MB" -ForegroundColor Cyan
        
        if ($distSize -gt 10) {
            Write-Warning "Build volumineux (>10MB)"
        }
    } else {
        throw "Build échoué"
    }
} catch {
    Write-Error "Erreur lors du build frontend: $_"
    $deploymentSuccess = $false
    Pop-Location
    exit 1
}
Pop-Location

# ====================================
# ÉTAPE 5: Vérifier les variables d'environnement
# ====================================
Write-Step "5/10" "Vérification des variables d'environnement..."

$envCheck = $true

# Backend
if (Test-Path ".env") {
    $envContent = Get-Content .env -Raw
    $requiredVars = @("DATABASE_URL", "JWT_SECRET", "CORS_ALLOWED_ORIGINS")
    
    foreach ($var in $requiredVars) {
        if ($envContent -notmatch "$var=.+") {
            Write-Warning "Variable manquante: $var (vérifier Railway)"
            $envCheck = $false
        }
    }
} else {
    Write-Warning "Fichier .env non trouvé (variables doivent être dans Railway)"
}

# Frontend
if (Test-Path "frontend\.env.production") {
    $frontendEnv = Get-Content "frontend\.env.production" -Raw
    if ($frontendEnv -notmatch "VITE_API_URL=.+") {
        Write-Warning "VITE_API_URL manquante (vérifier Vercel)"
        $envCheck = $false
    }
}

if ($envCheck) {
    Write-Success "Variables d'environnement OK"
} else {
    Write-Warning "Vérifiez les variables d'environnement dans Railway et Vercel"
}

# ====================================
# ÉTAPE 6: Optimisation de la Base de Données
# ====================================
if (-not $SkipDBOptimization) {
    Write-Step "6/10" "Optimisation de la base de données..."
    
    Write-Host "Voulez-vous optimiser la base de données maintenant? (O/N)" -ForegroundColor Yellow
    Write-Host "  (Nécessite une connexion à la base de production)" -ForegroundColor Gray
    $optimizeDB = Read-Host
    
    if ($optimizeDB -eq "O" -or $optimizeDB -eq "o") {
        Write-Host "`nExécutez ces commandes dans un terminal séparé:" -ForegroundColor Cyan
        Write-Host "  railway connect postgresql" -ForegroundColor Gray
        Write-Host "  \i scripts/optimisation_indexes.sql" -ForegroundColor Gray
        Write-Host "`nAppuyez sur Entrée quand c'est fait..." -ForegroundColor Yellow
        Read-Host
        Write-Success "Optimisation DB marquée comme complète"
    } else {
        Write-Warning "Optimisation DB ignorée"
    }
} else {
    Write-Warning "Optimisation DB ignorée (--SkipDBOptimization)"
}

# ====================================
# ÉTAPE 7: Push vers Git
# ====================================
Write-Step "7/10" "Push vers Git..."

try {
    git push origin main 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Push réussi"
    } else {
        Write-Warning "Push échoué ou déjà à jour"
    }
} catch {
    Write-Warning "Erreur lors du push: $_"
}

# ====================================
# ÉTAPE 8: Attente du déploiement
# ====================================
Write-Step "8/10" "Attente du déploiement..."

Write-Host "`nLes déploiements automatiques sont en cours sur:" -ForegroundColor Cyan
Write-Host "  - Railway (Backend): ~2-5 minutes" -ForegroundColor Gray
Write-Host "  - Vercel (Frontend): ~1-3 minutes" -ForegroundColor Gray
Write-Host "`nVoulez-vous attendre et tester maintenant? (O/N)" -ForegroundColor Yellow
$wait = Read-Host

if ($wait -eq "O" -or $wait -eq "o") {
    Write-Host "`nAttente de 3 minutes pour le déploiement..." -ForegroundColor Cyan
    Start-Sleep -Seconds 180
    Write-Success "Attente terminée"
} else {
    Write-Warning "Tests ignorés - exécutez .\scripts\test-performance.ps1 plus tard"
}

# ====================================
# ÉTAPE 9: Tests post-déploiement
# ====================================
if ($wait -eq "O" -or $wait -eq "o") {
    Write-Step "9/10" "Tests post-déploiement..."
    
    $frontendURL = Read-Host "URL du Frontend (ex: https://votre-app.vercel.app)"
    $backendURL = Read-Host "URL du Backend (ex: https://votre-api.railway.app)"
    
    # Test Frontend
    Write-Host "`nTest du frontend..." -ForegroundColor Cyan
    try {
        $response = Invoke-WebRequest -Uri $frontendURL -UseBasicParsing -TimeoutSec 10
        if ($response.StatusCode -eq 200) {
            Write-Success "Frontend accessible"
            
            # Mesurer le temps de chargement
            $loadTime = Measure-Command {
                Invoke-WebRequest -Uri $frontendURL -UseBasicParsing | Out-Null
            }
            $seconds = [math]::Round($loadTime.TotalSeconds, 2)
            Write-Host "  Temps de chargement: $seconds s" -ForegroundColor Cyan
            
            if ($seconds -lt 3) {
                Write-Success "Performance excellente (<3s)"
            } elseif ($seconds -lt 5) {
                Write-Success "Performance acceptable (<5s)"
            } else {
                Write-Warning "Performance à améliorer (>5s)"
            }
        }
    } catch {
        Write-Error "Frontend non accessible: $_"
        $deploymentSuccess = $false
    }
    
    # Test Backend
    Write-Host "`nTest du backend..." -ForegroundColor Cyan
    try {
        $backendTest = Invoke-WebRequest -Uri "$backendURL/api/health" -UseBasicParsing -TimeoutSec 10 -ErrorAction SilentlyContinue
        if ($backendTest.StatusCode -eq 200) {
            Write-Success "Backend accessible"
        }
    } catch {
        Write-Warning "Endpoint /api/health non disponible (normal si non implémenté)"
    }
} else {
    Write-Warning "Tests ignorés"
}

# ====================================
# ÉTAPE 10: Résumé
# ====================================
Write-Step "10/10" "Résumé du déploiement"

$endTime = Get-Date
$duration = $endTime - $startTime
$durationMinutes = [math]::Round($duration.TotalMinutes, 1)

Write-Host "`n==================================================" -ForegroundColor Cyan
Write-Host "           DÉPLOIEMENT TERMINÉ" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan

if ($deploymentSuccess) {
    Write-Success "Déploiement réussi en $durationMinutes minutes"
} else {
    Write-Error "Déploiement terminé avec des erreurs"
}

Write-Host "`nProchaines étapes:" -ForegroundColor Cyan
Write-Host "  1. Vérifier le déploiement sur:" -ForegroundColor White
Write-Host "     - Railway: https://railway.app/dashboard" -ForegroundColor Gray
Write-Host "     - Vercel: https://vercel.com/dashboard" -ForegroundColor Gray
Write-Host "`n  2. Tester les performances:" -ForegroundColor White
Write-Host "     .\scripts\test-performance.ps1" -ForegroundColor Gray
Write-Host "`n  3. Surveiller les logs:" -ForegroundColor White
Write-Host "     railway logs" -ForegroundColor Gray
Write-Host "     vercel logs" -ForegroundColor Gray
Write-Host "`n  4. Analyser avec Lighthouse:" -ForegroundColor White
Write-Host "     https://pagespeed.web.dev/" -ForegroundColor Gray

Write-Host "`n==================================================" -ForegroundColor Cyan

# Ouvrir les dashboards
Write-Host "`nVoulez-vous ouvrir les dashboards dans le navigateur? (O/N)" -ForegroundColor Yellow
$openBrowser = Read-Host

if ($openBrowser -eq "O" -or $openBrowser -eq "o") {
    Start-Process "https://railway.app/dashboard"
    Start-Process "https://vercel.com/dashboard"
    Write-Success "Dashboards ouverts"
}

Write-Host "`nAppuyez sur une touche pour terminer..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
