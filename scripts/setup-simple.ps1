# Script simple de setup des promesses
# Date: 2026-08-11

$ErrorActionPreference = "Continue"

$DB_USER = "postgres"
$DB_NAME = "credit_platform"
$DB_HOST = "localhost"
$DB_PORT = "5432"

Write-Host ""
Write-Host "========================================"
Write-Host "SETUP DES PROMESSES"
Write-Host "========================================"
Write-Host ""

# Etape 1: Diagnostic
Write-Host "Etape 1: Diagnostic de la base de donnees..."
Write-Host ""
psql -U $DB_USER -d $DB_NAME -h $DB_HOST -p $DB_PORT -f "scripts\diagnostic_promesses_detaille.sql"

Write-Host ""
Write-Host "Appuyez sur une touche pour continuer..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

# Etape 2: Creer promesse de test
Write-Host ""
Write-Host "Etape 2: Creation d'une promesse de test..."
Write-Host ""
psql -U $DB_USER -d $DB_NAME -h $DB_HOST -p $DB_PORT -f "scripts\creer_promesse_test.sql"

Write-Host ""
Write-Host "Appuyez sur une touche pour la verification finale..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

# Etape 3: Verification
Write-Host ""
Write-Host "Etape 3: Verification finale..."
Write-Host ""

$query = @"
SELECT 
    d.id,
    c.reference_contrat,
    cl.nom || ' ' || cl.prenom as client,
    d.statut_actuel,
    d.date_promesse,
    d.montant_promesse,
    CASE 
        WHEN d.date_promesse IS NOT NULL THEN 'OUI'
        ELSE 'NON'
    END as a_une_promesse
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
JOIN clients cl ON c.client_id = cl.id
WHERE d.statut_actuel IN ('PROMESSE', 'SUIVI_PROMESSE')
ORDER BY d.id;
"@

psql -U $DB_USER -d $DB_NAME -h $DB_HOST -p $DB_PORT -c $query

Write-Host ""
Write-Host "========================================"
Write-Host "SETUP TERMINE"
Write-Host "========================================"
Write-Host ""
Write-Host "PROCHAINES ETAPES:"
Write-Host ""
Write-Host "1. Recompiler: mvn clean package -DskipTests"
Write-Host "2. Redemarrer l'application"
Write-Host "3. Ouvrir le dossier C22J0016 dans le navigateur"
Write-Host "4. Vous devriez voir la promesse de 3000 MAD"
Write-Host ""
