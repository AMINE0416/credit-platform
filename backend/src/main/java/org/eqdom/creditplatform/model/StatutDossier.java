package org.eqdom.creditplatform.model;

public enum StatutDossier {
    // Statuts de travail actifs (utilisés par les agents)
    EN_COURS,           // Dossier en cours de traitement (point de départ pour tous les imports)
    PROMESSE,           // Client a fait une promesse de paiement
    INJOIGNABLE,        // Client injoignable
    REGULARISE,         // Dossier régularisé (paiement effectué)
    
    // Statuts de suivi mensuel (créés automatiquement lors de l'archivage)
    ALERTE,             // Récurrent : était EN_COURS le mois précédent (non résolu)
    SUIVI_PROMESSE,     // Récurrent : avait fait une PROMESSE le mois précédent
    SUIVI_INJOIGNABLE,  // Récurrent : était INJOIGNABLE le mois précédent
    
    // Statut final
    CLOTURE             // Dossier clôturé définitivement
}