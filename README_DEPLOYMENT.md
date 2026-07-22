# 🚀 Déploiement Credit Platform - Vue d'Ensemble

> Configuration complète pour déployer l'application sur **Render** (Backend) + **Vercel** (Frontend)

---

## 📁 Structure des Fichiers de Déploiement

```
credit-platform/
│
├── 🐳 BACKEND (Render)
│   ├── Dockerfile                          # Image Docker optimisée
│   ├── .dockerignore                       # Exclusions build
│   ├── render.yaml                         # Config Infrastructure as Code
│   ├── src/main/resources/
│   │   ├── application.properties         # Config générale
│   │   └── application-prod.properties    # Config production
│   └── pom.xml                            # Dépendances (+ Actuator)
│
├── 🎨 FRONTEND (Vercel)
│   └── frontend/
│       ├── vercel.json                    # Config Vercel
│       ├── .env.development               # Variables dev local
│       ├── .env.production                # Variables production
│       ├── .env.example                   # Template
│       └── src/services/                  # APIs (mis à jour)
│
├── 📚 DOCUMENTATION
│   ├── DEPLOYMENT_GUIDE.md               # Guide complet et détaillé
│   ├── DEPLOY_QUICK_START.md             # Guide rapide 15 min
│   ├── DEPLOYMENT_SUMMARY.md             # Résumé technique
│   ├── NEXT_STEPS.md                     # À faire maintenant ⭐
│   └── README_DEPLOYMENT.md              # Ce fichier
│
└── 🛠️ SCRIPTS
    ├── generate-jwt-secret.ps1           # Génère JWT (Windows)
    ├── generate-jwt-secret.sh            # Génère JWT (Linux/Mac)
    └── pre-deploy-check.ps1              # Vérifie config
```

---

## 🎯 Par Où Commencer ?

### Pour un déploiement rapide (15 min)

```
👉 Lis : NEXT_STEPS.md
```

Ce fichier te guide étape par étape pour déployer MAINTENANT.

### Pour comprendre en détail

```
👉 Lis : DEPLOY_QUICK_START.md (guide 5 étapes)
👉 Ou : DEPLOYMENT_GUIDE.md (guide complet)
```

---

## 🏗️ Architecture de Déploiement

```
                    ┌─────────────────┐
                    │                 │
                    │  UTILISATEURS   │
                    │                 │
                    └────────┬────────┘
                             │
                             │ HTTPS
                             │
           ┌─────────────────┴─────────────────┐
           │                                   │
           │                                   │
    ┌──────▼──────┐                    ┌──────▼──────┐
    │             │                    │             │
    │   VERCEL    │◄───────────────────┤   RENDER    │
    │  (Frontend) │    API Requests    │  (Backend)  │
    │             │                    │             │
    │ React+Vite  │                    │ Spring Boot │
    │   Static    │                    │  + Docker   │
    │             │                    │             │
    └─────────────┘                    └──────┬──────┘
                                              │
                                              │ JDBC
                                              │
                                       ┌──────▼──────┐
                                       │             │
                                       │  MySQL DB   │
                                       │   Render    │
                                       │             │
                                       └─────────────┘
```

---

## ⚙️ Variables d'Environnement

### Backend (Render) - 8 variables

```env
SPRING_DATASOURCE_URL      # jdbc:mysql://host:3306/credit_platform...
DB_USERNAME                # credit_user
DB_PASSWORD                # [secret]
JWT_SECRET                 # [128 caractères hex]
JWT_EXPIRATION             # 7200000 (2h)
CORS_ALLOWED_ORIGINS       # https://your-app.vercel.app,...
SERVER_PORT                # 8081
JAVA_OPTS                  # -Xmx512m -Xms256m -Dspring.profiles.active=prod
```

### Frontend (Vercel) - 1 variable

```env
VITE_API_URL               # https://credit-platform-api.onrender.com
```

---

## 🔧 Commandes Utiles

### Vérifier la configuration

```powershell
# Windows
.\scripts\pre-deploy-check.ps1

# Linux/Mac
./scripts/pre-deploy-check.sh
```

### Générer JWT Secret

```powershell
# Windows
.\scripts\generate-jwt-secret.ps1

# Linux/Mac
./scripts/generate-jwt-secret.sh
```

### Tester localement

```bash
# Backend (nécessite Maven et MySQL local)
./mvnw spring-boot:run

# Frontend
cd frontend
npm install
npm run dev
```

---

## 📊 Checklist de Déploiement

### Préparation
- [ ] Code commité et pushé sur Git
- [ ] JWT Secret généré (128 caractères)
- [ ] Base de données MySQL créée

