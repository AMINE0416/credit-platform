package org.eqdom.creditplatform.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExcelImportService {

    /**
     * Lire un fichier Excel et retourner les lignes sous forme de Map
     */
    public List<Map<String, Object>> lireFichierExcel(MultipartFile file) throws Exception {
        List<Map<String, Object>> lignes = new ArrayList<>();

        log.info("========== DEBUT LECTURE EXCEL ==========");
        log.info("Fichier: {} ({} octets, type: {})", 
            file.getOriginalFilename(), file.getSize(), file.getContentType());

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            log.info("Feuille '{}': {} lignes", sheet.getSheetName(), sheet.getLastRowNum() + 1);

            // Chercher la première ligne non-nulle (certains fichiers ont des lignes vides avant les headers)
            Row headerRow = null;
            int headerRowIndex = -1;
            for (int i = 0; i <= Math.min(5, sheet.getLastRowNum()); i++) {
                Row row = sheet.getRow(i);
                if (row != null && row.getLastCellNum() > 0) {
                    // Vérifier qu'au moins 3 cellules sont non-vides (c'est bien un header)
                    int nonEmpty = 0;
                    for (int j = 0; j < Math.min(10, row.getLastCellNum()); j++) {
                        String val = getCellValue(row, j).trim();
                        if (!val.isEmpty()) nonEmpty++;
                    }
                    if (nonEmpty >= 3) {
                        headerRow = row;
                        headerRowIndex = i;
                        log.info("En-têtes trouvés à la ligne {} ({} cellules non-vides)", i + 1, nonEmpty);
                        break;
                    }
                }
            }

            if (headerRow == null) {
                // Log détaillé pour diagnostiquer
                for (int i = 0; i <= Math.min(5, sheet.getLastRowNum()); i++) {
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        StringBuilder sb = new StringBuilder();
                        for (int j = 0; j < Math.min(5, row.getLastCellNum()); j++) {
                            sb.append("[").append(j).append("]='").append(getCellValue(row, j)).append("' ");
                        }
                        log.error("Ligne {} contenu: {}", i + 1, sb);
                    } else {
                        log.error("Ligne {} est NULL", i + 1);
                    }
                }
                throw new IllegalArgumentException(
                    "Le fichier Excel ne contient pas d'en-têtes valides dans les 5 premières lignes. " +
                    "Vérifiez que la ligne d'en-têtes commence bien à la ligne 1."
                );
            }

            // Créer le mapping des colonnes — on stocke le nom EXACT et le nom normalisé
            Map<String, Integer> columnMap = new LinkedHashMap<>();
            Map<String, Integer> columnMapNorm = new LinkedHashMap<>();

            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                String header = getCellValue(headerRow, i).trim();
                if (!header.isEmpty()) {
                    columnMap.put(header, i);
                    // Normaliser : tout en minuscules, remplacer espaces/tirets par underscore
                    String norm = header.toLowerCase()
                        .replace(" ", "_")
                        .replace("-", "_")
                        .replace(".", "_");
                    columnMapNorm.put(norm, i);
                }
            }

            log.info("=== {} colonnes trouvées ===", columnMap.size());
            for (Map.Entry<String, Integer> entry : columnMap.entrySet()) {
                log.info("  Col[{}] = '{}'", entry.getValue(), entry.getKey());
            }
            log.info("===========================");

            if (columnMap.isEmpty()) {
                throw new IllegalArgumentException("La ligne d'en-têtes est vide ou illisible.");
            }

            // Vérifier les colonnes critiques
            String[] colonnesCritiques = {"num_cin1", "ANC_REFDO", "nom", "prenom", "CSP_PDT"};
            for (String col : colonnesCritiques) {
                Integer idx = findColumnIndex(col, columnMap, columnMapNorm);
                log.info("  Colonne '{}' → {}", col, idx != null ? "trouvée (col " + idx + ")" : "⚠️ NON TROUVÉE");
            }

            // Lire les lignes de données (à partir de headerRowIndex + 1)
            int nbLignesLues = 0;
            int nbLignesVides = 0;
            for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // Ignorer les lignes complètement vides — vérifier avec les colonnes CIN et REF
                String cinTest = getFlexible(row, columnMap, columnMapNorm, "num_cin1");
                String refTest = getFlexible(row, columnMap, columnMapNorm, "ANC_REFDO");
                
                if (cinTest.trim().isEmpty() && refTest.trim().isEmpty()) {
                    nbLignesVides++;
                    continue;
                }

                Map<String, Object> ligne = new HashMap<>();

                // Client
                ligne.put("cin",              getFlexible(row, columnMap, columnMapNorm, "num_cin1"));
                ligne.put("nom",              getFlexible(row, columnMap, columnMapNorm, "nom"));
                ligne.put("prenom",           getFlexible(row, columnMap, columnMapNorm, "prenom"));
                ligne.put("telephone",        getFlexible(row, columnMap, columnMapNorm, "cli_tel"));
                ligne.put("telephone2",       getFlexible(row, columnMap, columnMapNorm, "TEL2"));
                ligne.put("adresse",          getFlexible(row, columnMap, columnMapNorm, "cli_adr"));
                ligne.put("ville",            getFlexible(row, columnMap, columnMapNorm, "Lib_Ville"));
                ligne.put("codeAgence",       getFlexible(row, columnMap, columnMapNorm, "cod_age"));
                ligne.put("libAgence",        getFlexible(row, columnMap, columnMapNorm, "lib_age"));
                ligne.put("adresseCadrage",   getFlexible(row, columnMap, columnMapNorm, "Adresse_Cadrage"));

                // Credit
                ligne.put("referenceContrat", getFlexible(row, columnMap, columnMapNorm, "ANC_REFDO"));
                ligne.put("fromProv",         getFlexible(row, columnMap, columnMapNorm, "From_Prov"));
                ligne.put("gestCin",          getFlexible(row, columnMap, columnMapNorm, "GEST_CIN"));
                ligne.put("typeCredit",       getFlexible(row, columnMap, columnMapNorm, "CSP_PDT"));
                ligne.put("segment",          getFlexible(row, columnMap, columnMapNorm, "Segment"));
                ligne.put("nbrImpaye",        parseIntegerOrZero(getFlexible(row, columnMap, columnMapNorm, "nbr_imp")));
                ligne.put("mensualite",       parseBigDecimalOrZero(getFlexible(row, columnMap, columnMapNorm, "Mensualite")));
                ligne.put("encours",          parseBigDecimalOrZero(getFlexible(row, columnMap, columnMapNorm, "encours")));
                ligne.put("impayeMontant",    parseBigDecimalOrZero(getFlexible(row, columnMap, columnMapNorm, "Mnt_Imp")));
                ligne.put("prvCum",           parseBigDecimalOrZero(getFlexible(row, columnMap, columnMapNorm, "prv_cum")));
                ligne.put("stage",            getFlexible(row, columnMap, columnMapNorm, "stage"));
                ligne.put("dvr",              getFlexible(row, columnMap, columnMapNorm, "DVR"));
                ligne.put("reecheance",       getFlexible(row, columnMap, columnMapNorm, "Reecheance"));
                ligne.put("immatriculation",  getFlexible(row, columnMap, columnMapNorm, "Immatriculation"));

                // Score — essaie plusieurs variantes
                String scoreVal = getFlexible(row, columnMap, columnMapNorm, "score_final");
                if (scoreVal.trim().isEmpty()) scoreVal = getFlexible(row, columnMap, columnMapNorm, "Score_final");
                if (scoreVal.trim().isEmpty()) scoreVal = getFlexible(row, columnMap, columnMapNorm, "Score");
                if (scoreVal.trim().isEmpty()) scoreVal = getFlexible(row, columnMap, columnMapNorm, "score");
                ligne.put("score", parseBigDecimalOrZero(scoreVal));

                ligne.put("agentEmail",       getFlexible(row, columnMap, columnMapNorm, "Agent_Email"));

                // Log des 3 premières lignes pour debug
                if (nbLignesLues < 3) {
                    log.info("Ligne {} → CIN='{}', REF='{}', NOM='{}', PRENOM='{}'",
                        i + 1, ligne.get("cin"), ligne.get("referenceContrat"),
                        ligne.get("nom"), ligne.get("prenom"));
                }

                lignes.add(ligne);
                nbLignesLues++;
            }

            log.info("========== FIN LECTURE EXCEL ==========");
            log.info("Lignes lues: {}, Lignes vides ignorées: {}", nbLignesLues, nbLignesVides);
        }

        return lignes;
    }

    // ========== METHODE DE RECHERCHE FLEXIBLE ==========

    /**
     * Recherche flexible d'une colonne par son nom.
     * Priorité : 1) exact match, 2) case-insensitive, 3) normalisé, 4) sous-chaîne (contains)
     */
    private String getFlexible(Row row, Map<String, Integer> columnMap, 
                               Map<String, Integer> columnMapNorm, String headerName) {
        Integer idx = findColumnIndex(headerName, columnMap, columnMapNorm);
        if (idx == null) return "";
        return getCellValue(row, idx);
    }

    /**
     * Trouve l'index de colonne en utilisant plusieurs stratégies de matching
     */
    private Integer findColumnIndex(String headerName, Map<String, Integer> columnMap, 
                                     Map<String, Integer> columnMapNorm) {
        // 1. Exact match
        Integer idx = columnMap.get(headerName);
        if (idx != null) return idx;

        // 2. Case-insensitive exact match via la map normalisée
        String norm = headerName.toLowerCase().replace(" ", "_").replace("-", "_").replace(".", "_");
        idx = columnMapNorm.get(norm);
        if (idx != null) return idx;

        // 3. Sous-chaîne : le header Excel CONTIENT le nom cherché ou vice-versa
        String normLower = norm;
        for (Map.Entry<String, Integer> entry : columnMapNorm.entrySet()) {
            String excelNorm = entry.getKey();
            if (excelNorm.contains(normLower) || normLower.contains(excelNorm)) {
                return entry.getValue();
            }
        }

        // 4. Variantes communes
        // num_cin1 → num_cin, cin, num_cin1
        if (normLower.equals("num_cin1")) {
            for (String alt : new String[]{"num_cin", "cin", "cin1", "num cin1", "numcin1"}) {
                idx = columnMapNorm.get(alt);
                if (idx != null) return idx;
            }
        }
        if (normLower.equals("anc_refdo")) {
            for (String alt : new String[]{"anc refdo", "ancrefdo", "ref_dossier", "reference", "refdo"}) {
                idx = columnMapNorm.get(alt);
                if (idx != null) return idx;
            }
        }

        return null;
    }

    // ========== UTILITAIRES ==========

    private String getCellValue(Row row, int columnIndex) {
        if (row == null) return "";
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                }
                double numericValue = cell.getNumericCellValue();
                if (numericValue == Math.floor(numericValue)) {
                    yield String.valueOf((long) numericValue);
                }
                yield String.valueOf(numericValue);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    yield cell.getStringCellValue();
                }
            }
            default -> "";
        };
    }

    private BigDecimal parseBigDecimalOrZero(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            String cleaned = value.trim()
                .replace(",", ".")
                .replace(" ", "")
                .replaceAll("[^0-9.]", "");
            if (cleaned.isEmpty()) return BigDecimal.ZERO;
            return new BigDecimal(cleaned);
        } catch (Exception e) {
            log.warn("Impossible de parser BigDecimal: {}", value);
            return BigDecimal.ZERO;
        }
    }

    private Integer parseIntegerOrZero(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            String cleaned = value.trim().replaceAll("[^0-9]", "");
            if (cleaned.isEmpty()) return 0;
            return Integer.parseInt(cleaned);
        } catch (Exception e) {
            log.warn("Impossible de parser Integer: {}", value);
            return 0;
        }
    }
}
