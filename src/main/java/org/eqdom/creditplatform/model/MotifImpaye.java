package org.eqdom.creditplatform.model;

public enum MotifImpaye {
    PERTE_EMPLOI("Perte d'emploi"),
    DIFFICULTE_FINANCIERE_TEMPORAIRE("Difficulté financière temporaire"),
    LITIGE_VEHICULE_PRODUIT("Litige sur le véhicule/produit"),
    DECES_EMPRUNTEUR("Décès de l'emprunteur"),
    MALADIE_HOSPITALISATION("Maladie / hospitalisation"),
    OUBLI_PAIEMENT("Oubli de paiement"),
    CONTESTATION_MONTANT("Contestation du montant"),
    CLIENT_INJOIGNABLE("Client injoignable (motif inconnu)"),
    AUTRE("Autre");

    private final String label;

    MotifImpaye(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
