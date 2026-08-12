# ═══════════════════════════════════════════════════════════════════
# RESET COMPLET HISTORIQUE ET ARCHIVAGE
# ═══════════════════════════════════════════════════════════════════
# Vide toutes les tables d'historique pour recommencer les tests
# ═══════════════════════════════════════════════════════════════════

Write-Host ""
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  RESET COMPLET HISTORIQUE" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# Configuration
$DB_USER = "postgres"
$DB_PASS = "postgres123"
$DB_NAME = "credit_platform"
$env:PGPASSWORD = $DB_PASS

Write-Host "⚠️  ATTENTION: Cette opération va supprimer:" -ForegroundColor Yellow
Write-Host "   - Tous les historiques de statuts" -ForegroundColor Yellow
Write-Host "   - Tous les logs d'archivage" -ForegroundColor Yellow
Write-Host "   - Toutes les archives de dossiers" -ForegroundColor Yellow
Write-Host "   - Tous les dossiers du mois précédent" -ForegroundColor Yellow
Write-Host ""
Write-Host "✅ Les données courantes (dossiers, actions) seront conservées" -ForegroundColor Green
Write-Host ""

$confirmation = Read-Host "Voulez-vous continuer? (tapez OUI pour confirmer)"

if ($confirmation -ne "OUI") {
    Write-Host ""
    Write-Host "❌ Opération annulée" -ForegroundColor Red
    exit 0
}

Write-Host ""
Write-Host "🗑️  Début du nettoyage..." -ForegroundColor Cyan
Write-Host ""

# Fonction pour exécuter une requête SQL
function Execute-SQL {
    param([string]$Query, [string]$Description)
    
    Write-Host "   $Description..." -ForegroundColor Gray
    
    try {
        psql -U $DB_USER -d $DB_NAME -c $Query -q 2>$null
        if ($LASTEXITCODE -eq 0) {
            Write-Host "   ✅ $Description - OK" -ForegroundColor Green
            return $true
        } else {
            Write-Host "   ⚠️  $Description - Erreur" -ForegroundColor Yellow
            return $false
        }
    } catch {
        Write-Host "   ⚠️  $Description - Erreur: $($_.Exception.Message)" -ForegroundColor Yellow
        return $false
    }
}

# 1. Supprimer historique_statuts
Execute-SQL "DELETE FROM historique_statuts;" "Suppression historiques de statuts"

# 2. Supprimer logs_archivage
Execute-SQL "DELETE FROM logs_archivage;" "Suppression logs d'archivage"

# 3. Supprimer dossiers_archive
Execute-SQL "DELETE FROM dossiers_archive;" "Suppression dossiers archivés"

# 4. Supprimer dossiers_mois_precedent
Execute-SQL "DELETE FROM dossiers_mois_precedent;" "Suppression dossiers mois précédent"

Write-Host ""
Write-Host "📊 Vérification des suppressions:" -ForegroundColor Cyan
Write-Host ""

# Vérification
$verif = @"
SELECT 
    'historique_statuts' as table_name, 
    COUNT(*) as nb_lignes 
FROM historique_statuts
UNION ALL
SELECT 
    'logs_archivage', 
    COUNT(*) 
FROM logs_archivage
UNION ALL
SELECT 
    'dossiers_archive', 
    COUNT(*) 
FROM dossiers_archive
UNION ALL
SELECT 
    'dossiers_mois_precedent', 
    COUNT(*) 
FROM dossiers_mois_precedent;
"@

psql -U $DB_USER -d $DB_NAME -c $verif

Write-Host ""
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Green
Write-Host "  ✅ NETTOYAGE TERMINÉ" -ForegroundColor Green
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Green
Write-Host ""
Write-Host "📌 Toutes les tables d'historique sont maintenant vides" -ForegroundColor White
Write-Host "🚀 Vous pouvez recommencer vos tests depuis le début" -ForegroundColor White
Write-Host ""
