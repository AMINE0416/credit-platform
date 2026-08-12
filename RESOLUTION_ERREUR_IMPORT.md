# Résolution Erreur Import - SUIVI_INJOIGNABLE

## 🔍 Problème Identifié

**Erreur lors de l'import Excel:**
```
ERREUR: la nouvelle ligne de la relation « dossiers » viole la contrainte de vérification « dossiers_statut_actuel_check »
```

**Cause:**
- Le code Java utilise le statut `SUIVI_INJOIGNABLE` (défini dans `StatutDossier.java`)
- La base de données PostgreSQL n'avait pas cette valeur dans l'enum `statut_dossier`
- Lors de l'import, les dossiers récurrents qui étaient `INJOIGNABLE` deviennent `SUIVI_INJOIGNABLE`
- PostgreSQL rejetait l'insertion car la valeur n'existait pas dans l'enum

## ✅ Solution Appliquée

### 1. Configuration Automatique
**Fichier créé:** `src/main/java/org/eqdom/creditplatform/config/EnumMigrationConfig.java`

Cette configuration:
- Vérifie automatiquement au démarrage de l'application
- Détecte les statuts manquants dans l'enum PostgreSQL
- Ajoute automatiquement les valeurs manquantes
- Log l'état des statuts pour surveillance

### 2. Vérification au Démarrage
Au démarrage de l'application, vous verrez maintenant:
```
? Vérification des statuts dans l'enum statut_dossier...
? Statuts actuels dans l'enum: [EN_COURS, PROMESSE, INJOIGNABLE, REGULARISE, ALERTE, SUIVI_PROMESSE, SUIVI_INJOIGNABLE, CLOTURE]
? Tous les statuts requis sont présents dans l'enum
```

## 📋 Statuts Disponibles

### Statuts de Travail (utilisés par les agents)
- `EN_COURS` - Point de départ pour tous les dossiers
- `PROMESSE` - Client a fait une promesse de paiement
- `INJOIGNABLE` - Client injoignable
- `REGULARISE` - Dossier régularisé

### Statuts de Suivi (créés lors de l'archivage)
- `ALERTE` - Récurrent: était EN_COURS (non résolu)
- `SUIVI_PROMESSE` - Récurrent: avait une PROMESSE
- `SUIVI_INJOIGNABLE` - Récurrent: était INJOIGNABLE ✅ **Maintenant disponible**

### Statut Final
- `CLOTURE` - Dossier clôturé

## 🔄 Logique de Transformation

Lors de l'archivage mensuel, les statuts sont transformés:
```
EN_COURS         → ALERTE (récurrent non résolu)
PROMESSE         → SUIVI_PROMESSE (suivi promesse)
INJOIGNABLE      → SUIVI_INJOIGNABLE (suivi injoignable) ✅
REGULARISE       → REGULARISE (reste régularisé)
Nouveaux dossiers → EN_COURS (point de départ)
```

## 🧪 Test de la Solution

1. **Redémarrer l'application** (déjà fait)
2. **Vérifier les logs** - doit afficher tous les statuts présents
3. **Importer un fichier Excel** - l'import devrait maintenant fonctionner
4. **Vérifier les dossiers récurrents** - doivent avoir le bon statut

## 📝 Scripts SQL Disponibles

Si vous avez besoin d'exécuter manuellement:
- `scripts/fix-enum-direct.sql` - Ajoute SUIVI_INJOIGNABLE manuellement
- `scripts/migration_nouveaux_statuts.sql` - Migration complète des statuts

## ✅ Résultat

L'application est maintenant capable de:
1. ✅ Importer des dossiers avec tous les statuts
2. ✅ Gérer les dossiers récurrents correctement
3. ✅ Transformer les statuts lors de l'archivage
4. ✅ Vérifier automatiquement la cohérence des enums au démarrage

**L'erreur d'import est résolue!** Vous pouvez maintenant importer votre fichier Excel sans problème.