### Backend (Render)
- [ ] Web Service Docker créé
- [ ] 8 variables d'environnement configurées
- [ ] Build réussi (5-10 min)
- [ ] Health check OK : `/actuator/health`

### Frontend (Vercel)
- [ ] Projet créé avec root directory `frontend`
- [ ] Variable `VITE_API_URL` configurée
- [ ] Build réussi (1-3 min)
- [ ] Site accessible

### Finalisation
- [ ] CORS mis à jour avec URL Vercel réelle
- [ ] Login fonctionne
- [ ] Mot de passe admin changé

---

## 🎯 Étapes Rapides

### 1. Génère le JWT Secret
```powershell
.\scripts\generate-jwt-secret.ps1
```
Copie le résultat ✅

### 2. Crée la Base de Données
- [Render Database](https://dashboard.render.com/) → New MySQL
- Note les infos de connexion ✅

### 3. Déploie le Backend
- [Render](https://dashboard.render.com/) → New Web Service → Docker
- Ajoute les 8 variables d'environnement
- Deploy ✅

### 4. Déploie le Frontend
- [Vercel](https://vercel.com/dashboard) → New Project
- Root Directory: `frontend`
- Ajoute `VITE_API_URL`
- Deploy ✅

### 5. Teste
- Ouvre `https://your-app.vercel.app`
- Login : `admin` / `admin123`
- Change le mot de passe ✅

---

## 🚨 Troubleshooting Rapide

| Problème | Solution |
|----------|----------|
| ❌ Backend "Connection refused" | Vérifie `SPRING_DATASOURCE_URL`, `DB_USERNAME`, `DB_PASSWORD` |
| ❌ Frontend "Network Error" | Vérifie `VITE_API_URL` sur Vercel + CORS sur Render |
| ⏰ Backend lent (60s) | Normal sur plan Free (cold start). Passe au Starter |
| ❌ Build failed Render | Vérifie logs : Render Dashboard → Service → Logs |
| ❌ Build failed Vercel | Vérifie root directory = `frontend` |

---

## 📚 Documentation

| Document | Usage |
|----------|-------|
| **NEXT_STEPS.md** ⭐ | **COMMENCE ICI** - À faire maintenant |
| DEPLOY_QUICK_START.md | Guide rapide 5 étapes (15 min) |
| DEPLOYMENT_GUIDE.md | Guide complet avec tous les détails |
| DEPLOYMENT_SUMMARY.md | Résumé technique des configurations |
| README_DEPLOYMENT.md | Ce fichier - Vue d'ensemble |

---

## 🔗 Liens Utiles

### Plateformes
- [Render Dashboard](https://dashboard.render.com/)
- [Vercel Dashboard](https://vercel.com/dashboard)

### Documentation
- [Render Docs](https://render.com/docs)
- [Vercel Docs](https://vercel.com/docs)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Vite Env Variables](https://vitejs.dev/guide/env-and-mode.html)

### Support
- [Render Community](https://community.render.com/)
- [Vercel Discussions](https://github.com/vercel/vercel/discussions)

---

## 💡 Conseils

### Pour Débuter (Gratuit)
- ✅ Render : Plan **Free**
- ✅ Vercel : Plan **Hobby** (gratuit)
- ✅ Render Database : Plan **Free**
- ⚠️ Accepte le cold start (60s première requête)

### Pour Production (Recommandé)
- 🚀 Render : Plan **Starter** (7$/mois) - Pas de cold start
- 🚀 Vercel : Plan **Hobby** suffit (gratuit)
- 🚀 Render Database : Plan **Starter** (7$/mois) - Backups automatiques

---

## 🎉 C'est Parti !

**Tu es prêt pour le déploiement !**

👉 **Commence maintenant** : Ouvre `NEXT_STEPS.md` et suis les étapes

---

## ⏱️ Temps Estimé

| Étape | Temps |
|-------|-------|
| Préparation (JWT + Git) | 5 min |
| Base de données | 5 min |
| Backend Render | 10 min |
| Frontend Vercel | 5 min |
| Configuration finale | 5 min |
| **TOTAL** | **30 min** |

---

## 📞 Support

En cas de problème :
1. Consulte la section Troubleshooting dans DEPLOY_QUICK_START.md
2. Vérifie les logs (Render Dashboard + Vercel Dashboard)
3. Consulte la documentation officielle
4. Pose une question sur les forums communautaires

---

**Tout est prêt ! Bon déploiement ! 🚀**

*Configuration créée avec Kiro AI*
