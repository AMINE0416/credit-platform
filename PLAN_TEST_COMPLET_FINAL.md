# 🧪 PLAN DE TEST COMPLET - SYSTÈME D'ARCHIVAGE

## ✅ CORRECTIONS APPLIQUÉES

- ✅ Erreur mapping JPA corrigée (utilisateur_id)
- ✅ Email client retiré (n'existe pas dans le modèle)
- ✅ Type ENUM PostgreSQL utilisé pour statut_dossier

---

## 📋 ÉTAPE 1 : Créer les Tables (5 min)

### 1.1 Exécuter le Script SQL

```powershell
# PowerShell
$env:PGPASSWORD="votre_password"
psql -U postgres -d credit_platform -f scripts\create_archivage_system.sql
Remove-Item Env:\PGPASSWORD
```

### 1.2 Vérifier les Tables

```sql
-- Ouvrir psql
psql -U postgres -d credit_platform

-- Vérifier les tables créées
\dt *archive*
\dt *mois_precedent*
\dt *logs_archivage*

-- Vérifier les colonnes ajoutées à dossiers
\d dossiers
```

✅ **Attendu :**
- Table `dossiers_archive` créée
- Table `dossiers_mois_precedent` créée
- Table `logs_archivage` créée
- Colonnes `mois_import`, `est_recurrent`, `mois_precedent_statut` dans `dossiers`

---

## 📋 ÉTAPE 2 : Recompiler et Démarrer (3 min)

### 2.1 Recompiler

```powershell
mvn clean compile -DskipTests
```

✅ **Attendu :** `BUILD SUCCESS` (sans erreur JPA)

### 2.2 Démarrer l'Application

```powershell
mvn spring-boot:run
```

✅ **Attendu dans les logs :**
```
Started CreditPlatformApplication
Mapped "{[/api/archivage/archiver],methods=[POST]}"
Mapped "{[/api/archivage/nettoyer/{mois}],methods=[DELETE]}"
Mapped "{[/api/archivage/mois],methods=[GET]}"
Mapped "{[/api/imports/avec-detection],methods=[POST]}"
```

❌ **Si erreur JPA :**
- Vérifier que LogArchivage.java a bien la relation User commentée
- Redémarrer l'application

---

## 📋 ÉTAPE 3 : Test SQL Complet (5 min)

### 3.1 Créer Données Mois 7

```sql
-- Dans psql
\i scripts/test_donnees_mois7.sql
```

**Ce script crée :**
- 5 clients de test
- 5 crédits associés
- 5 dossiers avec statuts variés (CONTACTE, PROMESSE, INJOIGNABLE, EN_ATTENTE, EN_COURS)
- Tous marqués mois_import = '2024-07'

✅ **Vérification :**
```sql
SELECT COUNT(*) FROM dossiers WHERE mois_import = '2024-07';
-- Attendu: 5
```

### 3.2 Archiver le Mois 7

```sql
-- Exécuter l'archivage
SELECT * FROM archiver_mois('2024-07');
```

✅ **Vérifications :**
```sql
-- 1. Archives créées
SELECT COUNT(*) FROM dossiers_archive WHERE mois_archive = '2024-07';
-- Attendu: 5

-- 2. Table détection remplie
SELECT COUNT(*) FROM dossiers_mois_precedent WHERE mois_origine = '2024-07';
-- Attendu: 5

-- 3. Log créé
SELECT * FROM logs_archivage WHERE operation = 'ARCHIVAGE' AND mois_concerne = '2024-07';
-- Attendu: 1 ligne avec statut SUCCESS

-- 4. Dossiers toujours actifs (normal avant nettoyage)
SELECT COUNT(*) FROM dossiers WHERE mois_import = '2024-07';
-- Attendu: 5
```

### 3.3 Nettoyer le Mois 7

```sql
-- Exécuter le nettoyage
SELECT * FROM nettoyer_dossiers('2024-07');
```

✅ **Vérifications :**
```sql
-- 1. Dossiers actifs supprimés
SELECT COUNT(*) FROM dossiers WHERE mois_import = '2024-07';
-- Attendu: 0

-- 2. Archives intactes
SELECT COUNT(*) FROM dossiers_archive WHERE mois_archive = '2024-07';
-- Attendu: 5

-- 3. Détection intacte
SELECT COUNT(*) FROM dossiers_mois_precedent;
-- Attendu: 5
```

### 3.4 Importer Mois 8 avec Détection

```sql
\i scripts/test_import_mois8_detection.sql
```

**Ce script :**
- Crée 2 nouveaux clients
- Importe 5 dossiers (3 récurrents + 2 nouveaux)
- Applique automatiquement les statuts selon règles métier

✅ **Vérifications :**
```sql
-- 1. Total dossiers mois 8
SELECT COUNT(*) FROM dossiers WHERE mois_import = '2024-08';
-- Attendu: 5

-- 2. Récurrents détectés
SELECT COUNT(*) FROM dossiers WHERE mois_import = '2024-08' AND est_recurrent = true;
-- Attendu: 3

-- 3. Nouveaux dossiers
SELECT COUNT(*) FROM dossiers WHERE mois_import = '2024-08' AND est_recurrent = false;
-- Attendu: 2

-- 4. Voir les transitions
SELECT 
    cr.reference_contrat,
    CASE WHEN d.est_recurrent THEN '🔄 RÉCURRENT' ELSE '🆕 NOUVEAU' END as type,
    COALESCE(d.mois_precedent_statut, 'N/A') || ' → ' || d.statut_actuel::text as transition
FROM dossiers d
JOIN credits cr ON d.credit_id = cr.id
WHERE d.mois_import = '2024-08'
ORDER BY d.est_recurrent DESC, cr.reference_contrat;
```

✅ **Attendu :**
```
DOSS-TEST-001 | 🔄 RÉCURRENT | CONTACTE → RELANCE
DOSS-TEST-002 | 🔄 RÉCURRENT | PROMESSE → SUIVI_PROMESSE
DOSS-TEST-003 | 🔄 RÉCURRENT | INJOIGNABLE → INJOIGNABLE
DOSS-TEST-006 | 🆕 NOUVEAU   | N/A → EN_COURS
DOSS-TEST-007 | 🆕 NOUVEAU   | N/A → EN_COURS
```

---

## 📋 ÉTAPE 4 : Test des APIs (10 min)

### 4.1 Obtenir un Token Admin

```powershell
# PowerShell
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body '{"username":"admin","password":"admin"}'

$token = $response.token
Write-Host "Token: $token"
```

### 4.2 Test API : Lister les Mois Archivés

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/archivage/mois" `
    -Method GET `
    -Headers @{ "Authorization" = "Bearer $token" }
```

✅ **Attendu :** `["2024-07"]`

### 4.3 Test API : Statistiques Mois 7

```powershell
$stats = Invoke-RestMethod -Uri "http://localhost:8080/api/archivage/statistiques/2024-07" `
    -Method GET `
    -Headers @{ "Authorization" = "Bearer $token" }

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
  }
}
```

### 4.4 Test API : Consulter les Logs

```powershell
$logs = Invoke-RestMethod -Uri "http://localhost:8080/api/archivage/logs" `
    -Method GET `
    -Headers @{ "Authorization" = "Bearer $token" }

$logs | Format-Table operation, moisConcerne, nbDossiers, statut
```

✅ **Attendu :**
```
operation   moisConcerne  nbDossiers  statut
---------   ------------  ----------  ------
ARCHIVAGE   2024-07       5           SUCCESS
NETTOYAGE   2024-07       5           SUCCESS
IMPORT      2024-08       5           SUCCESS
```

### 4.5 Test API : Consulter Dossiers Archivés

```powershell
$archives = Invoke-RestMethod -Uri "http://localhost:8080/api/archivage/dossiers/2024-07" `
    -Method GET `
    -Headers @{ "Authorization" = "Bearer $token" }

Write-Host "Nombre d'archives: $($archives.Count)"
$archives | Select-Object numeroDossier, nomClient, statutFinal | Format-Table
```

✅ **Attendu :** 5 dossiers avec leurs informations

---

## 📋 ÉTAPE 5 : Test Interface Web (5 min)

### 5.1 Accéder à la Page

```
URL : http://localhost:8080/admin/gestion-mensuelle
```

### 5.2 Vérifications Interface

✅ **Section "Mois Actuel" doit afficher :**
- Bouton "Archiver et Clôturer {mois actuel}"
- Bouton "Nettoyer le Mois {mois actuel}"
- Bouton "Importer le Mois Suivant"

✅ **Section "Historique" doit afficher :**
- Mois 2024-07 (si test SQL exécuté)
- Boutons "Voir détails" et "Télécharger CSV"

✅ **Section "Logs" doit afficher :**
- 3 lignes (ARCHIVAGE, NETTOYAGE, IMPORT)
- Toutes avec statut SUCCESS

### 5.3 Test Bouton "Voir Détails"

1. Cliquer sur "Voir détails" pour le mois 2024-07
2. ✅ Popup doit afficher :
   - Total : 5 dossiers
   - Répartition par statut
   - Total dû mensuel

---

## 📋 ÉTAPE 6 : Test Cycle Complet via Interface (10 min)

### 6.1 Préparer un Nouveau Mois

```sql
-- Créer quelques dossiers pour le mois actuel
-- Adapter le mois selon la date actuelle
INSERT INTO dossiers (credit_id, statut_actuel, date_creation, mois_import, est_recurrent)
SELECT 
    cr.id,
    'EN_COURS'::statut_dossier,
    CURRENT_DATE,
    TO_CHAR(CURRENT_DATE, 'YYYY-MM'),
    false
FROM credits cr
WHERE cr.reference_contrat LIKE 'DOSS-TEST-%'
LIMIT 3;
```

### 6.2 Test Archivage via Interface

1. Rafraîchir la page `/admin/gestion-mensuelle`
2. Cliquer "Archiver et Clôturer {mois actuel}"
3. Confirmer dans la popup
4. ✅ Message de succès doit apparaître
5. ✅ Le mois doit apparaître dans "Historique"

### 6.3 Test Nettoyage via Interface

1. Cliquer "Nettoyer le Mois {mois}"
2. Confirmer dans la popup
3. ✅ Message de succès

### 6.4 Vérification SQL

```sql
-- Vérifier que le nettoyage a fonctionné
SELECT COUNT(*) FROM dossiers WHERE mois_import = '{votre_mois}';
-- Attendu: 0

-- Vérifier l'archive
SELECT COUNT(*) FROM dossiers_archive WHERE mois_archive = '{votre_mois}';
-- Attendu: 3 (ou le nombre de dossiers créés)
```

---

## 📋 ÉTAPE 7 : Test Cas d'Erreur (5 min)

### Test 1 : Archiver un Mois Déjà Archivé

```powershell
# Essayer d'archiver 2024-07 à nouveau
Invoke-RestMethod -Uri "http://localhost:8080/api/archivage/archiver" `
    -Method POST `
    -Headers @{ 
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    } `
    -Body '{"mois":"2024-07","exporterCsv":true}'
```

✅ **Attendu :**
```json
{
  "success": false,
  "message": "Le mois 2024-07 est déjà archivé"
}
```

### Test 2 : Nettoyer un Mois Non Archivé

```powershell
# Essayer de nettoyer un mois qui n'existe pas
Invoke-RestMethod -Uri "http://localhost:8080/api/archivage/nettoyer/2024-12" `
    -Method DELETE `
    -Headers @{ "Authorization" = "Bearer $token" }
```

✅ **Attendu :** Erreur 400 avec message d'erreur

### Test 3 : Statistiques Mois Inexistant

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/archivage/statistiques/2024-12" `
    -Method GET `
    -Headers @{ "Authorization" = "Bearer $token" }
```

✅ **Attendu :** 404 Not Found

---

## 📊 RÉSUMÉ DES TESTS

### ✅ Checklist Complète

#### Setup
- [ ] Tables SQL créées (3 tables)
- [ ] Colonnes ajoutées à dossiers
- [ ] Compilation Maven SUCCESS
- [ ] Application démarre sans erreur
- [ ] Endpoints mappés dans les logs

#### Tests SQL
- [ ] 5 dossiers créés mois 7
- [ ] Archivage mois 7 réussi
- [ ] 5 archives créées
- [ ] 5 entrées détection créées
- [ ] Nettoyage mois 7 réussi
- [ ] 5 dossiers importés mois 8
- [ ] 3 récurrents détectés
- [ ] Transitions correctes

#### Tests API
- [ ] GET /mois fonctionne
- [ ] GET /statistiques/{mois} fonctionne
- [ ] GET /dossiers/{mois} fonctionne
- [ ] GET /logs fonctionne
- [ ] POST /archiver fonctionne
- [ ] DELETE /nettoyer/{mois} fonctionne

#### Tests Interface
- [ ] Page /gestion-mensuelle accessible
- [ ] Section Mois Actuel OK
- [ ] Section Historique OK
- [ ] Section Logs OK
- [ ] Bouton Archiver fonctionne
- [ ] Bouton Nettoyer fonctionne
- [ ] Statistiques s'affichent

#### Tests Erreur
- [ ] Erreur archivage doublon
- [ ] Erreur nettoyage sans archivage
- [ ] Erreur mois inexistant

---

## 🎯 VALIDATION FINALE

### Requête de Validation Globale

```sql
-- Vue d'ensemble du système
SELECT 
    'Archives mois 7' as description,
    COUNT(*)::text as valeur
FROM dossiers_archive WHERE mois_archive = '2024-07'
UNION ALL
SELECT 'Dossiers actifs mois 8', COUNT(*)::text
FROM dossiers WHERE mois_import = '2024-08'
UNION ALL
SELECT 'Récurrents mois 8', COUNT(*)::text
FROM dossiers WHERE mois_import = '2024-08' AND est_recurrent = true
UNION ALL
SELECT 'Nouveaux mois 8', COUNT(*)::text
FROM dossiers WHERE mois_import = '2024-08' AND est_recurrent = false
UNION ALL
SELECT 'Logs SUCCESS', COUNT(*)::text
FROM logs_archivage WHERE statut = 'SUCCESS'
UNION ALL
SELECT 'Total mois archivés', COUNT(DISTINCT mois_archive)::text
FROM dossiers_archive;
```

✅ **Résultat Attendu :**
```
Archives mois 7      | 5
Dossiers actifs mois 8 | 5
Récurrents mois 8    | 3
Nouveaux mois 8      | 2
Logs SUCCESS         | 3+
Total mois archivés  | 1+
```

---

## 🚀 ÉTAPES SUIVANTES

### En Développement
1. ✅ Tous les tests passent
2. ✅ Documentation lue
3. ✅ Système compris

### En Production
1. Backup complet de la BDD
2. Exécuter `create_archivage_system.sql`
3. Vérifier les tables
4. Tester avec données réelles (petit volume)
5. Former l'équipe admin
6. Déployer

---

## 📚 DOCUMENTATION

- **TEST_2MIN.md** - Test ultra-rapide
- **SYSTEME_ARCHIVAGE_README.md** - Documentation complète
- **QUOI_FAIRE_MAINTENANT.md** - Actions immédiates

---

## ✨ RÉSULTAT

Si tous les tests passent :
- ✅ Système 100% fonctionnel
- ✅ Détection automatique OK
- ✅ APIs opérationnelles
- ✅ Interface prête
- ✅ Prêt pour la production

**Bon test ! 🎉**
