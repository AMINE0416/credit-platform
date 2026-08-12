# 📦 SYSTÈME D'ARCHIVAGE MENSUEL - DOCUMENTATION COMPLÈTE

## 🎯 OBJECTIF

Ce système permet de gérer le cycle de vie mensuel des dossiers de crédit avec :
- **Archivage automatique** des dossiers à la fin de chaque mois
- **Export CSV** pour historique et reporting
- **Détection automatique** des dossiers récurrents lors de l'import
- **Application automatique** des statuts selon l'historique

---

## 📋 FICHIERS CRÉÉS

### Backend (Java/Spring Boot)

#### Modèles (Entities)
- `DossierArchive.java` - Archive complète des dossiers
- `DossierMoisPrecedent.java` - Table temporaire pour détection
- `LogArchivage.java` - Journal des opérations

#### DTOs
- `ArchivageMoisRequest.java` - Requête d'archivage
- `ArchivageMoisResponse.java` - Réponse d'archivage
- `ImportAvecDetectionResponse.java` - Réponse import avec détection
- `StatistiquesArchiveDTO.java` - Statistiques d'un mois archivé

#### Repositories
- `DossierArchiveRepository.java`
- `DossierMoisPrecedentRepository.java`
- `LogArchivageRepository.java`

#### Services
- `ArchivageMensuelService.java` - Logique d'archivage
- `ImportAvecDetectionService.java` - Import avec détection récurrents
- `ExcelImportService.java` - Lecture fichiers Excel

#### Controllers
- `ArchivageMensuelController.java` - Endpoints API archivage
- `ImportController.java` - Ajout endpoint import avec détection

### Frontend (React/TypeScript)

#### Pages
- `AdminGestionMensuelle.tsx` - Interface gestion mensuelle
- `AdminGestionMensuelle.css` - Styles de la page

#### Mises à jour
- `App.tsx` - Ajout route gestion mensuelle
- `AdminLayout.tsx` - Ajout lien menu

### Base de données (SQL)

#### Scripts de création
- `create_archivage_system.sql` - Tables, fonctions, vues

#### Scripts de test
- `test_donnees_mois7.sql` - Données de test mois 7
- `test_archivage_mois7.sql` - Test archivage
- `test_nettoyage_mois7.sql` - Test nettoyage
- `test_import_mois8_detection.sql` - Test import avec détection
- `test_complet_archivage.sql` - Test complet automatisé

### Documentation
- `PLAN_TEST_ARCHIVAGE_MENSUEL.md` - Plan de test complet
- `GUIDE_DEMARRAGE_ARCHIVAGE.md` - Guide de démarrage
- `SYSTEME_ARCHIVAGE_README.md` - Ce fichier

---

## 🏗️ ARCHITECTURE

### Tables créées

```sql
dossiers_archive           -- Archive complète des dossiers
dossiers_mois_precedent    -- Détection récurrents
logs_archivage             -- Journal des opérations
```

### Colonnes ajoutées à `dossiers`

```sql
mois_import              VARCHAR(7)    -- Format: "2024-07"
est_recurrent            BOOLEAN       -- Dossier récurrent ?
mois_precedent_statut    VARCHAR(50)   -- Statut du mois précédent
```

### Fonctions SQL

```sql
archiver_mois(mois)              -- Archiver un mois
nettoyer_dossiers(mois)          -- Nettoyer après archivage
calculer_statut_recurrent(...)   -- Logique statut auto
```

---

## 🔄 WORKFLOW MENSUEL

### 1. Fin du Mois (ex: 31 Juillet)

**Action Admin : Archiver**
```
POST /api/archivage/archiver
Body: { "mois": "2024-07", "exporterCsv": true }
```

**Résultat :**
- ✅ Export CSV créé
- ✅ Dossiers copiés dans `dossiers_archive`
- ✅ Identifiants sauvegardés dans `dossiers_mois_precedent`
- ✅ Log créé dans `logs_archivage`
- ⚠️ Dossiers toujours présents dans table `dossiers`

### 2. Nettoyage (après archivage)

**Action Admin : Nettoyer**
```
DELETE /api/archivage/nettoyer/2024-07
```

**Résultat :**
- ✅ Dossiers supprimés de table `dossiers`
- ✅ Archive intacte dans `dossiers_archive`
- ✅ Détection intacte dans `dossiers_mois_precedent`
- ✅ Table `dossiers` vide et prête pour le mois suivant

### 3. Début du Mois Suivant (ex: 1er Août)

**Action Admin : Importer avec Détection**
```
POST /api/imports/avec-detection
Form-Data: 
  - file: fichier Excel
  - moisImport: "2024-08"
```

