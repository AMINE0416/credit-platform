# 🧪 PLAN DE TEST - SYSTÈME D'ARCHIVAGE MENSUEL

## 📋 OBJECTIF
Tester complètement le système d'archivage mensuel avec détection automatique des dossiers récurrents.

---

## 🔧 PRÉPARATION DE L'ENVIRONNEMENT

### Étape 0 : Créer les tables
```bash
# Exécuter le script SQL de création
psql -U votre_user -d credit_platform -f scripts/create_archivage_system.sql
```

**✅ Vérification :**
```sql
-- Vérifier que les tables sont créées
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN ('dossiers_archive', 'dossiers_mois_precedent', 'logs_archivage');

-- Vérifier les colonnes ajoutées
SELECT column_name FROM information_schema.columns 
WHERE table_name = 'dossiers' 
AND column_name IN ('mois_import', 'est_recurrent', 'mois_precedent_statut');
```

---

## 📅 SCÉNARIO DE TEST : CYCLE COMPLET 3 MOIS

### Test 1 : Import Mois 7 (Juillet 2024)

#### 1.1 Créer des données de test pour Juillet
```sql
-- scripts/test_donnees_mois7.sql
-- Insérer des clients de test
INSERT INTO clients (cin, nom, prenom, telephone, email) VALUES
('AB123456', 'BENALI', 'Mohammed', '0612345678', 'benali@test.com'),
('CD789012', 'ALAMI', 'Fatima', '0623456789', 'alami@test.com'),
('EF345678', 'IDRISSI', 'Youssef', '0634567890', 'idrissi@test.com'),
('GH901234', 'BOUAZZA', 'Samira', '0645678901', 'bouazza@test.com'),
('IJ567890', 'ZOUAOUI', 'Karim', '0656789012', 'zouaoui@test.com');

-- Insérer des crédits de test
INSERT INTO credits (client_id, reference_contrat, type_credit, segment, nbr_impaye, mensualite, encours, impaye_montant, score) 
SELECT 
    c.id,
    'DOSS-' || LPAD(c.id::text, 6, '0'),
    'AUTO_CLASSIQUE',
    'AUTO PP',
    2,
    1500.00,
    45000.00,
    3000.00,
    75.0
FROM clients c
WHERE c.cin IN ('AB123456', 'CD789012', 'EF345678', 'GH901234', 'IJ567890');

-- Créer les dossiers avec mois_import = "2024-07"
INSERT INTO dossiers (credit_id, statut_actuel, date_creation, mois_import, est_recurrent)
SELECT 
    cr.id,
    CASE 
        WHEN cr.reference_contrat = 'DOSS-000001' THEN 'CONTACTE'
        WHEN cr.reference_contrat = 'DOSS-000002' THEN 'PROMESSE'
        WHEN cr.reference_contrat = 'DOSS-000003' THEN 'INJOIGNABLE'
        ELSE 'EN_ATTENTE'
    END::statut_dossier,
    CURRENT_DATE,
    '2024-07',
    false
FROM credits cr
JOIN clients cl ON cr.client_id = cl.id
WHERE cl.cin IN ('AB123456', 'CD789012', 'EF345678', 'GH901234', 'IJ567890');
```

#### 1.2 Vérifier les données Mois 7
```sql
-- Compter les dossiers du mois 7
SELECT mois_import, COUNT(*) as total
FROM dossiers
WHERE mois_import = '2024-07'
GROUP BY mois_import;

-- Voir la répartition par statut
SELECT d.statut_actuel, COUNT(*) as nombre
FROM dossiers d
WHERE d.mois_import = '2024-07'
GROUP BY d.statut_actuel;

-- Afficher les dossiers
SELECT 
    cr.reference_contrat as numero,
    cl.nom || ' ' || cl.prenom as client,
    d.statut_actuel,
    d.mois_import,
    d.est_recurrent
FROM dossiers d
JOIN credits cr ON d.credit_id = cr.id
JOIN clients cl ON cr.client_id = cl.id
WHERE d.mois_import = '2024-07'
ORDER BY cr.reference_contrat;
```

**✅ Résultat attendu :** 5 dossiers du mois 2024-07

---

### Test 2 : Archivage du Mois 7

