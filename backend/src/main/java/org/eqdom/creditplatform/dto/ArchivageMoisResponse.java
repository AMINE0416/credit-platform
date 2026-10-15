package org.eqdom.creditplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArchivageMoisResponse {
    private boolean success;
    private String message;
    private String moisArchive;
    private Long nbDossiersArchives;
    private Long nbDossiersSauvegardes; // Pour table temporaire
    private Long exportId; // ID de l'export CSV créé
    private String exportFileName;
}
