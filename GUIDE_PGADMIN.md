# 🔍 Guide pgAdmin - Visualiser tes Données PostgreSQL

> **Guide complet pour installer et configurer pgAdmin pour visualiser ta base de données**

---

## 📋 Table des Matières

1. [Installation pgAdmin](#installation)
2. [Connexion Base Locale](#connexion-locale)
3. [Connexion Base Render (Production)](#connexion-render)
4. [Navigation dans pgAdmin](#navigation)
5. [Requêtes SQL Utiles](#requetes-utiles)
6. [Troubleshooting](#troubleshooting)

---

## 🔧 Installation pgAdmin

### Windows

#### Option 1 : Installateur Officiel (Recommandé)

1. **Télécharger pgAdmin** :
   - Va sur https://www.pgadmin.org/download/pgadmin-4-windows/
   - Télécharge la dernière version (exe)

2. **Installer** :
   ```
   - Double-clic sur pgadmin4-x.x-x64.exe
   - Suivre l'assistant d'installation
   - Laisser les options par défaut
   - Installer
   ```

3. **Lancer pgAdmin** :
   - Cherche "pgAdmin 4" dans le menu Démarrer
   - Ou double-clic sur l'icône du bureau
   - S'ouvre dans ton navigateur (http://localhost:5050)

#### Option 2 : Avec Chocolatey

```powershell
choco install pgadmin4
```

### Linux (Ubuntu/Debian)

```bash
# Ajouter le repository
curl -fsS https://www.pgadmin.org/static/packages_pgadmin_org.pub | sudo gpg --dearmor -o /usr/share/keyrings/packages-pgadmin-org.gpg

sudo sh -c 'echo "deb [signed-by=/usr/share/keyrings/packages-pgadmin-org.gpg] https://ftp.postgresql.org/pub/pgadmin/pgadmin4/apt/$(lsb_release -cs) pgadmin4 main" > /etc/apt/sources.list.d/pgadmin4.list'

# Installer
sudo apt update
sudo apt install pgadmin4

# Lancer
pgadmin4
```

### macOS

```bash
# Avec Homebrew
brew install --cask pgadmin4

# Lancer
open -a pgAdmin\ 4
```

---

## 🏠 Connexion Base Locale (Développement)

### Prérequis

1. **PostgreSQL installé localement**
2. **Base de données créée** :
   ```bash
   psql -U postgres
   CREATE DATABASE credit_platform;
   \q
   ```
3. **Application Spring Boot démarrée** (pour créer les tables)

### Configuration dans pgAdmin

#### Étape 1 : Créer un Serveur

1. **Ouvrir pgAdmin** (http://localhost:5050)
2. **Définir Master Password** (première fois uniquement)
   - Choisir un mot de passe pour pgAdmin
   - Tu devras le saisir à chaque ouverture

3. **Clic droit sur "Servers"** → **Register** → **Server**

#### Étape 2 : Configuration Connexion Locale

**Onglet "General"** :
```
Name: Credit Platform - Local
```

**Onglet "Connection"** :
```
Host name/address: localhost
Port: 5432
Maintenance database: postgres
Username: postgres
Password: [ton mot de passe PostgreSQL]
☑ Save password
```

**Onglet "Advanced"** (optionnel) :
```
DB restriction: credit_platform
```

4. **Cliquer "Save"**

#### Étape 3 : Explorer les Données

```
Servers
  └── Credit Platform - Local
      └── Databases
          └── credit_platform
              └── Schemas
                  └── public
                      └── Tables
                          ├── dossier
                          ├── user
                          ├── action_history
                          └── ...
```

**Pour voir les données** :
- Clic droit sur une table → **View/Edit Data** → **All Rows**

---

## ☁️ Connexion Base Render (Production)

### Prérequis

1. **Base PostgreSQL déployée sur Render**
2. **Informations de connexion depuis Render Dashboard**

### Récupérer les Infos de Connexion

1. **Va sur [Render Dashboard](https://dashboard.render.com/)**
2. **Clique sur ta base PostgreSQL** : `credit-platform-db`
3. **Note les informations** :

```
Info                          Exemple
──────────────────────────────────────────────────────
Hostname (External)          oregon-postgres.render.com
Port                         5432
Database                     credit_platform
Username                     credit_user
Password                     ••••••••••••••••
```

4. **Copie l'External Database URL** (commence par `postgresql://...`)

### Configuration dans pgAdmin

#### Option 1 : Avec l'URL Complète (Plus Simple)

**Étape 1** : Dans pgAdmin, **Tools** → **Import/Export Servers**
- Mais c'est plus simple de le faire manuellement ⬇️

**Étape 2** : Créer le serveur manuellement

1. Clic droit sur "Servers" → **Register** → **Server**

**Onglet "General"** :
```
Name: Credit Platform - Render Production
Group: Servers (par défaut)
```

**Onglet "Connection"** :

Si ton URL External est :
```
postgresql://credit_user:aBc123XyZ@oregon-postgres.render.com:5432/credit_platform
```

Alors :
```
Host name/address: oregon-postgres.render.com
Port: 5432
Maintenance database: credit_platform
Username: credit_user
Password: aBc123XyZ
☑ Save password
```

**Onglet "SSL"** :
```
SSL mode: Require
```

⚠️ **Important** : Render nécessite SSL !

**Cliquer "Save"**

#### Option 2 : Connexion Rapide avec URL

1. Dans pgAdmin, **File** → **Add New Server**
2. Ou utilise la **barre de connexion rapide** (si disponible)
3. Colle l'URL complète :
   ```
   postgresql://credit_user:password@host:5432/credit_platform?sslmode=require
   ```

### Vérification Connexion

Si la connexion réussit :
```
✅ Le serveur "Credit Platform - Render Production" apparaît
✅ Tu peux explorer Databases → credit_platform → Schemas → public → Tables
✅ Les tables de ton application sont visibles
```

Si échec :
```
❌ "Connection refused" → Vérifie l'URL et le port
❌ "Authentication failed" → Vérifie username et password
❌ "SSL required" → Active SSL mode: Require
```

---

## 🗺️ Navigation dans pgAdmin

### Interface Principale

```
┌─────────────────────────────────────────────────┐
│  Browser (gauche)    │   Main Panel (droite)   │
│                      │                          │
│  Servers             │   Dashboard              │
│  ├── Local           │   ou                     │
│  │   └── Databases   │   Query Tool             │
│  │       └── credit_ │   ou                     │
│  │           platform│   Data View              │
│  └── Render Prod     │                          │
│      └── Databases   │                          │
│          └── ...     │                          │
└─────────────────────────────────────────────────┘
```

### Actions Courantes

#### 1. Voir les Données d'une Table

```
Clic droit sur table → View/Edit Data → All Rows
```

**Raccourci** : Sélectionner la table et cliquer sur l'icône "View Data"

#### 2. Exécuter une Requête SQL

```
Clic droit sur database → Query Tool
```

Ou : **Tools** → **Query Tool**

**Raccourci clavier** : `Alt + Shift + Q`

#### 3. Filtrer les Données

Dans la vue des données :
- Cliquer sur l'icône **Filter** (entonnoir)
- Choisir la colonne et la condition
- Appliquer

#### 4. Trier les Données

Dans la vue des données :
- Cliquer sur l'en-tête de colonne
- 1 clic : tri croissant
- 2 clics : tri décroissant

#### 5. Exporter des Données

```
Clic droit sur table → Import/Export Data
Ou : Query Tool → File → Export
```

Formats disponibles : CSV, Excel, JSON, etc.

---

## 📊 Requêtes SQL Utiles

### Ouvrir Query Tool

```
Database → Clic droit → Query Tool
```

### Requêtes pour Ton Application

#### 1. Voir Tous les Utilisateurs

```sql
SELECT 
    id,
    username,
    full_name,
    role,
    created_at
FROM "user"
ORDER BY created_at DESC;
```

#### 2. Voir Tous les Dossiers

```sql
SELECT 
    id,
    numero_dossier,
    client_name,
    status,
    affected_agent_id,
    created_at,
    updated_at
FROM dossier
ORDER BY created_at DESC
LIMIT 50;
```

#### 3. Dossiers par Statut

```sql
SELECT 
    status,
    COUNT(*) as nombre
FROM dossier
GROUP BY status
ORDER BY nombre DESC;
```

#### 4. Dossiers d'un Agent Spécifique

```sql
SELECT 
    d.numero_dossier,
    d.client_name,
    d.status,
    u.full_name as agent_name
FROM dossier d
LEFT JOIN "user" u ON d.affected_agent_id = u.id
WHERE u.username = 'nom_agent'
ORDER BY d.created_at DESC;
```

#### 5. Historique des Actions

```sql
SELECT 
    ah.id,
    ah.action_type,
    ah.old_status,
    ah.new_status,
    ah.comment,
    ah.action_date,
    u.full_name as user_name,
    d.numero_dossier
FROM action_history ah
LEFT JOIN "user" u ON ah.user_id = u.id
LEFT JOIN dossier d ON ah.dossier_id = d.id
ORDER BY ah.action_date DESC
LIMIT 100;
```

#### 6. KPIs Rapides

```sql
-- Nombre total de dossiers
SELECT COUNT(*) as total_dossiers FROM dossier;

-- Nombre d'agents
SELECT COUNT(*) as total_agents FROM "user" WHERE role = 'AGENT';

-- Dossiers en attente
SELECT COUNT(*) as dossiers_attente 
FROM dossier 
WHERE status = 'EN_ATTENTE';

-- Dossiers traités aujourd'hui
SELECT COUNT(*) as traites_aujourdhui
FROM dossier
WHERE DATE(updated_at) = CURRENT_DATE
  AND status IN ('VALIDE', 'REJETE');
```

#### 7. Changer le Mot de Passe Admin (en SQL)

```sql
-- Voir le hash actuel
SELECT id, username, password FROM "user" WHERE username = 'admin';

-- Changer le mot de passe nécessite Spring Security BCrypt
-- Mieux vaut le faire via l'application !
```

#### 8. Réinitialiser les Données de Test

```sql
-- ⚠️ ATTENTION : Supprime toutes les données !
-- À utiliser UNIQUEMENT en développement local

TRUNCATE TABLE action_history CASCADE;
TRUNCATE TABLE dossier CASCADE;
DELETE FROM "user" WHERE username != 'admin';

-- Réinitialiser les séquences
ALTER SEQUENCE dossier_id_seq RESTART WITH 1;
ALTER SEQUENCE user_id_seq RESTART WITH 2;
ALTER SEQUENCE action_history_id_seq RESTART WITH 1;
```

---

## 🎨 Personnalisation pgAdmin

### Thème Sombre

1. **File** → **Preferences**
2. **Miscellaneous** → **Themes**
3. Choisir **"Dark"**
4. **Save**

### Affichage des Données

1. **File** → **Preferences**
2. **Query Tool** → **Results grid**
3. Ajuster :
   - Nombre de lignes par page
   - Format de date
   - Format de nombres

### Raccourcis Clavier

| Action | Raccourci |
|--------|-----------|
| Ouvrir Query Tool | `F5` ou `Alt+Shift+Q` |
| Exécuter requête | `F5` |
| Exécuter sélection | `F5` (avec texte sélectionné) |
| Commenter ligne | `Ctrl+/` |
| Auto-complétion | `Ctrl+Space` |
| Historique requêtes | `Ctrl+Shift+H` |

---

## 🔍 Fonctionnalités Avancées

### 1. Visualisation du Schéma (ERD)

```
Clic droit sur database → ERD For Database
```

Affiche un diagramme entité-relation de ta base.

### 2. Sauvegarde de la Base

```
Clic droit sur database → Backup...
```

Options :
- Format : Custom, Tar, Plain SQL
- Nom du fichier : `credit_platform_backup_20250122.backup`
- **Backup**

### 3. Restauration

```
Clic droit sur database → Restore...
```

Sélectionner le fichier de backup et restaurer.

### 4. Monitoring en Temps Réel

```
Dashboard du serveur → Server Activity
```

Voir :
- Connexions actives
- Requêtes en cours
- Locks
- Performance

### 5. Créer un Favori de Requête

```
Dans Query Tool :
1. Écrire ta requête
2. Cliquer sur l'icône ⭐ (Add to Macros)
3. Donner un nom
4. Utiliser : Ctrl+Shift+M pour accéder aux macros
```

---

## 🆘 Troubleshooting

### Problème : Cannot connect to server

**Symptômes** :
```
could not connect to server: Connection refused
```

**Solutions** :
1. Vérifie que PostgreSQL est démarré :
   ```powershell
   # Windows
   Get-Service postgresql*
   
   # Si arrêté
   Start-Service postgresql-x64-15
   ```

2. Vérifie le port (doit être 5432)
3. Vérifie le hostname (localhost pour local)

---

### Problème : Authentication failed

**Symptômes** :
```
FATAL: password authentication failed for user "postgres"
```

**Solutions** :
1. Vérifie le mot de passe PostgreSQL
2. Réinitialise le mot de passe si besoin :
   ```bash
   # Windows (en admin)
   psql -U postgres
   ALTER USER postgres PASSWORD 'nouveau_password';
   ```

---

### Problème : SSL required (Render)

**Symptômes** :
```
connection requires SSL
```

**Solution** :
Dans pgAdmin, onglet **SSL** :
- SSL mode : **Require**

---

### Problème : Database does not exist

**Symptômes** :
```
FATAL: database "credit_platform" does not exist
```

**Solutions** :
1. Créer la base :
   ```sql
   CREATE DATABASE credit_platform;
   ```

2. Ou démarrer l'application Spring Boot (crée les tables auto)

---

### Problème : Connexion Render trop lente

**Cause** : Render Free tier peut avoir du lag

**Solutions** :
1. Utiliser la connexion **Internal** si tu es dans un service Render
2. Passer au plan Starter pour meilleures performances
3. Limiter le nombre de lignes affichées

---

## 📚 Alternatives à pgAdmin

Si pgAdmin ne te convient pas :

### DBeaver (Gratuit, Multi-DB)
- Plus léger
- Interface moderne
- Support MySQL, PostgreSQL, etc.
- https://dbeaver.io/

### TablePlus (Payant, Élégant)
- Interface très propre
- Rapide
- macOS, Windows, Linux
- https://tableplus.com/

### psql (Ligne de Commande)
```bash
# Connexion locale
psql -U postgres -d credit_platform

# Connexion Render
psql postgresql://user:pass@host:5432/credit_platform?sslmode=require

# Requêtes utiles
\dt              # Liste des tables
\d+ tablename    # Détails d'une table
SELECT * FROM "user" LIMIT 10;
```

---

## 🎯 Résumé des Connexions

### Locale (Développement)

```
Host: localhost
Port: 5432
Database: credit_platform
User: postgres
Password: [ton mot de passe local]
SSL: Non requis
```

### Render (Production)

```
Host: [ton-host].render.com
Port: 5432
Database: credit_platform
User: credit_user
Password: [depuis Render Dashboard]
SSL: REQUIS (Require)
```

---

## ✅ Checklist Configuration

- [ ] pgAdmin installé
- [ ] Connexion locale configurée
- [ ] Connexion Render configurée (si déjà déployé)
- [ ] Tables visibles
- [ ] Données visibles
- [ ] Query Tool testé
- [ ] Favoris de requêtes créés (optionnel)

---

## 🚀 Prochaines Étapes

Maintenant que tu peux visualiser tes données :

1. **Explorer les tables** créées par Hibernate
2. **Tester les requêtes SQL** ci-dessus
3. **Créer des vues** personnalisées si besoin
4. **Configurer des sauvegardes** régulières
5. **Monitorer les performances**

---

**Tu peux maintenant visualiser toutes tes données ! 🎉**

Besoin d'aide avec une requête spécifique ? Consulte la section "Requêtes SQL Utiles" !
