package org.eqdom.creditplatform.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "credits")
public class Credit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false, unique = true)
    private String referenceContrat; // ANC_REFDO

    private String fromProv;
    private String gestCin;
    private String typeCredit;   // CSP/PDT ex: AUTO_CLASSIQUE
    private String segment;      // ex: AUTO PP

    private Integer nbrImpaye;
    private BigDecimal mensualite;
    private BigDecimal encours;
    private BigDecimal impayeMontant;   // Mnt Imp
    private BigDecimal prvCum;

    private String stage;
    private String dvr;
    private String reecheance;
    private String immatriculation;

    // Getters / Setters
    public Long getId() { return id; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public String getReferenceContrat() { return referenceContrat; }
    public void setReferenceContrat(String referenceContrat) { this.referenceContrat = referenceContrat; }
    public String getFromProv() { return fromProv; }
    public void setFromProv(String fromProv) { this.fromProv = fromProv; }
    public String getGestCin() { return gestCin; }
    public void setGestCin(String gestCin) { this.gestCin = gestCin; }
    public String getTypeCredit() { return typeCredit; }
    public void setTypeCredit(String typeCredit) { this.typeCredit = typeCredit; }
    public String getSegment() { return segment; }
    public void setSegment(String segment) { this.segment = segment; }
    public Integer getNbrImpaye() { return nbrImpaye; }
    public void setNbrImpaye(Integer nbrImpaye) { this.nbrImpaye = nbrImpaye; }
    public BigDecimal getMensualite() { return mensualite; }
    public void setMensualite(BigDecimal mensualite) { this.mensualite = mensualite; }
    public BigDecimal getEncours() { return encours; }
    public void setEncours(BigDecimal encours) { this.encours = encours; }
    public BigDecimal getImpayeMontant() { return impayeMontant; }
    public void setImpayeMontant(BigDecimal impayeMontant) { this.impayeMontant = impayeMontant; }
    public BigDecimal getPrvCum() { return prvCum; }
    public void setPrvCum(BigDecimal prvCum) { this.prvCum = prvCum; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public String getDvr() { return dvr; }
    public void setDvr(String dvr) { this.dvr = dvr; }
    public String getReecheance() { return reecheance; }
    public void setReecheance(String reecheance) { this.reecheance = reecheance; }
    public String getImmatriculation() { return immatriculation; }
    public void setImmatriculation(String immatriculation) { this.immatriculation = immatriculation; }
}