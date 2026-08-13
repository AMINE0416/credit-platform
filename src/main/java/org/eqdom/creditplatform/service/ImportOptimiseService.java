package org.eqdom.creditplatform.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eqdom.creditplatform.dto.ImportRapport;
import org.eqdom.creditplatform.model.*;
import org.eqdom.creditplatform.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service d'import optimisé pour les gros volumes (3000+ lignes)
 * 
 * Optimisations appliquées :
 * 1. Batch processing (traitement par lots de 500 lignes)
 * 2. Cache en mémoire pour éviter les requêtes répétées
 * 3. Bulk insert avec saveAll()
 * 4. Une seule transaction pour tous les lots
 * 5. Pré-chargement des données de référence
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ImportOptimiseService {

    private final ClientRepository clientRepository;
    private final CreditRepository creditRepository;
    private final DossierRepository dossierRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private static final int BATCH_SIZE = 500; // Traiter par lots de 500
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private final DataFormatter dataFormatter = new DataFormatter();

    /**
     * Import optimisé avec traitement par lots
     */
    @Transactional
    public ImportRapport importerExcelOptimise(MultipartFile file, String moisImport) {
        log.info("🚀 Début import OPTIMISÉ pour le mois: {}", moisImport);
        long startTime = System.currentTimeMillis();

        ImportRapport rapport = new ImportRapport();
        rapport.setStatut("EN_COURS");
        
        int nbInserees = 0;
        int nbMisesAJour = 0;
        int nbRejetees = 0;
        List<Map<String, String>> erreurs = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            
            if (headerRow == null) {
                rapport.setStatut("ERREUR");
                rapport.setNbRejetees(1);
                return rapport;
            }

            // 1. Créer le mapping des colonnes
            Map<String, Integer> columnMap = new HashMap<>();
            Map<String, Integer> columnMapLower = new HashMap<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                String header = getCellValue(headerRow, i).trim();
                columnMap.put(header, i);
                columnMapLower.put(header.toLowerCase(), i);
            }

            log.info("📋 Colonnes trouvées: {}", columnMap.keySet().size());

            // 2. PRÉ-CHARGEMENT : Charger tous les clients, crédits et agents existants
            log.info("⏳ Pré-chargement des données existantes...");
            Map<String, Client> clientsCache = clientRepository.findAll().stream()
                .collect(Collectors.toMap(Client::getCin, c -> c));
            
            Map<String, Credit> creditsCache = creditRepository.findAll().stream()
                .collect(Collectors.toMap(Credit::getReferenceContrat, c -> c));
            
            Map<String, User> agentsCache = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getEmail, u -> u));
            
            Set<Long> dossiersExistantsCreditIds = dossierRepository.findAll().stream()
                .map(d -> d.getCredit().getId())
                .collect(Collectors.toSet());

            log.info("✅ Cache initialisé - Clients: {}, Crédits: {}, Agents: {}, Dossiers: {}", 
                clientsCache.size(), creditsCache.size(), agentsCache.size(), dossiersExistantsCreditIds.size());

            // 3. TRAITEMENT PAR LOTS
            List<Client> clientsAInserer = new ArrayList<>();
            List<Client> clientsAMettreAJour = new ArrayList<>();
            List<Credit> creditsAInserer = new ArrayList<>();
            List<Credit> creditsAMettreAJour = new ArrayList<>();
            List<Dossier> dossiersAInserer = new ArrayList<>();
            List<User> agentsACreer = new ArrayList<>();

            int totalRows = sheet.getLastRowNum();
            log.info("📊 Nombre total de lignes à traiter: {}", totalRows);

            for (int i = 1; i <= totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    // Extraire les données de la ligne
                    String cin = getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "num_cin1").trim();
                    String referenceContrat = getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "ANC_REFDO").trim();
                    String agentEmail = getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "Agent_Email").trim();

                    if (cin.isEmpty() || referenceContrat.isEmpty()) {
                        nbRejetees++;
                        erreurs.add(Map.of(
                            "ligne", String.valueOf(i + 1),
                            "referenceContrat", referenceContrat,
                            "motif", "CIN ou référence manquante"
                        ));
                        continue;
                    }

                    // === TRAITER LE CLIENT ===
                    Client client = clientsCache.get(cin);
                    if (client == null) {
                        // Nouveau client
                        client = creerClientDepuisLigne(row, columnMap, columnMapLower, cin);
                        clientsAInserer.add(client);
                        clientsCache.put(cin, client); // Ajouter au cache
                    } else {
                        // Mettre à jour client existant
                        mettreAJourClientDepuisLigne(client, row, columnMap, columnMapLower);
                        clientsAMettreAJour.add(client);
                    }

                    // === TRAITER LE CRÉDIT ===
                    Credit credit = creditsCache.get(referenceContrat);
                    boolean creditExistait = credit != null;
                    
                    if (credit == null) {
                        // Nouveau crédit
                        credit = creerCreditDepuisLigne(row, columnMap, columnMapLower, client, referenceContrat);
                        creditsAInserer.add(credit);
                        creditsCache.put(referenceContrat, credit);
                        nbInserees++;
                    } else {
                        // Mettre à jour crédit existant
                        mettreAJourCreditDepuisLigne(credit, row, columnMap, columnMapLower);
                        creditsAMettreAJour.add(credit);
                        nbMisesAJour++;
                    }

                    // === TRAITER L'AGENT ===
                    User agent = null;
                    if (!agentEmail.isEmpty()) {
                        agent = agentsCache.get(agentEmail);
                        if (agent == null) {
                            // Créer nouvel agent
                            agent = creerAgent(agentEmail);
                            if (agent != null) {
                                agentsACreer.add(agent);
                                agentsCache.put(agentEmail, agent);
                            }
                        }
                    }

                    // === TRAITER LE DOSSIER ===
                    // Le dossier sera créé après sauvegarde des crédits (pour avoir les IDs)
                    if (!creditExistait) {
                        Dossier dossier = new Dossier();
                        dossier.setCredit(credit);
                        dossier.setStatutActuel(StatutDossier.EN_COURS);
                        dossier.setDateCreation(LocalDate.now());
                        dossier.setMoisImport(moisImport);
                        dossier.setAgentAssigne(agent);
                        dossiersAInserer.add(dossier);
                    }

                    // === SAUVEGARDE PAR LOTS ===
                    if (i % BATCH_SIZE == 0 || i == totalRows) {
                        sauvegarderLot(
                            clientsAInserer, clientsAMettreAJour,
                            creditsAInserer, creditsAMettreAJour,
                            dossiersAInserer, agentsACreer
                        );
                        
                        log.info("✅ Lot traité: {}/{} lignes ({} %)", 
                            i, totalRows, (i * 100 / totalRows));

                        // Vider les listes pour le prochain lot
                        clientsAInserer.clear();
                        clientsAMettreAJour.clear();
                        creditsAInserer.clear();
                        creditsAMettreAJour.clear();
                        dossiersAInserer.clear();
                        agentsACreer.clear();
                    }

                } catch (Exception e) {
                    nbRejetees++;
                    erreurs.add(Map.of(
                        "ligne", String.valueOf(i + 1),
                        "referenceContrat", getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "ANC_REFDO"),
                        "motif", e.getMessage()
                    ));
                    log.error("Erreur ligne {}: {}", i + 1, e.getMessage());
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("🎉 Import terminé en {} secondes", duration / 1000.0);

            rapport.setNbLignesTotal(totalRows);
            rapport.setNbInserees(nbInserees);
            rapport.setNbMisesAJour(nbMisesAJour);
            rapport.setNbRejetees(nbRejetees);
            rapport.setStatut("COMPLETED");

        } catch (Exception e) {
            log.error("❌ Erreur fatale lors de l'import", e);
            rapport.setStatut("ERREUR");
        }

        return rapport;
    }

    /**
     * Sauvegarder un lot d'entités en une seule fois
     */
    private void sauvegarderLot(
        List<Client> clientsAInserer, List<Client> clientsAMettreAJour,
        List<Credit> creditsAInserer, List<Credit> creditsAMettreAJour,
        List<Dossier> dossiersAInserer, List<User> agentsACreer
    ) {
        // Sauvegarder les agents d'abord
        if (!agentsACreer.isEmpty()) {
            userRepository.saveAll(agentsACreer);
            log.debug("  → {} agents créés", agentsACreer.size());
        }

        // Sauvegarder les clients
        if (!clientsAInserer.isEmpty()) {
            clientRepository.saveAll(clientsAInserer);
            log.debug("  → {} clients insérés", clientsAInserer.size());
        }
        if (!clientsAMettreAJour.isEmpty()) {
            clientRepository.saveAll(clientsAMettreAJour);
            log.debug("  → {} clients mis à jour", clientsAMettreAJour.size());
        }

        // Sauvegarder les crédits
        if (!creditsAInserer.isEmpty()) {
            creditRepository.saveAll(creditsAInserer);
            log.debug("  → {} crédits insérés", creditsAInserer.size());
        }
        if (!creditsAMettreAJour.isEmpty()) {
            creditRepository.saveAll(creditsAMettreAJour);
            log.debug("  → {} crédits mis à jour", creditsAMettreAJour.size());
        }

        // Sauvegarder les dossiers
        if (!dossiersAInserer.isEmpty()) {
            dossierRepository.saveAll(dossiersAInserer);
            log.debug("  → {} dossiers insérés", dossiersAInserer.size());
        }
    }

    // ============================================================
    // MÉTHODES UTILITAIRES
    // ============================================================

    private Client creerClientDepuisLigne(Row row, Map<String, Integer> columnMap, 
                                          Map<String, Integer> columnMapLower, String cin) {
        Client client = new Client();
        client.setCin(cin);
        client.setNom(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "nom"));
        client.setPrenom(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "prenom"));
        client.setTelephone(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "cli_tel"));
        client.setTelephone2(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "TEL2"));
        client.setAdresse(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "cli_adr"));
        client.setVille(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "Lib_Ville"));
        client.setCodeAgence(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "cod_age"));
        client.setLibAgence(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "lib_age"));
        client.setAdresseCadrage(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "Adresse_Cadrage"));
        return client;
    }

    private void mettreAJourClientDepuisLigne(Client client, Row row, Map<String, Integer> columnMap, 
                                               Map<String, Integer> columnMapLower) {
        client.setNom(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "nom"));
        client.setPrenom(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "prenom"));
        client.setTelephone(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "cli_tel"));
        client.setTelephone2(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "TEL2"));
        client.setAdresse(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "cli_adr"));
        client.setVille(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "Lib_Ville"));
        client.setCodeAgence(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "cod_age"));
        client.setLibAgence(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "lib_age"));
        client.setAdresseCadrage(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "Adresse_Cadrage"));
    }

    private Credit creerCreditDepuisLigne(Row row, Map<String, Integer> columnMap, 
                                          Map<String, Integer> columnMapLower, Client client, 
                                          String referenceContrat) {
        Credit credit = new Credit();
        credit.setClient(client);
        credit.setReferenceContrat(referenceContrat);
        credit.setFromProv(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "From_Prov"));
        credit.setGestCin(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "GEST_CIN"));
        credit.setTypeCredit(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "CSP_PDT"));
        credit.setSegment(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "Segment"));
        credit.setNbrImpaye(parseIntOrZero(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "nbr_imp")));
        credit.setMensualite(parseBigDecimalOrZero(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "Mensualite")));
        credit.setEncours(parseBigDecimalOrZero(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "encours")));
        credit.setImpayeMontant(parseBigDecimalOrZero(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "Mnt_Imp")));
        credit.setPrvCum(parseBigDecimalOrZero(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "prv_cum")));
        credit.setStage(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "stage"));
        credit.setDvr(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "DVR"));
        credit.setReecheance(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "Reecheance"));
        credit.setImmatriculation(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "Immatriculation"));
        credit.setScore(parseScore(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "score_final")));
        return credit;
    }

    private void mettreAJourCreditDepuisLigne(Credit credit, Row row, Map<String, Integer> columnMap, 
                                               Map<String, Integer> columnMapLower) {
        credit.setFromProv(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "From_Prov"));
        credit.setGestCin(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "GEST_CIN"));
        credit.setTypeCredit(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "CSP_PDT"));
        credit.setSegment(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "Segment"));
        credit.setNbrImpaye(parseIntOrZero(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "nbr_imp")));
        credit.setMensualite(parseBigDecimalOrZero(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "Mensualite")));
        credit.setEncours(parseBigDecimalOrZero(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "encours")));
        credit.setImpayeMontant(parseBigDecimalOrZero(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "Mnt_Imp")));
        credit.setPrvCum(parseBigDecimalOrZero(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "prv_cum")));
        credit.setStage(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "stage"));
        credit.setDvr(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "DVR"));
        credit.setReecheance(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "Reecheance"));
        credit.setImmatriculation(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "Immatriculation"));
        credit.setScore(parseScore(getCellValueByHeaderFlexible(row, columnMap, columnMapLower, "score_final")));
    }

    private User creerAgent(String email) {
        try {
            Role roleAgent = roleRepository.findByNom("AGENT").orElse(null);
            if (roleAgent == null) {
                log.error("Rôle AGENT introuvable");
                return null;
            }

            String[] parts = email.split("@")[0].split("\\.");
            String prenom = parts.length > 0 ? capitalize(parts[0]) : "Agent";
            String nom = parts.length > 1 ? parts[1].toUpperCase() : "AGENT";

            User agent = new User();
            agent.setEmail(email);
            agent.setNom(nom);
            agent.setPrenom(prenom);
            agent.setPassword(passwordEncoder.encode("Eqdom2024!"));
            agent.setActif(true);
            agent.getRoles().add(roleAgent);
            
            return agent;
        } catch (Exception e) {
            log.error("Erreur création agent {}: {}", email, e.getMessage());
            return null;
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private String getCellValue(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex);
        if (cell == null) return "";
        return dataFormatter.formatCellValue(cell).trim();
    }

    private String getCellValueByHeaderFlexible(Row row, Map<String, Integer> columnMap, 
                                                Map<String, Integer> columnMapLower, String header) {
        Integer colIndex = columnMap.get(header);
        if (colIndex == null) {
            colIndex = columnMapLower.get(header.toLowerCase());
        }
        if (colIndex == null) return "";
        return getCellValue(row, colIndex);
    }

    private int parseIntOrZero(String value) {
        try {
            if (value == null || value.trim().isEmpty()) return 0;
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
            String cleanValue = value.trim().replace(',', '.').replaceAll("\\s+", "");
            return new BigDecimal(cleanValue);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal parseScore(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            String cleanValue = value.trim();
            if (cleanValue.endsWith("%")) {
                cleanValue = cleanValue.substring(0, cleanValue.length() - 1).trim();
            }
            BigDecimal score = new BigDecimal(cleanValue);
            if (score.compareTo(BigDecimal.ZERO) >= 0 && score.compareTo(BigDecimal.ONE) <= 1) {
                return score.multiply(new BigDecimal("100"));
            }
            return score;
        } catch (Exception e) {
            return null;
        }
    }
}
