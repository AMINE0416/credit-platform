package org.eqdom.creditplatform.repository;

import org.eqdom.creditplatform.model.DossierHistorique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DossierHistoriqueRepository extends JpaRepository<DossierHistorique, Long> {
    
    /**
     * Récupérer tous les dossiers archivés pour un mois donné
     */
    List<DossierHistorique> findByMoisAndAnnee(Integer mois, Integer annee);
    
    /**
     * Vérifier si un archivage existe déjà pour ce mois
     */
    boolean existsByMoisAndAnnee(Integer mois, Integer annee);
    
    /**
     * Récupérer l'historique d'un dossier spécifique à travers les mois
     */
    List<DossierHistorique> findByDossierIdOrigineOrderByAnneeDescMoisDesc(Long dossierIdOrigine);
    
    /**
     * Compter le nombre de dossiers archivés pour un mois
     */
    long countByMoisAndAnnee(Integer mois, Integer annee);
    
    /**
     * Récupérer tous les mois archivés (distinct)
     */
    @Query("SELECT DISTINCT d.annee, d.mois FROM DossierHistorique d ORDER BY d.annee DESC, d.mois DESC")
    List<Object[]> findDistinctMoisAnnee();
}