#### 2.1 Exporter et Archiver via API
```bash
# Requête POST via curl ou Postman
POST http://localhost:8080/api/archivage/archiver
Headers: Authorization: Bearer <votre_token_admin>
Body:
{
  "mois": "2024-07",
  "exporterCsv": true
}
```

#### 2.2 Vérifier l'archivage
```sql
-- Vérifier dossiers_archive
SELECT mois_archive, COUNT(*) as total
FROM dossiers_archive
WHERE mois_archive = '2024-07'
GROUP BY mois_archive;

-- Voir le détail
SELECT 
    numero_dossier,
    nom_client,
    statut_final,
    mois_archive,
    date_archivage
FROM dossiers_archive
WHERE mois_archive = '2024-07'
ORDER BY numero_dossier;

-- Vérifier dossiers_mois_precedent
SELECT numero_dossier, statut_final, mois_origine
FROM dossiers_mois_precedent
ORDER BY numero_dossier;

-- Vérifier les logs
SELECT operation, mois_concerne, nb_dossiers, statut, date_operation
FROM logs_archivage
WHERE mois_concerne = '2024-07'
ORDER BY date_operation DESC;

-- Vérifier l'export CSV dans historique_exports
SELECT nom_fichier, date_export, mois_periode, nombre_dossiers
FROM historique_exports
WHERE mois_periode = '2024-07';
```

**✅ Résultat attendu :**
- 5 dossiers dans dossiers_archive
- 5 entrées dans dossiers_mois_precedent
- 1 log d'archivage SUCCESS
- 1 export CSV dans historique_exports
- Les dossiers sont toujours dans la table `dossiers`

---

### Test 3 : Nettoyage du Mois 7

#### 3.1 Nettoyer via API
```bash
DELETE http://localhost:8080/api/archivage/nettoyer/2024-07
Headers: Authorization: Bearer <votre_token_admin>
```

#### 3.2 Vérifier le nettoyage
```sql
-- Vérifier que les dossiers sont supprimés
SELECT COUNT(*) as nb_dossiers_restants
FROM dossiers
WHERE mois_import = '2024-07';

-- Vérifier que l'archive est intacte
SELECT COUNT(*) as nb_archives
FROM dossiers_archive
WHERE mois_archive = '2024-07';

-- Vérifier le log de nettoyage
SELECT operation, mois_concerne, nb_dossiers, statut
FROM logs_archivage
WHERE mois_concerne = '2024-07' AND operation = 'NETTOYAGE';
```

**✅ Résultat attendu :**
- 0 dossiers dans table `dossiers` pour mois 2024-07
- 5 dossiers toujours dans `dossiers_archive`
- 1 log NETTOYAGE SUCCESS

---

### Test 4 : Import Mois 8 avec Détection Automatique

#### 4.1 Créer un fichier Excel pour Août
Le fichier doit contenir :
- **3 dossiers récurrents** (même numéro que Mois 7)
- **2 nouveaux dossiers**

Données pour Août :
```
| num_cin1  | nom      | prenom   | cli_tel      | ANC_REFDO    | CSP_PDT        | Segment | nbr_imp | Mensualite | encours  | Mnt_Imp  | score_final |
|-----------|----------|----------|--------------|--------------|----------------|---------|---------|------------|----------|----------|-------------|
| AB123456  | BENALI   | Mohammed | 0612345678   | DOSS-000001  | AUTO_CLASSIQUE | AUTO PP | 3       | 1500.00    | 43500.00 | 4500.00  | 70.0        |
| CD789012  | ALAMI    | Fatima   | 0623456789   | DOSS-000002  | AUTO_CLASSIQUE | AUTO PP | 1       | 2000.00    | 58000.00 | 2000.00  | 85.0        |
| EF345678  | IDRISSI  | Youssef  | 0634567890   | DOSS-000003  | AUTO_CLASSIQUE | AUTO PP | 4       | 1200.00    | 34800.00 | 4800.00  | 60.0        |
| KL123456  | NOUVEAU  | Ahmed    | 0667890123   | DOSS-000006  | AUTO_CLASSIQUE | AUTO PP | 1       | 1800.00    | 52000.00 | 1800.00  | 80.0        |
| MN789012  | NOUVELLE | Khadija  | 0678901234   | DOSS-000007  | AUTO_CLASSIQUE | AUTO PP | 2       | 2200.00    | 64000.00 | 4400.00  | 75.0        |
```

