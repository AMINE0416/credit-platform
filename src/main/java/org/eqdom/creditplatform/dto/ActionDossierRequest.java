package org.eqdom.creditplatform.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.eqdom.creditplatform.model.NatureAction;
import org.eqdom.creditplatform.model.MotifImpaye;
import org.eqdom.creditplatform.model.CodeResultat;
import org.eqdom.creditplatform.model.Recidive;

public class ActionDossierRequest {
    @NotBlank(message = "La nature de l'action est obligatoire")
    @JsonProperty("natureAction")
    private String natureActionStr;
    
    @JsonProperty("motifImpaye")
    private String motifImpayeStr;
    
    @NotBlank(message = "Le compte rendu est obligatoire")
    private String compteRendu;
    
    @NotBlank(message = "Le code résultat est obligatoire")
    @JsonProperty("codeResultat")
    private String codeResultatStr;
    
    @JsonProperty("recidive")
    private String recidiveStr;
    
    @Positive(message = "Le nombre d'actions doit être positif")
    private Integer nbreActionClient;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Le montant de la promesse doit être positif")
    private BigDecimal montantPromesse;
    
    private LocalDate datePromesse;
    private String nouveauTelephone;
    private String nouveauStatut;

    // Getters pour les enums (utilisés par le contrôleur) - Ignorés par Jackson
    @JsonIgnore
    public NatureAction getNatureAction() {
        return (natureActionStr != null && !natureActionStr.trim().isEmpty()) 
            ? NatureAction.valueOf(natureActionStr) 
            : null;
    }
    
    @JsonIgnore
    public MotifImpaye getMotifImpaye() {
        return (motifImpayeStr != null && !motifImpayeStr.trim().isEmpty()) 
            ? MotifImpaye.valueOf(motifImpayeStr) 
            : null;
    }
    
    @JsonIgnore
    public CodeResultat getCodeResultat() {
        return (codeResultatStr != null && !codeResultatStr.trim().isEmpty()) 
            ? CodeResultat.valueOf(codeResultatStr) 
            : null;
    }
    
    @JsonIgnore
    public Recidive getRecidive() {
        return (recidiveStr != null && !recidiveStr.trim().isEmpty()) 
            ? Recidive.valueOf(recidiveStr) 
            : null;
    }
    
    // Getters pour les strings (utilisés par TransitionService et Jackson)
    public String getNatureActionStr() { return natureActionStr; }
    public String getMotifImpayeStr() { return motifImpayeStr; }
    public String getCodeResultatStr() { return codeResultatStr; }
    public String getRecidiveStr() { return recidiveStr; }
    
    public void setNatureActionStr(String natureActionStr) { this.natureActionStr = natureActionStr; }
    public void setMotifImpayeStr(String motifImpayeStr) { this.motifImpayeStr = motifImpayeStr; }
    public void setCodeResultatStr(String codeResultatStr) { this.codeResultatStr = codeResultatStr; }
    public void setRecidiveStr(String recidiveStr) { this.recidiveStr = recidiveStr; }
    
    public String getCompteRendu() { return compteRendu; }
    public void setCompteRendu(String compteRendu) { this.compteRendu = compteRendu; }
    
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