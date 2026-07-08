package org.eqdom.creditplatform.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ActionDossierRequest {
    private String natureAction;
    private String motifImpaye;
    private String compteRendu;
    private String codeResultat;
    private Boolean recidive;
    private Integer nbreActionClient;
    private BigDecimal montantPromesse;
    private LocalDate datePromesse;
    private String nouveauTelephone;
    private String nouveauStatut; // NOUVEAU, EN_COURS, PROMESSE, INJOIGNABLE, REGULARISE, CLOTURE

    public String getNatureAction() { return natureAction; }
    public void setNatureAction(String natureAction) { this.natureAction = natureAction; }
    public String getMotifImpaye() { return motifImpaye; }
    public void setMotifImpaye(String motifImpaye) { this.motifImpaye = motifImpaye; }
    public String getCompteRendu() { return compteRendu; }
    public void setCompteRendu(String compteRendu) { this.compteRendu = compteRendu; }
    public String getCodeResultat() { return codeResultat; }
    public void setCodeResultat(String codeResultat) { this.codeResultat = codeResultat; }
    public Boolean getRecidive() { return recidive; }
    public void setRecidive(Boolean recidive) { this.recidive = recidive; }
    public Integer getNbreActionClient() { return nbreActionClient; }
    public void setNbreActionClient(Integer nbreActionClient) { this.nbreActionClient = nbreActionClient; }
    public BigDecimal getMontantPromesse() { return montantPromesse; }
    public void setMontantPromesse(BigDecimal montantPromesse) { this.montantPromesse = montantPromesse; }
    public LocalDate getDatePromesse() { return datePromesse; }
    public void setDatePromesse(LocalDate datePromesse) { this.datePromesse = datePromesse; }
    public String getNouveauTelephone() { return nouveauTelephone; }
    public void setNouveauTelephone(String nouveauTelephone) { this.nouveauTelephone = nouveauTelephone; }
    public String getNouveauStatut() { return nouveauStatut; }
    public void setNouveauStatut(String nouveauStatut) { this.nouveauStatut = nouveauStatut; }
}