# Credit Platform - Plateforme de Gestion de Crédits

Application web complète pour la gestion de dossiers de crédit avec authentification, gestion d'utilisateurs, et tableau de bord.

## 🏗️ Architecture

- **Backend** : Spring Boot 4.1.0 + PostgreSQL + JWT
- **Frontend** : React 19 + Vite + TypeScript + React Router
- **Déploiement** : Railway (Backend + PostgreSQL) ou Render + Vercel (Frontend)

## 🚀 Déploiement en Production (GRATUIT)

**Tous les fichiers de configuration sont prêts !**

### 🚂 Option 1 : Railway (⭐ RECOMMANDÉ - Gratuit sans CB)

👉 **[COMMENCER_ICI.md](./COMMENCER_ICI.md)** - Guide simple de démarrage  
👉 **[RAILWAY_QUICK_START.md](./RAILWAY_QUICK_START.md)** - Déploiement en 10 min

**Pourquoi Railway ?**
- ✅ Totalement gratuit (500h/mois)
- ✅ Pas de CB requise
- ✅ PostgreSQL inclus
- ✅ Simple et rapide

### 🎨 Option 2 : Render (CB maintenant requise)

👉 **[DEPLOYER_MAINTENANT.md](./DEPLOYER_MAINTENANT.md)** - Guide Render

### ⚖️ Pas sûr ?

👉 **[COMPARAISON_PLATEFORMES.md](./COMPARAISON_PLATEFORMES.md)** - Comparer les options

### Documentation de Déploiement

| Document | Description |
|----------|-------------|
| **[COMMENCER_ICI.md](./COMMENCER_ICI.md)** ⭐ | **COMMENCE ICI** - Guide de démarrage simple |
| **[RAILWAY_QUICK_START.md](./RAILWAY_QUICK_START.md)** ⭐ | Railway - Déploiement gratuit en 10 min |
| **[DEPLOYMENT_RAILWAY.md](./DEPLOYMENT_RAILWAY.md)** | Railway - Guide complet |
| **[COMPARAISON_PLATEFORMES.md](./COMPARAISON_PLATEFORMES.md)** | Comparer Railway vs Render vs autres |
| [DEPLOYER_MAINTENANT.md](./DEPLOYER_MAINTENANT.md) | Render - Déploiement rapide |
| [NEXT_STEPS.md](./NEXT_STEPS.md) | Render - Étapes détaillées |
| [DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md) | Guide de référence complet |
| [INDEX_DOCUMENTATION.md](./INDEX_DOCUMENTATION.md) | Navigation dans toute la doc |

### Fichiers de Configuration Créés

#### Backend (Railway / Render)
- ✅ `Dockerfile` - Image Docker multi-stage
- ✅ `.dockerignore`
- ✅ `render.yaml` - Configuration Render
- ✅ `src/main/resources/application-prod.properties` - Supporte Railway et Render

#### Frontend (Vercel)
- ✅ `frontend/vercel.json`
- ✅ `frontend/.env.development`
- ✅ `frontend/.env.production`

#### Scripts Utilitaires
- ✅ `scripts/generate-jwt-secret.ps1` - Génère un JWT secret
- ✅ `scripts/pre-deploy-check.ps1` - Vérifie la config

## 💻 Développement Local

### Prérequis

- Java 17+
- Node.js 18+
- PostgreSQL 14+ (ou MySQL 8+ si tu préfères)
- Maven 3.9+

### Backend

```bash
# Créer la base de données PostgreSQL
psql -U postgres
CREATE DATABASE credit_platform;
\q

# Ou avec MySQL (si tu préfères)
mysql -u root -p
CREATE DATABASE credit_platform;
exit

# Configurer les variables d'environnement (optionnel)
cp .env.example .env
# Éditer .env avec tes valeurs

# Démarrer le backend
./mvnw spring-boot:run
# Accessible sur http://localhost:8081
```

### Frontend

