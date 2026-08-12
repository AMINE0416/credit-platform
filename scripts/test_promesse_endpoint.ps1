# Test de l'endpoint derniere-promesse
# Usage: .\test_promesse_endpoint.ps1 <dossier_id> <jwt_token>

param(
    [int]$dossierId = 1,
    [string]$token = ""
)

$url = "http://localhost:8081/api/dossiers/$dossierId/derniere-promesse"

Write-Host "Testing endpoint: $url" -ForegroundColor Cyan
Write-Host ""

try {
    $headers = @{}
    if ($token) {
        $headers["Authorization"] = "Bearer $token"
    }
    
    $response = Invoke-WebRequest -Uri $url -Method GET -Headers $headers -UseBasicParsing
    
    Write-Host "Status Code: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response:" -ForegroundColor Yellow
    Write-Host $response.Content
    
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "Status Code: $statusCode" -ForegroundColor $(if ($statusCode -eq 204) { "Yellow" } else { "Red" })
    
    if ($statusCode -eq 204) {
        Write-Host "No promise found (204 No Content)" -ForegroundColor Yellow
    } elseif ($statusCode -eq 401) {
        Write-Host "Unauthorized - Token required or invalid" -ForegroundColor Red
    } elseif ($statusCode -eq 404) {
        Write-Host "Dossier not found" -ForegroundColor Red
    } else {
        Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}
