package org.eqdom.creditplatform.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.eqdom.creditplatform.model.NatureAction;
import org.eqdom.creditplatform.model.MotifImpaye;
import org.eqdom.creditplatform.model.CodeResultat;
import org.eqdom.creditplatform.model.Recidive;

public class ActionDossierRequest {
    private NatureAction natureAction;
    private MotifImpaye motifImpaye;
    private String compteRendu;
    private CodeResultat codeResultat;
    private Recidive recidive;
    private Integer nbreActionClient;
    private BigDecimal montantPromesse;
    private LocalDate datePromesse;
    private String nouveauTelephone;
    private String nouveauStatut; // NOUVEAU, EN_COURS, PROMESSE, INJOIGNABLE, REGULARISE, CLOTURE

    public NatureAction getNatureAction() { return natureAction; }
    public void setNatureAction(NatureAction natureAction) { this.natureAction = natureAction; }
    public MotifImpaye getMotifImpaye() { return motifImpaye; }
    public void setMotifImpaye(MotifImpaye motifImpaye) { this.motifImpaye = motifImpaye; }
    public String getCompteRendu() { return compteRendu; }
    public void setCompteRendu(String compteRendu) { this.compteRendu = compteRendu; }
    public CodeResultat getCodeResultat() { return codeResultat; }
    public void setCodeResultat(CodeResultat codeResultat) { this.codeResultat = codeResultat; }
    public Recidive getRecidive() { return recidive; }
    public void setRecidive(Recidive recidive) { this.recidive = recidive; }
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