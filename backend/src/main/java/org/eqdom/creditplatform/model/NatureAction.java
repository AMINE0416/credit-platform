package org.eqdom.creditplatform.model;

public enum NatureAction {
    APPEL_SORTANT("Appel sortant"),
    APPEL_ENTRANT("Appel entrant (client rappelle)"),
    SMS_RELANCE("SMS de relance"),
    EMAIL_RELANCE("Email de relance"),
    VISITE_TERRAIN("Visite terrain"),
    COURRIER_MISE_EN_DEMEURE("Courrier / mise en demeure"),
    RELANCE_VIA_TIERS("Relance via tiers (garant, entourage)");

    private final String label;

    NatureAction(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
