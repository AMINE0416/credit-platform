# 🚀 GUIDE DE DÉMARRAGE - SYSTÈME D'ARCHIVAGE MENSUEL

## 📋 OBJECTIF
Ce guide vous explique comment démarrer et tester le système d'archivage mensuel avec détection automatique des dossiers récurrents.

---

## ⚙️ INSTALLATION

### 1. Créer les tables et fonctions SQL
```bash
# Exécuter le script de création
psql -U postgres -d credit_platform -f scripts/create_archivage_system.sql
```

**✅ Vérification :**
```sql
-- Vérifier les tables
\dt *archive*
\dt *mois_precedent*
\dt *logs_archivage*

-- Vérifier les fonctions
\df archiver_mois
\df nettoyer_dossiers
\df calculer_statut_recurrent
```

### 2. Redémarrer l'application Spring Boot
```bash
# Les entités JPA vont se synchroniser automatiquement
mvn spring-boot:run
```

---

## 🧪 TEST COMPLET - CYCLE 3 MOIS

### Étape 1 : Créer des données pour Mois 7 (Juillet)

```bash
psql -U postgres -d credit_platform -f scripts/test_donnees_mois7.sql
```

**Résultat attendu :** 5 dossiers créés pour le mois 2024-07

### Étape 2 : Archiver le Mois 7

**Option A : Via API (Recommandé)**
```bash
# Obtenir un token admin
POST http://localhost:8080/api/auth/login
Body: {"username": "admin", "password": "votre_password"}

# Archiver
POST http://localhost:8080/api/archivage/archiver
Headers: Authorization: Bearer <token>
Body: {
  "mois": "2024-07",
  "exporterCsv": true
}
```

**Option B : Via SQL**
```bash
psql -U postgres -d credit_platform -f scripts/test_archivage_mois7.sql
```

**✅ Vérification :**
- 5 dossiers dans `dossiers_archive`
- 5 entrées dans `dossiers_mois_precedent`
- 1 log dans `logs_archivage`
- 1 export CSV dans `historique_exports`

### Étape 3 : Nettoyer le Mois 7

**Option A : Via API**
```bash
DELETE http://localhost:8080/api/archivage/nettoyer/2024-07
Headers: Authorization: Bearer <token>
```

**Option B : Via SQL**
```bash
psql -U postgres -d credit_platform -f scripts/test_nettoyage_mois7.sql
```

**✅ Vérification :**
- 0 dossiers dans `dossiers` pour mois 2024-07
- 5 dossiers toujours dans `dossiers_archive`

### Étape 4 : Importer Mois 8 avec Détection

```bash
psql -U postgres -d credit_platform -f scripts/test_import_mois8_detection.sql
```

**✅ Résultat attendu :**
```
Total dossiers : 5
├─ 🔄 Récurrents : 3
│  ├─ DOSS-TEST-001: CONTACTE → RELANCE
│  ├─ DOSS-TEST-002: PROMESSE → SUIVI_PROMESSE
│  └─ DOSS-TEST-003: INJOIGNABLE → INJOIGNABLE
│
└─ 🆕 Nouveaux : 2
   ├─ DOSS-TEST-006: N/A → EN_COURS
   └─ DOSS-TEST-007: N/A → EN_COURS
```

### Étape 5 : Vérifier les Statistiques

**Via API :**
```bash
# Lister les mois archivés
GET http://localhost:8080/api/archivage/mois

# Statistiques d'un mois
GET http://localhost:8080/api/archivage/statistiques/2024-07

# Consulter les dossiers archivés
GET http://localhost:8080/api/archivage/dossiers/2024-07

# Voir les logs
GET http://localhost:8080/api/archivage/logs
```

**Via SQL :**
```sql
-- Vue statistiques
SELECT * FROM v_stats_archivage;

-- Logs
SELECT * FROM logs_archivage ORDER BY date_operation DESC;

-- Dossiers archivés
SELECT * FROM dossiers_archive WHERE mois_archive = '2024-07';
```

---

## 🎯 WORKFLOW EN PRODUCTION

### Fin du Mois (ex: 31 Juillet)

#### 1. Exporter et Archiver
```javascript
// Frontend - Page Admin
const archiverMois = async () => {
  const response = await fetch('/api/archivage/archiver', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      mois: '2024-07',
      exporterCsv: true
    })
  });
  
  const result = await response.json();
  
  if (result.success) {
    alert(`✅ ${result.nbDossiersArchives} dossiers archivés`);
    // Télécharger le CSV si besoin
    if (result.exportId) {
      window.location.href = `/api/historique/telecharger/${result.exportId}`;
    }
  }
};
```

#### 2. Nettoyer les Dossiers du Mois
```javascript
const nettoyerMois = async () => {
  const confirmation = confirm(
    '⚠️ Vous allez supprimer tous les dossiers du mois 2024-07.\n' +
    'Assurez-vous que l\'archivage a été fait.\n' +
    'Continuer ?'
  );
  
  if (!confirmation) return;
  
  const response = await fetch('/api/archivage/nettoyer/2024-07', {
    method: 'DELETE',
    headers: { 'Authorization': `Bearer ${token}` }
  });
  
  const result = await response.json();
  
  if (result.success) {
    alert(`✅ ${result.nbSupprimes} dossiers supprimés`);
  }
};
```

