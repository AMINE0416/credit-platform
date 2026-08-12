package org.eqdom.creditplatform.service;

import org.eqdom.creditplatform.model.*;
import org.eqdom.creditplatform.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Service pour gérer l'archivage historique des dossiers
 * Permet de sauvegarder un snapshot complet des données avant chaque nouvel import
 */
@Service
public class HistoriqueService {

    private static final Logger log = LoggerFactory.getLogger(HistoriqueService.class);

    @Autowired private DossierHistoriqueRepository historiqueRepository;
    @Autowired private DossierRepository dossierRepository;
    @Autowired private ActionDossierRepository actionDossierRepository;

    /**
     * Archive tous les dossiers actuels dans la table historique pour un mois donné
     * Cette méthode doit être appelée AVANT chaque import de nouvelles données
     * 
     * @param mois Le numéro du mois (1-12)
     * @param annee L'année (ex: 2024)
     * @return Un rapport de l'archivage
     */
    @Transactional
    public Map<String, Object> archiverDossiersPourMois(Integer mois, Integer annee) {
        log.info("📦 Début archivage des dossiers pour {}/{}", mois, annee);
        
        Map<String, Object> rapport = new HashMap<>();
        
        try {
            // Vérifier si un archivage existe déjà pour ce mois
            if (historiqueRepository.existsByMoisAndAnnee(mois, annee)) {
                log.warn("⚠️  Un archivage existe déjà pour {}/{}", mois, annee);
                rapport.put("success", false);
                rapport.put("message", "Un archivage existe déjà pour ce mois");
                rapport.put("mois", mois);
                rapport.put("annee", annee);
                return rapport;
            }
            
            // Récupérer tous les dossiers actuels
            List<Dossier> dossiersActuels = dossierRepository.findAll();
            log.info("📊 Nombre de dossiers à archiver: {}", dossiersActuels.size());
            
            int nbArchives = 0;
            LocalDateTime dateArchivage = LocalDateTime.now();
            
            for (Dossier dossier : dossiersActuels) {
                try {
                    DossierHistorique historique = new DossierHistorique();
                    
                    // Informations période
                    historique.setMois(mois);
                    historique.setAnnee(annee);
                    historique.setDateArchivage(dateArchivage);
                    
                    // Informations dossier
                    historique.setDossierIdOrigine(dossier.getId());
                    historique.setStatutDossier(dossier.getStatutActuel().name());
                    
                    if (dossier.getAgentAssigne() != null) {
                        historique.setAgentAssigneEmail(dossier.getAgentAssigne().getEmail());
                        historique.setAgentAssigneNom(
                            dossier.getAgentAssigne().getPrenom() + " " + 
                            dossier.getAgentAssigne().getNom()
                        );
                    }
                    
                    // Informations client
                    Client client = dossier.getCredit().getClient();
                    historique.setCin(client.getCin());
                    historique.setNom(client.getNom());
                    historique.setPrenom(client.getPrenom());
                    historique.setTelephone(client.getTelephone());
                    historique.setTelephone2(client.getTelephone2());
                    historique.setAdresse(client.getAdresse());
                    historique.setVille(client.getVille());
                    historique.setCodeAgence(client.getCodeAgence());
                    historique.setLibAgence(client.getLibAgence());
                    historique.setAdresseCadrage(client.getAdresseCadrage());
                    
                    // Informations crédit
                    Credit credit = dossier.getCredit();
                    historique.setReferenceContrat(credit.getReferenceContrat());
                    historique.setFromProv(credit.getFromProv());
                    historique.setGestCin(credit.getGestCin());
                    historique.setTypeCredit(credit.getTypeCredit());
                    historique.setSegment(credit.getSegment());
                    historique.setNbrImpaye(credit.getNbrImpaye());
                    historique.setMensualite(credit.getMensualite());
                    historique.setEncours(credit.getEncours());
                    historique.setImpayeMontant(credit.getImpayeMontant());
                    historique.setPrvCum(credit.getPrvCum());
                    historique.setStage(credit.getStage());
                    historique.setDvr(credit.getDvr());
                    historique.setReecheance(credit.getReecheance());
                    historique.setImmatriculation(credit.getImmatriculation());
                    historique.setScore(credit.getScore());
                    
                    // Statistiques actions
                    List<ActionDossier> actions = actionDossierRepository.findByDossierIdOrderByDateActionDesc(dossier.getId());
                    historique.setNbActionsTotal(actions.size());
                    if (!actions.isEmpty()) {
                        historique.setDateDerniereAction(actions.get(0).getDateAction());
                    }
                    
                    historiqueRepository.save(historique);
                    nbArchives++;
                    
                } catch (Exception e) {
                    log.error("❌ Erreur lors de l'archivage du dossier {}: {}", 
                        dossier.getId(), e.getMessage());
                }
            }
            
            log.info("✅ Archivage terminé: {} dossiers archivés pour {}/{}", 
                nbArchives, mois, annee);
            
            rapport.put("success", true);
            rapport.put("message", "Archivage réussi");
            rapport.put("mois", mois);
            rapport.put("annee", annee);
            rapport.put("nbDossiersArchives", nbArchives);
            rapport.put("dateArchivage", dateArchivage);
            
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'archivage: {}", e.getMessage(), e);
            rapport.put("success", false);
            rapport.put("message", "Erreur: " + e.getMessage());
        }
        
        return rapport;
    }
    
    /**
     * Récupère la liste des périodes archivées (mois/année)
     */
    public List<Map<String, Object>> getPeriodesArchivees() {
        List<Object[]> periodes = historiqueRepository.findDistinctMoisAnnee();
        return periodes.stream()
            .map(p -> {
                Map<String, Object> map = new HashMap<>();
                map.put("annee", p[0]);
                map.put("mois", p[1]);
                long count = historiqueRepository.countByMoisAndAnnee((Integer) p[1], (Integer) p[0]);
                map.put("nbDossiers", count);
                return map;
            })
            .toList();
    }
    
    /**
     * Récupère tous les dossiers archivés pour une période donnée
     */
    public List<DossierHistorique> getDossiersArchives(Integer mois, Integer annee) {
        return historiqueRepository.findByMoisAndAnnee(mois, annee);
    }
    
    /**
     * Récupère l'historique complet d'un dossier à travers les différents mois
     */
    public List<DossierHistorique> getHistoriqueDossier(Long dossierIdOrigine) {
        return historiqueRepository.findByDossierIdOrigineOrderByAnneeDescMoisDesc(dossierIdOrigine);
    }
    
    /**
     * Compte le nombre total d'entrées d'historique
     */
    public long countAll() {
        return historiqueRepository.count();
    }
    
    /**
     * Supprime toutes les entrées d'historique
     * ⚠️ ATTENTION: Opération irréversible
     */
    @Transactional
    public void deleteAll() {
        log.warn("⚠️ Suppression de tous les historiques");
        historiqueRepository.deleteAll();
    }
}
