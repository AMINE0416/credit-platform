# 🚀 Démarrage Local - Credit Platform

> **Guide pour démarrer l'application en local (développement)**

---

## ✅ Configuration Terminée

Les fichiers `.env` ont été créés avec tes identifiants locaux :

### Backend (`.env`)
```env
DB_USERNAME=postgres
DB_PASSWORD=postgres123
JWT_SECRET=c8f3a92e7d4b1f6a8e3d9c5b2f7a4e1d6b9c3f8a5e2d7b4f1c9e6a3d8f5b2c7a9e4d1f6b3
JWT_EXPIRATION=7200000
CORS_ALLOWED_ORIGINS=http://localhost:*,http://127.0.0.1:*,http://192.168.*.*:*
```

### Frontend (`frontend/.env`)
```env
VITE_API_URL=http://localhost:8081
```

---

## 🔧 Prérequis

Vérifie que tu as :

- [x] PostgreSQL installé et démarré
- [x] Base de données `credit_platform` créée ✅
- [x] Java 17+ installé
- [x] Node.js 18+ installé
- [x] pgAdmin configuré ✅

---

## 🚀 Démarrer l'Application

### 1️⃣ Démarrer le Backend

```powershell
# Depuis la racine du projet
.\mvnw spring-boot:run
```

**Ce qui se passe** :
- Spring Boot se connecte à PostgreSQL
- Crée automatiquement les tables (`user`, `dossier`, `action_history`)
- Insère l'utilisateur admin par défaut
- Démarre sur `http://localhost:8081`

**Attends de voir** :
```
Started CreditPlatformApplication in X seconds
```

---

### 2️⃣ Démarrer le Frontend

**Nouvelle fenêtre PowerShell** :

```powershell
# Aller dans le dossier frontend
cd frontend

# Installer les dépendances (première fois seulement)
npm install

# Démarrer le serveur de développement
npm run dev
```

**Le frontend démarre sur** : `http://localhost:5173`

---

## 🌐 Accéder à l'Application

### Frontend
```
http://localhost:5173
```

### Backend API
```
http://localhost:8081
```

### Swagger UI (Documentation API)
```
http://localhost:8081/swagger-ui.html
```

### Health Check
```
http://localhost:8081/actuator/health
```

---

## 🔐 Connexion Par Défaut

```
Username: admin
Password: admin123
```

⚠️ **Pense à changer le mot de passe après la première connexion !**

---

## 👀 Visualiser les Données dans pgAdmin

1. **Ouvre pgAdmin**
2. **Connecte-toi** à `PostgreSQL 17` (ou ton serveur)
3. **Développe** :
   ```
   PostgreSQL 17
   └── Databases
       └── credit_platform
           └── Schemas
               └── public
                   └── Tables
                       ├── user ✅
                       ├── dossier ✅
                       └── action_history ✅
   ```

4. **Voir les données** :
   - Clic droit sur `user` → **View/Edit Data** → **All Rows**
   - Tu verras l'utilisateur `admin` créé automatiquement

---

## 🛑 Arrêter l'Application

### Backend
Dans la fenêtre PowerShell du backend :
```
Ctrl + C
```

### Frontend
Dans la fenêtre PowerShell du frontend :
```
Ctrl + C
```

---

## 🔄 Redémarrer

Il suffit de relancer :
```powershell
# Backend
.\mvnw spring-boot:run

# Frontend (dans un autre terminal)
cd frontend
npm run dev
```

---

## 📊 Tester l'API avec curl

```powershell
# Health check
curl http://localhost:8081/actuator/health

# Login
curl -X POST http://localhost:8081/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{"username":"admin","password":"admin123"}'

# Liste des dossiers (avec token)
curl http://localhost:8081/api/dossiers `
  -H "Authorization: Bearer [TON_TOKEN]"
```

---

## 🆘 Problèmes Courants

### Backend : "Connection refused"

**Cause** : PostgreSQL n'est pas démarré

**Solution** :
```powershell
# Vérifier le service
Get-Service postgresql*

# Démarrer si arrêté
Start-Service postgresql-x64-17
```

---

### Backend : "Database does not exist"

**Cause** : La base `credit_platform` n'existe pas

**Solution** :
```powershell
psql -U postgres
CREATE DATABASE credit_platform;
\q
```

---

### Frontend : "Network Error"

**Cause** : Le backend n'est pas démarré ou `VITE_API_URL` incorrect

**Solution** :
1. Vérifie que le backend tourne sur `http://localhost:8081`
2. Vérifie `frontend/.env` : `VITE_API_URL=http://localhost:8081`
3. Redémarre le frontend

---

### Port déjà utilisé

**Backend (8081)** :
```powershell
# Voir quel processus utilise le port
netstat -ano | findstr :8081

# Tuer le processus (remplace PID)
taskkill /PID [PID] /F
```

**Frontend (5173)** :
```powershell
# Voir quel processus utilise le port
netstat -ano | findstr :5173

# Tuer le processus
taskkill /PID [PID] /F
```

---

## 📝 Variables d'Environnement

### Backend (`.env`)

| Variable | Valeur | Description |
|----------|--------|-------------|
| `DB_USERNAME` | `postgres` | Username PostgreSQL |
| `DB_PASSWORD` | `postgres123` | Password PostgreSQL |
| `JWT_SECRET` | `c8f3a92e...` | Secret JWT (64 bytes) |
| `JWT_EXPIRATION` | `7200000` | 2 heures en ms |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:*,...` | URLs autorisées |

### Frontend (`frontend/.env`)

| Variable | Valeur | Description |
|----------|--------|-------------|
| `VITE_API_URL` | `http://localhost:8081` | URL de l'API backend |

---

## 🔧 Commandes Utiles

### Maven

```powershell
# Compiler
.\mvnw clean compile

# Tester
.\mvnw test

# Packager
.\mvnw clean package

# Démarrer
.\mvnw spring-boot:run

# Nettoyer
.\mvnw clean
```

### NPM (Frontend)

```bash
# Installer dépendances
npm install

# Démarrer dev
npm run dev

# Builder pour production
npm run build

# Preview du build
npm run preview

# Linter
npm run lint
```

---

## 📚 Documentation

- **pgAdmin** : [GUIDE_PGADMIN.md](./GUIDE_PGADMIN.md)
- **Requêtes SQL** : [scripts/requetes_utiles_postgresql.sql](./scripts/requetes_utiles_postgresql.sql)
- **Déploiement** : [TOUT_EST_PRET.md](./TOUT_EST_PRET.md)

---

## ✅ Checklist de Démarrage

- [ ] PostgreSQL démarré
- [ ] Base `credit_platform` créée
- [ ] Fichiers `.env` créés
- [ ] Backend démarré (`.\mvnw spring-boot:run`)
- [ ] Tables créées automatiquement
- [ ] Frontend démarré (`npm run dev`)
- [ ] Connexion réussie avec admin/admin123
- [ ] pgAdmin configuré pour visualiser les données

---

## 🎯 Workflow de Développement

1. **Démarre PostgreSQL** (si pas déjà démarré)
2. **Démarre le backend** : `.\mvnw spring-boot:run`
3. **Démarre le frontend** : `cd frontend && npm run dev`
4. **Ouvre le navigateur** : `http://localhost:5173`
5. **Développe** et les changements se rechargent automatiquement
6. **Visualise les données** dans pgAdmin si besoin

---

**Tu es prêt à développer ! 🚀**

**Bon développement !** 💻
