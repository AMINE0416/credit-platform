package org.eqdom.creditplatform.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eqdom.creditplatform.model.*;
import org.eqdom.creditplatform.repository.ActionDossierRepository;
import org.eqdom.creditplatform.repository.DossierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service pour générer des exports Excel dynamiques
 * - Feuille 1: État actuel de tous les dossiers
 * - Feuille 2: Historique complet des transitions de statut
 */
@Service
public class ExcelExportService {

    @Autowired
    private DossierRepository dossierRepository;

    @Autowired
    private ActionDossierRepository actionDossierRepository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Génère un fichier Excel avec deux feuilles:
     * 1. État actuel des dossiers
     * 2. Historique des transitions
     */
    public byte[] genererExportComplet() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            String dateExtraction = LocalDateTime.now().format(DATETIME_FORMAT);

            // Créer les deux feuilles
            creerFeuilleEtatActuel(workbook, dateExtraction);
            creerFeuilleHistoriqueStatuts(workbook);

            // Écrire dans un byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Crée la feuille "État actuel" avec snapshot de tous les dossiers
     */
    private void creerFeuilleEtatActuel(Workbook workbook, String dateExtraction) {
        Sheet sheet = workbook.createSheet("État actuel");
        
        // Styles
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);

        // En-têtes
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "Date extraction", "Référence contrat", "CIN", "Nom client", "Prénom client",
            "Téléphone", "Statut actuel", "Agent assigné", "Date création",
            "Nb impayés", "Montant impayé", "Mensualité", "Encours"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Données
        List<Dossier> dossiers = dossierRepository.findAll();
        int rowNum = 1;

        for (Dossier dossier : dossiers) {
            Row row = sheet.createRow(rowNum++);
            Credit credit = dossier.getCredit();
            Client client = credit.getClient();

            int colNum = 0;
            row.createCell(colNum++).setCellValue(dateExtraction);
            row.createCell(colNum++).setCellValue(credit.getReferenceContrat());
            row.createCell(colNum++).setCellValue(client.getCin());
            row.createCell(colNum++).setCellValue(client.getNom());
            row.createCell(colNum++).setCellValue(client.getPrenom());
            row.createCell(colNum++).setCellValue(client.getTelephone());
            row.createCell(colNum++).setCellValue(dossier.getStatutActuel().name());
            row.createCell(colNum++).setCellValue(
                dossier.getAgentAssigne() != null 
                    ? dossier.getAgentAssigne().getNom() + " " + dossier.getAgentAssigne().getPrenom()
                    : ""
            );
            
            Cell dateCell = row.createCell(colNum++);
            if (dossier.getDateCreation() != null) {
                dateCell.setCellValue(dossier.getDateCreation().format(DATE_FORMAT));
            }
            
            row.createCell(colNum++).setCellValue(credit.getNbrImpaye() != null ? credit.getNbrImpaye() : 0);
            row.createCell(colNum++).setCellValue(
                credit.getImpayeMontant() != null ? credit.getImpayeMontant().doubleValue() : 0
            );
            row.createCell(colNum++).setCellValue(
                credit.getMensualite() != null ? credit.getMensualite().doubleValue() : 0
            );
            row.createCell(colNum++).setCellValue(
                credit.getEncours() != null ? credit.getEncours().doubleValue() : 0
            );
        }

