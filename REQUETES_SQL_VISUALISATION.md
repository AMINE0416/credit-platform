# Requêtes SQL - Visualisation des Dossiers et Actions

## 🎯 Guide MySQL pour visualiser les données

---

## 1️⃣ Vue d'ensemble de tous les dossiers

### Compter les dossiers par statut
```sql
SELECT 
    statut_actuel,
    COUNT(*) as nombre_dossiers
FROM dossiers
GROUP BY statut_actuel
ORDER BY 
    FIELD(statut_actuel, 'NOUVEAU', 'EN_COURS', 'INJOIGNABLE', 'PROMESSE', 'REGULARISE', 'CLOTURE');
```

**Résultat attendu :**
```
+----------------+------------------+
| statut_actuel  | nombre_dossiers  |
+----------------+------------------+
| NOUVEAU        |              5   |
| EN_COURS       |             12   |
| INJOIGNABLE    |              3   |
| PROMESSE       |              8   |
| REGULARISE     |             15   |
| CLOTURE        |             20   |
+----------------+------------------+
```

---

## 2️⃣ Liste complète des dossiers avec informations client

```sql
SELECT 
    d.id as dossier_id,
    d.statut_actuel,
    cl.cin,
    CONCAT(cl.prenom, ' ', cl.nom) as nom_complet,
    cl.telephone,
    cl.ville,
    cr.reference_contrat,
    cr.nbr_impaye,
    cr.impaye_montant,
    CONCAT(u.prenom, ' ', u.nom) as agent_assigne,
    d.date_creation
FROM dossiers d
INNER JOIN credits cr ON d.credit_id = cr.id
INNER JOIN clients cl ON cr.client_id = cl.id
LEFT JOIN users u ON d.agent_assigne_id = u.id
ORDER BY d.statut_actuel, d.id;
```

---

## 3️⃣ Dossiers par statut spécifique

### Dossiers NOUVEAU (non assignés)
```sql
SELECT 
    d.id,
    cl.cin,
    CONCAT(cl.prenom, ' ', cl.nom) as client,
    cr.reference_contrat,
    cr.impaye_montant,
    d.date_creation
FROM dossiers d
INNER JOIN credits cr ON d.credit_id = cr.id
INNER JOIN clients cl ON cr.client_id = cl.id
WHERE d.statut_actuel = 'NOUVEAU'
ORDER BY d.date_creation DESC;
```

### Dossiers EN_COURS
```sql
SELECT 
    d.id,
    cl.cin,
    CONCAT(cl.prenom, ' ', cl.nom) as client,
    cr.reference_contrat,
    CONCAT(u.prenom, ' ', u.nom) as agent,
    (SELECT COUNT(*) FROM actions_dossier WHERE dossier_id = d.id) as nb_actions,
    d.date_creation
FROM dossiers d
INNER JOIN credits cr ON d.credit_id = cr.id
INNER JOIN clients cl ON cr.client_id = cl.id
LEFT JOIN users u ON d.agent_assigne_id = u.id
WHERE d.statut_actuel = 'EN_COURS'
ORDER BY d.date_creation DESC;
```

### Dossiers avec PROMESSE
```sql
SELECT 
    d.id,
    cl.cin,
    CONCAT(cl.prenom, ' ', cl.nom) as client,
    cr.reference_contrat,
    CONCAT(u.prenom, ' ', u.nom) as agent,
    a.montant_promesse,
    a.date_promesse,
    DATEDIFF(a.date_promesse, CURDATE()) as jours_restants,
    a.date_action as date_enregistrement
FROM dossiers d
INNER JOIN credits cr ON d.credit_id = cr.id
INNER JOIN clients cl ON cr.client_id = cl.id
LEFT JOIN users u ON d.agent_assigne_id = u.id
INNER JOIN actions_dossier a ON d.id = a.dossier_id
WHERE d.statut_actuel = 'PROMESSE'
  AND a.montant_promesse IS NOT NULL
ORDER BY a.date_promesse ASC;
```

