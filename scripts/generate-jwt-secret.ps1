# Script PowerShell pour generer un secret JWT securise

Write-Host "Generation d'un secret JWT securise..." -ForegroundColor Cyan
Write-Host ""
Write-Host "Secret JWT (128 caracteres hex) :" -ForegroundColor Yellow

# Generer 64 bytes aleatoires et les convertir en hexadecimal
$secret = -join ((1..64) | ForEach-Object { "{0:X2}" -f (Get-Random -Maximum 256) })
Write-Host $secret -ForegroundColor Green

Write-Host ""
Write-Host "IMPORTANT: Copiez ce secret et utilisez-le comme valeur pour JWT_SECRET" -ForegroundColor Green
Write-Host "ATTENTION: Ne partagez JAMAIS ce secret publiquement" -ForegroundColor Red