```bash
cd frontend

# Installer les dépendances
npm install

# Démarrer le serveur de développement
npm run dev
# Accessible sur http://localhost:5173
```

### Connexion par Défaut

- **Username** : `admin`
- **Password** : `admin123`
- ⚠️ **À changer en production !**

## 📚 Fonctionnalités

- ✅ Authentification JWT
- ✅ Gestion des utilisateurs (Admin/Agent)
- ✅ Gestion des dossiers de crédit
- ✅ Tableau de bord avec KPIs
- ✅ Import Excel
- ✅ Export de données
- ✅ Machine d'états pour les dossiers
- ✅ Historique des actions
- ✅ CORS configurable

## 🔒 Sécurité

- ✅ Authentification JWT
- ✅ CORS configurable via variables d'environnement
- ✅ Mots de passe hashés (BCrypt)
- ✅ Validation des entrées
- ✅ Protection CSRF
- ✅ HTTPS en production

## 📊 API Documentation

Une fois l'application démarrée, accède à Swagger UI :

```
http://localhost:8081/swagger-ui.html
```

En production :
```
https://your-api.onrender.com/swagger-ui.html
```

## 🧪 Tests

```bash
# Backend
./mvnw test

# Frontend
cd frontend
npm run test
```

## 📝 Documentation Technique

- [AMELIORATIONS_CODE_QUALITY.md](./AMELIORATIONS_CODE_QUALITY.md) - Améliorations code
- [CHANGELOG_AUDIT.md](./CHANGELOG_AUDIT.md) - Historique des changements
- [DOCUMENTATION_AUDIT.md](./DOCUMENTATION_AUDIT.md) - Documentation audit
- [RESUME_AUDIT_SECURITE.md](./RESUME_AUDIT_SECURITE.md) - Audit sécurité
- [CORS_EXTERNALISATION.md](./CORS_EXTERNALISATION.md) - Configuration CORS

## 🛠️ Technologies

### Backend
- Spring Boot 4.1.0
- Spring Security
- Spring Data JPA
- MySQL
- JWT (jjwt 0.12.5)
- Apache POI (Excel)
- Lombok
- Springdoc OpenAPI

### Frontend
- React 19
- TypeScript
- Vite
- React Router 7
- Axios
- React DatePicker
- date-fns

## 📦 Structure du Projet

```
credit-platform/
├── src/main/java/org/eqdom/creditplatform/  # Backend Java
│   ├── config/                               # Configurations
│   ├── controller/                           # Controllers REST
│   ├── dto/                                  # Data Transfer Objects
│   ├── entity/                               # Entités JPA
│   ├── repository/                           # Repositories
│   ├── service/                              # Services métier
│   ├── security/                             # Sécurité JWT
│   └── exception/                            # Gestion erreurs
├── frontend/                                 # Frontend React
│   ├── src/
│   │   ├── components/                       # Composants
│   │   ├── pages/                            # Pages
│   │   ├── services/                         # Services API
│   │   └── layouts/                          # Layouts
└── scripts/                                  # Scripts SQL et utilitaires
```

## 🤝 Contribution

1. Fork le projet
2. Crée une branche feature (`git checkout -b feature/AmazingFeature`)
3. Commit tes changements (`git commit -m 'Add AmazingFeature'`)
4. Push sur la branche (`git push origin feature/AmazingFeature`)
5. Ouvre une Pull Request

## 📄 Licence

Ce projet est sous licence privée - tous droits réservés.

## 👥 Auteurs

- **Équipe EQDOM** - Développement initial

## 🆘 Support

Pour toute question ou problème :
1. Consulte la documentation
2. Vérifie les logs
3. Contacte l'équipe de développement

---

**Prêt pour le déploiement ?** 👉 Commence par [COMMENCER_ICI.md](./COMMENCER_ICI.md) ou [RAILWAY_QUICK_START.md](./RAILWAY_QUICK_START.md)