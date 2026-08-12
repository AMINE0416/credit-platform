package org.eqdom.creditplatform.service;

import org.eqdom.creditplatform.dto.TransitionInfo;
import org.eqdom.creditplatform.model.StatutDossier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service gérant la logique des transitions de statut et des champs requis
 * Basé sur la matrice définie dans la documentation métier
 */
@Service
public class TransitionService {

    // Map des transitions autorisées (logique simplifiée)
    private static final Map<StatutDossier, Set<StatutDossier>> TRANSITIONS_AUTORISEES = Map.of(
            // Statuts de travail actifs
            StatutDossier.EN_COURS, Set.of(StatutDossier.PROMESSE, StatutDossier.INJOIGNABLE, StatutDossier.REGULARISE),
            StatutDossier.PROMESSE, Set.of(StatutDossier.REGULARISE, StatutDossier.EN_COURS),
            StatutDossier.INJOIGNABLE, Set.of(StatutDossier.EN_COURS),
            StatutDossier.REGULARISE, Set.of(StatutDossier.CLOTURE),
            
            // Statuts de suivi (créés lors de l'archivage, peuvent retourner aux statuts actifs)
            StatutDossier.ALERTE, Set.of(StatutDossier.EN_COURS, StatutDossier.PROMESSE, StatutDossier.INJOIGNABLE, StatutDossier.REGULARISE),
            StatutDossier.SUIVI_PROMESSE, Set.of(StatutDossier.REGULARISE, StatutDossier.EN_COURS, StatutDossier.PROMESSE),
            StatutDossier.SUIVI_INJOIGNABLE, Set.of(StatutDossier.EN_COURS, StatutDossier.PROMESSE, StatutDossier.INJOIGNABLE, StatutDossier.REGULARISE),
            
            // Statut final (aucune transition)
            StatutDossier.CLOTURE, Set.of()
    );

    /**
     * Récupère les transitions autorisées pour un statut donné
     */
    public Set<StatutDossier> getTransitionsAutorisees(StatutDossier statutActuel) {
        return TRANSITIONS_AUTORISEES.getOrDefault(statutActuel, Set.of());
    }

    /**
     * Valide qu'une transition est autorisée
     */
    public boolean isTransitionAutorisee(StatutDossier statutActuel, StatutDossier nouveauStatut) {
        Set<StatutDossier> transitions = getTransitionsAutorisees(statutActuel);
        return transitions.contains(nouveauStatut);
    }

    /**
     * Récupère les informations de transition (transitions autorisées + champs requis par transition)
     */
    public TransitionInfo getTransitionInfo(StatutDossier statutActuel, StatutDossier nouveauStatut) {
        Set<String> transitionsAutorisees = getTransitionsAutorisees(statutActuel)
                .stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        // Si nouveauStatut est null, retourner seulement les transitions autorisées
        TransitionInfo.ChampsRequis champsRequis = nouveauStatut != null 
                ? determinerChampsRequis(statutActuel, nouveauStatut)
                : new TransitionInfo.ChampsRequis();
        
        return new TransitionInfo(transitionsAutorisees, champsRequis);
    }

