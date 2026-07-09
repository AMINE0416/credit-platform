package org.eqdom.creditplatform.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

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

    @Enumerated(EnumType.STRING)
    private NatureAction natureAction;

    @Enumerated(EnumType.STRING)
    private MotifImpaye motifImpaye;

    @Column(length = 2000)
    private String compteRendu;

    @Enumerated(EnumType.STRING)
    private CodeResultat codeResultat;

    @Enumerated(EnumType.STRING)
    private Recidive recidive;
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
    public LocalDateTime getDateAction() { return dateAction; }
    public void setDateAction(LocalDateTime dateAction) { this.dateAction = dateAction; }
}