### Dossiers INJOIGNABLE
```sql
SELECT 
    d.id,
    cl.cin,
    CONCAT(cl.prenom, ' ', cl.nom) as client,
    cl.telephone,
    cl.telephone2,
    cr.reference_contrat,
    CONCAT(u.prenom, ' ', u.nom) as agent,
    (SELECT COUNT(*) FROM actions_dossier WHERE dossier_id = d.id) as nb_tentatives,
    (SELECT MAX(date_action) FROM actions_dossier WHERE dossier_id = d.id) as derniere_tentative
FROM dossiers d
INNER JOIN credits cr ON d.credit_id = cr.id
INNER JOIN clients cl ON cr.client_id = cl.id
LEFT JOIN users u ON d.agent_assigne_id = u.id
WHERE d.statut_actuel = 'INJOIGNABLE'
ORDER BY derniere_tentative DESC;
```

### Dossiers REGULARISE
```sql
SELECT 
    d.id,
    cl.cin,
    CONCAT(cl.prenom, ' ', cl.nom) as client,
    cr.reference_contrat,
    cr.impaye_montant as montant_initial,
    (SELECT MAX(date_action) FROM actions_dossier WHERE dossier_id = d.id) as date_regularisation,
    DATEDIFF(
        (SELECT MAX(date_action) FROM actions_dossier WHERE dossier_id = d.id),
        d.date_creation
    ) as duree_recouvrement_jours
FROM dossiers d
INNER JOIN credits cr ON d.credit_id = cr.id
INNER JOIN clients cl ON cr.client_id = cl.id
WHERE d.statut_actuel = 'REGULARISE'
ORDER BY date_regularisation DESC;
```

---

## 4️⃣ Historique complet d'un dossier spécifique

### Remplacer `<ID_DOSSIER>` par l'ID du dossier (ex: 91)

```sql
SELECT 
    a.id as action_id,
    a.date_action,
    CONCAT(u.prenom, ' ', u.nom) as agent,
    a.nature_action,
    a.motif_impaye,
    a.code_resultat,
    a.recidive,
    a.nbre_action_client,
    a.montant_promesse,
    a.date_promesse,
    a.nouveau_telephone,
    LEFT(a.compte_rendu, 100) as compte_rendu_extrait
FROM actions_dossier a
INNER JOIN users u ON a.user_id = u.id
WHERE a.dossier_id = 85
ORDER BY a.date_action DESC;
```

**Exemple avec dossier 91 :**
```sql
SELECT 
    a.id as action_id,
    a.date_action,
    CONCAT(u.prenom, ' ', u.nom) as agent,
    a.nature_action,
    a.motif_impaye,
    a.code_resultat,
    a.recidive,
    a.nbre_action_client,
    a.montant_promesse,
    a.date_promesse,
    a.nouveau_telephone,
    a.compte_rendu
FROM actions_dossier a
INNER JOIN users u ON a.user_id = u.id
WHERE a.dossier_id = 91
ORDER BY a.date_action DESC;
```

---

## 5️⃣ Toutes les actions par type de statut

### Actions qui ont mené à un statut PROMESSE
```sql
SELECT 
    d.id as dossier_id,
    cl.cin,
    CONCAT(cl.prenom, ' ', cl.nom) as client,
    a.nature_action,
    a.montant_promesse,
    a.date_promesse,
    a.date_action,
    CONCAT(u.prenom, ' ', u.nom) as agent
FROM actions_dossier a
INNER JOIN dossiers d ON a.dossier_id = d.id
INNER JOIN credits cr ON d.credit_id = cr.id
INNER JOIN clients cl ON cr.client_id = cl.id
INNER JOIN users u ON a.user_id = u.id
WHERE a.montant_promesse IS NOT NULL
  AND a.date_promesse IS NOT NULL
ORDER BY a.date_action DESC;
```

### Actions INJOIGNABLE avec récidive
```sql
SELECT 
    d.id as dossier_id,
    cl.cin,
    CONCAT(cl.prenom, ' ', cl.nom) as client,
    a.nature_action,
    a.recidive,
    a.code_resultat,
    a.nbre_action_client,
    a.date_action,
    CONCAT(u.prenom, ' ', u.nom) as agent
FROM actions_dossier a
INNER JOIN dossiers d ON a.dossier_id = d.id
INNER JOIN credits cr ON d.credit_id = cr.id
INNER JOIN clients cl ON cr.client_id = cl.id
INNER JOIN users u ON a.user_id = u.id
WHERE d.statut_actuel = 'INJOIGNABLE'
  AND a.recidive = 'OUI'
ORDER BY a.date_action DESC;
```

---

## 6️⃣ Statistiques avancées