        // Auto-size colonnes
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Crée la feuille "Historique statuts" avec toutes les transitions
     */
    private void creerFeuilleHistoriqueStatuts(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Historique statuts");
        
        // Styles
        CellStyle headerStyle = createHeaderStyle(workbook);

        // En-têtes - colonnes essentielles seulement
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "Référence contrat", "Nom client", "Prénom client",
            "Transition", "Agent", "Date/Heure",
            "Nature action", "Code résultat", 
            "Motif impayé", "Récidive", 
            "Montant promesse", "Date promesse", 
            "Compte rendu"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Récupérer TOUTES les actions (historique complet) triées par date
        List<ActionDossier> actions = actionDossierRepository
                .findAll()
                .stream()
                .sorted((a1, a2) -> a1.getDateAction().compareTo(a2.getDateAction()))
                .toList();
        
        int rowNum = 1;

        for (ActionDossier action : actions) {
            Row row = sheet.createRow(rowNum++);
            Dossier dossier = action.getDossier();
            Credit credit = dossier.getCredit();
            Client client = credit.getClient();
            User agent = action.getAgent();

            int colNum = 0;
            
            // Colonnes de base
            row.createCell(colNum++).setCellValue(credit.getReferenceContrat());
            row.createCell(colNum++).setCellValue(client.getNom());
            row.createCell(colNum++).setCellValue(client.getPrenom());
            
            // Transition déterminée intelligemment
            String transition = determinerTransition(action, dossier);
            row.createCell(colNum++).setCellValue(transition);
            
            row.createCell(colNum++).setCellValue(
                agent.getNom() + " " + agent.getPrenom()
            );
            row.createCell(colNum++).setCellValue(
                action.getDateAction().format(DATETIME_FORMAT)
            );
            
            // Champs toujours obligatoires
            row.createCell(colNum++).setCellValue(
                action.getNatureAction() != null ? action.getNatureAction().name() : ""
            );
            row.createCell(colNum++).setCellValue(
                action.getCodeResultat() != null ? action.getCodeResultat().name() : ""
            );
            
            // COLONNES CONDITIONNELLES selon la logique métier
            
            // Motif impayé : Visible pour certaines transitions (EN_COURS → PROMESSE, INJOIGNABLE → EN_COURS, etc.)
            String motifImpaye = "";
            if (action.getMotifImpaye() != null) {
                motifImpaye = action.getMotifImpaye().name();
            }
            row.createCell(colNum++).setCellValue(motifImpaye);
            
            // Récidive : Visible uniquement pour transitions vers INJOIGNABLE
            String recidive = "";
            if (action.getRecidive() != null) {
                recidive = action.getRecidive().name();
            }
            row.createCell(colNum++).setCellValue(recidive);
            
            // Montant promesse : Visible uniquement pour transition EN_COURS → PROMESSE
            String montantPromesse = "";
            if (action.getMontantPromesse() != null) {
                montantPromesse = String.format("%.2f", action.getMontantPromesse().doubleValue());
            }
            row.createCell(colNum++).setCellValue(montantPromesse);
            
            // Date promesse : Visible uniquement pour transition EN_COURS → PROMESSE
            String datePromesse = "";
            if (action.getDatePromesse() != null) {
                datePromesse = action.getDatePromesse().format(DATE_FORMAT);
            }
            row.createCell(colNum++).setCellValue(datePromesse);
            
            // Compte rendu
            row.createCell(colNum++).setCellValue(
                action.getCompteRendu() != null ? action.getCompteRendu() : ""
            );
        }

        // Auto-size colonnes
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Détermine la transition basée sur l'action et ses champs
     * 
     * LOGIQUE MÉTIER :
     * - EN_COURS → PROMESSE : Si montant_promesse ET date_promesse présents
     * - EN_COURS → INJOIGNABLE : Si récidive = NON (première fois)
     * - INJOIGNABLE → INJOIGNABLE : Si récidive = OUI (récidive)
     * - PROMESSE → REGULARISE : Aucun nouveau montant/date (déjà connu)
     * - Autres : Basé sur le statut actuel du dossier
     * 
     * NOTE IMPORTANTE :
     * Pour une traçabilité parfaite, il serait idéal de stocker statutAvant et statutApres
     * directement dans l'entité ActionDossier. Cette approche par heuristique est une solution
     * de transition basée sur les champs métier présents dans l'action.
     */
    private String determinerTransition(ActionDossier action, Dossier dossier) {
        StatutDossier statutActuel = dossier.getStatutActuel();
        
        // Transition vers PROMESSE : Si montant ET date promesse présents
        // Logique métier : EN_COURS → PROMESSE (obligatoire)
        if (action.getMontantPromesse() != null && action.getDatePromesse() != null) {
            return "EN_COURS → PROMESSE";
        }
        
        // Transition vers INJOIGNABLE avec récidive
        // Logique métier : INJOIGNABLE → INJOIGNABLE (récidive = OUI)
        if (action.getRecidive() != null && action.getRecidive() == Recidive.OUI) {
            return "INJOIGNABLE → INJOIGNABLE (récidive)";
        }
        
        // Transition vers INJOIGNABLE première fois
        // Logique métier : EN_COURS → INJOIGNABLE (récidive = NON)
        if (action.getRecidive() != null && action.getRecidive() == Recidive.NON) {
            return "EN_COURS → INJOIGNABLE";
        }
        
        // Pour les autres cas, on infère basé sur le statut actuel
        // Note : Cette approche a des limites si un dossier a changé plusieurs fois
        // Solution idéale : Stocker statutAvant dans ActionDossier lors de la création
        switch (statutActuel) {
            case PROMESSE:
                // Si on est en PROMESSE sans montant dans cette action, c'est probablement
                // une transition INJOIGNABLE → EN_COURS → PROMESSE
                return "→ PROMESSE";
            case REGULARISE:
                // Peut venir de PROMESSE ou EN_COURS
                if (action.getMontantPromesse() == null) {
                    return "PROMESSE → REGULARISE";
                } else {
                    return "EN_COURS → REGULARISE";
                }
            case CLOTURE:
                return "REGULARISE → CLOTURE";
            case EN_COURS:
                // Retour en EN_COURS depuis INJOIGNABLE ou PROMESSE
                if (action.getMotifImpaye() != null) {
                    return "INJOIGNABLE → EN_COURS";
                } else {
                    return "PROMESSE → EN_COURS (rupture)";
                }
            case NOUVEAU:
                return "Création";
            case INJOIGNABLE:
                // Déjà géré ci-dessus, mais par sécurité
                return "→ INJOIGNABLE";
            default:
                return "→ " + statutActuel.name();
        }
    }

    /**
     * Style pour les en-têtes
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    /**
     * Style pour les dates
     */
    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));
        return style;
    }
}
