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
     * Crée la feuille "État actuel" avec TOUTES les colonnes d'origine + données agent
     * Format Excel enrichi pour export complet des dossiers
     */
    private void creerFeuilleEtatActuel(Workbook workbook, String dateExtraction) {
        Sheet sheet = workbook.createSheet("État actuel");
        
        // Styles
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);

        // En-têtes : COLONNES ORIGINALES D'IMPORT + DONNÉES AGENT
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            // === COLONNES CLIENT (ORIGINALES) ===
            "num_cin1", "nom", "prenom", "cli_tel", "TEL2", "cli_adr", 
            "Lib_Ville", "cod_age", "lib_age", "Adresse_Cadrage",
            
            // === COLONNES CRÉDIT (ORIGINALES) ===
            "ANC_REFDO", "From_Prov", "GEST_CIN", "CSP_PDT", "Segment",
            "nbr_imp", "Mensualite", "encours", "Mnt_Imp", "prv_cum",
            "stage", "DVR", "Reecheance", "Immatriculation", "Score",
            
            // === COLONNES GESTION DOSSIER ===
            "Agent_Email", "Statut_Actuel", "Date_Creation_Dossier",
            
            // === DONNÉES SAISIES PAR L'AGENT (dernière action) ===
            "Derniere_Action_Date", "Nature_Action", "Code_Resultat",
            "Motif_Impaye", "Recidive", "Montant_Promesse", "Date_Promesse",
            "Telephone_Qualifie", "Compte_Rendu"
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
            
            // === COLONNES CLIENT (ORIGINALES) ===
            row.createCell(colNum++).setCellValue(nvl(client.getCin()));
            row.createCell(colNum++).setCellValue(nvl(client.getNom()));
            row.createCell(colNum++).setCellValue(nvl(client.getPrenom()));
            row.createCell(colNum++).setCellValue(nvl(client.getTelephone()));
            row.createCell(colNum++).setCellValue(nvl(client.getTelephone2()));
            row.createCell(colNum++).setCellValue(nvl(client.getAdresse()));
            row.createCell(colNum++).setCellValue(nvl(client.getVille()));
            row.createCell(colNum++).setCellValue(nvl(client.getCodeAgence()));
            row.createCell(colNum++).setCellValue(nvl(client.getLibAgence()));
            row.createCell(colNum++).setCellValue(nvl(client.getAdresseCadrage()));
            
            // === COLONNES CRÉDIT (ORIGINALES) ===
            row.createCell(colNum++).setCellValue(nvl(credit.getReferenceContrat()));
            row.createCell(colNum++).setCellValue(nvl(credit.getFromProv()));
            row.createCell(colNum++).setCellValue(nvl(credit.getGestCin()));
            row.createCell(colNum++).setCellValue(nvl(credit.getTypeCredit()));
            row.createCell(colNum++).setCellValue(nvl(credit.getSegment()));
            row.createCell(colNum++).setCellValue(credit.getNbrImpaye() != null ? credit.getNbrImpaye() : 0);
            row.createCell(colNum++).setCellValue(
                credit.getMensualite() != null ? credit.getMensualite().doubleValue() : 0
            );
            row.createCell(colNum++).setCellValue(
                credit.getEncours() != null ? credit.getEncours().doubleValue() : 0
            );
            row.createCell(colNum++).setCellValue(
                credit.getImpayeMontant() != null ? credit.getImpayeMontant().doubleValue() : 0
            );
            row.createCell(colNum++).setCellValue(
                credit.getPrvCum() != null ? credit.getPrvCum().doubleValue() : 0
            );
            row.createCell(colNum++).setCellValue(nvl(credit.getStage()));
            row.createCell(colNum++).setCellValue(nvl(credit.getDvr()));
            row.createCell(colNum++).setCellValue(nvl(credit.getReecheance()));
            row.createCell(colNum++).setCellValue(nvl(credit.getImmatriculation()));
            row.createCell(colNum++).setCellValue(
                credit.getScore() != null ? credit.getScore().doubleValue() : 0
            );
            
            // === COLONNES GESTION DOSSIER ===
            row.createCell(colNum++).setCellValue(
                dossier.getAgentAssigne() != null ? nvl(dossier.getAgentAssigne().getEmail()) : ""
            );
            row.createCell(colNum++).setCellValue(dossier.getStatutActuel().name());
            row.createCell(colNum++).setCellValue(
                dossier.getDateCreation() != null ? dossier.getDateCreation().format(DATE_FORMAT) : ""
            );
            
            // === DONNÉES SAISIES PAR L'AGENT (dernière action) ===
            // Récupérer la dernière action du dossier
            ActionDossier derniereAction = actionDossierRepository
                    .findByDossierIdOrderByDateActionDesc(dossier.getId())
                    .stream()
                    .findFirst()
                    .orElse(null);
            
            if (derniereAction != null) {
                row.createCell(colNum++).setCellValue(
                    derniereAction.getDateAction().format(DATETIME_FORMAT)
                );
                row.createCell(colNum++).setCellValue(
                    derniereAction.getNatureAction() != null ? derniereAction.getNatureAction().name() : ""
                );
                row.createCell(colNum++).setCellValue(
                    derniereAction.getCodeResultat() != null ? derniereAction.getCodeResultat().name() : ""
                );
                row.createCell(colNum++).setCellValue(
                    derniereAction.getMotifImpaye() != null ? derniereAction.getMotifImpaye().name() : ""
                );
                row.createCell(colNum++).setCellValue(
                    derniereAction.getRecidive() != null ? derniereAction.getRecidive().name() : ""
                );
                row.createCell(colNum++).setCellValue(
                    derniereAction.getMontantPromesse() != null 
                        ? String.format("%.2f", derniereAction.getMontantPromesse().doubleValue()) 
                        : ""
                );
                row.createCell(colNum++).setCellValue(
                    derniereAction.getDatePromesse() != null 
                        ? derniereAction.getDatePromesse().format(DATE_FORMAT) 
                        : ""
                );
                row.createCell(colNum++).setCellValue(nvl(derniereAction.getNouveauTelephone()));
                row.createCell(colNum++).setCellValue(nvl(derniereAction.getCompteRendu()));
            } else {
                // Pas encore d'action : colonnes vides
                for (int i = 0; i < 9; i++) {
                    row.createCell(colNum++).setCellValue("");
                }
            }
        }

        // Activer les filtres automatiques sur la ligne d'en-têtes
        sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, headers.length - 1));

        // Auto-size colonnes
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    /**
     * Utilitaire pour gérer les valeurs nulles
     */
    private String nvl(String value) {
        return value != null ? value : "";
    }

    /**
     * Crée la feuille "Historique statuts" avec toutes les transitions
     */
    private void creerFeuilleHistoriqueStatuts(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Historique statuts");
        
        // Styles
        CellStyle headerStyle = createHeaderStyle(workbook);

        // En-têtes - TOUTES les informations saisies
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            // Identification
            "Référence contrat", "CIN", "Nom client", "Prénom client",
            "Téléphone principal", "Téléphone secondaire", "Adresse", "Ville",
            "Code Agence", "Lib Agence", "Adresse Cadrage",
            
            // Informations crédit
            "Type Crédit", "Segment", "Nbr Impayés", "Mensualité", 
            "Encours", "Montant Impayé", "Stage",
            
            // Action et statut
            "Statut Actuel", "Agent", "Date/Heure Action",
            "Nature Action", "Code Résultat", 
            "Motif Impayé", "Récidive", 
            "Montant Promesse", "Date Promesse",
            "Téléphone Qualifié", "Compte Rendu"
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
            
            // === IDENTIFICATION ===
            row.createCell(colNum++).setCellValue(nvl(credit.getReferenceContrat()));
            row.createCell(colNum++).setCellValue(nvl(client.getCin()));
            row.createCell(colNum++).setCellValue(nvl(client.getNom()));
            row.createCell(colNum++).setCellValue(nvl(client.getPrenom()));
            row.createCell(colNum++).setCellValue(nvl(client.getTelephone()));
            row.createCell(colNum++).setCellValue(nvl(client.getTelephone2()));
            row.createCell(colNum++).setCellValue(nvl(client.getAdresse()));
            row.createCell(colNum++).setCellValue(nvl(client.getVille()));
            row.createCell(colNum++).setCellValue(nvl(client.getCodeAgence()));
            row.createCell(colNum++).setCellValue(nvl(client.getLibAgence()));
            row.createCell(colNum++).setCellValue(nvl(client.getAdresseCadrage()));
            
            // === INFORMATIONS CRÉDIT ===
            row.createCell(colNum++).setCellValue(nvl(credit.getTypeCredit()));
            row.createCell(colNum++).setCellValue(nvl(credit.getSegment()));
            row.createCell(colNum++).setCellValue(credit.getNbrImpaye() != null ? credit.getNbrImpaye() : 0);
            row.createCell(colNum++).setCellValue(
                credit.getMensualite() != null ? credit.getMensualite().doubleValue() : 0
            );
            row.createCell(colNum++).setCellValue(
                credit.getEncours() != null ? credit.getEncours().doubleValue() : 0
            );
            row.createCell(colNum++).setCellValue(
                credit.getImpayeMontant() != null ? credit.getImpayeMontant().doubleValue() : 0
            );
            row.createCell(colNum++).setCellValue(nvl(credit.getStage()));
            
            // === ACTION ET STATUT ===
            row.createCell(colNum++).setCellValue(dossier.getStatutActuel().name());
            row.createCell(colNum++).setCellValue(agent.getNom() + " " + agent.getPrenom());
            row.createCell(colNum++).setCellValue(action.getDateAction().format(DATETIME_FORMAT));
            
            row.createCell(colNum++).setCellValue(
                action.getNatureAction() != null ? action.getNatureAction().name() : ""
            );
            row.createCell(colNum++).setCellValue(
                action.getCodeResultat() != null ? action.getCodeResultat().name() : ""
            );
            row.createCell(colNum++).setCellValue(
                action.getMotifImpaye() != null ? action.getMotifImpaye().name() : ""
            );
            row.createCell(colNum++).setCellValue(
                action.getRecidive() != null ? action.getRecidive().name() : ""
            );
            row.createCell(colNum++).setCellValue(
                action.getMontantPromesse() != null 
                    ? String.format("%.2f", action.getMontantPromesse().doubleValue()) 
                    : ""
            );
            row.createCell(colNum++).setCellValue(
                action.getDatePromesse() != null 
                    ? action.getDatePromesse().format(DATE_FORMAT) 
                    : ""
            );
            row.createCell(colNum++).setCellValue(nvl(action.getNouveauTelephone()));
            row.createCell(colNum++).setCellValue(nvl(action.getCompteRendu()));
        }

        // Activer les filtres automatiques sur la ligne d'en-têtes
        sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, headers.length - 1));
        
        // Auto-size colonnes
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
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
