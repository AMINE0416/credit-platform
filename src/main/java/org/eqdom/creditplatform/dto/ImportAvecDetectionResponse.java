package org.eqdom.creditplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportAvecDetectionResponse {
    private boolean success;
    private String message;
    private String moisImporte;
    private int totalDossiers;
    private int nouveauxDossiers;
    private int dossiersRecurrents;
    private Map<String, Integer> repartitionStatuts; // Statut -> Nombre
    private Map<String, Integer> repartitionRecurrents; // Pour les récurrents seulement
}
