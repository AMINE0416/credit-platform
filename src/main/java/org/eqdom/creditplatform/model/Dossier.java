package org.eqdom.creditplatform.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dossiers")
public class Dossier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "credit_id", nullable = false, unique = true)
    private Credit credit;

    @Enumerated(EnumType.STRING)
    private StatutDossier statutActuel = StatutDossier.EN_COURS;

    @ManyToOne
    @JoinColumn(name = "agent_assigne_id")
    private User agentAssigne;

    @OneToMany(mappedBy = "dossier", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActionDossier> actions = new ArrayList<>();

    private LocalDate dateCreation = LocalDate.now();

    // Champs pour gestion archivage mensuel
    @Column(name = "mois_import", length = 7)
    private String moisImport; // Format: "2024-07"

    @Column(name = "est_recurrent")
    private Boolean estRecurrent = false;

    @Column(name = "mois_precedent_statut", length = 50)
    private String moisPrecedentStatut; // Statut du mois précédent si récurrent

    // Champs pour PROMESSE et SUIVI_PROMESSE
    @Column(name = "date_promesse")
    private LocalDate datePromesse;

    @Column(name = "montant_promesse")
    private Double montantPromesse;

    // Getters / Setters
    public Long getId() { return id; }
    public Credit getCredit() { return credit; }
    public void setCredit(Credit credit) { this.credit = credit; }
    public StatutDossier getStatutActuel() { return statutActuel; }
    public void setStatutActuel(StatutDossier statutActuel) { this.statutActuel = statutActuel; }
    public User getAgentAssigne() { return agentAssigne; }
    public void setAgentAssigne(User agentAssigne) { this.agentAssigne = agentAssigne; }
    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }
    public String getMoisImport() { return moisImport; }
    public void setMoisImport(String moisImport) { this.moisImport = moisImport; }
    public Boolean getEstRecurrent() { return estRecurrent; }
    public void setEstRecurrent(Boolean estRecurrent) { this.estRecurrent = estRecurrent; }
    public String getMoisPrecedentStatut() { return moisPrecedentStatut; }
    public void setMoisPrecedentStatut(String moisPrecedentStatut) { this.moisPrecedentStatut = moisPrecedentStatut; }
    public LocalDate getDatePromesse() { return datePromesse; }
    public void setDatePromesse(LocalDate datePromesse) { this.datePromesse = datePromesse; }
    public Double getMontantPromesse() { return montantPromesse; }
    public void setMontantPromesse(Double montantPromesse) { this.montantPromesse = montantPromesse; }
    public List<ActionDossier> getActions() { return actions; }
    public void setActions(List<ActionDossier> actions) { this.actions = actions; }
}