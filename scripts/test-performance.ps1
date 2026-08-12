# Script de test de performance pour Credit Platform

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "     Test de Performance - Credit Platform" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$FRONTEND_URL = Read-Host "URL du Frontend (ex: https://votre-app.vercel.app)"
$BACKEND_URL = Read-Host "URL du Backend (ex: https://votre-api.railway.app)"

# Test 1: Disponibilité du Frontend
Write-Host "`n[Test 1] Vérification de la disponibilité du Frontend..." -ForegroundColor Yellow
try {
    $frontendResponse = Invoke-WebRequest -Uri $FRONTEND_URL -UseBasicParsing -TimeoutSec 10
    if ($frontendResponse.StatusCode -eq 200) {
        Write-Host "✓ Frontend accessible (Status: $($frontendResponse.StatusCode))" -ForegroundColor Green
    }
} catch {
    Write-Host "✗ Frontend non accessible: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Disponibilité du Backend
Write-Host "`n[Test 2] Vérification de la disponibilité du Backend..." -ForegroundColor Yellow
try {
    $backendResponse = Invoke-WebRequest -Uri "$BACKEND_URL/api/health" -UseBasicParsing -TimeoutSec 10 -ErrorAction SilentlyContinue
    if ($backendResponse.StatusCode -eq 200) {
        Write-Host "✓ Backend accessible (Status: $($backendResponse.StatusCode))" -ForegroundColor Green
    }
} catch {
    Write-Host "⚠ Endpoint /api/health non disponible (normal si non implémenté)" -ForegroundColor Yellow
}

# Test 3: Temps de chargement Frontend
Write-Host "`n[Test 3] Mesure du temps de chargement Frontend..." -ForegroundColor Yellow
$frontendTime = Measure-Command {
    Invoke-WebRequest -Uri $FRONTEND_URL -UseBasicParsing | Out-Null
}
$frontendSeconds = [math]::Round($frontendTime.TotalSeconds, 2)
Write-Host "Temps de chargement: $frontendSeconds secondes" -ForegroundColor Cyan

if ($frontendSeconds -lt 2) {
    Write-Host "✓ Excellent (<2s)" -ForegroundColor Green
} elseif ($frontendSeconds -lt 3) {
    Write-Host "✓ Bon (<3s)" -ForegroundColor Green
} elseif ($frontendSeconds -lt 5) {
    Write-Host "⚠ Acceptable (<5s)" -ForegroundColor Yellow
} else {
    Write-Host "✗ Lent (>5s) - Optimisation nécessaire" -ForegroundColor Red
}

# Test 4: Vérification HTTPS
Write-Host "`n[Test 4] Vérification de la sécurité..." -ForegroundColor Yellow
if ($FRONTEND_URL.StartsWith("https://")) {
    Write-Host "✓ Frontend: HTTPS activé" -ForegroundColor Green
} else {
    Write-Host "✗ Frontend: HTTPS non activé" -ForegroundColor Red
}

if ($BACKEND_URL.StartsWith("https://")) {
    Write-Host "✓ Backend: HTTPS activé" -ForegroundColor Green
} else {
    Write-Host "✗ Backend: HTTPS non activé" -ForegroundColor Red
}

# Test 5: Taille de la page
Write-Host "`n[Test 5] Analyse de la taille de la page..." -ForegroundColor Yellow
try {
    $page = Invoke-WebRequest -Uri $FRONTEND_URL -UseBasicParsing
    $sizeKB = [math]::Round($page.Content.Length / 1KB, 2)
    Write-Host "Taille HTML: $sizeKB KB" -ForegroundColor Cyan
    
    if ($sizeKB -lt 100) {
        Write-Host "✓ Taille optimale (<100KB)" -ForegroundColor Green
    } elseif ($sizeKB -lt 500) {
        Write-Host "✓ Taille acceptable (<500KB)" -ForegroundColor Green
    } else {
        Write-Host "⚠ Taille importante (>500KB)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠ Impossible de mesurer la taille" -ForegroundColor Yellow
}

# Test 6: Headers de cache
Write-Host "`n[Test 6] Vérification du cache..." -ForegroundColor Yellow
try {
    $headers = $page.Headers
    if ($headers.'Cache-Control') {
        Write-Host "✓ Cache-Control configuré: $($headers.'Cache-Control')" -ForegroundColor Green
    } else {
        Write-Host "⚠ Pas de Cache-Control trouvé" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠ Impossible de vérifier les headers" -ForegroundColor Yellow
}

# Test 7: Compression
Write-Host "`n[Test 7] Vérification de la compression..." -ForegroundColor Yellow
try {
    if ($headers.'Content-Encoding') {
        Write-Host "✓ Compression activée: $($headers.'Content-Encoding')" -ForegroundColor Green
    } else {
        Write-Host "⚠ Pas de compression détectée" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠ Impossible de vérifier la compression" -ForegroundColor Yellow
}

# Résumé
Write-Host "`n==================================================" -ForegroundColor Cyan
Write-Host "              RÉSUMÉ DES TESTS" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "Temps de chargement: $frontendSeconds s" -ForegroundColor Cyan
Write-Host ""

# Recommandations
Write-Host "`n==================================================" -ForegroundColor Cyan
Write-Host "            RECOMMANDATIONS" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "1. Tester avec Google Lighthouse:" -ForegroundColor White
Write-Host "   https://pagespeed.web.dev/?url=$FRONTEND_URL" -ForegroundColor Gray
Write-Host ""
Write-Host "2. Tester avec GTmetrix:" -ForegroundColor White
Write-Host "   https://gtmetrix.com/" -ForegroundColor Gray
Write-Host ""
Write-Host "3. Vérifier WebPageTest:" -ForegroundColor White
Write-Host "   https://www.webpagetest.org/" -ForegroundColor Gray
Write-Host ""
Write-Host "4. Objectifs de performance:" -ForegroundColor White
Write-Host "   - Temps de chargement: < 3s" -ForegroundColor Gray
Write-Host "   - First Contentful Paint: < 2s" -ForegroundColor Gray
Write-Host "   - Time to Interactive: < 3.5s" -ForegroundColor Gray
Write-Host "   - Score Lighthouse: > 80" -ForegroundColor Gray
Write-Host ""
Write-Host "==================================================" -ForegroundColor Cyan

# Pause finale
Write-Host "`nAppuyez sur une touche pour terminer..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
