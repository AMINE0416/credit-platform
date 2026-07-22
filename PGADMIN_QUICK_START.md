# ⚡ pgAdmin - Démarrage Rapide

> **Guide express pour visualiser tes données en 5 minutes**

## 📁 Fichier SQL Prêt à l'Emploi

**IMPORTANT** : Un fichier avec toutes les requêtes PostgreSQL est disponible :
→ **`scripts/visualisation_pgadmin.sql`**

Ce fichier contient toutes les requêtes adaptées pour visualiser vos données dans pgAdmin.

---

## 🚀 Installation (2 min)

### Windows
```
1. Télécharger : https://www.pgadmin.org/download/
2. Installer pgadmin4-x.x-x64.exe
3. Lancer pgAdmin 4 (s'ouvre dans le navigateur)
```

### Avec Chocolatey
```powershell
choco install pgadmin4
```

---

## 🔌 Connexion Locale (2 min)

1. **Ouvrir pgAdmin** → Master Password (première fois)

2. **Clic droit "Servers"** → Register → Server

3. **Remplir** :

   **General** :
   ```
   Name: Credit Platform Local
   ```

   **Connection** :
   ```
   Host: localhost
   Port: 5432
   Database: credit_platform
   Username: postgres
   Password: [ton mot de passe]
   ☑ Save password
   ```

4. **Save** → ✅ Connecté !

---

## 🌐 Connexion Render (Production)

### Récupérer les Infos

1. [Render Dashboard](https://dashboard.render.com/)
2. Cliquer sur ta base PostgreSQL
3. Noter :
   - Hostname (External)
   - Port (5432)
   - Database
   - Username
   - Password

### Configurer dans pgAdmin

**General** :
```
Name: Credit Platform Render
```

**Connection** :
```
Host: [ton-host].render.com
Port: 5432
Database: credit_platform
Username: credit_user
Password: [depuis Render]
☑ Save password
```

**SSL** :
```
SSL mode: Require ← IMPORTANT !
```

**Save** → ✅ Connecté !

---

## 👀 Voir les Données (1 min)

### Navigation
```
Servers
└── Credit Platform Local
    └── Databases
        └── credit_platform
            └── Schemas
                └── public
                    └── Tables
                        ├── dossier
                        ├── user
                        └── action_history
```

### Afficher les Données
```
Clic droit sur une table → View/Edit Data → All Rows
```

---

## 🔍 Requêtes Rapides

### Ouvrir Query Tool
```
Clic droit sur database → Query Tool
```

### Exemples de Requêtes

#### Voir les utilisateurs
```sql
SELECT * FROM "user" ORDER BY created_at DESC;
```

#### Voir les dossiers
```sql
SELECT * FROM dossier ORDER BY created_at DESC LIMIT 50;
```

#### Dossiers par statut
```sql
SELECT status, COUNT(*) as nombre
FROM dossier
GROUP BY status;
```

#### Dossiers d'un agent
```sql
SELECT d.*, u.full_name as agent
FROM dossier d
LEFT JOIN "user" u ON d.affected_agent_id = u.id
WHERE u.username = 'agent1';
```

---

## 📊 Interface Rapide

### Fenêtre Principale
```
┌─────────────────────────────────────┐
│ Browser │ Dashboard / Query Tool    │
│         │                           │
│ Servers │  ← Affichage principal    │
│ └─DB    │                           │
│   └─...│                           │
└─────────────────────────────────────┘
```

### Raccourcis Utiles

| Action | Raccourci |
|--------|-----------|
| Query Tool | `Alt+Shift+Q` |
| Exécuter SQL | `F5` |
| Commenter | `Ctrl+/` |
| Auto-complétion | `Ctrl+Space` |

---

## 🎯 Actions Courantes

### Filtrer des Données
```
Vue des données → Icône Filter (entonnoir)
```

### Trier des Données
```
Cliquer sur l'en-tête de colonne
```

### Exporter en CSV/Excel
```
Query Tool → File → Export
```

### Thème Sombre
```
File → Preferences → Miscellaneous → Themes → Dark
```

---

## 🆘 Problèmes Courants

### "Connection refused"
```
✅ Vérifie que PostgreSQL est démarré
✅ Vérifie le port (5432)
✅ Vérifie le hostname (localhost)
```

### "Authentication failed"
```
✅ Vérifie le mot de passe
✅ Vérifie le username
```

### "SSL required" (Render)
```
✅ Dans pgAdmin : SSL mode → Require
```

### "Database does not exist"
```
✅ Crée la base :
   psql -U postgres
   CREATE DATABASE credit_platform;
```

---

## 📚 Fichiers Utiles

| Fichier | Usage |
|---------|-------|
| **GUIDE_PGADMIN.md** | Guide complet détaillé |
| **scripts/requetes_utiles_postgresql.sql** | 50+ requêtes SQL prêtes |
| **VISUALISATION_DONNEES.md** | Ancien guide (MySQL) |

---

## 🎓 Pour Aller Plus Loin

### Guide Complet
```
→ Ouvre : GUIDE_PGADMIN.md
```

### Requêtes Avancées
```
→ Ouvre : scripts/requetes_utiles_postgresql.sql
```

### Documentation Officielle
```
→ https://www.pgadmin.org/docs/
```

---

## ✅ Checklist Installation

- [ ] pgAdmin installé
- [ ] Connexion locale OK
- [ ] Tables visibles
- [ ] Query Tool testé
- [ ] Données visibles
- [ ] Connexion Render OK (si déployé)

---

**Tu peux maintenant visualiser toutes tes données ! 🎉**

**Besoin de plus de détails ?** → Consulte **GUIDE_PGADMIN.md**