**Dossiers récurrents :**
- DOSS-000001 : était CONTACTE (mois 7) → devrait devenir RELANCE
- DOSS-000002 : était PROMESSE (mois 7) → devrait devenir SUIVI_PROMESSE
- DOSS-000003 : était INJOIGNABLE (mois 7) → devrait rester INJOIGNABLE

**Nouveaux dossiers :**
- DOSS-000006 : nouveau → EN_ATTENTE
- DOSS-000007 : nouveau → EN_ATTENTE

#### 4.2 Modifier le ImportController pour supporter la détection

Pour le test, on va simuler la détection manuellement avec SQL :

```sql
-- scripts/test_import_mois8_avec_detection.sql

-- 1. Créer les nouveaux clients
INSERT INTO clients (cin, nom, prenom, telephone, email) VALUES
('KL123456', 'NOUVEAU', 'Ahmed', '0667890123', 'nouveau@test.com'),
('MN789012', 'NOUVELLE', 'Khadija', '0678901234', 'nouvelle@test.com');

-- 2. Créer tous les crédits (récurrents + nouveaux)
-- Récurrents (mise à jour)
UPDATE credits SET
    nbr_impaye = 3,
    mensualite = 1500.00,
    encours = 43500.00,
    impaye_montant = 4500.00,
    score = 70.0
WHERE reference_contrat = 'DOSS-000001';

UPDATE credits SET
    nbr_impaye = 1,
    mensualite = 2000.00,
    encours = 58000.00,
    impaye_montant = 2000.00,
    score = 85.0
WHERE reference_contrat = 'DOSS-000002';

UPDATE credits SET
    nbr_impaye = 4,
    mensualite = 1200.00,
    encours = 34800.00,
    impaye_montant = 4800.00,
    score = 60.0
WHERE reference_contrat = 'DOSS-000003';

-- Nouveaux
INSERT INTO credits (client_id, reference_contrat, type_credit, segment, nbr_impaye, mensualite, encours, impaye_montant, score)
SELECT c.id, 'DOSS-000006', 'AUTO_CLASSIQUE', 'AUTO PP', 1, 1800.00, 52000.00, 1800.00, 80.0
FROM clients c WHERE c.cin = 'KL123456';

INSERT INTO credits (client_id, reference_contrat, type_credit, segment, nbr_impaye, mensualite, encours, impaye_montant, score)
SELECT c.id, 'DOSS-000007', 'AUTO_CLASSIQUE', 'AUTO PP', 2, 2200.00, 64000.00, 4400.00, 75.0
FROM clients c WHERE c.cin = 'MN789012';

-- 3. Créer les dossiers AVEC DÉTECTION AUTOMATIQUE
-- Fonction pour calculer le statut
CREATE OR REPLACE FUNCTION get_statut_auto(ref_contrat VARCHAR) 
RETURNS VARCHAR AS $$
DECLARE
    ancien_statut VARCHAR;
BEGIN
    SELECT statut_final INTO ancien_statut
    FROM dossiers_mois_precedent
    WHERE numero_dossier = ref_contrat;
    
    IF ancien_statut IS NULL THEN
        RETURN 'EN_ATTENTE'; -- Nouveau dossier
    END IF;
    
    -- Appliquer la logique métier
    RETURN CASE ancien_statut
        WHEN 'CONTACTE' THEN 'RELANCE'
        WHEN 'PROMESSE' THEN 'SUIVI_PROMESSE'
        WHEN 'INJOIGNABLE' THEN 'INJOIGNABLE'
        WHEN 'EN_ATTENTE' THEN 'EN_ATTENTE'
        ELSE 'EN_ATTENTE'
    END;
END;
$$ LANGUAGE plpgsql;

-- Insérer les dossiers avec détection
INSERT INTO dossiers (credit_id, statut_actuel, date_creation, mois_import, est_recurrent, mois_precedent_statut)
SELECT 
    cr.id,
    get_statut_auto(cr.reference_contrat)::statut_dossier,
    CURRENT_DATE,
    '2024-08',
    EXISTS(SELECT 1 FROM dossiers_mois_precedent WHERE numero_dossier = cr.reference_contrat),
    (SELECT statut_final FROM dossiers_mois_precedent WHERE numero_dossier = cr.reference_contrat)
FROM credits cr
WHERE cr.reference_contrat IN ('DOSS-000001', 'DOSS-000002', 'DOSS-000003', 'DOSS-000006', 'DOSS-000007');
```

