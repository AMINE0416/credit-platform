package org.eqdom.creditplatform.repository;

import org.eqdom.creditplatform.model.Dossier;
import org.eqdom.creditplatform.model.StatutDossier;
import org.eqdom.creditplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
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
}