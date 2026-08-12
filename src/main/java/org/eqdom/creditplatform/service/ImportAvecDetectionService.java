package org.eqdom.creditplatform.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eqdom.creditplatform.dto.ImportAvecDetectionResponse;
import org.eqdom.creditplatform.model.*;
import org.eqdom.creditplatform.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImportAvecDetectionService {

    private final DossierMoisPrecedentRepository moisPrecedentRepository;
    private final DossierRepository dossierRepository;
    private final CreditRepository creditRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final LogArchivageRepository logArchivageRepository;
    private final ExcelImportService excelImportService; // Service existant pour lire Excel
    private final PasswordEncoder passwordEncoder; // Pour encoder les mots de passe des nouveaux agents

    private static final DateTimeFormatter MOIS_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    /**
     * Enregistrer un log dans une transaction séparée (pour éviter les problèmes de rollback)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logImportDansNouvelleTransaction(String operation, String moisConcerne, 
                                                   Integer nbDossiers, Integer nbRecurrents, 
                                                   Integer nbNouveaux, Long userId, 
                                                   String details, String statut) {
        try {
            logArchivageRepository.save(LogArchivage.builder()
                .operation(operation)
                .moisConcerne(moisConcerne)
                .nbDossiers(nbDossiers)
                .nbRecurrents(nbRecurrents)
                .nbNouveaux(nbNouveaux)
                .utilisateurId(userId)
                .details(details)
                .statut(statut)
                .build());
        } catch (Exception e) {
            log.error("Impossible d'enregistrer le log (transaction séparée): {}", e.getMessage());
        }
    }

    /**
     * Importer un fichier Excel avec détection automatique des dossiers récurrents
     */
    @Transactional
    public ImportAvecDetectionResponse importerAvecDetection(
        MultipartFile file, 
        String moisImport,
        Long userId
    ) {
        log.info("Début import avec détection pour le mois: {}", moisImport);

        // Statistiques
        int totalDossiers = 0;
        int nouveaux = 0;
        int recurrents = 0;
        int erreurs = 0;
        Map<String, Integer> repartitionStatuts = new HashMap<>();
        Map<String, Integer> repartitionRecurrents = new HashMap<>();

        try {
            // 1. Lire le fichier Excel (utiliser le service existant ou créer une méthode)
            List<Map<String, Object>> lignesExcel = excelImportService.lireFichierExcel(file);
            
            log.info("Nombre de lignes dans le fichier: {}", lignesExcel.size());

            // 2. Pour chaque ligne, vérifier si dossier récurrent
            for (Map<String, Object> ligne : lignesExcel) {
                try {
                    String numeroDossier = (String) ligne.get("referenceContrat");
                    
                    log.debug("Traitement ligne - referenceContrat: '{}', cin: '{}'", 
                        numeroDossier, ligne.get("cin"));
                    
                    if (numeroDossier == null || numeroDossier.trim().isEmpty()) {
                        log.warn("Ligne ignorée: numéro dossier vide - CIN: '{}'", ligne.get("cin"));
                        erreurs++;
                        continue;
                    }

                    totalDossiers++;

                    // Vérifier si récurrent
                    Optional<DossierMoisPrecedent> precedent = 
                        moisPrecedentRepository.findByNumeroDossier(numeroDossier);

                    boolean isRecurrent = precedent.isPresent();
                    String statutAuto = isRecurrent 
                        ? calculerStatutRecurrent(precedent.get().getStatutFinal())
                        : "EN_COURS"; // Tous les nouveaux dossiers commencent EN_COURS

                    if (isRecurrent) {
                        recurrents++;
                        repartitionRecurrents.merge(statutAuto, 1, Integer::sum);
                        log.debug("Dossier récurrent détecté: {} -> statut: {}", 
                            numeroDossier, statutAuto);
                    } else {
                        nouveaux++;
                    }

                    repartitionStatuts.merge(statutAuto, 1, Integer::sum);

                    // 3. Créer le dossier avec les bonnes informations
                    creerDossier(ligne, moisImport, isRecurrent, 
                        isRecurrent ? precedent.get().getStatutFinal() : null,
                        statutAuto,
                        isRecurrent ? precedent.get() : null);
                        
                } catch (Exception e) {
                    log.error("❌ Erreur traitement ligne (ref: {}): {}", 
                        ligne.get("referenceContrat"), e.getMessage(), e);
                    erreurs++;
                    // Continuer avec les autres lignes même si une échoue
                }
            }

            log.info("Import terminé - Total: {}, Nouveaux: {}, Récurrents: {}, Erreurs: {}", 
                totalDossiers, nouveaux, recurrents, erreurs);

            // 4. Logger l'opération dans une nouvelle transaction pour éviter les problèmes de rollback
            logImportDansNouvelleTransaction(
                "IMPORT",
                moisImport,
                totalDossiers,
                recurrents,
                nouveaux,
                userId,
                "Import avec détection automatique" + (erreurs > 0 ? " - " + erreurs + " erreurs" : ""),
                erreurs == 0 ? "SUCCESS" : "PARTIAL_SUCCESS"
            );

            return ImportAvecDetectionResponse.builder()
                .success(true)
                .message("Import réussi avec détection automatique" + (erreurs > 0 ? " (" + erreurs + " lignes en erreur)" : ""))
                .moisImporte(moisImport)
                .totalDossiers(totalDossiers)
                .nouveauxDossiers(nouveaux)
                .dossiersRecurrents(recurrents)
                .repartitionStatuts(repartitionStatuts)
                .repartitionRecurrents(repartitionRecurrents)
                .build();

        } catch (Exception e) {
            log.error("Erreur critique lors de l'import avec détection", e);
            
            // Logger l'erreur dans une nouvelle transaction
            logImportDansNouvelleTransaction(
                "IMPORT",
                moisImport,
                null,
                null,
                null,
                userId,
                "Erreur: " + e.getMessage(),
                "ERROR"
            );

            throw new RuntimeException("Erreur import: " + e.getMessage(), e);
        }
    }

    /**
     * Calculer le statut automatique pour un dossier récurrent lors de l'import
     * Nouvelle logique simplifiée :
     * - Tous les nouveaux dossiers commencent EN_COURS
     * - Les dossiers récurrents héritent des statuts de suivi créés lors de l'archivage :
     *   - ALERTE (était EN_COURS non résolu)
     *   - SUIVI_PROMESSE (avait une promesse)
     *   - SUIVI_INJOIGNABLE (était injoignable)
     *   - REGULARISE (reste régularisé)
     */
    private String calculerStatutRecurrent(String ancienStatut) {
        if (ancienStatut == null) {
            return "EN_COURS";
        }

        // Les statuts de suivi restent tels quels
        return switch (ancienStatut.toUpperCase()) {
            case "ALERTE", "SUIVI_PROMESSE", "SUIVI_INJOIGNABLE", "REGULARISE" -> ancienStatut;
            default -> "EN_COURS";  // Par défaut, tous les nouveaux → EN_COURS
        };
    }

    /**
     * Créer un dossier à partir d'une ligne Excel
     */
    private void creerDossier(
        Map<String, Object> ligne,
        String moisImport,
        boolean isRecurrent,
        String ancienStatut,
        String nouveauStatut,
        DossierMoisPrecedent dossierPrecedent
    ) {
        try {
            log.debug("=== Début création dossier ===");
            log.debug("  Référence: {}, CIN: {}", ligne.get("referenceContrat"), ligne.get("cin"));
            log.debug("  isRecurrent: {}, nouveauStatut: {}", isRecurrent, nouveauStatut);
            
            // 1. Créer ou récupérer le client
            Client client = creerOuMettreAJourClient(ligne);
            if (client == null) {
                log.warn("Client null (CIN vide), dossier ignoré");
                return;
            }
            log.debug("  Client ID: {}, CIN: {}", client.getId(), client.getCin());
            
            // 2. Créer le crédit
            Credit credit = creerCredit(ligne, client);
            log.debug("  Credit ID: {}, Référence: {}", credit.getId(), credit.getReferenceContrat());
            
            // 3. Récupérer ou créer l'agent à assigner depuis l'Excel
            User agentAssigne = null;
            String agentEmail = (String) ligne.get("agentEmail");
            log.info("  📧 Email agent depuis Excel: '{}'", agentEmail);
            
            if (agentEmail != null && !agentEmail.trim().isEmpty()) {
                Optional<User> agent = userRepository.findByEmail(agentEmail.trim());
                if (agent.isPresent()) {
                    agentAssigne = agent.get();
                    log.info("  ✅ Agent trouvé: {} {} ({})", 
                        agentAssigne.getPrenom(), agentAssigne.getNom(), agentAssigne.getEmail());
                } else {
                    // Créer automatiquement l'agent s'il n'existe pas
                    log.info("  🆕 Création automatique de l'agent: {}", agentEmail);
                    agentAssigne = creerAgentAutomatiquement(agentEmail.trim());
                    if (agentAssigne != null) {
                        log.info("  ✅ Agent créé avec succès: {} (ID: {})", agentEmail, agentAssigne.getId());
                    } else {
                        log.error("  ❌ Échec de création de l'agent: {}", agentEmail);
                    }
                }
            } else {
                log.warn("  ⚠️ Aucun email d'agent fourni dans le fichier Excel pour ce dossier");
            }
            
            // 4. Vérifier si un dossier existe déjà pour ce crédit
            Optional<Dossier> dossierExistant = dossierRepository.findByCreditId(credit.getId());
            if (dossierExistant.isPresent()) {
                log.info("  ⚠️ Dossier existant trouvé pour crédit {}, mise à jour uniquement", credit.getId());
                Dossier d = dossierExistant.get();
                d.setMoisImport(moisImport);
                d.setEstRecurrent(isRecurrent);
                if (agentAssigne != null) {
                    d.setAgentAssigne(agentAssigne);
                }
                if (isRecurrent) {
                    d.setMoisPrecedentStatut(ancienStatut);
                    // Ne changer le statut que si c'est récurrent
                    d.setStatutActuel(StatutDossier.valueOf(nouveauStatut));
                    
                    // PRÉSERVER LES INFORMATIONS DE PROMESSE si le dossier précédent en avait
                    if (dossierPrecedent != null) {
                        if (dossierPrecedent.getDatePromesse() != null) {
                            d.setDatePromesse(dossierPrecedent.getDatePromesse());
                            log.info("  📅 Promesse préservée - Date: {}", dossierPrecedent.getDatePromesse());
                        }
                        if (dossierPrecedent.getMontantPromesse() != null) {
                            d.setMontantPromesse(dossierPrecedent.getMontantPromesse().doubleValue());
                            log.info("  💰 Promesse préservée - Montant: {}", dossierPrecedent.getMontantPromesse());
                        }
                    }
                }
                dossierRepository.save(d);
                log.debug("  ✅ Dossier existant mis à jour");
                return;
            }
            
            // 5. Créer le nouveau dossier
            Dossier dossier = new Dossier();
            dossier.setCredit(credit);
            dossier.setStatutActuel(StatutDossier.valueOf(nouveauStatut));
            dossier.setDateCreation(LocalDate.now());
            dossier.setMoisImport(moisImport);
            dossier.setEstRecurrent(isRecurrent);
            dossier.setMoisPrecedentStatut(ancienStatut);
            dossier.setAgentAssigne(agentAssigne);
            
            // PRÉSERVER LES INFORMATIONS DE PROMESSE si récurrent et si le dossier précédent en avait
            if (isRecurrent && dossierPrecedent != null) {
                if (dossierPrecedent.getDatePromesse() != null) {
                    dossier.setDatePromesse(dossierPrecedent.getDatePromesse());
                    log.info("  📅 Promesse préservée - Date: {}", dossierPrecedent.getDatePromesse());
                }
                if (dossierPrecedent.getMontantPromesse() != null) {
                    dossier.setMontantPromesse(dossierPrecedent.getMontantPromesse().doubleValue());
                    log.info("  💰 Promesse préservée - Montant: {}", dossierPrecedent.getMontantPromesse());
                }
            }
            
            Dossier saved = dossierRepository.save(dossier);
            log.info("  ✅ Nouveau dossier créé - ID: {}, Statut: {}, Agent: {}", 
                saved.getId(), saved.getStatutActuel(), 
                agentAssigne != null ? agentAssigne.getEmail() : "Non assigné");
            
        } catch (Exception e) {
            log.error("❌ Erreur lors de la création du dossier pour référence {}: {}", 
                ligne.get("referenceContrat"), e.getMessage(), e);
            // Ne pas propager l'exception pour continuer l'import des autres lignes
        }
    }


    private Client creerOuMettreAJourClient(Map<String, Object> ligne) {
        String cin = (String) ligne.get("cin");

        if (cin == null || cin.trim().isEmpty()) {
            log.warn("CIN vide, ligne ignorée");
            return null;
        }

        Optional<Client> existant = clientRepository.findByCin(cin.trim());

        if (existant.isPresent()) {
            // Mettre à jour les données du client existant
            Client c = existant.get();
            c.setNom((String) ligne.get("nom"));
            c.setPrenom((String) ligne.get("prenom"));
            c.setTelephone((String) ligne.get("telephone"));
            c.setTelephone2((String) ligne.get("telephone2"));
            c.setAdresse((String) ligne.get("adresse"));
            c.setVille((String) ligne.get("ville"));
            c.setCodeAgence((String) ligne.get("codeAgence"));
            c.setLibAgence((String) ligne.get("libAgence"));
            c.setAdresseCadrage((String) ligne.get("adresseCadrage"));
            return clientRepository.save(c);
        }

        Client client = new Client();
        client.setCin(cin.trim());
        client.setNom((String) ligne.get("nom"));
        client.setPrenom((String) ligne.get("prenom"));
        client.setTelephone((String) ligne.get("telephone"));
        client.setTelephone2((String) ligne.get("telephone2"));
        client.setAdresse((String) ligne.get("adresse"));
        client.setVille((String) ligne.get("ville"));
        client.setCodeAgence((String) ligne.get("codeAgence"));
        client.setLibAgence((String) ligne.get("libAgence"));
        client.setAdresseCadrage((String) ligne.get("adresseCadrage"));

        return clientRepository.save(client);
    }

    private Credit creerCredit(Map<String, Object> ligne, Client client) {
        String referenceContrat = (String) ligne.get("referenceContrat");
        if (referenceContrat != null) referenceContrat = referenceContrat.trim();

        log.debug("    Création/MAJ crédit - Référence: '{}', Client ID: {}", referenceContrat, client.getId());
        
        // Vérifier si un crédit avec cette référence existe déjà pour ce client
        Optional<Credit> existant = creditRepository
            .findByReferenceContratAndClientId(referenceContrat, client.getId());

        Credit credit = existant.orElseGet(Credit::new);
        credit.setClient(client);
        credit.setReferenceContrat(referenceContrat);
        credit.setFromProv((String) ligne.get("fromProv"));
        credit.setGestCin((String) ligne.get("gestCin"));
        credit.setTypeCredit((String) ligne.get("typeCredit"));
        credit.setSegment((String) ligne.get("segment"));
        credit.setNbrImpaye((Integer) ligne.get("nbrImpaye"));
        credit.setMensualite((java.math.BigDecimal) ligne.get("mensualite"));
        credit.setEncours((java.math.BigDecimal) ligne.get("encours"));
        credit.setImpayeMontant((java.math.BigDecimal) ligne.get("impayeMontant"));
        credit.setPrvCum((java.math.BigDecimal) ligne.get("prvCum"));
        credit.setStage((String) ligne.get("stage"));
        credit.setDvr((String) ligne.get("dvr"));
        credit.setReecheance((String) ligne.get("reecheance"));
        credit.setImmatriculation((String) ligne.get("immatriculation"));
        credit.setScore((java.math.BigDecimal) ligne.get("score"));

        Credit saved = creditRepository.save(credit);
        log.debug("    Crédit sauvegardé - ID: {}, existait: {}", saved.getId(), existant.isPresent());
        
        return saved;
    }


    /**
     * Créer automatiquement un agent à partir de son email
     * Extrait le nom et prénom depuis l'email si possible
     */
    private User creerAgentAutomatiquement(String email) {
        try {
            // Extraire le nom et prénom depuis l'email (ex: hassan.alami@eqdom.ma -> Hassan ALAMI)
            String[] parts = email.split("@")[0].split("\\.");
            String prenom = parts.length > 0 ? capitalizeFirst(parts[0]) : "Agent";
            String nom = parts.length > 1 ? parts[1].toUpperCase() : "AGENT";
            
            // Chercher le rôle AGENT
            Optional<Role> roleAgent = roleRepository.findByNom("AGENT");
            if (!roleAgent.isPresent()) {
                log.error("Rôle AGENT introuvable dans la base de données");
                return null;
            }
            
            // Créer l'utilisateur avec le rôle
            User agent = new User();
            agent.setEmail(email);
            agent.setNom(nom);
            agent.setPrenom(prenom);
            agent.setActif(true);
            // Mot de passe par défaut encodé avec BCrypt (password123)
            agent.setPassword(passwordEncoder.encode("password123"));
            
            // Ajouter le rôle AGENT (la collection est déjà initialisée dans le constructeur)
            agent.getRoles().add(roleAgent.get());
            
            User savedAgent = userRepository.save(agent);
            log.info("Agent créé avec succès: {} {} ({}), ID: {}, mot de passe: password123", 
                savedAgent.getPrenom(), savedAgent.getNom(), email, savedAgent.getId());
            
            return savedAgent;
            
        } catch (Exception e) {
            log.error("Erreur lors de la création automatique de l'agent {}: {}", email, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Mettre en majuscule la première lettre d'une chaîne
     */
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    /**
     * Nettoyer la table temporaire des mois précédents
     */
    @Transactional
    public void nettoyerMoisPrecedents() {
        log.info("Nettoyage de la table dossiers_mois_precedent");
        moisPrecedentRepository.deleteAllDossiers();
    }
}
