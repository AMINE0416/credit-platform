# Diagnostic et Solution - Problème d'Affichage des Données Financières

## 🔍 Problème Identifié

Les données financières suivantes ne s'affichent pas dans l'application :
- **Mensualité** (Mensualite)
- **Encours** (encours)
- **Montant impayé** (Mnt_Imp → impaye_montant)
- **Provision cumulée** (prv_cum)

## ✅ Vérifications Effectuées

### 1. Backend (Java/Spring Boot)
- ✅ Le modèle `Credit.java` contient bien les champs avec les bons types (BigDecimal)
- ✅ Le DTO `DossierDetailDTO.CreditDTO` expose bien ces champs
- ✅ Le contrôleur `ImportController` lit bien les colonnes Excel avec les bons noms

### 2. Frontend (React/TypeScript)
- ✅ Le composant `DossierDetail.tsx` affiche bien les champs avec formatage

### 3. Mapping Excel → Base de données
Le code d'import cherche les colonnes suivantes (sensible à la casse) :
```java
"Mensualite"  → mensualite
"encours"     → encours  
"Mnt_Imp"     → impaye_montant
"prv_cum"     → prv_cum
```

## 🎯 Causes Possibles

### Cause #1 : Noms de colonnes dans Excel
Les en-têtes de colonnes dans votre fichier Excel doivent correspondre **EXACTEMENT** (casse comprise) :
- ❌ `mensualite` → ✅ `Mensualite` (majuscule M)
- ❌ `Encours` → ✅ `encours` (minuscule e)
- ❌ `Mnt_imp` → ✅ `Mnt_Imp` (majuscule I)
- ❌ `prv_Cum` → ✅ `prv_cum` (minuscule c)

### Cause #2 : Espaces invisibles dans les en-têtes
Des espaces avant/après les noms de colonnes peuvent empêcher la reconnaissance.

### Cause #3 : Valeurs vides ou format incorrect
Les cellules Excel peuvent contenir :
- Des valeurs vides
- Du texte au lieu de nombres
- Des formules qui retournent des erreurs

### Cause #4 : Données non importées
Si l'import précédent n'a pas lu les bonnes colonnes, les données sont stockées comme NULL ou 0 dans la base.

## 🔧 Solutions

### Solution 1 : Vérifier les En-têtes Excel

1. Ouvrez votre fichier Excel
2. Vérifiez que la ligne d'en-tête (ligne 1) contient EXACTEMENT :
   ```
   num_cin1 | nom | prenom | cli_tel | TEL2 | cli_adr | Lib_Ville | cod_age | lib_age | ANC_REFDO | From_Prov | GEST_CIN | CSP_PDT | Segment | nbr_imp | Mensualite | encours | Mnt_Imp | prv_cum | stage | DVR | Reecheance | Immatriculation | ...
   ```

3. Assurez-vous qu'il n'y a **aucun espace** avant ou après les noms de colonnes

### Solution 2 : Améliorer le Code d'Import (Tolérance Casse)

Modifiez `ImportController.java` pour ignorer la casse des en-têtes :

```java
// Dans la méthode qui lit les en-têtes
Map<String, Integer> columnMap = new HashMap<>();
for (int i = 0; i < headerRow.getLastCellNum(); i++) {
    String header = getCellValue(headerRow, i).trim();
    // Stocker en minuscules pour ignorer la casse
    columnMap.put(header.toLowerCase(), i);
}
```

Puis créer une méthode helper :
```java
private String getCellValueByHeaderIgnoreCase(Row row, Map<String, Integer> columnMap, String header) {
    // Essayer plusieurs variantes de casse
    Integer colIndex = columnMap.get(header);
    if (colIndex == null) {
        colIndex = columnMap.get(header.toLowerCase());
    }
    if (colIndex == null) {
        colIndex = columnMap.get(header.toUpperCase());
    }
    if (colIndex == null) return "";
    return getCellValue(row, colIndex);
}
```

### Solution 3 : Vérifier la Base de Données

Exécutez le script de diagnostic :

```bash
psql -U postgres -d credit_platform -f scripts/diagnostic_donnees_financieres.sql
```

Ou dans pgAdmin, exécutez :
```sql
SELECT 
    reference_contrat,
    mensualite,
    encours,
    impaye_montant,
    prv_cum
FROM credits
LIMIT 10;
```

Si les valeurs sont NULL ou 0, vous devez ré-importer les données.

### Solution 4 : Ré-importer les Données

Si les données en base sont vides :

1. **Sauvegardez vos actions** (si vous avez des saisies importantes)
   ```sql
   CREATE TABLE actions_backup AS SELECT * FROM actions_dossier;
   ```

2. **Réinitialisez la base** via l'interface admin :
   - Allez dans "Gestion des Imports"
   - Cliquez sur "Réinitialiser la base de données"

3. **Ré-importez le fichier Excel** avec les en-têtes corrects

### Solution 5 : Modification Rapide du Code (Recommandée)

Je vais modifier le code pour qu'il soit plus tolérant avec les noms de colonnes.

## 📊 Script de Test

Pour tester si les données sont bien en base, exécutez :

```sql
-- Test rapide
SELECT COUNT(*) as total,
       COUNT(mensualite) as avec_mensualite,
       COUNT(encours) as avec_encours,
       COUNT(impaye_montant) as avec_impaye,
       COUNT(prv_cum) as avec_provision
FROM credits;
```

Si les 4 dernières colonnes montrent 0, les données n'ont pas été importées.

## 🎬 Prochaines Étapes

1. Exécutez le script de diagnostic SQL
2. Vérifiez les en-têtes de votre fichier Excel
3. Appliquez la solution recommandée (code tolérant à la casse)
4. Ré-importez les données si nécessaire
5. Vérifiez l'affichage dans l'interface

---

**Date de création** : 3 août 2026  
**Dernière mise à jour** : 3 août 2026
