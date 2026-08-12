package org.eqdom.creditplatform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "dossiers_mois_precedent")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DossierMoisPrecedent {

    @Id
    @Column(length = 50)
    private String numeroDossier;

    @Column(nullable = false, length = 50)
    private String statutFinal;

    private LocalDateTime dateDerniereAction;

    @Column(nullable = false, length = 7)
    private String moisOrigine;

    @Column(nullable = false)
    private LocalDateTime dateSauvegarde = LocalDateTime.now();

    // Champs pour préserver les informations de promesse
    @Column(name = "date_promesse")
    private LocalDate datePromesse;

    @Column(name = "montant_promesse", precision = 15, scale = 2)
    private BigDecimal montantPromesse;
}
