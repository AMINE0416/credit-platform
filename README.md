# Credit Platform - Plateforme de Gestion de Crédits

Application web complète pour la gestion de dossiers de crédit avec authentification, gestion d'utilisateurs, et tableau de bord.

## 🏗️ Architecture

- **Backend** : Spring Boot 4.1.0 + MySQL + JWT
- **Frontend** : React 19 + Vite + TypeScript + React Router
- **Déploiement** : Render (Backend) + Vercel (Frontend)

## 🚀 Déploiement en Production

**Tous les fichiers de configuration sont prêts !**

👉 **Pour déployer maintenant** : Ouvre **[NEXT_STEPS.md](./NEXT_STEPS.md)**

### Documentation de Déploiement

| Document | Description |
|----------|-------------|
| **[NEXT_STEPS.md](./NEXT_STEPS.md)** ⭐ | **COMMENCE ICI** - Étapes à faire maintenant |
| [README_DEPLOYMENT.md](./README_DEPLOYMENT.md) | Vue d'ensemble du déploiement |
| [DEPLOY_QUICK_START.md](./DEPLOY_QUICK_START.md) | Guide rapide en 5 étapes (15 min) |
| [DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md) | Guide complet et détaillé |
| [DEPLOYMENT_SUMMARY.md](./DEPLOYMENT_SUMMARY.md) | Résumé technique |

### Fichiers de Configuration Créés

#### Backend (Render)
- ✅ `Dockerfile` - Image Docker multi-stage
- ✅ `.dockerignore`
- ✅ `render.yaml`
- ✅ `src/main/resources/application-prod.properties`

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
- MySQL 8+
- Maven 3.9+

### Backend

```bash
# Créer la base de données
mysql -u root -p
CREATE DATABASE credit_platform;

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

**Prêt pour le déploiement ?** 👉 Commence par [NEXT_STEPS.md](./NEXT_STEPS.md)