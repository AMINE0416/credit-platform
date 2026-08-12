# Script complet de setup et diagnostic des promesses
# Date: 2026-08-11

$ErrorActionPreference = "Continue"

# Configuration
$DB_USER = "postgres"
$DB_NAME = "credit_platform"
$DB_HOST = "localhost"
$DB_PORT = "5432"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "SETUP COMPLET DES PROMESSES" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Étape 1: Diagnostic détaillé
Write-Host "📊 Étape 1: Diagnostic detaille de la base de donnees..." -ForegroundColor Yellow
Write-Host ""
psql -U $DB_USER -d $DB_NAME -h $DB_HOST -p $DB_PORT -f "scripts\diagnostic_promesses_detaille.sql"

Write-Host ""
Write-Host "Appuyez sur une touche pour continuer vers la création de promesse de test..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

# Étape 2: Créer une promesse de test
Write-Host ""
Write-Host "📝 Étape 2: Creation d'une promesse de test..." -ForegroundColor Yellow
Write-Host ""
psql -U $DB_USER -d $DB_NAME -h $DB_HOST -p $DB_PORT -f "scripts\creer_promesse_test.sql"

Write-Host ""
Write-Host "Appuyez sur une touche pour continuer vers la vérification..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

# Étape 3: Vérification finale
Write-Host ""
Write-Host "✅ Étape 3: Verification finale..." -ForegroundColor Yellow
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
        WHEN d.date_promesse IS NOT NULL THEN '✅ OUI'
        ELSE '❌ NON'
    END as a_une_promesse
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
JOIN clients cl ON c.client_id = cl.id
WHERE d.statut_actuel IN ('PROMESSE', 'SUIVI_PROMESSE')
ORDER BY d.id;
"@

psql -U $DB_USER -d $DB_NAME -h $DB_HOST -p $DB_PORT -c $query

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "✅ SETUP TERMINÉ" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "📋 PROCHAINES ÉTAPES:" -ForegroundColor Cyan
Write-Host ""
Write-Host "1️⃣  Recompiler l'application:" -ForegroundColor White
Write-Host "   mvn clean package -DskipTests" -ForegroundColor Gray
Write-Host ""
Write-Host "2️⃣  Redemarrer l'application:" -ForegroundColor White
Write-Host "   java -jar target\credit-platform-0.0.1-SNAPSHOT.jar" -ForegroundColor Gray
Write-Host ""
Write-Host "3️⃣  Tester dans le navigateur:" -ForegroundColor White
Write-Host "   - Ouvrir le dossier C22J0016 (Khadija Lahlou)" -ForegroundColor Gray
Write-Host "   - Vous devriez voir l'encadre avec:" -ForegroundColor Gray
Write-Host "     * Montant: 3000.00 MAD" -ForegroundColor Gray
Write-Host "     * Date: 20 aout 2026" -ForegroundColor Gray
Write-Host ""
Write-Host "4️⃣  Pour ajouter d'autres promesses:" -ForegroundColor White
Write-Host "   - Via l'interface: creer une action PROMESSE" -ForegroundColor Gray
Write-Host "   - Les informations seront automatiquement sauvegardees" -ForegroundColor Gray
Write-Host ""
Write-Host "5️⃣  Test du cycle complet (archivage + import):" -ForegroundColor White
Write-Host "   - Archiver le mois actuel" -ForegroundColor Gray
Write-Host "   - Importer le mois suivant" -ForegroundColor Gray
Write-Host "   - Les promesses seront preservees automatiquement" -ForegroundColor Gray
Write-Host ""
