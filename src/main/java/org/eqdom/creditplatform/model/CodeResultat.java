package org.eqdom.creditplatform.model;

public enum CodeResultat {
    RC("RC", "Right Contact - contact avec le bon débiteur"),
    RPC("RPC", "Right Party Contact - confirmé, promesse obtenue"),
    NRP("NRP", "No Right Party - contact avec un tiers"),
    NRC("NRC", "No Reply Contact - pas de réponse"),
    NUM_INVALIDE("NUM_INVALIDE", "Numéro invalide/hors service"),
    REFUS("REFUS", "Client refuse de payer"),
    DIFFERE("DIFFERE", "Client demande un délai"),
    REGLE("REGLE", "Client confirme paiement effectué");

    private final String code;
    private final String description;

    CodeResultat(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
