package org.eqdom.creditplatform.repository;

import org.eqdom.creditplatform.model.DossierArchive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DossierArchiveRepository extends JpaRepository<DossierArchive, Long> {

    List<DossierArchive> findByMoisArchiveOrderByDateArchivageDesc(String moisArchive);

    @Query("SELECT DISTINCT da.moisArchive FROM DossierArchive da ORDER BY da.moisArchive DESC")
    List<String> findDistinctMoisArchive();

    @Query("SELECT COUNT(da) FROM DossierArchive da WHERE da.moisArchive = :mois")
    Long countByMoisArchive(@Param("mois") String mois);

    @Query("SELECT COUNT(da) FROM DossierArchive da WHERE da.moisArchive = :mois AND da.statutFinal = :statut")
    Long countByMoisArchiveAndStatut(@Param("mois") String mois, @Param("statut") String statut);

    boolean existsByMoisArchive(String moisArchive);
}
