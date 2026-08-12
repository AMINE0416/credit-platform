# Script pour tester la connexion d'un agent
# Test avec l'agent agent3.fes@eqdom.ma

$loginUrl = "http://localhost:8081/api/auth/login"
$body = @{
    email = "agent3.fes@eqdom.ma"
    password = "password123"
} | ConvertTo-Json

Write-Host "=== TEST DE CONNEXION AGENT ===" -ForegroundColor Cyan
Write-Host "Email: agent3.fes@eqdom.ma" -ForegroundColor Yellow
Write-Host "URL: $loginUrl" -ForegroundColor Yellow
Write-Host ""

try {
    $response = Invoke-RestMethod -Uri $loginUrl -Method Post -Body $body -ContentType "application/json"
    Write-Host "✅ CONNEXION RÉUSSIE !" -ForegroundColor Green
    Write-Host ""
    Write-Host "Token: $($response.token.Substring(0, 50))..." -ForegroundColor Green
    Write-Host "Nom: $($response.nom)" -ForegroundColor Green
    Write-Host "Prénom: $($response.prenom)" -ForegroundColor Green
    Write-Host "Rôles: $($response.roles -join ', ')" -ForegroundColor Green
    Write-Host ""
    Write-Host "✅ L'agent peut maintenant se connecter !" -ForegroundColor Green
} catch {
    Write-Host "❌ ERREUR DE CONNEXION" -ForegroundColor Red
    Write-Host "Status: $($_.Exception.Response.StatusCode.Value__)" -ForegroundColor Red
    Write-Host "Message: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Vérifiez que:" -ForegroundColor Yellow
    Write-Host "1. Le serveur Spring Boot est démarré" -ForegroundColor Yellow
    Write-Host "2. L'agent existe dans la base de données" -ForegroundColor Yellow
    Write-Host "3. Le mot de passe est correct (password123)" -ForegroundColor Yellow
}
