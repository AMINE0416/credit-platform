package org.eqdom.creditplatform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "dossiers_archive", indexes = {
    @Index(name = "idx_archive_mois", columnList = "moisArchive"),
    @Index(name = "idx_archive_numero", columnList = "numeroDossier"),
    @Index(name = "idx_archive_date", columnList = "dateArchivage")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DossierArchive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Données du dossier
    @Column(nullable = false, length = 50)
    private String numeroDossier;

    @Column(length = 255)
    private String nomClient;

    @Column(length = 20)
    private String telephone;

    @Column(length = 255)
    private String email;

    @Column(precision = 15, scale = 2)
    private BigDecimal montantPret;

    @Column(precision = 15, scale = 2)
    private BigDecimal montantEcheance;

    private Integer nombreEcheancesImpayees;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalDu;

    private LocalDate datePremierImpaye;

    private Integer ancienneteImpaye;

    @Column(length = 50)
    private String statut;

    private Long agentId;

    private LocalDate datePromesse;

    @Column(precision = 15, scale = 2)
    private BigDecimal montantPromesse;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    private LocalDateTime dateDerniereAction;

    private Integer scorePriorite;

    @Column(length = 7)
    private String moisImport;

    // Métadonnées d'archivage
    @Column(nullable = false, length = 7)
    private String moisArchive;

    @Column(nullable = false)
    private LocalDateTime dateArchivage = LocalDateTime.now();

    @Column(length = 50)
    private String statutFinal;

    // Getters additionnels pour l'affichage
    public String getPeriodeAffichage() {
        return moisArchive; // Format: "2024-07"
    }
}
