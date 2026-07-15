# 🔍 Guide de Visualisation des Données

## Méthode 1 : MySQL en ligne de commande (CMD)

### Étape 1 : Se connecter à MySQL
```cmd
mysql -u root -p
```
Ensuite entrez votre mot de passe : `aminux`

### Étape 2 : Sélectionner la base de données
```sql
USE credit_platform;
```

### Étape 3 : Visualiser les données

#### 📊 Voir toutes les tables disponibles
```sql
SHOW TABLES;
```
Résultat :
- actions_dossier
- clients
- credits
- dossiers
- roles
- users
- user_roles

#### 👥 Voir tous les utilisateurs (agents et admin)
```sql
SELECT id, nom, prenom, email, actif FROM users;
```

#### 📁 Voir tous les dossiers
```sql
SELECT 
    d.id,
    c.reference_contrat,
    cl.cin,
    cl.nom,
    cl.prenom,
    d.statut_actuel,
    CONCAT(u.prenom, ' ', u.nom) AS agent_assigne,
    d.date_creation
FROM dossiers d
JOIN credits c ON d.credit_id = c.id
JOIN clients cl ON c.client_id = cl.id
LEFT JOIN users u ON d.agent_assigne_id = u.id;
```

#### 📝 Voir toutes les actions enregistrées ⭐⭐⭐
```sql
SELECT 
    a.id,
    a.dossier_id,
    CONCAT(u.prenom, ' ', u.nom) AS agent,
    a.nature_action,
    a.code_resultat,
    a.compte_rendu,
    a.montant_promesse,
    a.date_promesse,
    a.date_action
FROM actions_dossier a
JOIN users u ON a.user_id = u.id
ORDER BY a.date_action DESC;
```

#### 📋 Voir l'historique complet d'un dossier spécifique
```sql
-- Remplacez '1' par l'ID du dossier
SELECT 
    a.id,
    a.date_action,
    CONCAT(u.prenom, ' ', u.nom) AS agent,
    a.nature_action,
    a.code_resultat,
    a.motif_impaye,
    a.compte_rendu,
    a.montant_promesse,
    a.date_promesse,
    a.recidive,
    a.nbre_action_client
FROM actions_dossier a
JOIN users u ON a.user_id = u.id
WHERE a.dossier_id = 1
ORDER BY a.date_action DESC;
```

#### 📊 Statistiques par agent
```sql
SELECT 
    CONCAT(u.prenom, ' ', u.nom) AS agent,
    COUNT(a.id) AS nombre_actions,
    COUNT(DISTINCT a.dossier_id) AS dossiers_traites
FROM actions_dossier a
JOIN users u ON a.user_id = u.id
GROUP BY u.id, u.prenom, u.nom;
```

#### 🔢 Compter le nombre d'actions par statut
```sql
SELECT 
    d.statut_actuel,
    COUNT(DISTINCT d.id) AS nombre_dossiers,
    COUNT(a.id) AS nombre_actions_total
FROM dossiers d
LEFT JOIN actions_dossier a ON d.id = a.dossier_id
GROUP BY d.statut_actuel;
```

#### 🎯 Actions du jour
```sql
SELECT 
    a.id,
    CONCAT(cl.prenom, ' ', cl.nom) AS client,
    CONCAT(u.prenom, ' ', u.nom) AS agent,
    a.nature_action,
    a.code_resultat,
    a.compte_rendu,
    TIME(a.date_action) AS heure
FROM actions_dossier a
JOIN dossiers d ON a.dossier_id = d.id
JOIN credits c ON d.credit_id = c.id
JOIN clients cl ON c.client_id = cl.id
JOIN users u ON a.user_id = u.id
WHERE DATE(a.date_action) = CURDATE()
ORDER BY a.date_action DESC;
```

### 📤 Quitter MySQL
```sql
EXIT;
```

---

## Méthode 2 : Via l'interface Web (Frontend)

### Pour l'Agent :
1. Connexion : `http://localhost:5173/login`
2. Identifiants : `agent@eqdom.ma` / `Agent@123`
3. Dashboard → Cliquer sur un dossier
4. Visualiser l'historique des actions dans la section "Historique"

