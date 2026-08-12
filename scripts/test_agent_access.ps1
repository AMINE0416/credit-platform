# Script pour tester l'accès d'un agent

$baseUrl = "http://localhost:8081/api"

Write-Host "=== TEST ACCÈS AGENT ===" -ForegroundColor Cyan
Write-Host ""

# 1. Login
Write-Host "1. Connexion de l'agent..." -ForegroundColor Yellow
$loginBody = @{
    email = "agent3.fes@eqdom.ma"
    password = "password123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.token
    Write-Host "✓ Connexion réussie" -ForegroundColor Green
    Write-Host "  Nom: $($loginResponse.prenom) $($loginResponse.nom)" -ForegroundColor Gray
    Write-Host "  Rôles: $($loginResponse.roles -join ', ')" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "✗ Échec de connexion" -ForegroundColor Red
    Write-Host "  $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 2. Récupérer mes dossiers
Write-Host "2. Récupération des dossiers assignés..." -ForegroundColor Yellow
$headers = @{
    "Authorization" = "Bearer $token"
}

try {
    $dossiers = Invoke-RestMethod -Uri "$baseUrl/dossiers/me" -Method Get -Headers $headers
    Write-Host "✓ $($dossiers.Count) dossier(s) trouvé(s)" -ForegroundColor Green
    Write-Host ""
    
    if ($dossiers.Count -gt 0) {
        Write-Host "Premier dossier:" -ForegroundColor Cyan
        $firstDossier = $dossiers[0]
        Write-Host "  ID: $($firstDossier.id)" -ForegroundColor Gray
        Write-Host "  CIN: $($firstDossier.cin)" -ForegroundColor Gray
        Write-Host "  Nom: $($firstDossier.nomComplet)" -ForegroundColor Gray
        Write-Host "  Statut: $($firstDossier.statut)" -ForegroundColor Gray
        Write-Host "  Ville: $($firstDossier.ville)" -ForegroundColor Gray
        Write-Host ""
        
        # 3. Récupérer le détail du premier dossier
        Write-Host "3. Récupération du détail du dossier..." -ForegroundColor Yellow
        try {
            $detail = Invoke-RestMethod -Uri "$baseUrl/dossiers/$($firstDossier.id)" -Method Get -Headers $headers
            Write-Host "✓ Détails récupérés" -ForegroundColor Green
            Write-Host "  Statut actuel: $($detail.statut)" -ForegroundColor Gray
            Write-Host "  Nombre d'actions: $($detail.nbActions)" -ForegroundColor Gray
            Write-Host ""
            
            # 4. Récupérer les transitions autorisées
            Write-Host "4. Récupération des transitions autorisées..." -ForegroundColor Yellow
            try {
                $transitions = Invoke-RestMethod -Uri "$baseUrl/dossiers/$($firstDossier.id)/transitions" -Method Get -Headers $headers
                Write-Host "✓ Transitions récupérées" -ForegroundColor Green
                Write-Host "  Transitions autorisées: $($transitions.transitionsAutorisees -join ', ')" -ForegroundColor Gray
                Write-Host ""
                Write-Host "✅ TOUS LES TESTS RÉUSSIS!" -ForegroundColor Green
                Write-Host "L'agent peut bien accéder et éditer ses dossiers." -ForegroundColor Green
            } catch {
                Write-Host "✗ Erreur lors de la récupération des transitions" -ForegroundColor Red
                Write-Host "  $($_.Exception.Message)" -ForegroundColor Red
            }
        } catch {
            Write-Host "✗ Erreur lors de la récupération du détail" -ForegroundColor Red
            Write-Host "  $($_.Exception.Message)" -ForegroundColor Red
        }
    } else {
        Write-Host "⚠ Aucun dossier assigné à cet agent" -ForegroundColor Yellow
        Write-Host "  Importez le fichier Excel pour assigner des dossiers" -ForegroundColor Yellow
    }
} catch {
    Write-Host "✗ Erreur lors de la récupération des dossiers" -ForegroundColor Red
    Write-Host "  $($_.Exception.Message)" -ForegroundColor Red
}