### Début du Mois (ex: 1er Août)

#### 3. Importer le Nouveau Mois avec Détection
```javascript
const importerNouveauMois = async (file) => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('moisImport', '2024-08');
  
  const response = await fetch('/api/imports/avec-detection', {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${token}` },
    body: formData
  });
  
  const result = await response.json();
  
  if (result.success) {
    console.log(`
      ✅ Import réussi !
      📊 Total: ${result.totalDossiers}
      🔄 Récurrents: ${result.dossiersRecurrents}
      🆕 Nouveaux: ${result.nouveauxDossiers}
    `);
    
    // Afficher la répartition
    console.table(result.repartitionStatuts);
    console.table(result.repartitionRecurrents);
  }
};
```

---

## 📊 REQUÊTES UTILES

### Voir l'historique complet
```sql
SELECT 
    mois_archive,
    COUNT(*) as total,
    SUM(total_du) as total_du,
    COUNT(CASE WHEN statut_final = 'CONTACTE' THEN 1 END) as contactes,
    COUNT(CASE WHEN statut_final = 'PROMESSE' THEN 1 END) as promesses
FROM dossiers_archive
GROUP BY mois_archive
ORDER BY mois_archive DESC;
```

### Tracer les dossiers récurrents
```sql
-- Voir l'historique d'un dossier spécifique
SELECT 
    mois_archive,
    numero_dossier,
    nom_client,
    statut_final,
    total_du,
    date_archivage
FROM dossiers_archive
WHERE numero_dossier = 'DOSS-TEST-001'
ORDER BY mois_archive;
```

### Analyser les transitions
```sql
-- Voir les dossiers qui reviennent chaque mois
WITH dossiers_frequents AS (
    SELECT 
        numero_dossier,
        COUNT(DISTINCT mois_archive) as nb_mois
    FROM dossiers_archive
    GROUP BY numero_dossier
    HAVING COUNT(DISTINCT mois_archive) > 2
)
SELECT 
    df.numero_dossier,
    df.nb_mois as mois_consecutifs,
    array_agg(da.statut_final ORDER BY da.mois_archive) as evolution_statuts,
    array_agg(da.mois_archive ORDER BY da.mois_archive) as mois
FROM dossiers_frequents df
JOIN dossiers_archive da ON da.numero_dossier = df.numero_dossier
GROUP BY df.numero_dossier, df.nb_mois
ORDER BY df.nb_mois DESC;
```

---

## 🚨 TROUBLESHOOTING

### Problème : "Le mois est déjà archivé"
**Solution :** Vérifier dans `dossiers_archive` si le mois existe déjà
```sql
SELECT * FROM dossiers_archive WHERE mois_archive = '2024-07';
-- Si besoin de ré-archiver, supprimer d'abord
DELETE FROM dossiers_archive WHERE mois_archive = '2024-07';
```

### Problème : "Impossible de nettoyer: le mois n'a pas été archivé"
**Solution :** Toujours archiver avant de nettoyer
```sql
-- Vérifier l'archivage
SELECT COUNT(*) FROM dossiers_archive WHERE mois_archive = '2024-07';
```

### Problème : La détection ne fonctionne pas
**Solution :** Vérifier que `dossiers_mois_precedent` est rempli
```sql
SELECT * FROM dossiers_mois_precedent;
-- Si vide, ré-exécuter l'archivage
SELECT * FROM archiver_mois('2024-07');
```

### Problème : Les statuts auto sont incorrects
**Solution :** Vérifier la logique dans la fonction
```sql
SELECT calculer_statut_recurrent('CONTACTE'); -- Devrait retourner 'RELANCE'
SELECT calculer_statut_recurrent('PROMESSE'); -- Devrait retourner 'SUIVI_PROMESSE'
```

---

## 📖 DOCUMENTATION COMPLÈTE

- **Plan de test complet :** `PLAN_TEST_ARCHIVAGE_MENSUEL.md`
- **Scripts SQL :**
  - `create_archivage_system.sql` : Création tables/fonctions
  - `test_donnees_mois7.sql` : Données de test
  - `test_archivage_mois7.sql` : Test archivage
  - `test_nettoyage_mois7.sql` : Test nettoyage
  - `test_import_mois8_detection.sql` : Test détection

---

## ✅ CHECKLIST DE DÉMARRAGE

- [ ] Tables créées (dossiers_archive, dossiers_mois_precedent, logs_archivage)
- [ ] Fonctions SQL créées (archiver_mois, nettoyer_dossiers, calculer_statut_recurrent)
- [ ] Colonnes ajoutées à table dossiers (mois_import, est_recurrent, mois_precedent_statut)
- [ ] Application Spring Boot redémarrée
- [ ] Test complet du cycle 3 mois réussi
- [ ] APIs testées (archiver, nettoyer, statistiques, logs)
- [ ] Détection automatique validée
- [ ] Documentation lue et comprise

**Une fois tous les points cochés, le système est prêt pour la production ! 🎉**
