package org.eqdom.creditplatform.dto;

import org.eqdom.creditplatform.model.Client;
import org.eqdom.creditplatform.model.Credit;
import org.eqdom.creditplatform.model.Dossier;

public class DossierDetailDTO {
    private Long dossierId;
    private String statut;
    private Client client;
    private Credit credit;

    public DossierDetailDTO(Dossier dossier) {
        this.dossierId = dossier.getId();
        this.statut = dossier.getStatutActuel().name();
        this.credit = dossier.getCredit();
        this.client = dossier.getCredit().getClient();
    }

    public Long getDossierId() { return dossierId; }
    public String getStatut() { return statut; }
    public Client getClient() { return client; }
    public Credit getCredit() { return credit; }
}