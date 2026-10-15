package org.eqdom.creditplatform.repository;

import org.eqdom.creditplatform.model.Dossier;
import org.eqdom.creditplatform.model.StatutDossier;
import org.eqdom.creditplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface DossierRepository extends JpaRepository<Dossier, Long> {
    List<Dossier> findByAgentAssigne(User agent);
    long countByAgentAssigne(User agent);
    List<Dossier> findByStatutActuel(StatutDossier statut);
    Optional<Dossier> findByCreditId(Long creditId);
    
    @EntityGraph(attributePaths = {"credit", "credit.client", "agentAssigne"})
    Optional<Dossier> findById(Long id);
    
    // Méthodes pour gestion archivage mensuel
    List<Dossier> findByMoisImport(String moisImport);
    long countByMoisImport(String moisImport);
    
    @Modifying
    @Transactional
    void deleteByMoisImport(String moisImport);
    
    List<Dossier> findByEstRecurrent(Boolean estRecurrent);
    long countByEstRecurrent(Boolean estRecurrent);
    
    // ========================================================
    // MÉTHODES OPTIMISÉES POUR CHARGEMENT RAPIDE DU TABLEAU DE BORD
    // ========================================================
    
    /**
     * Récupère tous les dossiers avec jointures optimisées
     * Charge en une seule requête : dossier + credit + client + agent
     */
    @EntityGraph(attributePaths = {"credit", "credit.client", "agentAssigne"})
    @Query("SELECT d FROM Dossier d")
    List<Dossier> findAllWithDetails();
    
    /**
     * Filtre par statut avec jointures
     */
    @EntityGraph(attributePaths = {"credit", "credit.client", "agentAssigne"})
    @Query("SELECT d FROM Dossier d WHERE d.statutActuel = :statut")
    List<Dossier> findByStatutActuelWithDetails(@Param("statut") StatutDossier statut);
    
    /**
     * Filtre par ville du client avec jointures
     */
    @EntityGraph(attributePaths = {"credit", "credit.client", "agentAssigne"})
    @Query("SELECT d FROM Dossier d WHERE d.credit.client.ville = :ville")
    List<Dossier> findByVilleWithDetails(@Param("ville") String ville);
    
    /**
     * Filtre par agent assigné avec jointures
     */
    @EntityGraph(attributePaths = {"credit", "credit.client", "agentAssigne"})
    @Query("SELECT d FROM Dossier d WHERE d.agentAssigne.id = :agentId")
    List<Dossier> findByAgentIdWithDetails(@Param("agentId") Long agentId);
    
    /**
     * Filtre par dossiers non assignés avec jointures
     */
    @EntityGraph(attributePaths = {"credit", "credit.client", "agentAssigne"})
    @Query("SELECT d FROM Dossier d WHERE d.agentAssigne IS NULL")
    List<Dossier> findByAgentNullWithDetails();
    
    /**
     * Filtre combiné: statut + ville avec jointures
     */
    @EntityGraph(attributePaths = {"credit", "credit.client", "agentAssigne"})
    @Query("SELECT d FROM Dossier d WHERE d.statutActuel = :statut AND d.credit.client.ville = :ville")
    List<Dossier> findByStatutAndVilleWithDetails(@Param("statut") StatutDossier statut, @Param("ville") String ville);
    
    /**
     * Filtre combiné: statut + agent avec jointures
     */
    @EntityGraph(attributePaths = {"credit", "credit.client", "agentAssigne"})
    @Query("SELECT d FROM Dossier d WHERE d.statutActuel = :statut AND d.agentAssigne.id = :agentId")
    List<Dossier> findByStatutAndAgentWithDetails(@Param("statut") StatutDossier statut, @Param("agentId") Long agentId);
    
    /**
     * Filtre combiné: ville + agent avec jointures
     */
    @EntityGraph(attributePaths = {"credit", "credit.client", "agentAssigne"})
    @Query("SELECT d FROM Dossier d WHERE d.credit.client.ville = :ville AND d.agentAssigne.id = :agentId")
    List<Dossier> findByVilleAndAgentWithDetails(@Param("ville") String ville, @Param("agentId") Long agentId);
    
    /**
     * Filtre combiné: statut + ville + agent avec jointures
     */
    @EntityGraph(attributePaths = {"credit", "credit.client", "agentAssigne"})
    @Query("SELECT d FROM Dossier d WHERE d.statutActuel = :statut AND d.credit.client.ville = :ville AND d.agentAssigne.id = :agentId")
    List<Dossier> findByStatutAndVilleAndAgentWithDetails(
        @Param("statut") StatutDossier statut, 
        @Param("ville") String ville, 
        @Param("agentId") Long agentId
    );
}