# Script de verification pre-deploiement

Write-Host "=== Verification Pre-Deploiement - Credit Platform ===" -ForegroundColor Cyan
Write-Host ""

$errors = 0
$warnings = 0

# Verification 1 : Fichiers de configuration
Write-Host "[1] Verification des fichiers de configuration..." -ForegroundColor Yellow

$requiredFiles = @(
    "Dockerfile",
    ".dockerignore",
    "render.yaml",
    "src\main\resources\application-prod.properties",
    "frontend\vercel.json",
    "frontend\.env.production"
)

foreach ($file in $requiredFiles) {
    if (Test-Path $file) {
        Write-Host "  [OK] $file" -ForegroundColor Green
    } else {
        Write-Host "  [MANQUANT] $file" -ForegroundColor Red
        $errors++
    }
}

Write-Host ""

# Verification 2 : Variables d'environnement a configurer
Write-Host "[2] Variables d'environnement a configurer sur Render :" -ForegroundColor Yellow
Write-Host "  - SPRING_DATASOURCE_URL (URL JDBC MySQL)" -ForegroundColor Magenta
Write-Host "  - DB_USERNAME" -ForegroundColor Magenta
Write-Host "  - DB_PASSWORD" -ForegroundColor Magenta
Write-Host "  - JWT_SECRET (generer avec generate-jwt-secret.ps1)" -ForegroundColor Magenta
Write-Host "  - JWT_EXPIRATION (ex: 7200000)" -ForegroundColor Magenta
Write-Host "  - CORS_ALLOWED_ORIGINS (URLs Vercel)" -ForegroundColor Magenta
Write-Host ""

# Verification 3 : Git status
Write-Host "[3] Verification Git..." -ForegroundColor Yellow

$gitStatus = git status --porcelain 2>$null

if ($LASTEXITCODE -eq 0) {
    if ($gitStatus) {
        Write-Host "  [ATTENTION] Fichiers non commites detectes" -ForegroundColor Yellow
        Write-Host "  Executez : git add . && git commit -m 'Add deployment config'" -ForegroundColor Gray
        $warnings++
    } else {
        Write-Host "  [OK] Tous les fichiers sont commites" -ForegroundColor Green
    }
    
    $branch = git branch --show-current 2>$null
    if ($branch) {
        Write-Host "  [INFO] Branche actuelle : $branch" -ForegroundColor Cyan
    }
} else {
    Write-Host "  [ERREUR] Git n'est pas initialise ou pas dans un repo Git" -ForegroundColor Red
    $errors++
}

Write-Host ""

# Verification 4 : Frontend dependencies
Write-Host "[4] Verification Frontend..." -ForegroundColor Yellow

if (Test-Path "frontend\node_modules") {
    Write-Host "  [OK] node_modules presents" -ForegroundColor Green
} else {
    Write-Host "  [ATTENTION] node_modules manquants (executez : cd frontend && npm install)" -ForegroundColor Yellow
    $warnings++
}

if (Test-Path "frontend\package.json") {
    $packageJson = Get-Content "frontend\package.json" -Raw | ConvertFrom-Json
    Write-Host "  [INFO] Version : $($packageJson.version)" -ForegroundColor Cyan
    Write-Host "  [INFO] Build command : $($packageJson.scripts.build)" -ForegroundColor Cyan
} else {
    Write-Host "  [ERREUR] package.json manquant" -ForegroundColor Red
    $errors++
}

Write-Host ""

# Verification 5 : Backend dependencies
Write-Host "[5] Verification Backend..." -ForegroundColor Yellow

if (Test-Path "pom.xml") {
    Write-Host "  [OK] pom.xml present" -ForegroundColor Green
    
    # Verifier si actuator est present
    $pomContent = Get-Content "pom.xml" -Raw
    if ($pomContent -match "spring-boot-starter-actuator") {
        Write-Host "  [OK] Spring Boot Actuator configure" -ForegroundColor Green
    } else {
        Write-Host "  [ATTENTION] Spring Boot Actuator manquant pour health check" -ForegroundColor Yellow
        $warnings++
    }
} else {
    Write-Host "  [ERREUR] pom.xml manquant" -ForegroundColor Red
    $errors++
}

Write-Host ""

# Resume
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "RESUME" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

if ($errors -eq 0 -and $warnings -eq 0) {
    Write-Host "[SUCCESS] Tout est pret pour le deploiement !" -ForegroundColor Green
    Write-Host ""
    Write-Host "Prochaines etapes :" -ForegroundColor Yellow
    Write-Host "  1. Generer le JWT_SECRET : .\scripts\generate-jwt-secret.ps1" -ForegroundColor Gray
    Write-Host "  2. Creer la base de donnees MySQL sur Render ou ailleurs" -ForegroundColor Gray
    Write-Host "  3. Pusher le code : git push origin main" -ForegroundColor Gray
    Write-Host "  4. Deployer sur Render (backend) et Vercel (frontend)" -ForegroundColor Gray
    Write-Host "  5. Consulter DEPLOY_QUICK_START.md pour les instructions detaillees" -ForegroundColor Gray
} elseif ($errors -eq 0) {
    Write-Host "[ATTENTION] $warnings avertissement(s) detecte(s)" -ForegroundColor Yellow
    Write-Host "Vous pouvez continuer, mais verifiez les avertissements ci-dessus." -ForegroundColor Yellow
} else {
    Write-Host "[ERREUR] $errors erreur(s) et $warnings avertissement(s) detecte(s)" -ForegroundColor Red
    Write-Host "Corrigez les erreurs avant de deployer." -ForegroundColor Red
}

Write-Host ""