**Processus automatique :**
```
Pour chaque ligne Excel :
  1. Vérifier si numero_dossier existe dans dossiers_mois_precedent
  
  2. Si OUI → DOSSIER RÉCURRENT
     - Récupérer ancien statut
     - Appliquer logique transition :
       • CONTACTE → RELANCE
       • PROMESSE → SUIVI_PROMESSE
       • INJOIGNABLE → INJOIGNABLE
       • EN_ATTENTE → EN_ATTENTE
     - Marquer est_recurrent = true
     
  3. Si NON → NOUVEAU DOSSIER
     - Statut = EN_COURS
     - Marquer est_recurrent = false
  
  4. Créer le dossier avec le statut calculé
```

**Résultat :**
- ✅ Import réussi
- ✅ Récurrents détectés automatiquement
- ✅ Statuts appliqués selon l'historique
- ✅ Log d'import avec statistiques

### 4. Export du Mois Actuel

**Action Admin : Exporter**
```
GET /api/historique/exporter
```

**Résultat :**
- ✅ Export contient UNIQUEMENT les dossiers du mois actuel
- ✅ Les archives ne sont pas incluses

---

## 🎨 INTERFACE UTILISATEUR

### Page "Gestion Mensuelle"

**URL:** `/admin/gestion-mensuelle`

**Sections :**

1. **Mois Actuel**
   - Bouton : "Archiver et Clôturer"
   - Bouton : "Nettoyer le Mois"
   - Bouton : "Importer le Mois Suivant"

2. **Historique des Mois Archivés**
   - Liste des mois archivés
   - Boutons : "Voir détails" et "Télécharger CSV"
   - Affichage statistiques (total, répartition statuts, total dû)

3. **Journal des Opérations**
   - Table avec toutes les opérations
   - Colonnes : Date, Opération, Mois, Nb dossiers, Détails, Statut

---

## 📊 APIs DISPONIBLES

### Archivage

```http
POST /api/archivage/archiver
Body: { "mois": "2024-07", "exporterCsv": true }
Response: {
  "success": true,
  "nbDossiersArchives": 450,
  "nbDossiersSauvegardes": 450,
  "exportId": 123,
  "exportFileName": "export_archive_2024-07.csv"
}
```

### Nettoyage

```http
DELETE /api/archivage/nettoyer/{mois}
Response: {
  "success": true,
  "nbSupprimes": 450
}
```

### Liste mois archivés

```http
GET /api/archivage/mois
Response: ["2024-08", "2024-07", "2024-06"]
```

### Statistiques mois archivé

```http
GET /api/archivage/statistiques/{mois}
Response: {
  "moisArchive": "2024-07",
  "totalDossiers": 450,
  "repartitionStatuts": {
    "CONTACTE": 120,
    "PROMESSE": 80,
    "INJOIGNABLE": 50,
    "EN_ATTENTE": 200
  },
  "totalDuMensuel": 2500000.00,
  "dateArchivage": "2024-07-31T23:59:59"
}
```

### Import avec détection

```http
POST /api/imports/avec-detection
Form-Data:
  - file: fichier Excel
  - moisImport: "2024-08"
Response: {
  "success": true,
  "totalDossiers": 500,
  "nouveauxDossiers": 350,
  "dossiersRecurrents": 150,
  "repartitionStatuts": { ... },
  "repartitionRecurrents": { ... }
}
```

### Logs

```http
GET /api/archivage/logs
Response: [
  {
    "id": 1,
    "operation": "ARCHIVAGE",
    "moisConcerne": "2024-07",
    "nbDossiers": 450,
    "statut": "SUCCESS",
    "dateOperation": "2024-07-31T23:59:59"
  }
]
```

---

## 🧪 TESTS

### Test Rapide (5 minutes)

```bash
# 1. Créer les tables
psql -U postgres -d credit_platform -f scripts/create_archivage_system.sql

# 2. Test complet automatisé
psql -U postgres -d credit_platform -f scripts/test_complet_archivage.sql
```

### Test Complet (15 minutes)

Suivre le guide : `PLAN_TEST_ARCHIVAGE_MENSUEL.md`

### Test en Production

1. **Avant le 1er import :**
   - Exécuter `create_archivage_system.sql`
   - Redémarrer l'application Spring Boot
   - Vérifier les tables créées

2. **Premier mois (ex: Août) :**
   - Importer normalement (tous seront "nouveaux")
   - Travailler le mois normalement

3. **Fin du premier mois :**
   - Archiver via l'interface
   - Nettoyer via l'interface
   - Vérifier l'export CSV

