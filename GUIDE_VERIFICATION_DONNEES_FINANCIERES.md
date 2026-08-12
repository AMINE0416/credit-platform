# Guide de Vérification des Données Financières

## 📋 Checklist de Vérification

### Étape 1 : Vérifier les En-têtes Excel

Ouvrez votre fichier Excel et assurez-vous que la première ligne contient ces colonnes **exactement** (ou des variantes acceptées) :

| Colonne Requise | Variantes Acceptées | Description |
|----------------|---------------------|-------------|
| `Mensualite` | mensualite, MENSUALITE | Mensualité du crédit |
| `encours` | Encours, ENCOURS | Encours du crédit |
| `Mnt_Imp` | mnt_imp, MNT_IMP, Montant_Impaye | Montant impayé |
| `prv_cum` | PRV_CUM, Prv_Cum, provision_cumulee | Provision cumulée |

✅ **Le code a été amélioré** pour accepter toutes ces variantes automatiquement !

### Étape 2 : Vérifier le Contenu des Cellules

1. **Format des nombres** : Les cellules doivent contenir des nombres, pas du texte
   - ✅ Bon : `3520.50`
   - ✅ Bon : `3520`
   - ❌ Mauvais : `3 520,50` (espace comme séparateur de milliers)
   - ❌ Mauvais : `3520,50 MAD` (texte inclus)

2. **Cellules vides** : Acceptées, seront importées comme 0

3. **Formules** : Supportées, la valeur calculée sera importée

### Étape 3 : Recompiler et Redémarrer

```bash
# Recompiler le backend
mvn clean package -DskipTests

# Redémarrer l'application
# Sur Windows (PowerShell) :
java -jar target\credit-platform-0.0.1-SNAPSHOT.jar

# Ou utiliser votre IDE (bouton Run)
```

### Étape 4 : Ré-importer les Données

1. Connectez-vous en tant qu'administrateur
2. Allez dans **Gestion des Imports**
3. Cliquez sur **Réinitialiser la base de données** (⚠️ supprime toutes les données)
4. Importez votre fichier Excel
5. Vérifiez les logs dans la console pour voir :
   ```
   === Colonnes Excel trouvées (XX colonnes) ===
     - 'Mensualite'
     - 'encours'
     - 'Mnt_Imp'
     - 'prv_cum'
   ============================================
   
   Ligne 1 - Données financières:
     Mensualité: '3520.50', Encours: '45000', Impayé: '7041', Provision: '2500'
   ```

### Étape 5 : Vérifier l'Affichage

1. Allez sur la page de détail d'un dossier
2. Dans la section **Données Crédit**, vous devriez voir :
   - ✅ **Mensualité (MAD)** : `3 520,50 MAD`
   - ✅ **Encours (MAD)** : `45 000 MAD`
   - ✅ **Montant impayé (MAD)** : `7 041 MAD` (en surbrillance orange)
   - ✅ **Provision cumulée** : `2 500 MAD`

### Étape 6 : Vérifier en Base de Données (Optionnel)

Si vous avez accès à pgAdmin ou psql :

```sql
-- Vérifier quelques enregistrements
SELECT 
    reference_contrat,
    mensualite,
    encours,
    impaye_montant,
    prv_cum,
    nbr_impaye
FROM credits
WHERE mensualite IS NOT NULL
LIMIT 10;

-- Statistiques
SELECT 
    COUNT(*) as total,
    COUNT(mensualite) as avec_mensualite,
    AVG(mensualite) as mensualite_moyenne,
    SUM(impaye_montant) as total_impayes
FROM credits;
```

## 🔧 Améliorations Apportées au Code

### 1. Tolérance à la Casse
Le code accepte maintenant les colonnes même si la casse ne correspond pas exactement :
- `Mensualite` = `mensualite` = `MENSUALITE`

### 2. Variantes de Noms
Le code essaie plusieurs variantes pour chaque colonne :
- **Mnt_Imp** → cherche aussi `mnt_imp`, `Montant_Impaye`, `impaye_montant`
- **prv_cum** → cherche aussi `PRV_CUM`, `provision_cumulee`

### 3. Logs Améliorés
Des logs détaillés dans la console pour diagnostiquer :
- Liste de toutes les colonnes trouvées
- Valeurs des 3 premières lignes pour vérification

### 4. Double Mapping
Deux maps de colonnes :
- `columnMap` : mapping exact avec casse originale
- `columnMapLower` : mapping en minuscules pour recherche flexible

## ⚠️ Problèmes Fréquents

### Problème 1 : "Colonne non trouvée"
**Symptôme** : Les valeurs restent à 0 ou "—"

**Solution** :
1. Vérifiez les logs de l'import dans la console
2. Cherchez la section "Colonnes Excel trouvées"
3. Comparez avec les noms attendus
4. Si différents, ajoutez une variante dans `getCellValueByHeaderFlexible()`

### Problème 2 : "Valeurs = 0 après import"
**Symptôme** : Les colonnes sont reconnues mais les valeurs sont 0

**Causes possibles** :
- Cellules contenant du texte au lieu de nombres
- Format de nombre incompatible
- Cellules fusionnées dans Excel

**Solution** :
1. Dans Excel, sélectionnez la colonne
2. Format → Nombre → Sans décimales ou 2 décimales
3. Vérifiez qu'il n'y a pas de cellules fusionnées
4. Sauvegardez et ré-importez

### Problème 3 : "Chiffres mal formatés"
**Symptôme** : `3520.5` au lieu de `3 520,50 MAD`

**Explication** : C'est normal ! Le formatage est fait par le frontend :
- Backend stocke : `3520.5` (BigDecimal)
- Frontend affiche : `3 520,50 MAD` (avec séparateurs et devise)

Vérifiez que le composant `DossierDetail.tsx` utilise bien :
```typescript
credit.mensualite ? `${Number(credit.mensualite).toLocaleString("fr-MA")} MAD` : "—"
```

## 📊 Exemple de Fichier Excel Valide

```
num_cin1  | nom    | prenom | ... | Mensualite | encours | Mnt_Imp | prv_cum | ...
AB123456  | ALAMI  | Hassan | ... | 3520.50    | 45000   | 7041    | 2500    | ...
CD789012  | BENNANI| Fatima | ... | 2800       | 30000   | 5600    | 1800    | ...
```

## 🎯 Résultat Attendu

Après avoir suivi ce guide, vous devriez voir dans l'interface :

**Section Données Crédit :**
```
Réf. contrat          : REF123456
Provenance            : AGENCE_FES
CIN gestionnaire      : GH111111
Type de crédit        : AUTO_CLASSIQUE
Segment               : AUTO PP
Nombre d'impayés      : 2               [en orange]
Mensualité (MAD)      : 3 520,50 MAD
Encours (MAD)         : 45 000 MAD
Montant impayé (MAD)  : 7 041 MAD       [en orange]
Provision cumulée     : 2 500 MAD
Stage                 : S2
DVR                   : 30
Réécheance            : —
Immatriculation       : ABC-1234
```

---

**Date de création** : 3 août 2026  
**Dernière mise à jour** : 3 août 2026
