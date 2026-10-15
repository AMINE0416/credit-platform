package org.eqdom.creditplatform.dto;

import java.math.BigDecimal;

public class DossierListItemDTO {
    private Long id;
    private String cin;
    private String nomComplet;
    private String referenceContrat;
    private String statut;
    private String ville;
    private String agentAssigne;
    private BigDecimal score;

    public DossierListItemDTO(Long id, String cin, String nomComplet, String referenceContrat,
                              String statut, String ville, String agentAssigne, BigDecimal score) {
        this.id = id; this.cin = cin; this.nomComplet = nomComplet;
        this.referenceContrat = referenceContrat; this.statut = statut;
        this.ville = ville; this.agentAssigne = agentAssigne; this.score = score;
    }

    public Long getId() { return id; }
    public String getCin() { return cin; }
    public String getNomComplet() { return nomComplet; }
    public String getReferenceContrat() { return referenceContrat; }
    public String getStatut() { return statut; }
    public String getVille() { return ville; }
    public String getAgentAssigne() { return agentAssigne; }
    public BigDecimal getScore() { return score; }
}