4. **Deuxième mois (ex: Septembre) :**
   - Importer le nouveau fichier
   - Vérifier la détection automatique
   - Vérifier les statuts appliqués

---

## 🔍 REQUÊTES SQL UTILES

### Voir les dossiers récurrents

```sql
SELECT 
    cr.reference_contrat,
    cl.nom || ' ' || cl.prenom as client,
    d.statut_actuel,
    d.est_recurrent,
    d.mois_precedent_statut
FROM dossiers d
JOIN credits cr ON d.credit_id = cr.id
JOIN clients cl ON cr.client_id = cl.id
WHERE d.est_recurrent = true
ORDER BY cr.reference_contrat;
```

### Historique complet d'un dossier

```sql
SELECT 
    mois_archive,
    numero_dossier,
    nom_client,
    statut_final,
    total_du
FROM dossiers_archive
WHERE numero_dossier = 'DOSS-123456'
ORDER BY mois_archive;
```

### Statistiques mensuelles

```sql
SELECT * FROM v_stats_archivage 
ORDER BY mois_archive DESC;
```

### Dossiers super-récurrents (3+ mois)

```sql
SELECT 
    numero_dossier,
    COUNT(*) as nb_mois,
    array_agg(mois_archive ORDER BY mois_archive) as mois,
    array_agg(statut_final ORDER BY mois_archive) as statuts
FROM dossiers_archive
GROUP BY numero_dossier
HAVING COUNT(*) >= 3
ORDER BY nb_mois DESC;
```

---

## ⚙️ CONFIGURATION

### Variables d'environnement

Aucune nouvelle variable nécessaire. Le système utilise la même base de données.

### Properties Spring Boot

Aucune nouvelle property nécessaire. Tout est géré par JPA.

---

## 🚨 TROUBLESHOOTING

### Problème : Import ne détecte pas les récurrents

**Cause :** Table `dossiers_mois_precedent` vide

**Solution :**
```sql
-- Vérifier
SELECT COUNT(*) FROM dossiers_mois_precedent;

-- Re-exécuter l'archivage si besoin
SELECT * FROM archiver_mois('2024-07');
```

### Problème : "Le mois est déjà archivé"

**Solution :**
```sql
-- Si besoin de ré-archiver, supprimer d'abord
DELETE FROM dossiers_archive WHERE mois_archive = '2024-07';
DELETE FROM dossiers_mois_precedent WHERE mois_origine = '2024-07';
```

### Problème : Statuts automatiques incorrects

**Solution :**
```sql
-- Vérifier la fonction
SELECT calculer_statut_recurrent('CONTACTE'); -- Devrait retourner 'RELANCE'
SELECT calculer_statut_recurrent('PROMESSE'); -- Devrait retourner 'SUIVI_PROMESSE'
```

### Problème : Export CSV ne contient pas les bonnes données

**Solution :**
```sql
-- Vérifier le mois_import
SELECT mois_import, COUNT(*) 
FROM dossiers 
GROUP BY mois_import;
```

---

## 📚 DOCUMENTATION COMPLÉMENTAIRE

- **PLAN_TEST_ARCHIVAGE_MENSUEL.md** : Plan de test détaillé avec checklist
- **GUIDE_DEMARRAGE_ARCHIVAGE.md** : Guide de démarrage rapide
- **Scripts SQL** : Tous les scripts dans `scripts/`

---

## ✅ CHECKLIST DE DÉPLOIEMENT

- [ ] Backup de la base de données
- [ ] Exécuter `create_archivage_system.sql`
- [ ] Vérifier les tables créées
- [ ] Redémarrer l'application Spring Boot
- [ ] Vérifier les logs de démarrage (pas d'erreur JPA)
- [ ] Tester l'interface "/admin/gestion-mensuelle"
- [ ] Exécuter le test complet SQL
- [ ] Valider avec un import de test
- [ ] Former les utilisateurs admin
- [ ] Documenter le processus mensuel interne

---

## 🎉 RÉSUMÉ

Le système est maintenant **COMPLET** et **PRÊT À L'EMPLOI** avec :

✅ **Backend** : Toutes les entités, services, repositories, controllers
✅ **Frontend** : Interface complète de gestion mensuelle
✅ **Base de données** : Tables, fonctions, vues
✅ **Tests** : Scripts de test automatisés
✅ **Documentation** : Guides complets et détaillés

Le flux mensuel est automatisé et sécurisé :
1. **Archiver** → Export CSV + Sauvegarde
2. **Nettoyer** → Suppression sécurisée
3. **Importer** → Détection automatique récurrents
4. **Travailler** → Statuts pré-assignés selon historique

**Prochaine étape** : Tester le système complet ! 🚀
