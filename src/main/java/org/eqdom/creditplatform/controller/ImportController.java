package org.eqdom.creditplatform.controller;

import org.eqdom.creditplatform.dto.ImportRapport;
import org.eqdom.creditplatform.exception.BusinessException;
import org.eqdom.creditplatform.repository.ActionDossierRepository;
import org.eqdom.creditplatform.repository.ClientRepository;
import org.eqdom.creditplatform.repository.CreditRepository;
import org.eqdom.creditplatform.repository.DossierRepository;
import org.eqdom.creditplatform.repository.UserRepository;
import org.eqdom.creditplatform.repository.RoleRepository;
import org.eqdom.creditplatform.model.Client;
import org.eqdom.creditplatform.model.Credit;
import org.eqdom.creditplatform.model.Dossier;
import org.eqdom.creditplatform.model.StatutDossier;
import org.eqdom.creditplatform.model.User;
import org.eqdom.creditplatform.model.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/imports")
@PreAuthorize("hasAuthority('ADMIN')")
public class ImportController {

    private static final Logger log = LoggerFactory.getLogger(ImportController.class);

    @Autowired private ActionDossierRepository actionDossierRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private CreditRepository creditRepository;
    @Autowired private DossierRepository dossierRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    /**
     * Endpoint pour réinitialiser complètement la base de données
     * Supprime TOUS les dossiers, crédits, clients et agents (mais garde l'admin)
     * ⚠️ ATTENTION : OPÉRATION IRRÉVERSIBLE ⚠️
     */
    @DeleteMapping("/reset-database")
    public ResponseEntity<?> resetDatabase() {
        try {
            log.warn("⚠️  === DÉBUT RESET DATABASE === ⚠️ ");
            
            // Compter ce qui va être supprimé
            long nbActions = actionDossierRepository.count();
            long nbDossiers = dossierRepository.count();
            long nbCredits = creditRepository.count();
            long nbClients = clientRepository.count();
            
            // Compter les agents
            Role roleAgent = roleRepository.findByNom("AGENT").orElse(null);
            long nbAgents = 0;
            if (roleAgent != null) {
                nbAgents = userRepository.findAll().stream()
                        .filter(u -> u.getRoles().stream().anyMatch(r -> r.getNom().equals("AGENT")))
                        .count();
            }
            
            log.info("📊 Avant suppression:");
            log.info("  - Actions: {}", nbActions);
            log.info("  - Dossiers: {}", nbDossiers);
            log.info("  - Crédits: {}", nbCredits);
            log.info("  - Clients: {}", nbClients);
            log.info("  - Agents: {}", nbAgents);
            
            // 1. Supprimer toutes les actions
            actionDossierRepository.deleteAll();
            log.info("✅ Actions supprimées");
            
            // 2. Supprimer tous les dossiers
            dossierRepository.deleteAll();
            log.info("✅ Dossiers supprimés");
            
            // 3. Supprimer tous les crédits
            creditRepository.deleteAll();
            log.info("✅ Crédits supprimés");
            
            // 4. Supprimer tous les clients
            clientRepository.deleteAll();
            log.info("✅ Clients supprimés");
            
            // 5. Supprimer tous les agents (mais pas l'admin)
            if (roleAgent != null) {
                List<User> agents = userRepository.findAll().stream()
                        .filter(u -> u.getRoles().stream().anyMatch(r -> r.getNom().equals("AGENT")))
                        .toList();
                
                userRepository.deleteAll(agents);
                log.info("✅ Agents supprimés: {}", agents.size());
            }
            
            // Vérification après suppression
            long nbActionsApres = actionDossierRepository.count();
            long nbDossiersApres = dossierRepository.count();
            long nbCreditsApres = creditRepository.count();
            long nbClientsApres = clientRepository.count();
            long nbUsersApres = userRepository.count();
            
            log.info("📊 Après suppression:");
            log.info("  - Actions: {}", nbActionsApres);
            log.info("  - Dossiers: {}", nbDossiersApres);
            log.info("  - Crédits: {}", nbCreditsApres);
            log.info("  - Clients: {}", nbClientsApres);
            log.info("  - Users restants: {} (admin(s))", nbUsersApres);
            log.info("✅ === FIN RESET DATABASE === ✅");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Base de données réinitialisée avec succès",
                "suppressed", Map.of(
                    "actions", nbActions,
                    "dossiers", nbDossiers,
                    "credits", nbCredits,
                    "clients", nbClients,
                    "agents", nbAgents
                ),
                "remaining", Map.of(
                    "actions", nbActionsApres,
                    "dossiers", nbDossiersApres,
                    "credits", nbCreditsApres,
                    "clients", nbClientsApres,
                    "users", nbUsersApres
                )
            ));
            
        } catch (Exception e) {
            log.error("❌ Erreur lors du reset : {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Erreur : " + e.getMessage()
            ));
        }
    }
    
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
                        
                        // ⭐ NOUVELLE COLONNE : Email de l'agent assigné
                        String agentEmail = getCellValueByHeader(row, columnMap, "Agent_Email");

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

                        // ⭐ LOGIQUE D'AFFECTATION AUTOMATIQUE
                        if (!creditExists) {
                            if (!dossierRepository.findByCreditId(credit.getId()).isPresent()) {
                                Dossier dossier = new Dossier();
                                dossier.setCredit(credit);
                                
                                // Si un email agent est fourni dans le fichier Excel
                                if (agentEmail != null && !agentEmail.trim().isEmpty()) {
                                    try {
                                        // Chercher l'agent ou le créer automatiquement
                                        User agent = userRepository.findByEmail(agentEmail.trim())
                                                .orElseGet(() -> {
                                                    log.info("⚡ Création automatique de l'agent : {}", agentEmail);
                                                    return creerAgentAutomatiquement(agentEmail.trim(), ville);
                                                });
                                        
                                        if (agent != null) {
                                            // Agent trouvé ou créé : affectation automatique
                                            dossier.setAgentAssigne(agent);
                                            dossier.setStatutActuel(StatutDossier.EN_COURS);
                                            log.debug("✓ Dossier {} affecté à {}", referenceContrat, agentEmail);
                                        } else {
                                            dossier.setStatutActuel(StatutDossier.NOUVEAU);
                                            log.warn("⚠ Impossible de créer l'agent {}", agentEmail);
                                        }
                                    } catch (Exception e) {
                                        dossier.setStatutActuel(StatutDossier.NOUVEAU);
                                        log.error("⚠ Erreur affectation agent {} : {}", agentEmail, e.getMessage());
                                    }
                                } else {
                                    // Pas d'email agent : statut NOUVEAU par défaut
                                    dossier.setStatutActuel(StatutDossier.NOUVEAU);
                                }
                                
                                dossierRepository.save(dossier);
                                nbInserees++;
                            }
                        } else {
                            // Crédit existant : mise à jour de l'affectation si nécessaire
                            dossierRepository.findByCreditId(credit.getId()).ifPresent(dossier -> {
                                if (agentEmail != null && !agentEmail.trim().isEmpty()) {
                                    User agent = userRepository.findByEmail(agentEmail.trim())
                                            .orElseGet(() -> creerAgentAutomatiquement(agentEmail.trim(), ville));
                                    
                                    if (agent != null) {
                                        dossier.setAgentAssigne(agent);
                                        if (dossier.getStatutActuel() == StatutDossier.NOUVEAU) {
                                            dossier.setStatutActuel(StatutDossier.EN_COURS);
                                        }
                                        dossierRepository.save(dossier);
                                        log.debug("✓ Dossier existant {} réaffecté à {}", referenceContrat, agentEmail);
                                    }
                                }
                            });
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

    /**
     * ⭐ Méthode pour créer automatiquement un agent s'il n'existe pas
     * 
     * @param email Email de l'agent (ex: agent1.fes@eqdom.ma)
     * @param ville Ville du dossier (ex: FES)
     * @return L'utilisateur créé ou null en cas d'erreur
     */
    private User creerAgentAutomatiquement(String email, String ville) {
        try {
            // Vérifier si le rôle AGENT existe
            Role roleAgent = roleRepository.findByNom("AGENT")
                    .orElseThrow(() -> new BusinessException("Rôle AGENT introuvable"));
            
            // Extraire des informations depuis l'email
            // Ex: agent1.fes@eqdom.ma -> nom=Agent1, prenom=Fes
            String[] parts = email.split("@")[0].split("\\.");
            String nom = parts.length > 0 ? capitalize(parts[0]) : "Agent";
            String prenom = parts.length > 1 ? capitalize(parts[1]) : ville;
            
            // Créer le nouvel utilisateur
            User newAgent = new User();
            newAgent.setEmail(email);
            newAgent.setNom(nom);
            newAgent.setPrenom(prenom);
            // Mot de passe par défaut : Eqdom2024!
            newAgent.setPassword(passwordEncoder.encode("Eqdom2024!"));
            newAgent.setActif(true);
            newAgent.setRoles(Set.of(roleAgent));
            
            User savedAgent = userRepository.save(newAgent);
            log.info("✅ Agent créé avec succès : {} (mot de passe: Eqdom2024!)", email);
            
            return savedAgent;
        } catch (Exception e) {
            log.error("❌ Erreur lors de la création de l'agent {} : {}", email, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Capitaliser la première lettre d'une chaîne
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
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
