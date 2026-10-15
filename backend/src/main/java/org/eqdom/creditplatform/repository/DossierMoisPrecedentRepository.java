package org.eqdom.creditplatform.repository;

import org.eqdom.creditplatform.model.DossierMoisPrecedent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface DossierMoisPrecedentRepository extends JpaRepository<DossierMoisPrecedent, String> {

    Optional<DossierMoisPrecedent> findByNumeroDossier(String numeroDossier);

    @Modifying
    @Transactional
    @Query("DELETE FROM DossierMoisPrecedent")
    void deleteAllDossiers();

    long countByMoisOrigine(String moisOrigine);
}
