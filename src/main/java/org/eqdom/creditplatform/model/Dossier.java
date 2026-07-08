package org.eqdom.creditplatform.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "dossiers")
public class Dossier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "credit_id", nullable = false, unique = true)
    private Credit credit;

    @Enumerated(EnumType.STRING)
    private StatutDossier statutActuel = StatutDossier.NOUVEAU;

    @ManyToOne
    @JoinColumn(name = "agent_assigne_id")
    private User agentAssigne;

    private LocalDate dateCreation = LocalDate.now();

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
}