### Nombre d'actions par agent
```sql
SELECT 
    CONCAT(u.prenom, ' ', u.nom) as agent,
    COUNT(*) as total_actions,
    COUNT(DISTINCT a.dossier_id) as dossiers_traites,
    SUM(CASE WHEN d.statut_actuel = 'REGULARISE' THEN 1 ELSE 0 END) as dossiers_regularises,
    SUM(CASE WHEN a.montant_promesse IS NOT NULL THEN 1 ELSE 0 END) as promesses_obtenues
FROM actions_dossier a
INNER JOIN users u ON a.user_id = u.id
INNER JOIN dossiers d ON a.dossier_id = d.id
GROUP BY u.id, u.prenom, u.nom
ORDER BY total_actions DESC;
```

### Promesses par statut de respect
```sql
SELECT 
    CASE 
        WHEN d.statut_actuel = 'REGULARISE' THEN 'Promesse respectée'
        WHEN d.statut_actuel = 'PROMESSE' AND a.date_promesse >= CURDATE() THEN 'En attente'
        WHEN d.statut_actuel IN ('PROMESSE', 'EN_COURS') AND a.date_promesse < CURDATE() THEN 'Promesse rompue'
        ELSE 'Autre'
    END as statut_promesse,
    COUNT(*) as nombre,
    SUM(a.montant_promesse) as montant_total
FROM actions_dossier a
INNER JOIN dossiers d ON a.dossier_id = d.id
WHERE a.montant_promesse IS NOT NULL
GROUP BY statut_promesse
ORDER BY nombre DESC;
```

### Dossiers par durée de traitement
```sql
SELECT 
    d.statut_actuel,
    COUNT(*) as nombre_dossiers,
    AVG(DATEDIFF(
        COALESCE(
            (SELECT MAX(date_action) FROM actions_dossier WHERE dossier_id = d.id),
            CURDATE()
        ),
        d.date_creation
    )) as duree_moyenne_jours,
    MIN(DATEDIFF(
        COALESCE(
            (SELECT MAX(date_action) FROM actions_dossier WHERE dossier_id = d.id),
            CURDATE()
        ),
        d.date_creation
    )) as duree_min_jours,
    MAX(DATEDIFF(
        COALESCE(
            (SELECT MAX(date_action) FROM actions_dossier WHERE dossier_id = d.id),
            CURDATE()
        ),
        d.date_creation
    )) as duree_max_jours
FROM dossiers d
GROUP BY d.statut_actuel
ORDER BY FIELD(d.statut_actuel, 'NOUVEAU', 'EN_COURS', 'INJOIGNABLE', 'PROMESSE', 'REGULARISE', 'CLOTURE');
```

### Nature d'actions les plus fréquentes
```sql
SELECT 
    a.nature_action,
    COUNT(*) as nombre_actions,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM actions_dossier), 2) as pourcentage
FROM actions_dossier a
GROUP BY a.nature_action
ORDER BY nombre_actions DESC;
```

---

## 7️⃣ Alertes et points d'attention

### Promesses arrivant à échéance (J-3)
```sql
SELECT 
    d.id as dossier_id,
    cl.cin,
    CONCAT(cl.prenom, ' ', cl.nom) as client,
    cl.telephone,
    cr.reference_contrat,
    a.montant_promesse,
    a.date_promesse,
    DATEDIFF(a.date_promesse, CURDATE()) as jours_restants,
    CONCAT(u.prenom, ' ', u.nom) as agent
FROM dossiers d
INNER JOIN actions_dossier a ON d.id = a.dossier_id
INNER JOIN credits cr ON d.credit_id = cr.id
INNER JOIN clients cl ON cr.client_id = cl.id
LEFT JOIN users u ON d.agent_assigne_id = u.id
WHERE d.statut_actuel = 'PROMESSE'
  AND a.montant_promesse IS NOT NULL
  AND a.date_promesse BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 3 DAY)
ORDER BY a.date_promesse ASC;
```

### Promesses dépassées non traitées
```sql
SELECT 
    d.id as dossier_id,
    cl.cin,
    CONCAT(cl.prenom, ' ', cl.nom) as client,
    cl.telephone,
    cr.reference_contrat,
    a.montant_promesse,
    a.date_promesse,
    DATEDIFF(CURDATE(), a.date_promesse) as jours_retard,
    CONCAT(u.prenom, ' ', u.nom) as agent
FROM dossiers d
INNER JOIN actions_dossier a ON d.id = a.dossier_id
INNER JOIN credits cr ON d.credit_id = cr.id
INNER JOIN clients cl ON cr.client_id = cl.id
LEFT JOIN users u ON d.agent_assigne_id = u.id
WHERE d.statut_actuel = 'PROMESSE'
  AND a.montant_promesse IS NOT NULL
  AND a.date_promesse < CURDATE()
ORDER BY jours_retard DESC;
```