### Pour l'Admin :
1. Connexion : `http://localhost:5173/login`
2. Identifiants : `admin@eqdom.ma` / `Admin@123`
3. Navigation :
   - **Dashboard** : Vue d'ensemble des statistiques
   - **Dossiers** : Liste de tous les dossiers
   - **Utilisateurs** : Gestion des agents
   - **Import** : Import de fichiers Excel

---

## Méthode 3 : Via des outils graphiques MySQL

### Option A : MySQL Workbench (Recommandé)
1. Télécharger : https://dev.mysql.com/downloads/workbench/
2. Connexion :
   - Host: localhost
   - Port: 3306
   - Username: root
   - Password: aminux
3. Sélectionner la base : `credit_platform`
4. Interface graphique pour naviguer dans les tables

### Option B : phpMyAdmin
1. Installer XAMPP ou WAMP (inclut phpMyAdmin)
2. Accéder : `http://localhost/phpmyadmin`
3. Sélectionner `credit_platform`

### Option C : DBeaver (Gratuit, multi-plateforme)
1. Télécharger : https://dbeaver.io/download/
2. Créer une connexion MySQL
3. Explorer visuellement les tables et données

---

## 📊 Exemple d'utilisation pratique

### Scénario : Un agent vient de traiter 3 dossiers

**1. Vérifier dans MySQL :**
```cmd
mysql -u root -paminux credit_platform

-- Voir les dernières actions
SELECT 
    CONCAT(cl.nom, ' ', cl.prenom) AS client,
    a.nature_action,
    a.compte_rendu,
    a.date_action
FROM actions_dossier a
JOIN dossiers d ON a.dossier_id = d.id
JOIN credits c ON d.credit_id = c.id
JOIN clients cl ON c.client_id = cl.id
ORDER BY a.date_action DESC
LIMIT 10;
```

**2. Vérifier via l'API (avec curl) :**
```powershell
# D'abord se connecter pour obtenir le token
$body = '{"email":"agent@eqdom.ma","password":"Agent@123"}'
$response = Invoke-WebRequest -Uri "http://localhost:8081/api/auth/login" -Method POST -Body $body -ContentType "application/json"
$token = ($response.Content | ConvertFrom-Json).token

# Récupérer l'historique d'un dossier (exemple: dossier ID 1)
$headers = @{ "Authorization" = "Bearer $token" }
$actions = Invoke-WebRequest -Uri "http://localhost:8081/api/dossiers/1/actions" -Headers $headers
$actions.Content | ConvertFrom-Json
```

**3. Vérifier dans le Frontend :**
- Aller sur la page du dossier
- Scroller vers la section "Historique des Actions"
- Toutes les actions s'affichent avec détails

---

## 🎯 Commandes rapides pour déboguer

### Vérifier si des actions existent
```sql
SELECT COUNT(*) AS total_actions FROM actions_dossier;
```

### Vérifier si des dossiers existent
```sql
SELECT COUNT(*) AS total_dossiers FROM dossiers;
```

### Voir la dernière action enregistrée
```sql
SELECT * FROM actions_dossier 
ORDER BY date_action DESC 
LIMIT 1;
```

### Vider toutes les actions (pour tests)
```sql
DELETE FROM actions_dossier;
```

### Réinitialiser tout (ATTENTION : supprime toutes les données)
```sql
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM actions_dossier;
DELETE FROM dossiers;
DELETE FROM credits;
DELETE FROM clients;
SET FOREIGN_KEY_CHECKS = 1;
```

---

## 💡 Conseils pratiques

1. **Pour le développement** : Utilisez MySQL en ligne de commande pour des vérifications rapides
2. **Pour l'analyse** : Utilisez MySQL Workbench pour des requêtes complexes et graphiques
3. **Pour la production** : Les utilisateurs finaux utilisent uniquement le Frontend
4. **Pour le débogage** : Combinez MySQL + logs du serveur backend

---

## 🔐 Sécurité

⚠️ **Important** : Ne jamais exposer l'accès direct à MySQL en production !
- Les utilisateurs finaux passent TOUJOURS par l'API REST
- Seuls les développeurs/admins système accèdent à MySQL directement
- Les agents et admins utilisent UNIQUEMENT l'interface web

