package org.eqdom.creditplatform.controller;

import org.eqdom.creditplatform.dto.ImportRapport;
import org.eqdom.creditplatform.repository.ClientRepository;
import org.eqdom.creditplatform.repository.CreditRepository;
import org.eqdom.creditplatform.repository.DossierRepository;
import org.eqdom.creditplatform.model.Client;
import org.eqdom.creditplatform.model.Credit;
import org.eqdom.creditplatform.model.Dossier;
import org.eqdom.creditplatform.model.StatutDossier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/imports")
public class ImportController {

    @Autowired private ClientRepository clientRepository;
    @Autowired private CreditRepository creditRepository;
    @Autowired private DossierRepository dossierRepository;

    @PostMapping("/excel")
    public ResponseEntity<ImportRapport> importExcel(@RequestParam("file") MultipartFile file) {
        ImportRapport rapport = new ImportRapport();
        rapport.setStatut("EN_COURS");
        int nbInserees = 0;
        int nbMisesAJour = 0;
        int nbRejetees = 0;
        List<Map<String, String>> erreurs = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = 0;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    rowCount++;
                    
                    // Colonnes Client
                    String cin = getCellValue(row, 0);           // num_cin1
                    String nom = getCellValue(row, 1);           // nom
                    String prenom = getCellValue(row, 2);        // prenom
                    String telephone = getCellValue(row, 3);     // cli_tel
                    String telephone2 = getCellValue(row, 4);    // TEL2
                    String adresse = getCellValue(row, 5);       // cli_adr
                    String ville = getCellValue(row, 6);         // Lib_Ville
                    String codeAgence = getCellValue(row, 7);    // cod_age
                    String libAgence = getCellValue(row, 8);     // lib_age
                    String adresseCadrage = getCellValue(row, 9); // Adresse_Cadrage

                    // Colonnes Crédit
                    String referenceContrat = getCellValue(row, 10);  // ANC_REFDO
                    String fromProv = getCellValue(row, 11);          // From_Prov
                    String gestCin = getCellValue(row, 12);           // GEST_CIN
                    String typeCredit = getCellValue(row, 13);        // CSP_PDT
                    String segment = getCellValue(row, 14);           // Segment
                    String nbrImpaye = getCellValue(row, 15);         // nbr_imp
                    String mensualite = getCellValue(row, 16);        // Mensualite
                    String encours = getCellValue(row, 17);           // encours
                    String impayeMontant = getCellValue(row, 18);     // Mnt_Imp
                    String prvCum = getCellValue(row, 19);            // prv_cum
                    String stage = getCellValue(row, 20);             // stage
                    String dvr = getCellValue(row, 21);               // DVR
                    String reecheance = getCellValue(row, 22);        // Reecheance
                    String immatriculation = getCellValue(row, 23);   // Immatriculation

                    if (cin.isEmpty() || referenceContrat.isEmpty()) {
                        nbRejetees++;
                        erreurs.add(Map.of("ligne", String.valueOf(i + 1), "referenceContrat", referenceContrat,
                                "motif", "CIN ou référence contrat manquante"));
                        continue;
                    }

                    Client client = clientRepository.findByCin(cin)
                            .map(existingClient -> {
                                existingClient.setNom(nom);
                                existingClient.setPrenom(prenom);
                                existingClient.setTelephone(telephone);
                                existingClient.setTelephone2(telephone2);
                                existingClient.setAdresse(adresse);
                                existingClient.setVille(ville);
                                existingClient.setCodeAgence(codeAgence);
                                existingClient.setLibAgence(libAgence);
                                existingClient.setAdresseCadrage(adresseCadrage);
                                return clientRepository.save(existingClient);
                            })
                            .orElseGet(() -> {
                                Client newClient = new Client();
                                newClient.setCin(cin);
                                newClient.setNom(nom);
                                newClient.setPrenom(prenom);
                                newClient.setTelephone(telephone);
                                newClient.setTelephone2(telephone2);
                                newClient.setAdresse(adresse);
                                newClient.setVille(ville);
                                newClient.setCodeAgence(codeAgence);
                                newClient.setLibAgence(libAgence);
                                newClient.setAdresseCadrage(adresseCadrage);
                                return clientRepository.save(newClient);
                            });

                    boolean creditExists = creditRepository.findByReferenceContrat(referenceContrat).isPresent();
                    Credit credit = creditRepository.findByReferenceContrat(referenceContrat)
                            .map(existingCredit -> {
                                existingCredit.setClient(client);
                                existingCredit.setFromProv(fromProv);
                                existingCredit.setGestCin(gestCin);
                                existingCredit.setTypeCredit(typeCredit);
                                existingCredit.setSegment(segment);
                                existingCredit.setNbrImpaye(parseIntOrZero(nbrImpaye));
                                existingCredit.setMensualite(parseBigDecimalOrZero(mensualite));
                                existingCredit.setEncours(parseBigDecimalOrZero(encours));
                                existingCredit.setImpayeMontant(parseBigDecimalOrZero(impayeMontant));
                                existingCredit.setPrvCum(parseBigDecimalOrZero(prvCum));
                                existingCredit.setStage(stage);
                                existingCredit.setDvr(dvr);
                                existingCredit.setReecheance(reecheance);
                                existingCredit.setImmatriculation(immatriculation);
                                return creditRepository.save(existingCredit);
                            })
                            .orElseGet(() -> {
                                Credit newCredit = new Credit();
                                newCredit.setClient(client);
                                newCredit.setReferenceContrat(referenceContrat);
                                newCredit.setFromProv(fromProv);
                                newCredit.setGestCin(gestCin);
                                newCredit.setTypeCredit(typeCredit);
                                newCredit.setSegment(segment);
                                newCredit.setNbrImpaye(parseIntOrZero(nbrImpaye));
                                newCredit.setMensualite(parseBigDecimalOrZero(mensualite));
                                newCredit.setEncours(parseBigDecimalOrZero(encours));
                                newCredit.setImpayeMontant(parseBigDecimalOrZero(impayeMontant));
                                newCredit.setPrvCum(parseBigDecimalOrZero(prvCum));
                                newCredit.setStage(stage);
                                newCredit.setDvr(dvr);
                                newCredit.setReecheance(reecheance);
                                newCredit.setImmatriculation(immatriculation);
                                return creditRepository.save(newCredit);
                            });

                    if (!creditExists) {
                        if (!dossierRepository.findByCreditId(credit.getId()).isPresent()) {
                            Dossier dossier = new Dossier();
                            dossier.setCredit(credit);
                            dossier.setStatutActuel(StatutDossier.NOUVEAU);
                            dossierRepository.save(dossier);
                            nbInserees++;
                        }
                    } else {
                        nbMisesAJour++;
                    }

                } catch (Exception e) {
                    nbRejetees++;
                    erreurs.add(Map.of("ligne", String.valueOf(i + 1), "referenceContrat", getCellValue(row, 10),
                            "motif", e.getMessage()));
                }
            }

            rapport.setNbLignesTotal(rowCount);
            rapport.setNbInserees(nbInserees);
            rapport.setNbMisesAJour(nbMisesAJour);
            rapport.setNbRejetees(nbRejetees);
            rapport.setStatut("COMPLETED");

        } catch (Exception e) {
            rapport.setStatut("ERREUR");
        }

        return ResponseEntity.ok(rapport);
    }

    private String getCellValue(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex);
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private int parseIntOrZero(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private BigDecimal parseBigDecimalOrZero(String value) {
        try {
            return new BigDecimal(value.trim());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
