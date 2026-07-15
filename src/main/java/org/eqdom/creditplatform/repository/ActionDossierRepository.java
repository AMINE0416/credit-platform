package org.eqdom.creditplatform.repository;

import org.eqdom.creditplatform.model.ActionDossier;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActionDossierRepository extends JpaRepository<ActionDossier, Long> {
    List<ActionDossier> findByDossierIdOrderByDateActionDesc(Long dossierId);
    long countByDossierId(Long dossierId);
}