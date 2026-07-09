package org.eqdom.creditplatform.model;

public enum Recidive {
    OUI("Oui"),
    NON("Non");

    private final String label;

    Recidive(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
