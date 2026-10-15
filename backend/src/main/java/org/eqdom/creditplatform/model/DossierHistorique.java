package org.eqdom.creditplatform.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Table d'historique pour archiver les dossiers lors de chaque import mensuel
 * Permet de conserver un snapshot complet des données à chaque période
 */
@Entity
@Table(name = "dossier_historique")
public class DossierHistorique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mois", nullable = false)
    private Integer mois; // Ex: 7 pour juillet, 8 pour août, etc.

    @Column(name = "annee", nullable = false)
    private Integer annee; // Ex: 2024

    @Column(name = "date_archivage", nullable = false)
    private LocalDateTime dateArchivage;

    // ============ Informations Dossier ============
    @Column(name = "dossier_id_origine")
    private Long dossierIdOrigine; // ID du dossier original

    @Column(name = "statut_dossier", length = 50)
    private String statutDossier; // NOUVEAU, EN_COURS, etc.

    @Column(name = "agent_assigne_email")
    private String agentAssigneEmail;

    @Column(name = "agent_assigne_nom")
    private String agentAssigneNom;

    // ============ Informations Client ============
    @Column(name = "cin", length = 50)
    private String cin;

    @Column(name = "nom", length = 100)
    private String nom;

    @Column(name = "prenom", length = 100)
    private String prenom;

    @Column(name = "telephone", length = 50)
    private String telephone;

    @Column(name = "telephone2", length = 50)
    private String telephone2;

    @Column(name = "adresse", length = 255)
    private String adresse;

    @Column(name = "ville", length = 100)
    private String ville;

    @Column(name = "code_agence", length = 50)
    private String codeAgence;

    @Column(name = "lib_agence", length = 100)
    private String libAgence;

    @Column(name = "adresse_cadrage", length = 255)
    private String adresseCadrage;

    // ============ Informations Crédit ============
    @Column(name = "reference_contrat", length = 50)
    private String referenceContrat;

    @Column(name = "from_prov", length = 50)
    private String fromProv;

    @Column(name = "gest_cin", length = 50)
    private String gestCin;

    @Column(name = "type_credit", length = 100)
    private String typeCredit;

    @Column(name = "segment", length = 50)
    private String segment;

    @Column(name = "nbr_impaye")
    private Integer nbrImpaye;

    @Column(name = "mensualite", precision = 15, scale = 2)
    private BigDecimal mensualite;

    @Column(name = "encours", precision = 15, scale = 2)
    private BigDecimal encours;

    @Column(name = "impaye_montant", precision = 15, scale = 2)
    private BigDecimal impayeMontant;

    @Column(name = "prv_cum", precision = 15, scale = 2)
    private BigDecimal prvCum;

    @Column(name = "stage", length = 50)
    private String stage;

    @Column(name = "dvr", length = 50)
    private String dvr;

    @Column(name = "reecheance", length = 50)
    private String reecheance;

    @Column(name = "immatriculation", length = 50)
    private String immatriculation;

    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;

    // ============ Statistiques à l'archivage ============
    @Column(name = "nb_actions_total")
    private Integer nbActionsTotal; // Nombre d'actions effectuées sur ce dossier

    @Column(name = "date_derniere_action")
    private LocalDateTime dateDerniereAction;

    // Constructeurs
    public DossierHistorique() {}

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getMois() {
        return mois;
    }

    public void setMois(Integer mois) {
        this.mois = mois;
    }

    public Integer getAnnee() {
        return annee;
    }

    public void setAnnee(Integer annee) {
        this.annee = annee;
    }

    public LocalDateTime getDateArchivage() {
        return dateArchivage;
    }

    public void setDateArchivage(LocalDateTime dateArchivage) {
        this.dateArchivage = dateArchivage;
    }

    public Long getDossierIdOrigine() {
        return dossierIdOrigine;
    }

    public void setDossierIdOrigine(Long dossierIdOrigine) {
        this.dossierIdOrigine = dossierIdOrigine;
    }

    public String getStatutDossier() {
        return statutDossier;
    }

    public void setStatutDossier(String statutDossier) {
        this.statutDossier = statutDossier;
    }

    public String getAgentAssigneEmail() {
        return agentAssigneEmail;
    }

    public void setAgentAssigneEmail(String agentAssigneEmail) {
        this.agentAssigneEmail = agentAssigneEmail;
    }

    public String getAgentAssigneNom() {
        return agentAssigneNom;
    }

    public void setAgentAssigneNom(String agentAssigneNom) {
        this.agentAssigneNom = agentAssigneNom;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getTelephone2() {
        return telephone2;
    }

    public void setTelephone2(String telephone2) {
        this.telephone2 = telephone2;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getCodeAgence() {
        return codeAgence;
    }

    public void setCodeAgence(String codeAgence) {
        this.codeAgence = codeAgence;
    }

    public String getLibAgence() {
        return libAgence;
    }

    public void setLibAgence(String libAgence) {
        this.libAgence = libAgence;
    }

    public String getAdresseCadrage() {
        return adresseCadrage;
    }

    public void setAdresseCadrage(String adresseCadrage) {
        this.adresseCadrage = adresseCadrage;
    }

    public String getReferenceContrat() {
        return referenceContrat;
    }

    public void setReferenceContrat(String referenceContrat) {
        this.referenceContrat = referenceContrat;
    }

    public String getFromProv() {
        return fromProv;
    }

    public void setFromProv(String fromProv) {
        this.fromProv = fromProv;
    }

    public String getGestCin() {
        return gestCin;
    }

    public void setGestCin(String gestCin) {
        this.gestCin = gestCin;
    }

    public String getTypeCredit() {
        return typeCredit;
    }

    public void setTypeCredit(String typeCredit) {
        this.typeCredit = typeCredit;
    }

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public Integer getNbrImpaye() {
        return nbrImpaye;
    }

    public void setNbrImpaye(Integer nbrImpaye) {
        this.nbrImpaye = nbrImpaye;
    }

    public BigDecimal getMensualite() {
        return mensualite;
    }

    public void setMensualite(BigDecimal mensualite) {
        this.mensualite = mensualite;
    }

    public BigDecimal getEncours() {
        return encours;
    }

    public void setEncours(BigDecimal encours) {
        this.encours = encours;
    }

    public BigDecimal getImpayeMontant() {
        return impayeMontant;
    }

    public void setImpayeMontant(BigDecimal impayeMontant) {
        this.impayeMontant = impayeMontant;
    }

    public BigDecimal getPrvCum() {
        return prvCum;
    }

    public void setPrvCum(BigDecimal prvCum) {
        this.prvCum = prvCum;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getDvr() {
        return dvr;
    }

    public void setDvr(String dvr) {
        this.dvr = dvr;
    }

    public String getReecheance() {
        return reecheance;
    }

    public void setReecheance(String reecheance) {
        this.reecheance = reecheance;
    }

    public String getImmatriculation() {
        return immatriculation;
    }

    public void setImmatriculation(String immatriculation) {
        this.immatriculation = immatriculation;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public Integer getNbActionsTotal() {
        return nbActionsTotal;
    }

    public void setNbActionsTotal(Integer nbActionsTotal) {
        this.nbActionsTotal = nbActionsTotal;
    }

    public LocalDateTime getDateDerniereAction() {
        return dateDerniereAction;
    }

    public void setDateDerniereAction(LocalDateTime dateDerniereAction) {
        this.dateDerniereAction = dateDerniereAction;
    }
}
