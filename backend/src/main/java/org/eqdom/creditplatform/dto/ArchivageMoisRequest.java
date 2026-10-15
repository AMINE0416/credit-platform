package org.eqdom.creditplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArchivageMoisRequest {
    private String mois; // Format: "2024-07"
    private boolean exporterCsv = true; // Par défaut, on exporte le CSV
}
