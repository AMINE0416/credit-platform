package org.eqdom.creditplatform.service;

import org.eqdom.creditplatform.dto.*;
import org.eqdom.creditplatform.exception.*;
import org.eqdom.creditplatform.model.*;
import org.eqdom.creditplatform.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service contenant la logique métier de gestion des dossiers.
 * Sépare la logique métier de la couche présentation (contrôleur).
 */
@Service
@Transactional
public class DossierService {

    @Autowired private DossierRepository dossierRepository;
    @Autowired private ActionDossierRepository actionDossierRepository;
    @Autowired private CreditRepository creditRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private TransitionService transitionService;

    /**
     * Récupère la liste des dossiers avec filtrage optionnel.
     * Les ADMIN voient tous les dossiers, les AGENT voient uniquement les leurs.
     */
    public List<DossierListItemDTO> getDossiers(
            String statut, 
            String ville, 
            Long agentId, 
            User currentUser) {

        // Charger tous les dossiers
        List<Dossier> dossiers = dossierRepository.findAll();

        // Si l'utilisateur est un AGENT, ne retourner que ses dossiers
        boolean isAgent = currentUser.getRoles().stream()
                .anyMatch(role -> "AGENT".equals(role.getNom()));

        if (isAgent) {
            dossiers = dossiers.stream()
                    .filter(d -> d.getAgentAssigne() != null 
                            && d.getAgentAssigne().getId().equals(currentUser.getId()))
                    .collect(Collectors.toList());
        }

        // Appliquer les filtres si fournis
        if (statut != null && !statut.isEmpty()) {
            StatutDossier statutEnum = StatutDossier.valueOf(statut);
            dossiers = dossiers.stream()
                    .filter(d -> d.getStatutActuel() == statutEnum)
                    .collect(Collectors.toList());
        }

        if (ville != null && !ville.isEmpty()) {
            dossiers = dossiers.stream()
                    .filter(d -> ville.equals(d.getCredit().getClient().getVille()))
                    .collect(Collectors.toList());
        }

        if (agentId != null) {
            dossiers = dossiers.stream()
                    .filter(d -> d.getAgentAssigne() != null 
                            && d.getAgentAssigne().getId().equals(agentId))
                    .collect(Collectors.toList());
        }

        // Mapper vers DTO
        return dossiers.stream()
                .map(this::mapToDossierListItemDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère uniquement les dossiers assignés à l'agent connecté.
     */
    public List<DossierListItemDTO> getMesDossiers(User currentUser) {
        List<Dossier> dossiers = dossierRepository.findByAgentAssigne(currentUser);
        
        return dossiers.stream()
                .map(this::mapToDossierListItemDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les détails complets d'un dossier.
     * @throws DossierNotFoundException si le dossier n'existe pas
     */
    public DossierDetailDTO getDossierDetail(Long id) {
        Dossier dossier = dossierRepository.findById(id)
                .orElseThrow(() -> new DossierNotFoundException("Dossier non trouvé avec l'ID: " + id));

        long nbActions = actionDossierRepository.countByDossierId(id);
        return new DossierDetailDTO(dossier, nbActions);
    }

    /**
     * Récupère les transitions autorisées et les champs requis pour un dossier.
     * @throws DossierNotFoundException si le dossier n'existe pas
     */
    public TransitionInfo getTransitionsAutorisees(Long id, String nouveauStatut) {
        Dossier dossier = dossierRepository.findById(id)
                .orElseThrow(() -> new DossierNotFoundException("Dossier non trouvé avec l'ID: " + id));

        StatutDossier nouveauStatutEnum = null;
        if (nouveauStatut != null && !nouveauStatut.isEmpty()) {
            try {
                nouveauStatutEnum = StatutDossier.valueOf(nouveauStatut);
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Statut invalide: " + nouveauStatut);
            }
        }

        return transitionService.getTransitionInfo(dossier.getStatutActuel(), nouveauStatutEnum);
    }

    /**
     * Ajoute une action à un dossier et effectue la transition de statut si nécessaire.
     * @throws DossierNotFoundException si le dossier n'existe pas
     * @throws InvalidTransitionException si la transition de statut n'est pas autorisée
     * @throws BusinessException si les champs obligatoires sont manquants
     */
    public Map<String, Object> ajouterAction(Long dossierId, ActionDossierRequest request, User currentUser) {
        // Récupérer le dossier
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new DossierNotFoundException("Dossier non trouvé avec l'ID: " + dossierId));

        // Valider la transition si un nouveau statut est demandé
        StatutDossier ancienStatut = dossier.getStatutActuel();
        StatutDossier nouveauStatut = null;

        if (request.getNouveauStatut() != null && !request.getNouveauStatut().isEmpty()) {
            try {
                nouveauStatut = StatutDossier.valueOf(request.getNouveauStatut());
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Statut invalide: " + request.getNouveauStatut());
            }

            // Vérifier que la transition est autorisée
            if (!transitionService.isTransitionAutorisee(ancienStatut, nouveauStatut)) {
                throw new InvalidTransitionException(
                    "Transition non autorisée de " + ancienStatut + " vers " + nouveauStatut
                );
            }

            // Valider les champs obligatoires selon la transition
            String validationError = transitionService.validerChampsObligatoires(
                    ancienStatut,
                    nouveauStatut,
                    request.getNatureActionStr(),
                    request.getMotifImpayeStr(),
                    request.getCompteRendu(),
                    request.getCodeResultatStr(),
                    request.getRecidiveStr(),
                    request.getMontantPromesse(),
                    request.getDatePromesse()
            );

            if (validationError != null) {
                throw new BusinessException(validationError);
            }
        }

        // Créer l'action
        ActionDossier action = new ActionDossier();
        action.setDossier(dossier);
        action.setAgent(currentUser);
        action.setNatureAction(request.getNatureAction());
        action.setMotifImpaye(request.getMotifImpaye());
        action.setCompteRendu(request.getCompteRendu());
        action.setCodeResultat(request.getCodeResultat());
        action.setRecidive(request.getRecidive());
        action.setNbreActionClient(request.getNbreActionClient());
        action.setMontantPromesse(request.getMontantPromesse());
        action.setDatePromesse(request.getDatePromesse());
        action.setNouveauTelephone(request.getNouveauTelephone());

        actionDossierRepository.save(action);

        // Mettre à jour le statut du dossier si nécessaire
        if (nouveauStatut != null) {
            dossier.setStatutActuel(nouveauStatut);
            dossierRepository.save(dossier);
        }

        return Map.of(
                "success", true,
                "message", "Action ajoutée avec succès",
                "actionId", action.getId(),
                "ancienStatut", ancienStatut.name(),
                "nouveauStatut", nouveauStatut != null ? nouveauStatut.name() : ancienStatut.name()
        );
    }

    /**
     * Affecte ou réaffecte plusieurs dossiers à un agent.
     * @throws UserNotFoundException si l'agent n'existe pas
     * @throws DossierNotFoundException si un des dossiers n'existe pas
     */
    public Map<String, Object> affecterDossiers(List<Long> dossierIds, Long agentId) {
        // Récupérer l'agent
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new UserNotFoundException("Agent non trouvé avec l'ID: " + agentId));

        int nbAffectes = 0;

        for (Long dossierId : dossierIds) {
            Dossier dossier = dossierRepository.findById(dossierId)
                    .orElseThrow(() -> new DossierNotFoundException("Dossier non trouvé avec l'ID: " + dossierId));

            dossier.setAgentAssigne(agent);
            dossierRepository.save(dossier);
            nbAffectes++;
        }

        return Map.of(
                "success", true,
                "message", nbAffectes + " dossier(s) affecté(s) à " + agent.getPrenom() + " " + agent.getNom(),
                "nbAffectes", nbAffectes
        );
    }

    /**
     * Récupère l'historique des actions d'un dossier.
     * @throws DossierNotFoundException si le dossier n'existe pas
     */
    public List<ActionDossier> getActions(Long dossierId) {
        // Vérifier que le dossier existe
        if (!dossierRepository.existsById(dossierId)) {
            throw new DossierNotFoundException("Dossier non trouvé avec l'ID: " + dossierId);
        }

        return actionDossierRepository.findByDossierIdOrderByDateActionDesc(dossierId);
    }

    /**
     * Mapper un Dossier vers un DossierListItemDTO.
     */
    private DossierListItemDTO mapToDossierListItemDTO(Dossier dossier) {
        Credit credit = dossier.getCredit();
        Client client = credit.getClient();

        String agentNom = dossier.getAgentAssigne() != null
                ? dossier.getAgentAssigne().getPrenom() + " " + dossier.getAgentAssigne().getNom()
                : "Non assigné";

        return new DossierListItemDTO(
                dossier.getId(),
                client.getCin(),
                client.getPrenom() + " " + client.getNom(),
                credit.getReferenceContrat(),
                dossier.getStatutActuel().name(),
                client.getVille(),
                agentNom
        );
    }
}
