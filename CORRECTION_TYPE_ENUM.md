# 🔧 CORRECTION - Type ENUM statut_dossier

## ❌ Problème

L'erreur suivante apparaît lors de l'exécution des scripts SQL :
```
ERROR: le type « statut_dossier » n'existe pas
LINE 9:     END::statut_dossier,^
SQL state: 42704
```

## 🎯 Cause

Le type ENUM `statut_dossier` n'a pas été créé lors de la création initiale de la base de données.

## ✅ Solution en 2 Étapes

---

### ÉTAPE 1 : Créer le Type ENUM (si nécessaire)

```powershell
# Exécuter le script de création du type ENUM
$env:PGPASSWORD="votre_password"
psql -U postgres -d credit_platform -f scripts\create_enum_statut.sql
Remove-Item Env:\PGPASSWORD
```

**Ce que fait ce script :**
- Vérifie si le type ENUM `statut_dossier` existe déjà
- Le crée s'il n'existe pas
- Affiche les valeurs possibles

**Valeurs du type ENUM :**
- `EN_COURS`
- `CONTACTE`
- `PROMESSE`
- `INJOIGNABLE`
- `RELANCE`
- `SUIVI_PROMESSE`
- `EN_ATTENTE`
- `REGLE`
- `CONTENTIEUX`

---

### ÉTAPE 2 : Vérifier le Type de la Colonne statut_actuel

```sql
-- Se connecter à psql
psql -U postgres -d credit_platform

-- Vérifier le type de la colonne
\d dossiers

-- Chercher la ligne "statut_actuel"
```

**Deux cas possibles :**

#### CAS 1 : La colonne est déjà de type statut_dossier ✅

```
statut_actuel | statut_dossier | not null
```

→ **Rien à faire**, vous pouvez passer aux tests

#### CAS 2 : La colonne est de type VARCHAR ⚠️

```
statut_actuel | character varying(50) | not null
```

→ **Conversion nécessaire** (voir ci-dessous)

---

### CONVERSION VARCHAR → ENUM (Si nécessaire)

Si la colonne `statut_actuel` est de type VARCHAR, vous devez la convertir en ENUM :

```sql
-- Script de conversion
ALTER TABLE dossiers 
ALTER COLUMN statut_actuel TYPE statut_dossier 
USING statut_actuel::statut_dossier;
```

⚠️ **ATTENTION :** Cette commande échouera si des valeurs dans la colonne ne correspondent pas aux valeurs de l'ENUM.

**Pour vérifier les valeurs actuelles :**

```sql
-- Lister toutes les valeurs distinctes
SELECT DISTINCT statut_actuel FROM dossiers;
```

Si certaines valeurs ne sont pas dans l'ENUM, vous devez :
1. Les corriger manuellement
2. Ou les ajouter à l'ENUM

**Ajouter une valeur à l'ENUM :**

```sql
ALTER TYPE statut_dossier ADD VALUE 'NOUVELLE_VALEUR';
```

---

## 🚀 ALTERNATIVE : Utiliser VARCHAR partout

Si vous préférez ne pas utiliser l'ENUM, vous pouvez modifier les scripts pour utiliser VARCHAR :

### Option A : Ne pas créer l'ENUM

Les scripts ont été mis à jour pour fonctionner avec ou sans ENUM :
- `test_donnees_mois7.sql` détecte automatiquement le type
- `calculer_statut_recurrent()` retourne VARCHAR

### Option B : Garder VARCHAR dans la colonne

Si votre colonne `statut_actuel` est de type VARCHAR et que vous voulez la garder ainsi :

1. **Ne pas exécuter** `create_enum_statut.sql`
2. Les scripts de test s'adapteront automatiquement
3. Tout fonctionnera en VARCHAR

---

## ✅ TEST APRÈS CORRECTION

### Test 1 : Vérifier le Type ENUM

```sql
-- Afficher le type et ses valeurs
SELECT 
    typname as "Type",
    array_agg(enumlabel ORDER BY enumsortorder) as "Valeurs"
FROM pg_type 
JOIN pg_enum ON pg_type.oid = pg_enum.enumtypid
WHERE typname = 'statut_dossier'
GROUP BY typname;
```

✅ **Attendu :**
```
Type            | Valeurs
----------------+-----------------------------------------------------------
statut_dossier  | {EN_COURS,CONTACTE,PROMESSE,INJOIGNABLE,RELANCE,...}
```

### Test 2 : Vérifier la Colonne

```sql
-- Vérifier le type de la colonne statut_actuel
SELECT 
    column_name,
    data_type,
    udt_name
FROM information_schema.columns
WHERE table_name = 'dossiers' 
AND column_name = 'statut_actuel';
```

✅ **Attendu (avec ENUM) :**
```
column_name    | data_type      | udt_name
---------------+----------------+----------------
statut_actuel  | USER-DEFINED   | statut_dossier
```

✅ **Attendu (sans ENUM) :**
```
column_name    | data_type             | udt_name
---------------+-----------------------+----------
statut_actuel  | character varying     | varchar
```

### Test 3 : Exécuter les Tests

```powershell
# Relancer le test complet
$env:PGPASSWORD="votre_password"
psql -U postgres -d credit_platform -f scripts\test_complet_archivage.sql
Remove-Item Env:\PGPASSWORD
```

✅ **Attendu :** Pas d'erreur de type

---

## 📋 RÉSUMÉ DES ACTIONS

### Si vous voulez utiliser l'ENUM (Recommandé)

1. ✅ Exécuter `scripts\create_enum_statut.sql`
2. ✅ Vérifier le type de la colonne `statut_actuel`
3. ✅ Convertir la colonne si nécessaire
4. ✅ Relancer les tests

### Si vous voulez utiliser VARCHAR

1. ✅ Ne rien faire avec l'ENUM
2. ✅ Les scripts s'adapteront automatiquement
3. ✅ Relancer les tests

---

## 🆘 DÉPANNAGE

### Erreur : "cannot cast type character varying to statut_dossier"

**Solution :**
```sql
-- Vérifier les valeurs actuelles
SELECT DISTINCT statut_actuel FROM dossiers;

-- Si des valeurs invalides, les corriger d'abord
UPDATE dossiers 
SET statut_actuel = 'EN_COURS' 
WHERE statut_actuel NOT IN (
    'EN_COURS', 'CONTACTE', 'PROMESSE', 'INJOIGNABLE', 
    'RELANCE', 'SUIVI_PROMESSE', 'EN_ATTENTE', 'REGLE', 'CONTENTIEUX'
);

-- Puis reconvertir
ALTER TABLE dossiers 
ALTER COLUMN statut_actuel TYPE statut_dossier 
USING statut_actuel::statut_dossier;
```

### Erreur : "type statut_dossier already exists"

**C'est normal !** Le type existe déjà, vous pouvez continuer.

---

## 📚 FICHIERS MODIFIÉS

- ✅ `scripts/create_enum_statut.sql` - Créé (nouveau)
- ✅ `scripts/create_archivage_system.sql` - Corrigé (fonction calculer_statut_recurrent)
- ✅ `scripts/test_donnees_mois7.sql` - Corrigé (détection automatique)

---

## ✨ PROCHAINES ÉTAPES

Une fois la correction appliquée :

1. Relancer le script de test automatique :
   ```powershell
   .\scripts\test-complet-automatique.ps1
   ```

2. Ou suivre le guide complet :
   ```
   PLAN_TEST_FINAL_SANS_EMAIL.md
   ```

---

**Problème résolu ! 🎉**
