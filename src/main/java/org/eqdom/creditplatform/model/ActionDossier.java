package org.eqdom.creditplatform.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "actions_dossier")
public class ActionDossier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "dossier_id", nullable = false)
    private Dossier dossier;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User agent;

    private String natureAction;
    private String motifImpaye;

    @Column(length = 2000)
    private String compteRendu;

    private String codeResultat;
    private Boolean recidive;
    private Integer nbreActionClient;

    private BigDecimal montantPromesse;
    private LocalDate datePromesse;

    private String nouveauTelephone;

    private LocalDateTime dateAction = LocalDateTime.now();

    // Getters / Setters
    public Long getId() { return id; }
    public Dossier getDossier() { return dossier; }
    public void setDossier(Dossier dossier) { this.dossier = dossier; }
    public User getAgent() { return agent; }
    public void setAgent(User agent) { this.agent = agent; }
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
    public LocalDateTime getDateAction() { return dateAction; }
    public void setDateAction(LocalDateTime dateAction) { this.dateAction = dateAction; }
}