#### 4.3 Vérifier la détection automatique
```sql
-- Vue d'ensemble
SELECT 
    cr.reference_contrat as numero,
    cl.nom || ' ' || cl.prenom as client,
    d.statut_actuel,
    d.est_recurrent,
    d.mois_precedent_statut as ancien_statut,
    d.mois_import,
    CASE 
        WHEN d.est_recurrent THEN '🔄 RÉCURRENT'
        ELSE '🆕 NOUVEAU'
    END as type
FROM dossiers d
JOIN credits cr ON d.credit_id = cr.id
JOIN clients cl ON cr.client_id = cl.id
WHERE d.mois_import = '2024-08'
ORDER BY d.est_recurrent DESC, cr.reference_contrat;

-- Statistiques de l'import
SELECT 
    '📊 TOTAL' as categorie,
    COUNT(*) as nombre
FROM dossiers WHERE mois_import = '2024-08'
UNION ALL
SELECT '🔄 Récurrents', COUNT(*) 
FROM dossiers WHERE mois_import = '2024-08' AND est_recurrent = true
UNION ALL
SELECT '🆕 Nouveaux', COUNT(*) 
FROM dossiers WHERE mois_import = '2024-08' AND est_recurrent = false;

-- Répartition des statuts auto pour récurrents
SELECT 
    d.mois_precedent_statut as statut_mois7,
    d.statut_actuel as statut_mois8_auto,
    COUNT(*) as nombre
FROM dossiers d
WHERE d.mois_import = '2024-08' AND d.est_recurrent = true
GROUP BY d.mois_precedent_statut, d.statut_actuel;
```

**✅ Résultat attendu :**
```
📊 Résumé Import Mois 8
- Total dossiers: 5
- Récurrents: 3 (DOSS-000001, DOSS-000002, DOSS-000003)
- Nouveaux: 2 (DOSS-000006, DOSS-000007)

🔄 Détection automatique:
- DOSS-000001: CONTACTE → RELANCE ✅
- DOSS-000002: PROMESSE → SUIVI_PROMESSE ✅
- DOSS-000003: INJOIGNABLE → INJOIGNABLE ✅
- DOSS-000006: N/A → EN_ATTENTE ✅
- DOSS-000007: N/A → EN_ATTENTE ✅
```

---

### Test 5 : Export du Mois 8 (uniquement Mois 8)

#### 5.1 Exporter via API
```bash
GET http://localhost:8080/api/historique/exporter
Headers: Authorization: Bearer <votre_token_admin>
```

#### 5.2 Vérifier que seul le Mois 8 est exporté
```sql
-- Vérifier que l'export contient seulement le mois 8
SELECT 
    he.nom_fichier,
    he.mois_periode,
    he.nombre_dossiers,
    he.date_export
FROM historique_exports he
ORDER BY he.date_export DESC
LIMIT 1;

-- Compter les dossiers actuels (devrait être uniquement mois 8)
SELECT mois_import, COUNT(*) as total
FROM dossiers
GROUP BY mois_import;
```

**✅ Résultat attendu :**
- L'export contient 5 dossiers (uniquement ceux du mois 2024-08)
- Table dossiers contient uniquement les dossiers du mois 2024-08

---

### Test 6 : Cycle Complet Mois 9

Répéter les étapes 2, 3, 4, 5 pour le mois 9 :

```sql
-- Créer 2 dossiers récurrents du mois 8 + 3 nouveaux
-- DOSS-000001 et DOSS-000006 reviennent en mois 9
-- + 3 nouveaux : DOSS-000008, DOSS-000009, DOSS-000010
```

---

## 🎯 TESTS D'INTÉGRATION VIA API

### Test API 1 : Lister les mois archivés
```bash
GET http://localhost:8080/api/archivage/mois
Headers: Authorization: Bearer <token_admin>

Résultat attendu:
["2024-08", "2024-07"]
```

