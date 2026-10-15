package org.eqdom.creditplatform.repository;

import org.eqdom.creditplatform.model.LogArchivage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LogArchivageRepository extends JpaRepository<LogArchivage, Long> {

    List<LogArchivage> findByMoisConcerneOrderByDateOperationDesc(String moisConcerne);

    List<LogArchivage> findAllByOrderByDateOperationDesc();

    @Query("SELECT la FROM LogArchivage la WHERE la.operation = :operation ORDER BY la.dateOperation DESC")
    List<LogArchivage> findByOperation(@Param("operation") String operation);

    @Query("SELECT la FROM LogArchivage la WHERE la.moisConcerne = :mois AND la.operation = :operation")
    List<LogArchivage> findByMoisAndOperation(@Param("mois") String mois, @Param("operation") String operation);

    /**
     * Trouver le log d'archivage contenant le CSV pour un mois donné
     */
    @Query("SELECT la FROM LogArchivage la WHERE la.moisConcerne = :mois AND la.operation = 'ARCHIVAGE' AND la.csvData IS NOT NULL ORDER BY la.dateOperation DESC")
    List<LogArchivage> findCsvArchivageByMois(@Param("mois") String mois);
}
