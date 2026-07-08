package org.eqdom.creditplatform.repository;

import org.eqdom.creditplatform.model.Credit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CreditRepository extends JpaRepository<Credit, Long> {
    Optional<Credit> findByReferenceContrat(String referenceContrat);
}