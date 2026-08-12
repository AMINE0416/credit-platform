# Script de test de préservation des promesses lors de l'import
# Auteur: Assistant IA
# Date: 2026-08-11

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test de préservation des promesses" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$DB_USER = "postgres"
$DB_NAME = "credit_platform"
$DB_HOST = "localhost"
$DB_PORT = "5432"

Write-Host "1️⃣  Vérification de la structure de la table dossiers_mois_precedent..." -ForegroundColor Yellow

$query1 = @"
SELECT 
    column_name, 
    data_type, 
    is_nullable
FROM information_schema.columns 
WHERE table_name = 'dossiers_mois_precedent' 
  AND column_name IN ('date_promesse', 'montant_promesse')
ORDER BY ordinal_position;
"@

psql -U $DB_USER -d $DB_NAME -h $DB_HOST -p $DB_PORT -c $query1

Write-Host ""
Write-Host "2️⃣  Vérification des dossiers avec promesse actuels..." -ForegroundColor Yellow

$query2 = @"
SELECT 
    d.id,
    c.reference_contrat,
    d.statut_actuel,
    d.date_promesse,
    d.montant_promesse,
    d.mois_import,
    d.est_recurrent
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
WHERE d.date_promesse IS NOT NULL
   OR d.statut_actuel IN ('PROMESSE', 'SUIVI_PROMESSE')
ORDER BY c.reference_contrat;
"@

psql -U $DB_USER -d $DB_NAME -h $DB_HOST -p $DB_PORT -c $query2

Write-Host ""
Write-Host "3️⃣  Vérification de la table temporaire mois précédent..." -ForegroundColor Yellow

$query3 = @"
SELECT 
    numero_dossier,
    statut_final,
    date_promesse,
    montant_promesse,
    mois_origine,
    date_sauvegarde
FROM dossiers_mois_precedent
ORDER BY numero_dossier;
"@

psql -U $DB_USER -d $DB_NAME -h $DB_HOST -p $DB_PORT -c $query3

Write-Host ""
Write-Host "4️⃣  Statistiques des promesses..." -ForegroundColor Yellow

$query4 = @"
SELECT 
    'Dossiers avec promesse (actifs)' as type,
    COUNT(*) as nombre
FROM dossiers 
WHERE date_promesse IS NOT NULL
UNION ALL
SELECT 
    'Dossiers avec promesse (table temporaire)' as type,
    COUNT(*) as nombre
FROM dossiers_mois_precedent
WHERE date_promesse IS NOT NULL
UNION ALL
SELECT 
    'Total PROMESSE' as type,
    COUNT(*) as nombre
FROM dossiers 
WHERE statut_actuel = 'PROMESSE'
UNION ALL
SELECT 
    'Total SUIVI_PROMESSE' as type,
    COUNT(*) as nombre
FROM dossiers 
WHERE statut_actuel = 'SUIVI_PROMESSE';
"@

psql -U $DB_USER -d $DB_NAME -h $DB_HOST -p $DB_PORT -c $query4

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "✅ Test terminé" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Prochaines étapes:" -ForegroundColor Cyan
Write-Host "  1. Si les colonnes date_promesse et montant_promesse n'existent pas dans dossiers_mois_precedent," -ForegroundColor White
Write-Host "     exécuter: psql -U $DB_USER -d $DB_NAME -f scripts/add_promesse_to_mois_precedent.sql" -ForegroundColor White
Write-Host ""
Write-Host "  2. Recompiler l'application: mvn clean package -DskipTests" -ForegroundColor White
Write-Host ""
Write-Host "  3. Redémarrer l'application" -ForegroundColor White
Write-Host ""
Write-Host "  4. Tester le cycle complet (voir PRESERVATION_PROMESSE_IMPORT.md)" -ForegroundColor White
Write-Host ""
