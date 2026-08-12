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
import org.eqdom.creditplatform.service.ImportAvecDetectionService;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    public ResponseEntity<ImportRapport> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "moisImport", required = false) String moisImport) {
        ImportRapport rapport = new ImportRapport();
        rapport.setStatut("EN_COURS");
        int nbInserees = 0;
        int nbMisesAJour = 0;
        int nbRejetees = 0;
        List<Map<String, String>> erreurs = new ArrayList<>();

        // Déterminer le mois d'import : 
        // Si le paramètre est une date complète (YYYY-MM-DD), extraire l'année-mois
        // Sinon utiliser le paramètre tel quel ou le mois courant
        final String moisImportFinal;
        if (moisImport != null && !moisImport.trim().isEmpty()) {
            String param = moisImport.trim();
            // Si le format est YYYY-MM-DD (10 caractères), extraire YYYY-MM
            if (param.length() == 10 && param.matches("\\d{4}-\\d{2}-\\d{2}")) {
                moisImportFinal = param.substring(0, 7); // Extraire YYYY-MM
                log.info("Import Excel - Date reçue: {}, Mois extrait: {}", param, moisImportFinal);
            } else {
                moisImportFinal = param;
                log.info("Import Excel - Mois d'import: {}", moisImportFinal);
            }
        } else {
            moisImportFinal = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            log.info("Import Excel - Mois d'import (par défaut): {}", moisImportFinal);
        }

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

                // Créer un mapping des colonnes - stocker à la fois le nom original et en minuscules
                Map<String, Integer> columnMap = new HashMap<>();
                Map<String, Integer> columnMapLower = new HashMap<>();
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    String header = getCellValue(headerRow, i).trim();
                    columnMap.put(header, i);
                    columnMapLower.put(header.toLowerCase(), i);
                }
                
                // Log des colonnes trouvées pour debug
                log.info("=== Colonnes Excel trouvées ({} colonnes) ===", columnMap.size());
                columnMap.keySet().stream().sorted().forEach(col -> 
                    log.info("  - '{}'", col)
                );
                log.info("============================================");

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    try {
                        rowCount++;
                    
                        // Colonnes Client - utiliser les en-têtes avec tolérance de casse
                        String cin = getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "num_cin1");
                        final String nom = getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "nom");
                        final String prenom = getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "prenom");
                        final String telephone = getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "cli_tel");
                        final String telephone2 = getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "TEL2");
                        final String adresse = getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "cli_adr");
                        final String ville = getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "Lib_Ville");
                        final String codeAgence = getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "cod_age");
                        final String libAgence = getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "lib_age");
                        final String adresseCadrage = getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "Adresse_Cadrage");

                        // Colonnes Crédit - utiliser les en-têtes avec tolérance de casse
                        String referenceContrat = getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "ANC_REFDO");
                        final String fromProv = getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "From_Prov");
                        final String gestCin = getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "GEST_CIN");
                        final String typeCredit = getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "CSP_PDT");
                        final String segment = getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "Segment");
                        final String nbrImpaye = getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "nbr_imp");
                        final String mensualite = getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "Mensualite");
                        final String encours = getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "encours");
                        final String impayeMontant = getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "Mnt_Imp");
                        final String prvCum = getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "prv_cum");
                        final String stage = getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "stage");
                        final String dvr = getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "DVR");
                        final String reecheance = getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "Reecheance");
                        final String immatriculation = getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "Immatriculation");
                        
                        // ⭐ NOUVELLE COLONNE : Score (cherche plusieurs variantes)
                        String scoreTemp = getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "score_final");
                        if (scoreTemp == null || scoreTemp.trim().isEmpty()) {
                            scoreTemp = getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "Score_final");
                        }
                        if (scoreTemp == null || scoreTemp.trim().isEmpty()) {
                            scoreTemp = getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "SCORE_FINAL");
                        }
                        if (scoreTemp == null || scoreTemp.trim().isEmpty()) {
                            scoreTemp = getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "Score");
                        }
                        if (scoreTemp == null || scoreTemp.trim().isEmpty()) {
                            scoreTemp = getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "SCORE");
                        }
                        if (scoreTemp == null || scoreTemp.trim().isEmpty()) {
                            scoreTemp = getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "score");
                        }
                        final String score = scoreTemp;
                        
                        // Log pour debug
                        if (rowCount <= 3) { // Logger seulement les 3 premières lignes
                            log.info("Ligne {}: score brut = '{}', référence = '{}'", 
                                rowCount, score, referenceContrat);
                        }
                        
                        // ⭐ NOUVELLE COLONNE : Email de l'agent assigné
                        String agentEmail = getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "Agent_Email");
                        
                        // Définir les variables finales pour utilisation dans les lambdas
                        final String cinFinal = cin.trim();
                        final String referenceContratFinal = referenceContrat.trim();
                        
                        // Debug pour les 3 premières lignes
                        if (rowCount <= 3) {
                            log.info("Ligne {} - Données financières:", rowCount);
                            log.info("  Mensualité brute: '{}' → parsée: {}", 
                                mensualite, parseBigDecimalOrZero(mensualite));
                            log.info("  Encours brut: '{}' → parsé: {}", 
                                encours, parseBigDecimalOrZero(encours));
                            log.info("  Impayé brut: '{}' → parsé: {}", 
                                impayeMontant, parseBigDecimalOrZero(impayeMontant));
                            log.info("  Provision brute: '{}' → parsée: {}", 
                                prvCum, parseBigDecimalOrZero(prvCum));
                            log.info("  Score: '{}', Référence: '{}'", score, referenceContratFinal);
                        }

                        if (cinFinal.isEmpty() || referenceContratFinal.isEmpty()) {
                            nbRejetees++;
                            erreurs.add(Map.of("ligne", String.valueOf(i + 1), "referenceContrat", referenceContratFinal,
                                    "motif", "CIN ou référence contrat manquante"));
                            continue;
                        }

                        Client client = clientRepository.findByCin(cinFinal)
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
                                    newClient.setCin(cinFinal);
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

                        boolean creditExists = creditRepository
                                .findByReferenceContratAndClientId(referenceContratFinal, client.getId())
                                .isPresent();
                        Credit credit = creditRepository
                                .findByReferenceContratAndClientId(referenceContratFinal, client.getId())
                                .map(existingCredit -> {
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
                                    existingCredit.setScore(parseScore(score));
                                    return creditRepository.save(existingCredit);
                                })
                                .orElseGet(() -> {
                                    Credit newCredit = new Credit();
                                    newCredit.setClient(client);
                                    newCredit.setReferenceContrat(referenceContratFinal);
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
                                    newCredit.setScore(parseScore(score));
                                    return creditRepository.save(newCredit);
                                });

                        if (!creditExists) {
                            if (dossierRepository.findByCreditId(credit.getId()).isEmpty()) {
                                Dossier dossier = new Dossier();
                                dossier.setCredit(credit);
                                dossier.setMoisImport(moisImportFinal); // ⭐ Renseigner le mois d'import

                                if (agentEmail != null && !agentEmail.trim().isEmpty()) {
                                    try {
                                        User agent = userRepository.findByEmail(agentEmail.trim())
                                                .orElseGet(() -> {
                                                    log.info("Création automatique de l'agent : {}", agentEmail);
                                                    return creerAgentAutomatiquement(agentEmail.trim(), ville);
                                                });

                                        if (agent != null) {
                                            dossier.setAgentAssigne(agent);
                                            dossier.setStatutActuel(StatutDossier.EN_COURS);
                                            log.debug("Dossier {} affecté à {}", referenceContratFinal, agentEmail);
                                        } else {
                                            dossier.setStatutActuel(StatutDossier.EN_COURS);
                                            log.warn("Impossible de créer l'agent {}", agentEmail);
                                        }
                                    } catch (Exception e) {
                                        dossier.setStatutActuel(StatutDossier.EN_COURS);
                                        log.error("Erreur affectation agent {} : {}", agentEmail, e.getMessage());
                                    }
                                } else {
                                    dossier.setStatutActuel(StatutDossier.EN_COURS);
                                }

                                dossierRepository.save(dossier);
                                nbInserees++;
                            }
                        } else {
                            dossierRepository.findByCreditId(credit.getId()).ifPresent(dossier -> {
                                if (agentEmail != null && !agentEmail.trim().isEmpty()) {
                                    User agent = userRepository.findByEmail(agentEmail.trim())
                                            .orElseGet(() -> creerAgentAutomatiquement(agentEmail.trim(), ville));

                                    if (agent != null) {
                                        dossier.setAgentAssigne(agent);
                                        if (dossier.getStatutActuel() == StatutDossier.EN_COURS) {
                                            dossier.setStatutActuel(StatutDossier.EN_COURS);
                                        }
                                        dossierRepository.save(dossier);
                                        log.debug("Dossier existant {} réaffecté à {}", referenceContratFinal, agentEmail);
                                    }
                                }
                            });
                            nbMisesAJour++;
                        }

                    } catch (Exception e) {
                        nbRejetees++;
                        erreurs.add(Map.of("ligne", String.valueOf(i + 1), "referenceContrat", 
                                getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "ANC_REFDO"),
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

    private final DataFormatter dataFormatter = new DataFormatter();

    private String getCellValue(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex);
        if (cell == null) return "";
        return dataFormatter.formatCellValue(cell).trim();
    }

    private String getCellValueByHeader(Row row, Map<String, Integer> columnMap, String header) {
        Integer colIndex = columnMap.get(header);
        if (colIndex == null) return "";
        return getCellValue(row, colIndex);
    }

    /**
     * Version flexible qui essaie plusieurs variantes du nom de colonne
     * pour être tolérant aux différences de casse
     */
    private String getCellValueByHeaderFlexible(Row row, Map<String, Integer> columnMap, 
                                                Map<String, Integer> columnMapLower, String header) {
        // 1. Essayer le nom exact
        Integer colIndex = columnMap.get(header);
        
        // 2. Essayer en minuscules
        if (colIndex == null) {
            colIndex = columnMapLower.get(header.toLowerCase());
        }
        
        // 3. Pour certaines colonnes, essayer des variantes connues
        if (colIndex == null && header.equals("Mensualite")) {
            // Essayer mensualite, MENSUALITE, Mensualité, etc.
            colIndex = columnMapLower.get("mensualite");
        }
        if (colIndex == null && header.equals("encours")) {
            colIndex = columnMapLower.get("encours");
        }
        if (colIndex == null && header.equals("Mnt_Imp")) {
            // Essayer Mnt_Imp, mnt_imp, MNT_IMP, Montant_Impaye, etc.
            colIndex = columnMapLower.get("mnt_imp");
            if (colIndex == null) colIndex = columnMapLower.get("montant_impaye");
            if (colIndex == null) colIndex = columnMapLower.get("impaye_montant");
        }
        if (colIndex == null && header.equals("prv_cum")) {
            // Essayer prv_cum, PRV_CUM, Prv_Cum, provision_cumulee, etc.
            colIndex = columnMapLower.get("prv_cum");
            if (colIndex == null) colIndex = columnMapLower.get("provision_cumulee");
        }
        
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
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        try {
            String cleanValue = value.trim();
            
            // Remplacer la virgule par un point (format français → format anglais)
            cleanValue = cleanValue.replace(',', '.');
            
            // Supprimer les espaces (ex: "1 234,56" → "1234.56")
            cleanValue = cleanValue.replaceAll("\\s+", "");
            
            // Supprimer les séparateurs de milliers si présents
            // Ex: "1.234,56" devient "1234.56" ou "1,234.56" devient "1234.56"
            if (cleanValue.contains(".") && cleanValue.indexOf('.') < cleanValue.lastIndexOf('.')) {
                // Cas "1.234.567,89" - les points sont des séparateurs de milliers
                cleanValue = cleanValue.replaceAll("\\.", "");
                cleanValue = cleanValue.replace(',', '.');
            }
            
            return new BigDecimal(cleanValue);
        } catch (Exception e) {
            log.warn("Impossible de parser le nombre: '{}' - Erreur: {}", value, e.getMessage());
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * Parse le score depuis Excel
     * Gère les formats: "78%", "78", "0.78", etc.
     * Convertit tout en valeur 0-100
     */
    private BigDecimal parseScore(String value) {
        if (value == null || value.trim().isEmpty()) {
            log.debug("Score vide ou null");
            return null;
        }
        
        try {
            String cleanValue = value.trim();
            log.debug("Parse score: valeur brute = '{}'", cleanValue);
            
            // Si le format contient un %, on enlève le % et on garde la valeur
            if (cleanValue.endsWith("%")) {
                cleanValue = cleanValue.substring(0, cleanValue.length() - 1).trim();
                BigDecimal result = new BigDecimal(cleanValue);
                log.debug("Score avec % : '{}' → {}", value, result);
                return result;
            }
            
            // Sinon, parser normalement
            BigDecimal score = new BigDecimal(cleanValue);
            
            // Si la valeur est entre 0 et 1 (ex: 0.78), la convertir en pourcentage
            if (score.compareTo(BigDecimal.ZERO) >= 0 && score.compareTo(BigDecimal.ONE) <= 1) {
                BigDecimal result = score.multiply(new BigDecimal("100"));
                log.debug("Score décimal : {} → {}", score, result);
                return result;
            }
            
            log.debug("Score normal : {}", score);
            return score;
        } catch (Exception e) {
            log.warn("Impossible de parser le score: '{}' - Erreur: {}", value, e.getMessage());
            return null;
        }
    }

    // ================================================================
    // NOUVEAU : Import avec détection automatique des récurrents
    // ================================================================

    @Autowired
    private ImportAvecDetectionService importAvecDetectionService;

    /**
     * ⭐ NOUVEAU ENDPOINT : Import avec détection automatique des dossiers récurrents
     * 
     * @param file Fichier Excel à importer
     * @param moisImport Le mois d'import (format: "2024-08" ou "2024-08-15" - date complète acceptée)
     * @param authentication L'utilisateur admin connecté
     * @return Rapport d'import avec statistiques de détection
     */
    @PostMapping("/avec-detection")
    public ResponseEntity<?> importerAvecDetection(
        @RequestParam("file") MultipartFile file,
        @RequestParam("moisImport") String moisImport,
        org.springframework.security.core.Authentication authentication
    ) {
        try {
            org.eqdom.creditplatform.security.CustomUserDetails userDetails =
                (org.eqdom.creditplatform.security.CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();
            
            // Extraire le mois si une date complète est fournie (YYYY-MM-DD)
            String moisImportFinal = moisImport;
            if (moisImport.length() == 10 && moisImport.matches("\\d{4}-\\d{2}-\\d{2}")) {
                moisImportFinal = moisImport.substring(0, 7); // Extraire YYYY-MM
                log.info("Import avec détection - Date reçue: {}, Mois extrait: {}, Utilisateur: {}", 
                    moisImport, moisImportFinal, user.getEmail());
            } else {
                log.info("Import avec détection - Mois: {}, Utilisateur: {}", 
                    moisImportFinal, user.getEmail());
            }
            
            var response = importAvecDetectionService.importerAvecDetection(
                file, 
                moisImportFinal, 
                user.getId()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de l'import avec détection", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Erreur: " + e.getMessage()
            ));
        }
    }
}