### Dossiers INJOIGNABLE avec plus de 5 tentatives
```sql
SELECT 
    d.id as dossier_id,
    cl.cin,
    CONCAT(cl.prenom, ' ', cl.nom) as client,
    cl.telephone,
    cl.telephone2,
    cr.reference_contrat,
    COUNT(a.id) as nb_tentatives,
    MAX(a.date_action) as derniere_tentative,
    CONCAT(u.prenom, ' ', u.nom) as agent
FROM dossiers d
INNER JOIN actions_dossier a ON d.id = a.dossier_id
INNER JOIN credits cr ON d.credit_id = cr.id
INNER JOIN clients cl ON cr.client_id = cl.id
LEFT JOIN users u ON d.agent_assigne_id = u.id
WHERE d.statut_actuel = 'INJOIGNABLE'
GROUP BY d.id, cl.cin, cl.prenom, cl.nom, cl.telephone, cl.telephone2, cr.reference_contrat, u.prenom, u.nom
HAVING nb_tentatives >= 5
ORDER BY nb_tentatives DESC, derniere_tentative DESC;
```

---

## 8️⃣ Export pour reporting

### Export complet pour Excel
```sql
SELECT 
    d.id as 'ID Dossier',
    d.statut_actuel as 'Statut',
    cl.cin as 'CIN',
    cl.prenom as 'Prénom',
    cl.nom as 'Nom',
    cl.telephone as 'Téléphone',
    cl.ville as 'Ville',
    cr.reference_contrat as 'Référence',
    cr.nbr_impaye as 'Nb Impayés',
    cr.impaye_montant as 'Montant Impayé',
    CONCAT(u.prenom, ' ', u.nom) as 'Agent Assigné',
    (SELECT COUNT(*) FROM actions_dossier WHERE dossier_id = d.id) as 'Nb Actions',
    (SELECT MAX(date_action) FROM actions_dossier WHERE dossier_id = d.id) as 'Dernière Action',
    d.date_creation as 'Date Création'
FROM dossiers d
INNER JOIN credits cr ON d.credit_id = cr.id
INNER JOIN clients cl ON cr.client_id = cl.id
LEFT JOIN users u ON d.agent_assigne_id = u.id
ORDER BY d.statut_actuel, d.id;
```

---

## 🔧 Utilisation dans MySQL Workbench ou ligne de commande

### Connexion à la base de données
```bash
mysql -u root -p credit_platform
```

### Exécuter une requête
```sql
USE credit_platform;

-- Copier-coller une des requêtes ci-dessus
SELECT ...
```

### Exporter les résultats vers un fichier CSV
```sql
SELECT ... 
INTO OUTFILE 'C:/exports/dossiers_par_statut.csv'
FIELDS TERMINATED BY ';'
ENCLOSED BY '"'
LINES TERMINATED BY '\n';
```

---

## 📊 Requêtes pour tableaux de bord

### Vue synthétique quotidienne
```sql
SELECT 
    'Total dossiers' as indicateur,
    COUNT(*) as valeur
FROM dossiers
UNION ALL
SELECT 
    'Dossiers EN_COURS' as indicateur,
    COUNT(*) as valeur
FROM dossiers WHERE statut_actuel = 'EN_COURS'
UNION ALL
SELECT 
    'Promesses actives' as indicateur,
    COUNT(*) as valeur
FROM dossiers WHERE statut_actuel = 'PROMESSE'
UNION ALL
SELECT 
    'Dossiers régularisés aujourd\'hui' as indicateur,
    COUNT(DISTINCT a.dossier_id) as valeur
FROM actions_dossier a
INNER JOIN dossiers d ON a.dossier_id = d.id
WHERE d.statut_actuel = 'REGULARISE'
  AND DATE(a.date_action) = CURDATE()
UNION ALL
SELECT 
    'Actions créées aujourd\'hui' as indicateur,
    COUNT(*) as valeur
FROM actions_dossier
WHERE DATE(date_action) = CURDATE();
```

---

**Dernière mise à jour :** 14 juillet 2026  
**Base de données :** credit_platform  
**Version MySQL :** 8.0+
