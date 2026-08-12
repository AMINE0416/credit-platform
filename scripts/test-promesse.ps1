# ═══════════════════════════════════════════════════════════════════════════
# Script PowerShell pour tester l'implémentation des promesses
# ═══════════════════════════════════════════════════════════════════════════

$ErrorActionPreference = "Stop"

Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  Test de l'implémentation des promesses de paiement" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# ───────────────────────────────────────────────────────────────────────────
# Chargement des variables d'environnement
# ───────────────────────────────────────────────────────────────────────────
if (Test-Path .env) {
    Write-Host "📄 Chargement des variables depuis .env" -ForegroundColor Yellow
    Get-Content .env | ForEach-Object {
        if ($_ -match '^([^=]+)=(.*)$') {
            $key = $matches[1].Trim()
            $value = $matches[2].Trim()
            [Environment]::SetEnvironmentVariable($key, $value, "Process")
        }
    }
} else {
    Write-Host "⚠️  Fichier .env introuvable, utilisation des valeurs par défaut" -ForegroundColor Yellow
}

$DB_URL = $env:DATABASE_URL
if (-not $DB_URL) {
    Write-Host "❌ DATABASE_URL non défini" -ForegroundColor Red
    exit 1
}

# Extraire les informations de connexion
if ($DB_URL -match 'postgresql://([^:]+):([^@]+)@([^:]+):(\d+)/(.+)') {
    $DB_USER = $matches[1]
    $DB_PASS = $matches[2]
    $DB_HOST = $matches[3]
    $DB_PORT = $matches[4]
    $DB_NAME = $matches[5]
} else {
    Write-Host "❌ Format DATABASE_URL invalide" -ForegroundColor Red
    exit 1
}

Write-Host "🗄️  Base de données: $DB_NAME @ $DB_HOST" -ForegroundColor Green
Write-Host ""

# ───────────────────────────────────────────────────────────────────────────
# Menu
# ───────────────────────────────────────────────────────────────────────────
Write-Host "Choisissez une action:" -ForegroundColor Cyan
Write-Host "  1. Appliquer la migration (ajouter les colonnes)" -ForegroundColor White
Write-Host "  2. Exécuter le test complet" -ForegroundColor White
Write-Host "  3. Vérifier les dossiers avec promesse" -ForegroundColor White
Write-Host "  4. Vérifier les promesses arrivant à échéance" -ForegroundColor White
Write-Host "  5. Tout exécuter (migration + test)" -ForegroundColor White
Write-Host "  6. Nettoyer les données de test" -ForegroundColor White
Write-Host ""

$choice = Read-Host "Votre choix (1-6)"

# ───────────────────────────────────────────────────────────────────────────
# Fonction pour exécuter une requête SQL
# ───────────────────────────────────────────────────────────────────────────
function Invoke-SqlQuery {
    param (
        [string]$Query,
        [string]$File
    )
    
    $env:PGPASSWORD = $DB_PASS
    
    if ($File) {
        Write-Host "🔄 Exécution du fichier: $File" -ForegroundColor Yellow
        psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f $File
    } else {
        Write-Host "🔄 Exécution de la requête..." -ForegroundColor Yellow
        psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c $Query
    }
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Succès" -ForegroundColor Green
    } else {
        Write-Host "✕ Erreur" -ForegroundColor Red
    }
}

# ───────────────────────────────────────────────────────────────────────────
# Exécution selon le choix
# ───────────────────────────────────────────────────────────────────────────
switch ($choice) {
    "1" {
        Write-Host ""
        Write-Host "📊 Application de la migration..." -ForegroundColor Cyan
        Invoke-SqlQuery -File "scripts\add_promesse_fields.sql"
    }
    
    "2" {
        Write-Host ""
        Write-Host "🧪 Exécution du test complet..." -ForegroundColor Cyan
        Invoke-SqlQuery -File "scripts\test_promesse_complete.sql"
    }
    
    "3" {
        Write-Host ""
        Write-Host "🔍 Vérification des dossiers avec promesse..." -ForegroundColor Cyan
        $query = @"
SELECT 
    d.id as dossier_id,
    d.statut_actuel,
    d.date_promesse,
    d.montant_promesse,
    c.reference_contrat,
    cl.nom || ' ' || cl.prenom as client_nom
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
JOIN clients cl ON c.client_id = cl.id
WHERE d.statut_actuel IN ('PROMESSE', 'SUIVI_PROMESSE')
ORDER BY d.date_creation DESC;
"@
        Invoke-SqlQuery -Query $query
    }
    
    "4" {
        Write-Host ""
        Write-Host "📅 Vérification des promesses arrivant à échéance..." -ForegroundColor Cyan
        $query = @"
SELECT 
    d.id,
    c.reference_contrat,
    cl.nom || ' ' || cl.prenom as client,
    d.date_promesse,
    d.montant_promesse,
    CASE 
        WHEN d.date_promesse < CURRENT_DATE THEN 'Échue'
        WHEN d.date_promesse = CURRENT_DATE THEN 'Aujourd''hui'
        WHEN d.date_promesse <= CURRENT_DATE + 7 THEN 'Cette semaine'
        ELSE 'Ultérieur'
    END as statut
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
JOIN clients cl ON c.client_id = cl.id
WHERE d.statut_actuel IN ('PROMESSE', 'SUIVI_PROMESSE')
  AND d.date_promesse IS NOT NULL
ORDER BY d.date_promesse ASC;
"@
        Invoke-SqlQuery -Query $query
    }
    
    "5" {
        Write-Host ""
        Write-Host "🚀 Exécution complète..." -ForegroundColor Cyan
        Write-Host ""
        Write-Host "Étape 1/2: Migration" -ForegroundColor Yellow
        Invoke-SqlQuery -File "scripts\add_promesse_fields.sql"
        Write-Host ""
        Write-Host "Étape 2/2: Tests" -ForegroundColor Yellow
        Invoke-SqlQuery -File "scripts\test_promesse_complete.sql"
    }
    
    "6" {
        Write-Host ""
        Write-Host "🧹 Nettoyage des données de test..." -ForegroundColor Cyan
        $confirm = Read-Host "Êtes-vous sûr de vouloir supprimer les données de test? (oui/non)"
        if ($confirm -eq "oui") {
            $query = @"
DELETE FROM dossiers WHERE credit_id IN (
    SELECT id FROM credits WHERE client_id IN (
        SELECT id FROM clients WHERE cin = 'TEST_PROMESSE_001'
    )
);
DELETE FROM credits WHERE client_id IN (
    SELECT id FROM clients WHERE cin = 'TEST_PROMESSE_001'
);
DELETE FROM clients WHERE cin = 'TEST_PROMESSE_001';
"@
            Invoke-SqlQuery -Query $query
            Write-Host "✓ Données de test supprimées" -ForegroundColor Green
        } else {
            Write-Host "❌ Annulé" -ForegroundColor Yellow
        }
    }
    
    default {
        Write-Host "❌ Choix invalide" -ForegroundColor Red
        exit 1
    }
}

Write-Host ""
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  Terminé!" -ForegroundColor Green
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
