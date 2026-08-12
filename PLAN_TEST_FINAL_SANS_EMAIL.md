# 🧪 PLAN DE TEST COMPLET - SYSTÈME D'ARCHIVAGE
## Version adaptée sans email client

---

## ✅ CORRECTIONS APPLIQUÉES

- ✅ **Email client retiré** : Le modèle Client n'a pas de champ email
- ✅ **Mapping JPA corrigé** : Relation User commentée dans LogArchivage
- ✅ **Compilation Maven** : BUILD SUCCESS confirmé
- ✅ **Type ENUM PostgreSQL** : statut_dossier utilisé correctement

---

## 📋 TABLE DES MATIÈRES

1. [Prérequis](#prérequis)
2. [Phase 1 : Test Backend (10 min)](#phase-1-test-backend)
3. [Phase 2 : Test SQL Complet (5 min)](#phase-2-test-sql-complet)
4. [Phase 3 : Test APIs REST (10 min)](#phase-3-test-apis-rest)
5. [Phase 4 : Test Interface Web (5 min)](#phase-4-test-interface-web)
6. [Phase 5 : Test Cycle Complet (10 min)](#phase-5-test-cycle-complet)
7. [Validation Finale](#validation-finale)

---

## 🎯 PRÉREQUIS

### Logiciels Nécessaires
- ✅ PostgreSQL installé et démarré
- ✅ Java 17+ et Maven installé
- ✅ Node.js et npm installé (pour le frontend)
- ✅ psql ou pgAdmin pour les tests SQL
- ✅ PowerShell (Windows)

### Vérification Rapide

```powershell
# Vérifier PostgreSQL
psql --version

# Vérifier Java
java -version

# Vérifier Maven
mvn -version

# Vérifier la base de données
$env:PGPASSWORD="votre_password"
psql -U postgres -d credit_platform -c "SELECT version();"
Remove-Item Env:\PGPASSWORD
```

---

## 📋 PHASE 1 : TEST BACKEND (10 min)

### 1.1 Créer les Tables SQL

```powershell
# Depuis la racine du projet
cd c:\Users\asus\Documents\credit-platform

# Définir les variables d'environnement
$env:PGPASSWORD="votre_password"
$env:PGUSER="postgres"
$env:PGDATABASE="credit_platform"

# Exécuter le script de création
psql -U postgres -d credit_platform -f scripts\create_archivage_system.sql

# Nettoyer
Remove-Item Env:\PGPASSWORD
Remove-Item Env:\PGUSER
Remove-Item Env:\PGDATABASE
```

### 1.2 Vérifier les Tables Créées

```sql
-- Se connecter à psql
psql -U postgres -d credit_platform

-- Vérifier les tables
\dt *archive*

-- Attendu:
-- dossiers_archive
-- logs_archivage
```

```sql
-- Vérifier les colonnes ajoutées à dossiers
\d dossiers

-- Attendu:
-- mois_import          | character varying(7)
-- est_recurrent        | boolean
-- mois_precedent_statut| character varying(50)
```

✅ **Validation :**
- 3 nouvelles tables créées (dossiers_archive, dossiers_mois_precedent, logs_archivage)
- 3 colonnes ajoutées à la table dossiers
- Fonctions SQL créées (archiver_mois, nettoyer_dossiers, calculer_statut_recurrent)

### 1.3 Recompiler le Backend

```powershell
# Nettoyer et compiler
mvn clean compile -DskipTests
```

✅ **Attendu :**
```
[INFO] BUILD SUCCESS
[INFO] Total time:  XX.XXX s
```

❌ **Si erreur :**
- Vérifier que LogArchivage.java a la relation User commentée
- Vérifier les imports manquants
- Consulter le message d'erreur complet

### 1.4 Démarrer l'Application

```powershell
# Démarrer Spring Boot
mvn spring-boot:run
```

✅ **Attendu dans les logs :**
```
Started CreditPlatformApplication in X.XXX seconds
Mapped "{[/api/archivage/archiver],methods=[POST]}"
Mapped "{[/api/archivage/nettoyer/{mois}],methods=[DELETE]}"
Mapped "{[/api/archivage/mois],methods=[GET]}"
Mapped "{[/api/archivage/statistiques/{mois}],methods=[GET]}"
Mapped "{[/api/archivage/dossiers/{mois}],methods=[GET]}"
Mapped "{[/api/archivage/logs],methods=[GET]}"
```

❌ **Si erreur JPA :**
```
Table [logs_archivage] contains physical column name [utilisateur_id]...
```
→ Vérifier que la relation `@ManyToOne` vers User est bien commentée dans LogArchivage.java

---

## 📋 PHASE 2 : TEST SQL COMPLET (5 min)

### 2.1 Script de Test Automatique

```powershell
# Exécuter le test complet
$env:PGPASSWORD="votre_password"
psql -U postgres -d credit_platform -f scripts\test_complet_archivage.sql
Remove-Item Env:\PGPASSWORD
```

Ce script va :
1. ✅ Créer 5 dossiers pour le mois 7
2. ✅ Archiver le mois 7
3. ✅ Nettoyer le mois 7
4. ✅ Importer 5 dossiers pour le mois 8 (3 récurrents + 2 nouveaux)
5. ✅ Détecter automatiquement les dossiers récurrents
6. ✅ Appliquer les transitions de statut

### 2.2 Vérifications Manuelles

```sql
-- 1. Vérifier les archives du mois 7
SELECT COUNT(*) FROM dossiers_archive WHERE mois_archive = '2024-07';
-- Attendu: 5
```

```sql
-- 2. Vérifier que le mois 7 est nettoyé
SELECT COUNT(*) FROM dossiers WHERE mois_import = '2024-07';
-- Attendu: 0
```

```sql
-- 3. Vérifier les dossiers du mois 8
SELECT COUNT(*) FROM dossiers WHERE mois_import = '2024-08';
-- Attendu: 5
```

```sql
-- 4. Vérifier la détection des récurrents
SELECT COUNT(*) FROM dossiers 
WHERE mois_import = '2024-08' AND est_recurrent = true;
-- Attendu: 3
```

```sql
-- 5. Voir les transitions de statut
SELECT 
    cr.reference_contrat,
    CASE WHEN d.est_recurrent THEN '🔄 RÉCURRENT' ELSE '🆕 NOUVEAU' END as type,
    COALESCE(d.mois_precedent_statut, 'N/A') as ancien_statut,
    d.statut_actuel::text as nouveau_statut,
    COALESCE(d.mois_precedent_statut, 'N/A') || ' → ' || d.statut_actuel::text as transition
FROM dossiers d
JOIN credits cr ON d.credit_id = cr.id
WHERE d.mois_import = '2024-08'
ORDER BY d.est_recurrent DESC, cr.reference_contrat;
```

✅ **Attendu :**
```
reference_contrat | type          | ancien_statut | nouveau_statut    | transition
------------------+---------------+---------------+-------------------+-------------------------
DOSS-TEST-001     | 🔄 RÉCURRENT  | CONTACTE      | RELANCE          | CONTACTE → RELANCE
DOSS-TEST-002     | 🔄 RÉCURRENT  | PROMESSE      | SUIVI_PROMESSE   | PROMESSE → SUIVI_PROMESSE
DOSS-TEST-003     | 🔄 RÉCURRENT  | INJOIGNABLE   | INJOIGNABLE      | INJOIGNABLE → INJOIGNABLE
DOSS-TEST-006     | 🆕 NOUVEAU    | N/A           | EN_COURS         | N/A → EN_COURS
DOSS-TEST-007     | 🆕 NOUVEAU    | N/A           | EN_COURS         | N/A → EN_COURS
```

### 2.3 Vérifier les Logs

```sql
SELECT 
    operation,
    mois_concerne,
    nb_dossiers,
    COALESCE(nb_recurrents, 0) as recurrents,
    COALESCE(nb_nouveaux, 0) as nouveaux,
    statut,
    to_char(date_operation, 'YYYY-MM-DD HH24:MI:SS') as date
FROM logs_archivage
ORDER BY date_operation DESC;
```

✅ **Attendu (minimum 3 lignes) :**
```
operation   | mois_concerne | nb_dossiers | recurrents | nouveaux | statut  | date
------------+---------------+-------------+------------+----------+---------+---------------------
IMPORT      | 2024-08       | 5           | 3          | 2        | SUCCESS | 2024-XX-XX XX:XX:XX
NETTOYAGE   | 2024-07       | 5           | 0          | 0        | SUCCESS | 2024-XX-XX XX:XX:XX
ARCHIVAGE   | 2024-07       | 5           | 0          | 0        | SUCCESS | 2024-XX-XX XX:XX:XX
```

---

## 📋 PHASE 3 : TEST APIs REST (10 min)

### 3.1 Obtenir un Token Admin

```powershell
# Connexion avec l'utilisateur admin
$loginResponse = Invoke-RestMethod `
    -Uri "http://localhost:8080/api/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body (@{
        username = "admin"
        password = "admin"
    } | ConvertTo-Json)

$token = $loginResponse.token
Write-Host "✅ Token obtenu: $token"
```

### 3.2 Test API : Lister les Mois Archivés

```powershell
$moisArchives = Invoke-RestMethod `
    -Uri "http://localhost:8080/api/archivage/mois" `
    -Method GET `
    -Headers @{ "Authorization" = "Bearer $token" }

Write-Host "📋 Mois archivés:"
$moisArchives | ForEach-Object { Write-Host "  - $_" }
```

✅ **Attendu :**
```
📋 Mois archivés:
  - 2024-07
```

### 3.3 Test API : Statistiques d'un Mois

```powershell
$stats = Invoke-RestMethod `
    -Uri "http://localhost:8080/api/archivage/statistiques/2024-07" `
    -Method GET `
    -Headers @{ "Authorization" = "Bearer $token" }

Write-Host "📊 Statistiques mois 2024-07:"
$stats | ConvertTo-Json -Depth 5
```

✅ **Attendu :**
```json
{
  "moisArchive": "2024-07",
  "totalDossiers": 5,
  "repartitionStatuts": {
    "CONTACTE": 1,
    "PROMESSE": 1,
    "INJOIGNABLE": 1,
    "EN_ATTENTE": 1,
    "EN_COURS": 1
  },
  "totalDuMensuel": 50000.00,
  "dateArchivage": "2024-XX-XXTXX:XX:XX"
}
```

### 3.4 Test API : Consulter les Dossiers Archivés

```powershell
$dossiers = Invoke-RestMethod `
    -Uri "http://localhost:8080/api/archivage/dossiers/2024-07" `
    -Method GET `
    -Headers @{ "Authorization" = "Bearer $token" }

Write-Host "📦 Dossiers archivés (mois 2024-07): $($dossiers.Count)"
$dossiers | Select-Object numeroDossier, nomClient, statutFinal, totalDu | Format-Table
```

✅ **Attendu :**
```
numeroDossier   nomClient        statutFinal   totalDu
-------------   ---------        -----------   -------
DOSS-TEST-001   CLIENT Test 1    CONTACTE      10000.00
DOSS-TEST-002   CLIENT Test 2    PROMESSE      10000.00
DOSS-TEST-003   CLIENT Test 3    INJOIGNABLE   10000.00
DOSS-TEST-004   CLIENT Test 4    EN_ATTENTE    10000.00
DOSS-TEST-005   CLIENT Test 5    EN_COURS      10000.00
```

### 3.5 Test API : Consulter les Logs

```powershell
$logs = Invoke-RestMethod `
    -Uri "http://localhost:8080/api/archivage/logs" `
    -Method GET `
    -Headers @{ "Authorization" = "Bearer $token" }

Write-Host "📝 Logs d'archivage:"
$logs | Select-Object operation, moisConcerne, nbDossiers, statut, dateOperation | Format-Table
```

✅ **Attendu :**
```
operation   moisConcerne  nbDossiers  statut   dateOperation
---------   ------------  ----------  ------   -------------
IMPORT      2024-08       5           SUCCESS  2024-XX-XX...
NETTOYAGE   2024-07       5           SUCCESS  2024-XX-XX...
ARCHIVAGE   2024-07       5           SUCCESS  2024-XX-XX...
```

### 3.6 Test API : Archivage via API (optionnel)

```powershell
# Créer quelques dossiers pour un nouveau mois
# (À faire manuellement dans la BDD ou via import Excel)

# Puis archiver
$archivageResponse = Invoke-RestMethod `
    -Uri "http://localhost:8080/api/archivage/archiver" `
    -Method POST `
    -Headers @{ 
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    } `
    -Body (@{
        mois = "2024-09"
        exporterCsv = $true
    } | ConvertTo-Json)

Write-Host "📦 Résultat archivage:"
$archivageResponse | ConvertTo-Json -Depth 3
```

### 3.7 Test API : Nettoyage via API

```powershell
$nettoyageResponse = Invoke-RestMethod `
    -Uri "http://localhost:8080/api/archivage/nettoyer/2024-09" `
    -Method DELETE `
    -Headers @{ "Authorization" = "Bearer $token" }

Write-Host "🧹 Résultat nettoyage:"
$nettoyageResponse | ConvertTo-Json
```

---

## 📋 PHASE 4 : TEST INTERFACE WEB (5 min)

### 4.1 Démarrer le Frontend

```powershell
# Dans un autre terminal
cd c:\Users\asus\Documents\credit-platform\frontend

# Installer les dépendances (si nécessaire)
npm install

# Démarrer le serveur de développement
npm run dev
```

### 4.2 Accéder à la Page

1. Ouvrir le navigateur : `http://localhost:5173`
2. Se connecter avec : `admin` / `admin`
3. Aller sur : **Menu → Gestion Mensuelle**

### 4.3 Vérifications Interface

✅ **Section "Mois Actuel" doit afficher :**
- Titre : "Gestion du Mois Actuel"
- Bouton : "📦 Archiver et Clôturer {MOIS}"
- Bouton : "🧹 Nettoyer le Mois {MOIS}"
- Bouton : "📥 Importer le Mois Suivant"

✅ **Section "Historique des Archives" doit afficher :**
- Card pour le mois "2024-07"
- Nombre de dossiers : 5
- Boutons : "Voir détails" et "Télécharger CSV"

✅ **Section "Logs d'Archivage" doit afficher :**
- 3 lignes minimum (ARCHIVAGE, NETTOYAGE, IMPORT)
- Toutes avec statut "SUCCESS" (badge vert)
- Dates et heures correctes

### 4.4 Test Bouton "Voir Détails"

1. Cliquer sur "👁️ Voir détails" pour le mois 2024-07
2. ✅ Une popup doit s'ouvrir avec :
   - Titre : "Détails Archives - 2024-07"
   - Total dossiers : 5
   - Répartition par statut (graphique ou liste)
   - Total dû mensuel
   - Date d'archivage
   - Bouton "Fermer"

### 4.5 Test Bouton "Télécharger CSV"

1. Cliquer sur "📥 Télécharger CSV" pour le mois 2024-07
2. ✅ Un fichier `archives_2024-07.csv` doit se télécharger
3. ✅ Ouvrir le fichier et vérifier :
   - En-têtes de colonnes présentes
   - 5 lignes de données
   - Données correctes (numéro dossier, client, statut, montants)

---

## 📋 PHASE 5 : TEST CYCLE COMPLET (10 min)

### 5.1 Préparation

```sql
-- Créer manuellement 3 dossiers pour le mois actuel
-- Adapter le mois selon la date actuelle (ex: 2024-08)

DO $$
DECLARE
    v_mois TEXT := TO_CHAR(CURRENT_DATE, 'YYYY-MM');
BEGIN
    -- Insérer 3 dossiers de test
    INSERT INTO dossiers (credit_id, statut_actuel, date_creation, mois_import, est_recurrent)
    SELECT 
        cr.id,
        'EN_COURS'::statut_dossier,
        CURRENT_DATE,
        v_mois,
        false
    FROM credits cr
    WHERE cr.reference_contrat LIKE 'DOSS-TEST-%'
    LIMIT 3;
    
    RAISE NOTICE 'Créé 3 dossiers pour le mois %', v_mois;
END $$;
```

### 5.2 Test Archivage via Interface

1. Rafraîchir la page `/admin/gestion-mensuelle`
2. Vérifier que le mois actuel est affiché
3. Cliquer sur **"📦 Archiver et Clôturer {MOIS}"**
4. ✅ Une popup de confirmation doit apparaître
5. Cliquer sur "Confirmer"
6. ✅ Message de succès : "Archivage réussi : X dossiers archivés"
7. ✅ Le mois doit apparaître dans la section "Historique"

### 5.3 Test Nettoyage via Interface

1. Dans la section "Mois Actuel", cliquer sur **"🧹 Nettoyer le Mois {MOIS}"**
2. ✅ Une popup de confirmation doit apparaître avec avertissement
3. Cliquer sur "Confirmer"
4. ✅ Message de succès : "Nettoyage réussi : X dossiers supprimés"

### 5.4 Vérification SQL

```sql
-- Vérifier que le nettoyage a fonctionné
SELECT COUNT(*) FROM dossiers WHERE mois_import = '{votre_mois}';
-- Attendu: 0

-- Vérifier que l'archive existe
SELECT COUNT(*) FROM dossiers_archive WHERE mois_archive = '{votre_mois}';
-- Attendu: 3 (ou le nombre de dossiers créés)

-- Vérifier les logs
SELECT * FROM logs_archivage 
WHERE mois_concerne = '{votre_mois}' 
ORDER BY date_operation DESC;
-- Attendu: 2 lignes (ARCHIVAGE + NETTOYAGE)
```

### 5.5 Test Import avec Détection

1. Importer un fichier Excel avec des dossiers dont certains existent dans le mois précédent
2. ✅ La détection automatique doit fonctionner
3. ✅ Les dossiers récurrents doivent avoir leur statut mis à jour automatiquement

```sql
-- Vérifier l'import
SELECT 
    cr.reference_contrat,
    d.est_recurrent,
    d.mois_precedent_statut,
    d.statut_actuel::text
FROM dossiers d
JOIN credits cr ON d.credit_id = cr.id
WHERE d.mois_import = '{nouveau_mois}'
ORDER BY d.est_recurrent DESC;
```

---

## 📋 VALIDATION FINALE

### Checklist Globale

#### ✅ Setup et Configuration
- [ ] Tables SQL créées (dossiers_archive, dossiers_mois_precedent, logs_archivage)
- [ ] Colonnes ajoutées à dossiers (mois_import, est_recurrent, mois_precedent_statut)
- [ ] Fonctions SQL créées et fonctionnelles
- [ ] Compilation Maven réussie (BUILD SUCCESS)
- [ ] Application Spring Boot démarre sans erreur
- [ ] Tous les endpoints mappés correctement

#### ✅ Tests SQL
- [ ] Script test_complet_archivage.sql exécuté
- [ ] 5 dossiers créés pour le mois 7
- [ ] Archivage mois 7 réussi (5 archives)
- [ ] Table détection remplie (5 entrées)
- [ ] Nettoyage mois 7 réussi (0 dossiers actifs)
- [ ] Import mois 8 réussi (5 dossiers)
- [ ] 3 dossiers récurrents détectés
- [ ] 2 nouveaux dossiers détectés
- [ ] Transitions de statut correctes (CONTACTE→RELANCE, etc.)

#### ✅ Tests API
- [ ] GET /api/archivage/mois fonctionne
- [ ] GET /api/archivage/statistiques/{mois} fonctionne
- [ ] GET /api/archivage/dossiers/{mois} fonctionne
- [ ] GET /api/archivage/logs fonctionne
- [ ] POST /api/archivage/archiver fonctionne
- [ ] DELETE /api/archivage/nettoyer/{mois} fonctionne
- [ ] Authentification requise pour tous les endpoints
- [ ] Restriction ADMIN appliquée

#### ✅ Tests Interface
- [ ] Page /admin/gestion-mensuelle accessible
- [ ] Section "Mois Actuel" affichée correctement
- [ ] Section "Historique" affichée correctement
- [ ] Section "Logs" affichée correctement
- [ ] Bouton "Archiver" fonctionne
- [ ] Bouton "Nettoyer" fonctionne
- [ ] Bouton "Voir détails" fonctionne
- [ ] Bouton "Télécharger CSV" fonctionne
- [ ] Popups de confirmation s'affichent
- [ ] Messages de succès/erreur s'affichent

#### ✅ Tests Cycle Complet
- [ ] Archivage via interface OK
- [ ] Nettoyage via interface OK
- [ ] Import avec détection automatique OK
- [ ] Logs enregistrés correctement
- [ ] Données cohérentes entre archive et tables actives

---

## 📊 REQUÊTE DE VALIDATION GLOBALE

```sql
-- Vue d'ensemble complète du système
SELECT 
    'Archives mois 7' as description,
    COUNT(*)::text as valeur
FROM dossiers_archive 
WHERE mois_archive = '2024-07'

UNION ALL

SELECT 
    'Dossiers actifs mois 8',
    COUNT(*)::text
FROM dossiers 
WHERE mois_import = '2024-08'

UNION ALL

SELECT 
    'Récurrents mois 8',
    COUNT(*)::text
FROM dossiers 
WHERE mois_import = '2024-08' AND est_recurrent = true

UNION ALL

SELECT 
    'Nouveaux mois 8',
    COUNT(*)::text
FROM dossiers 
WHERE mois_import = '2024-08' AND est_recurrent = false

UNION ALL

SELECT 
    'Logs SUCCESS',
    COUNT(*)::text
FROM logs_archivage 
WHERE statut = 'SUCCESS'

UNION ALL

SELECT 
    'Total mois archivés',
    COUNT(DISTINCT mois_archive)::text
FROM dossiers_archive

UNION ALL

SELECT 
    'Transitions correctes',
    COUNT(*)::text
FROM dossiers 
WHERE mois_import = '2024-08'
AND est_recurrent = true
AND (
    (mois_precedent_statut = 'CONTACTE' AND statut_actuel = 'RELANCE')
    OR (mois_precedent_statut = 'PROMESSE' AND statut_actuel = 'SUIVI_PROMESSE')
    OR (mois_precedent_statut = 'INJOIGNABLE' AND statut_actuel = 'INJOIGNABLE')
);
```

✅ **Résultat Attendu :**
```
description             | valeur
------------------------+-------
Archives mois 7         | 5
Dossiers actifs mois 8  | 5
Récurrents mois 8       | 3
Nouveaux mois 8         | 2
Logs SUCCESS            | 3+
Total mois archivés     | 1+
Transitions correctes   | 3
```

---

## 🎯 RÉSULTAT FINAL

Si tous les tests passent :

### ✅ Système Opérationnel
- Tables et fonctions SQL créées et testées
- Backend Java compile et démarre sans erreur
- APIs REST fonctionnelles et sécurisées
- Interface web intuitive et réactive
- Détection automatique des dossiers récurrents
- Transitions de statut appliquées correctement
- Logs d'archivage enregistrés

### 🚀 Prêt pour la Production

Le système peut être déployé en production avec les étapes suivantes :
1. Backup complet de la base de données
2. Exécuter le script `create_archivage_system.sql`
3. Tester avec un petit volume de données réelles
4. Former l'équipe administrative
5. Mettre en production

---

## 📚 DOCUMENTATION DISPONIBLE

- **SYSTEME_ARCHIVAGE_README.md** - Documentation complète du système
- **GUIDE_DEMARRAGE_ARCHIVAGE.md** - Guide de démarrage rapide
- **GUIDE_TEST_RAPIDE.md** - Test en 2 minutes
- **RESUME_IMPLEMENTATION_ARCHIVAGE.md** - Résumé de l'implémentation

---

## 🆘 DÉPANNAGE

### Problème : Application ne démarre pas
```
Solution : Vérifier LogArchivage.java (relation User commentée)
```

### Problème : Tests SQL échouent
```
Solution : Vérifier que les tables ont été créées (create_archivage_system.sql)
```

### Problème : API retourne 403 Forbidden
```
Solution : Vérifier le token d'authentification et le rôle ADMIN
```

### Problème : Interface ne charge pas
```
Solution : Vérifier que le frontend est démarré (npm run dev)
```

---

**Bon test ! 🎉**

