# Script PowerShell: Nettoyage des archives de la base de données

Write-Host "╔═══════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║    NETTOYAGE DE L'HISTORIQUE DES MOIS ARCHIVÉS          ║" -ForegroundColor Cyan
Write-Host "╚═══════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

Write-Host "⚠️  ATTENTION: Cette opération va supprimer les données!" -ForegroundColor Red
Write-Host ""

# Menu des options
Write-Host "Choisissez une option:" -ForegroundColor Yellow
Write-Host "1. Supprimer TOUT l'historique d'archivage" -ForegroundColor White
Write-Host "2. Supprimer les archives d'un mois spécifique" -ForegroundColor White
Write-Host "3. Supprimer les archives de plus de 6 mois" -ForegroundColor White
Write-Host "4. Annuler" -ForegroundColor White
Write-Host ""

$choix = Read-Host "Votre choix (1-4)"

if ($choix -eq "4") {
    Write-Host "Opération annulée" -ForegroundColor Yellow
    exit 0
}

# Confirmation
Write-Host ""
Write-Host "⚠️  CETTE ACTION EST IRRÉVERSIBLE!" -ForegroundColor Red
$confirmation = Read-Host "Tapez 'OUI' en majuscules pour confirmer"

if ($confirmation -ne "OUI") {
    Write-Host "Opération annulée" -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "Connexion à la base de données Railway..." -ForegroundColor Cyan
Write-Host ""

# Instructions pour se connecter
Write-Host "Exécutez ces commandes dans un terminal séparé:" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. railway login" -ForegroundColor Gray
Write-Host "2. railway connect postgresql" -ForegroundColor Gray
Write-Host ""
Write-Host "3. Puis, selon votre choix:" -ForegroundColor Gray
Write-Host ""

switch ($choix) {
    "1" {
        Write-Host "   -- Supprimer TOUT" -ForegroundColor White
        Write-Host "   DELETE FROM dossier_archive;" -ForegroundColor Cyan
        Write-Host "   DELETE FROM dossier_mois_precedent;" -ForegroundColor Cyan
        Write-Host "   DELETE FROM log_archivage;" -ForegroundColor Cyan
        Write-Host "   VACUUM FULL dossier_archive;" -ForegroundColor Cyan
        Write-Host "   VACUUM FULL dossier_mois_precedent;" -ForegroundColor Cyan
        Write-Host "   VACUUM FULL log_archivage;" -ForegroundColor Cyan
    }
    "2" {
        $mois = Read-Host "Entrez le mois à supprimer (format: 2024-07-01)"
        Write-Host ""
        Write-Host "   -- Supprimer le mois $mois" -ForegroundColor White
        Write-Host "   DELETE FROM dossier_archive WHERE DATE_TRUNC('month', date_archivage) = '$mois'::date;" -ForegroundColor Cyan
        Write-Host "   DELETE FROM dossier_mois_precedent WHERE DATE_TRUNC('month', date_archivage) = '$mois'::date;" -ForegroundColor Cyan
        Write-Host "   DELETE FROM log_archivage WHERE DATE_TRUNC('month', date_archivage) = '$mois'::date;" -ForegroundColor Cyan
        Write-Host "   VACUUM FULL dossier_archive;" -ForegroundColor Cyan
    }
    "3" {
        Write-Host "   -- Supprimer les archives de plus de 6 mois" -ForegroundColor White
        Write-Host "   DELETE FROM dossier_archive WHERE date_archivage < NOW() - INTERVAL '6 months';" -ForegroundColor Cyan
        Write-Host "   DELETE FROM dossier_mois_precedent WHERE date_archivage < NOW() - INTERVAL '6 months';" -ForegroundColor Cyan
        Write-Host "   DELETE FROM log_archivage WHERE date_archivage < NOW() - INTERVAL '6 months';" -ForegroundColor Cyan
        Write-Host "   VACUUM FULL dossier_archive;" -ForegroundColor Cyan
    }
}

Write-Host ""
Write-Host "4. Pour vérifier le résultat:" -ForegroundColor Gray
Write-Host "   SELECT COUNT(*) FROM dossier_archive;" -ForegroundColor Cyan
Write-Host "   SELECT COUNT(*) FROM log_archivage;" -ForegroundColor Cyan
Write-Host ""

Write-Host "OU utilisez le script complet:" -ForegroundColor Yellow
Write-Host "   \i scripts/nettoyer_historique_archives.sql" -ForegroundColor Cyan
Write-Host ""

Write-Host "╔═══════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║  Instructions affichées - Exécutez-les dans psql         ║" -ForegroundColor Green
Write-Host "╚═══════════════════════════════════════════════════════════╝" -ForegroundColor Green

Write-Host ""
Write-Host "Appuyez sur une touche pour terminer..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