### Test API 2 : Statistiques d'un mois archivé
```bash
GET http://localhost:8080/api/archivage/statistiques/2024-07
Headers: Authorization: Bearer <token_admin>

Résultat attendu:
{
  "moisArchive": "2024-07",
  "totalDossiers": 5,
  "repartitionStatuts": {
    "CONTACTE": 1,
    "PROMESSE": 1,
    "INJOIGNABLE": 1,
    "EN_ATTENTE": 2
  },
  "totalDuMensuel": 15000.00,
  "dateArchivage": "2024-07-31T...",
  "exportCsvDisponible": true,
  "exportId": 1
}
```

### Test API 3 : Consulter les dossiers archivés
```bash
GET http://localhost:8080/api/archivage/dossiers/2024-07
Headers: Authorization: Bearer <token_admin>

Résultat attendu: Liste de 5 dossiers avec toutes leurs infos
```

### Test API 4 : Logs d'archivage
```bash
GET http://localhost:8080/api/archivage/logs
Headers: Authorization: Bearer <token_admin>

Résultat attendu: Liste des logs (ARCHIVAGE, NETTOYAGE, IMPORT)
```

---

## 🚨 TESTS DE CAS D'ERREUR

### Test Erreur 1 : Archiver un mois déjà archivé
```bash
POST http://localhost:8080/api/archivage/archiver
Body: {"mois": "2024-07", "exporterCsv": true}

Résultat attendu: 
{
  "success": false,
  "message": "Le mois 2024-07 est déjà archivé"
}
```

### Test Erreur 2 : Nettoyer un mois non archivé
```bash
DELETE http://localhost:8080/api/archivage/nettoyer/2024-09

Résultat attendu: 400 Bad Request
{
  "success": false,
  "message": "Impossible de nettoyer: le mois 2024-09 n'a pas été archivé"
}
```

### Test Erreur 3 : Archiver un mois sans dossiers
```bash
POST http://localhost:8080/api/archivage/archiver
Body: {"mois": "2024-12", "exporterCsv": true}

Résultat attendu:
{
  "success": false,
  "message": "Aucun dossier trouvé pour le mois 2024-12"
}
```

---

## ✅ CHECKLIST COMPLÈTE

### Phase 1 : Setup
- [ ] Tables créées (dossiers_archive, dossiers_mois_precedent, logs_archivage)
- [ ] Colonnes ajoutées à table dossiers
- [ ] Fonctions SQL créées

### Phase 2 : Mois 7
- [ ] Import de 5 dossiers mois 7
- [ ] Statuts variés (CONTACTE, PROMESSE, INJOIGNABLE, EN_ATTENTE)
- [ ] Export/Archivage réussi
- [ ] 5 dossiers dans dossiers_archive
- [ ] 5 entrées dans dossiers_mois_precedent
- [ ] Log ARCHIVAGE SUCCESS
- [ ] Export CSV créé dans historique_exports
- [ ] Nettoyage réussi (table dossiers vide)

### Phase 3 : Mois 8
- [ ] Import de 5 dossiers (3 récurrents + 2 nouveaux)
- [ ] Détection automatique fonctionnelle
- [ ] Statuts auto appliqués correctement
  - [ ] CONTACTE → RELANCE
  - [ ] PROMESSE → SUIVI_PROMESSE
  - [ ] INJOIGNABLE → INJOIGNABLE
  - [ ] Nouveaux → EN_ATTENTE
- [ ] Colonnes est_recurrent et mois_precedent_statut remplies
- [ ] Export mois 8 contient uniquement mois 8

### Phase 4 : APIs
- [ ] GET /api/archivage/mois fonctionne
- [ ] GET /api/archivage/statistiques/{mois} fonctionne
- [ ] GET /api/archivage/dossiers/{mois} fonctionne
- [ ] GET /api/archivage/logs fonctionne
- [ ] POST /api/archivage/archiver fonctionne
- [ ] DELETE /api/archivage/nettoyer/{mois} fonctionne

### Phase 5 : Cas d'erreur
- [ ] Erreur si archivage doublon
- [ ] Erreur si nettoyage sans archivage
- [ ] Erreur si mois vide

---

## 📝 RAPPORT DE TEST (Template)

```
Date: __________
Testeur: __________

✅ Tests Réussis: __ / 30
❌ Tests Échoués: __ / 30

Problèmes rencontrés:
1. 
2. 
3. 

Recommandations:
1. 
2. 
3. 

Prêt pour production: ☐ OUI  ☐ NON
```
