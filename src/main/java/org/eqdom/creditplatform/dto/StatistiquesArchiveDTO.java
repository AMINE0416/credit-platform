package org.eqdom.creditplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatistiquesArchiveDTO {
    private String moisArchive;
    private Long totalDossiers;
    private Map<String, Long> repartitionStatuts; // Statut -> Nombre
    private BigDecimal totalDuMensuel;
    private LocalDateTime dateArchivage;
    private boolean exportCsvDisponible;
    private Long exportId;
}
