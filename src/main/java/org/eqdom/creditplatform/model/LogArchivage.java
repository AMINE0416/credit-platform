package org.eqdom.creditplatform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Entity
@Table(name = "logs_archivage", indexes = {
    @Index(name = "idx_logs_mois", columnList = "moisConcerne"),
    @Index(name = "idx_logs_date", columnList = "dateOperation")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogArchivage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String operation; // 'EXPORT', 'ARCHIVAGE', 'NETTOYAGE', 'IMPORT'

    @Column(nullable = false, length = 7)
    private String moisConcerne;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime dateOperation = LocalDateTime.now();

    private Integer nbDossiers;

    private Integer nbRecurrents;

    private Integer nbNouveaux;

    @Column(name = "utilisateur_id")
    private Long utilisateurId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(length = 20)
    @Builder.Default
    private String statut = "SUCCESS"; // 'SUCCESS', 'ERROR', 'PARTIAL'

    // Stockage du fichier CSV exporté lors de l'archivage
    // @JsonIgnore empêche la sérialisation des bytes dans les listes JSON
    @JsonIgnore
    @Column(name = "csv_data", columnDefinition = "BYTEA")
    private byte[] csvData;

    @Column(name = "nom_fichier_csv", length = 100)
    private String nomFichierCsv;

    // Relation optionnelle avec User (commentée pour éviter le conflit de mapping)
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "utilisateur_id", insertable = false, updatable = false)
    // private User utilisateur;
}
