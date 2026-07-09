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
import java.util.HashMap;
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

                // Lire la première ligne pour les en-têtes
                Row headerRow = sheet.getRow(0);
                if (headerRow == null) {
                    rapport.setStatut("ERREUR");
                    rapport.setNbRejetees(1);
                    return ResponseEntity.ok(rapport);
                }

                Map<String, Integer> columnMap = new HashMap<>();
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    String header = getCellValue(headerRow, i).trim();
                    columnMap.put(header, i);
                }

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    try {
                        rowCount++;
                    
                        // Colonnes Client - utiliser les en-têtes
                        String cin = getCellValueByHeader(row, columnMap, "num_cin1");
                        String nom = getCellValueByHeader(row, columnMap, "nom");
                        String prenom = getCellValueByHeader(row, columnMap, "prenom");
                        String telephone = getCellValueByHeader(row, columnMap, "cli_tel");
                        String telephone2 = getCellValueByHeader(row, columnMap, "TEL2");
                        String adresse = getCellValueByHeader(row, columnMap, "cli_adr");
                        String ville = getCellValueByHeader(row, columnMap, "Lib_Ville");
                        String codeAgence = getCellValueByHeader(row, columnMap, "cod_age");
                        String libAgence = getCellValueByHeader(row, columnMap, "lib_age");
                        String adresseCadrage = getCellValueByHeader(row, columnMap, "Adresse_Cadrage");

                        // Colonnes Crédit - utiliser les en-têtes
                        String referenceContrat = getCellValueByHeader(row, columnMap, "ANC_REFDO");
                        String fromProv = getCellValueByHeader(row, columnMap, "From_Prov");
                        String gestCin = getCellValueByHeader(row, columnMap, "GEST_CIN");
                        String typeCredit = getCellValueByHeader(row, columnMap, "CSP_PDT");
                        String segment = getCellValueByHeader(row, columnMap, "Segment");
                        String nbrImpaye = getCellValueByHeader(row, columnMap, "nbr_imp");
                        String mensualite = getCellValueByHeader(row, columnMap, "Mensualite");
                        String encours = getCellValueByHeader(row, columnMap, "encours");
                        String impayeMontant = getCellValueByHeader(row, columnMap, "Mnt_Imp");
                        String prvCum = getCellValueByHeader(row, columnMap, "prv_cum");
                        String stage = getCellValueByHeader(row, columnMap, "stage");
                        String dvr = getCellValueByHeader(row, columnMap, "DVR");
                        String reecheance = getCellValueByHeader(row, columnMap, "Reecheance");
                        String immatriculation = getCellValueByHeader(row, columnMap, "Immatriculation");

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
                        erreurs.add(Map.of("ligne", String.valueOf(i + 1), "referenceContrat", 
                                getCellValueByHeader(row, columnMap, "ANC_REFDO"),
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

    private String getCellValueByHeader(Row row, Map<String, Integer> columnMap, String header) {
        Integer colIndex = columnMap.get(header);
        if (colIndex == null) return "";
        return getCellValue(row, colIndex);
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