    /**
     * Détermine quels champs sont obligatoires/visibles selon la transition
     */
    private TransitionInfo.ChampsRequis determinerChampsRequis(StatutDossier depart, StatutDossier arrivee) {
        TransitionInfo.ChampsRequis champs = new TransitionInfo.ChampsRequis();

        // Par défaut : compte rendu obligatoire
        champs.setCompteRenduObligatoire(true);

        switch (depart) {
            case EN_COURS:
                if (arrivee == StatutDossier.PROMESSE) {
                    champs.setMotifImpayeVisible(true);
                    champs.setMotifImpayeObligatoire(false);  // Optionnel
                    champs.setMontantPromesseVisible(true);
                    champs.setMontantPromesseObligatoire(true);
                    champs.setDatePromesseVisible(true);
                    champs.setDatePromesseObligatoire(true);
                } else if (arrivee == StatutDossier.INJOIGNABLE) {
                    champs.setCompteRenduObligatoire(false);
                    champs.setRecidiveVisible(true);
                    champs.setRecidiveObligatoire(true);
                } else if (arrivee == StatutDossier.REGULARISE) {
                    champs.setCompteRenduObligatoire(true);
                }
                break;

            case PROMESSE:
                if (arrivee == StatutDossier.REGULARISE) {
                    champs.setCompteRenduObligatoire(true);
                } else if (arrivee == StatutDossier.EN_COURS) {  // Promesse rompue
                    champs.setMotifImpayeVisible(true);
                    champs.setMotifImpayeObligatoire(false);
                    champs.setCompteRenduObligatoire(true);
                }
                break;

            case INJOIGNABLE:
                if (arrivee == StatutDossier.EN_COURS) {
                    champs.setMotifImpayeVisible(true);
                    champs.setMotifImpayeObligatoire(false);
                    champs.setCompteRenduObligatoire(true);
                }
                break;

            case ALERTE:
                // Depuis ALERTE, on peut aller vers tous les statuts actifs
                if (arrivee == StatutDossier.PROMESSE) {
                    champs.setMotifImpayeVisible(true);
                    champs.setMotifImpayeObligatoire(false);
                    champs.setMontantPromesseVisible(true);
                    champs.setMontantPromesseObligatoire(true);
                    champs.setDatePromesseVisible(true);
                    champs.setDatePromesseObligatoire(true);
                } else if (arrivee == StatutDossier.INJOIGNABLE) {
                    champs.setCompteRenduObligatoire(false);
                    champs.setRecidiveVisible(true);
                    champs.setRecidiveObligatoire(true);
                }
                break;

            case SUIVI_PROMESSE:
                if (arrivee == StatutDossier.REGULARISE) {
                    champs.setCompteRenduObligatoire(true);
                } else if (arrivee == StatutDossier.EN_COURS) {
                    champs.setMotifImpayeVisible(true);
                    champs.setMotifImpayeObligatoire(false);
                    champs.setCompteRenduObligatoire(true);
                } else if (arrivee == StatutDossier.PROMESSE) {
                    champs.setMontantPromesseVisible(true);
                    champs.setMontantPromesseObligatoire(true);
                    champs.setDatePromesseVisible(true);
                    champs.setDatePromesseObligatoire(true);
                }
                break;

            case SUIVI_INJOIGNABLE:
                if (arrivee == StatutDossier.EN_COURS) {
                    champs.setMotifImpayeVisible(true);
                    champs.setMotifImpayeObligatoire(false);
                    champs.setCompteRenduObligatoire(true);
                } else if (arrivee == StatutDossier.PROMESSE) {
                    champs.setMotifImpayeVisible(true);
                    champs.setMotifImpayeObligatoire(false);
                    champs.setMontantPromesseVisible(true);
                    champs.setMontantPromesseObligatoire(true);
                    champs.setDatePromesseVisible(true);
                    champs.setDatePromesseObligatoire(true);
                } else if (arrivee == StatutDossier.INJOIGNABLE) {
                    champs.setCompteRenduObligatoire(false);
                    champs.setRecidiveVisible(true);
                    champs.setRecidiveObligatoire(true);
                }
                break;

            case REGULARISE:
                if (arrivee == StatutDossier.CLOTURE) {
                    champs.setCompteRenduObligatoire(false);
                }
                break;

            case CLOTURE:
                // Aucune transition possible
                break;
        }

        return champs;
    }

    /**
     * Valide que les champs obligatoires sont bien fournis pour une transition donnée
     */
    public String validerChampsObligatoires(
            StatutDossier statutActuel,
            StatutDossier nouveauStatut,
            String natureAction,
            String motifImpaye,
            String compteRendu,
            String codeResultat,
            String recidive,
            java.math.BigDecimal montantPromesse,
            java.time.LocalDate datePromesse
    ) {
        TransitionInfo.ChampsRequis champs = determinerChampsRequis(statutActuel, nouveauStatut);

        // Nature Action toujours obligatoire
        if (natureAction == null || natureAction.trim().isEmpty()) {
            return "Nature Action est obligatoire";
        }

        // Code Résultat toujours obligatoire
        if (codeResultat == null || codeResultat.trim().isEmpty()) {
            return "Code résultat est obligatoire";
        }

        // Motif Impayé selon la transition (seulement si visible ET obligatoire)
        if (champs.isMotifImpayeVisible() && champs.isMotifImpayeObligatoire()) {
            if (motifImpaye == null || motifImpaye.trim().isEmpty()) {
                return "Motif Impayé est obligatoire pour cette transition";
            }
        }

        // Compte rendu selon la transition
        if (champs.isCompteRenduObligatoire()) {
            if (compteRendu == null || compteRendu.trim().isEmpty()) {
                return "Compte rendu est obligatoire pour cette transition";
            }
        }

        // Récidive si requise (vérifier que ce n'est pas une chaîne vide)
        if (champs.isRecidiveObligatoire()) {
            if (recidive == null || recidive.trim().isEmpty()) {
                return "Récidive est obligatoire pour cette transition";
            }
        }

        // Promesse si requise
        if (champs.isMontantPromesseObligatoire()) {
            if (montantPromesse == null || montantPromesse.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                return "Montant de la promesse est obligatoire et doit être supérieur à 0";
            }
        }
        if (champs.isDatePromesseObligatoire()) {
            if (datePromesse == null) {
                return "Date de la promesse est obligatoire";
            }
        }

        return null;  // Tout est valide
    }
}
