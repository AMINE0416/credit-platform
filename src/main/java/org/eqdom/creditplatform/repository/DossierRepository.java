package org.eqdom.creditplatform.repository;

import org.eqdom.creditplatform.model.Dossier;
import org.eqdom.creditplatform.model.StatutDossier;
import org.eqdom.creditplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface DossierRepository extends JpaRepository<Dossier, Long> {
    List<Dossier> findByAgentAssigne(User agent);
    List<Dossier> findByStatutActuel(StatutDossier statut);
    Optional<Dossier> findByCreditId(Long creditId);
    
    @Query("SELECT DISTINCT d FROM Dossier d " +
           "LEFT JOIN FETCH d.credit c " +
           "LEFT JOIN FETCH c.client " +
           "WHERE d.id = :id")
    Optional<Dossier> findByIdWithRelations(@Param("id") Long id);
}