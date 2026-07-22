# Guide de Déploiement - Credit Platform

Ce guide détaille le processus de déploiement de l'application Credit Platform sur **Render** (backend) et **Vercel** (frontend).

## 📋 Prérequis

- Compte [Render](https://render.com) (gratuit ou payant)
- Compte [Vercel](https://vercel.com) (gratuit ou payant)
- Base de données MySQL accessible depuis Internet (Render Database, PlanetScale, AWS RDS, etc.)
- Repository Git (GitHub, GitLab, Bitbucket)

---

## 🚀 Étape 1 : Préparer la Base de Données

### Option A : Utiliser Render Database (MySQL)

1. Connectez-vous à [Render Dashboard](https://dashboard.render.com/)
2. Cliquez sur **"New +"** → **"MySQL"**
3. Configurez :
   - **Name** : `credit-platform-db`
   - **Database** : `credit_platform`
   - **User** : `credit_user`
   - **Region** : Choisir la région la plus proche
   - **Plan** : Free (ou Starter pour plus de performances)
4. Cliquez sur **"Create Database"**
5. Notez les informations de connexion :
   - **Internal Database URL** (pour services Render)
   - **External Database URL** (si besoin d'accès externe)
   - **Username**
   - **Password**

### Option B : Utiliser une base de données externe

Si vous utilisez AWS RDS, PlanetScale, ou autre :
1. Créez une base de données MySQL
2. Créez un utilisateur avec les privilèges nécessaires
3. Autorisez les connexions depuis les IPs de Render
4. Notez l'URL de connexion JDBC

---

## 🔧 Étape 2 : Déployer le Backend sur Render

### 2.1 Préparer le Repository

1. Commitez tous les fichiers de configuration :
```bash
git add .
git commit -m "Add deployment configuration for Render and Vercel"
git push origin main
```

### 2.2 Créer le Web Service sur Render

1. Connectez-vous à [Render Dashboard](https://dashboard.render.com/)
2. Cliquez sur **"New +"** → **"Web Service"**
3. Connectez votre repository Git
4. Configurez le service :
   - **Name** : `credit-platform-api`
   - **Region** : Choisir la même région que la base de données
   - **Branch** : `main`
   - **Runtime** : `Docker`
   - **Plan** : Free (ou Starter pour éviter le cold start)

### 2.3 Configurer les Variables d'Environnement

Dans l'onglet **"Environment"** du service Render, ajoutez :

#### Variables Obligatoires

| Variable | Valeur | Description |
|----------|--------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://HOST:PORT/credit_platform?useSSL=true&serverTimezone=UTC` | URL JDBC de votre base MySQL |
| `DB_USERNAME` | `credit_user` | Nom d'utilisateur DB |
| `DB_PASSWORD` | `********` | Mot de passe DB (cliquer sur 🔒 pour le masquer) |
| `JWT_SECRET` | Générer avec `openssl rand -hex 64` | Secret JWT (au moins 64 caractères) |
| `JWT_EXPIRATION` | `7200000` | Durée de validité du token (2h) |
| `CORS_ALLOWED_ORIGINS` | `https://your-app.vercel.app,https://your-app-*.vercel.app` | URLs frontend autorisées |
| `SERVER_PORT` | `8081` | Port du serveur |
| `JAVA_OPTS` | `-Xmx512m -Xms256m -Dspring.profiles.active=prod` | Options JVM |

#### Exemple de SPRING_DATASOURCE_URL

Pour Render Database :
```
jdbc:mysql://dpg-xxxxx-a.oregon-postgres.render.com:3306/credit_platform?useSSL=true&serverTimezone=UTC
```

### 2.4 Générer un JWT_SECRET sécurisé

Sur votre machine locale, exécutez :
```bash
# Linux/Mac
openssl rand -hex 64

# Windows PowerShell
-join ((1..64) | ForEach-Object { '{0:X2}' -f (Get-Random -Maximum 256) })
```

Copiez le résultat (128 caractères) dans la variable `JWT_SECRET`.

### 2.5 Déployer

1. Cliquez sur **"Create Web Service"**
2. Render va :
   - Cloner votre repository
   - Builder l'image Docker
   - Démarrer le service
3. Le premier déploiement prend 5-10 minutes
4. Notez l'URL du service : `https://credit-platform-api.onrender.com`

### 2.6 Vérifier le Déploiement

1. Testez le health check :
   ```
   https://credit-platform-api.onrender.com/actuator/health
   ```
   Devrait retourner : `{"status":"UP"}`

2. Testez l'API :
   ```
   https://credit-platform-api.onrender.com/api/auth/login
   ```

3. Testez Swagger (si activé) :
   ```
   https://credit-platform-api.onrender.com/swagger-ui.html
   ```

---

## 🎨 Étape 3 : Déployer le Frontend sur Vercel

### 3.1 Préparer le Repository

Assurez-vous que les fichiers suivants sont bien présents :
- `frontend/vercel.json`
- `frontend/.env.production`
- `frontend/.env.example`

### 3.2 Créer le Projet sur Vercel

#### Option A : Via l'Interface Web

1. Connectez-vous à [Vercel Dashboard](https://vercel.com/dashboard)
2. Cliquez sur **"Add New..."** → **"Project"**
3. Importez votre repository Git
4. Configurez le projet :
   - **Framework Preset** : Vite
   - **Root Directory** : `frontend`
   - **Build Command** : `npm run build`
   - **Output Directory** : `dist`
   - **Install Command** : `npm install`

#### Option B : Via Vercel CLI

```bash
# Installer Vercel CLI
npm install -g vercel

# Se connecter
vercel login

# Depuis le dossier frontend
cd frontend
vercel

# Suivre les instructions
```

### 3.3 Configurer les Variables d'Environnement

Dans les **Project Settings** → **Environment Variables**, ajoutez :

| Variable | Valeur | Environnements |
|----------|--------|----------------|
| `VITE_API_URL` | `https://credit-platform-api.onrender.com` | Production, Preview |

**Important** : N'incluez PAS le trailing slash `/` à la fin de l'URL.

### 3.4 Déployer

1. Cliquez sur **"Deploy"**
2. Vercel va :
   - Installer les dépendances
   - Builder le projet avec Vite
   - Déployer sur le CDN global
3. Le déploiement prend 1-3 minutes
4. Notez l'URL de production : `https://your-app.vercel.app`

### 3.5 Configurer le Domaine Personnalisé (Optionnel)

1. Dans **Project Settings** → **Domains**
2. Ajoutez votre domaine : `app.votredomaine.com`
3. Configurez les DNS selon les instructions Vercel
4. Mettez à jour `CORS_ALLOWED_ORIGINS` sur Render avec votre nouveau domaine

---

## 🔄 Étape 4 : Mettre à Jour les Configurations CORS

### 4.1 Obtenir toutes les URLs Vercel

Après le déploiement sur Vercel, vous aurez plusieurs URLs :
- URL de production : `https://your-app.vercel.app`
- URLs de preview : `https://your-app-git-branch.vercel.app`
- URLs automatiques : `https://your-app-xxxxx.vercel.app`

### 4.2 Mettre à Jour CORS sur Render

Retournez sur Render Dashboard → votre service → **Environment** et mettez à jour :

```
CORS_ALLOWED_ORIGINS=https://your-app.vercel.app,https://your-app-*.vercel.app,https://your-app-git-*.vercel.app
```

Le pattern `*` permet d'autoriser toutes les branches de preview automatiquement.

### 4.3 Redémarrer le Service

Cliquez sur **"Manual Deploy"** → **"Deploy latest commit"** pour appliquer les changements.

---

## 🎯 Étape 5 : Initialiser la Base de Données

### 5.1 Activer l'Auto-création des Tables

Au premier démarrage, Spring Boot créera automatiquement les tables grâce à `spring.jpa.hibernate.ddl-auto=update`.

### 5.2 Créer le Compte Administrateur

L'utilisateur admin par défaut sera créé automatiquement par `DataInitializer.java` :
- **Username** : `admin`
- **Password** : `admin123` (à changer immédiatement !)

### 5.3 Se Connecter et Changer le Mot de Passe

1. Allez sur `https://your-app.vercel.app/login`
2. Connectez-vous avec `admin` / `admin123`
3. Allez dans les paramètres du profil
4. Changez immédiatement le mot de passe

### 5.4 Passer en Mode Production (Recommandé)

Une fois les tables créées, pour plus de sécurité, modifiez sur Render :

Dans `application-prod.properties`, changez :
```properties
spring.jpa.hibernate.ddl-auto=validate
```

Cela empêchera toute modification accidentelle de la structure de la base.

---

## 📊 Étape 6 : Monitoring et Logs

### 6.1 Logs Backend (Render)

1. Dashboard Render → votre service → **Logs**
2. Activez **"Auto-scroll"** pour voir les logs en temps réel
3. Utilisez les filtres pour rechercher des erreurs

### 6.2 Logs Frontend (Vercel)

1. Dashboard Vercel → votre projet → **Deployments**
2. Cliquez sur un déploiement → **Logs**
3. Consultez aussi **Analytics** pour les métriques de performance

### 6.3 Monitoring de la Base de Données

1. Dashboard Render → votre database → **Metrics**
2. Surveillez :
   - CPU usage
   - Memory usage
   - Connections actives

---

## ⚠️ Troubleshooting

### Backend ne démarre pas

**Symptômes** : Erreur de connexion à la base de données

**Solutions** :
1. Vérifiez `SPRING_DATASOURCE_URL` (format JDBC correct)
2. Vérifiez `DB_USERNAME` et `DB_PASSWORD`
3. Vérifiez que la base de données est accessible depuis Render
4. Consultez les logs : Dashboard → Service → Logs

### Frontend ne peut pas contacter l'API

**Symptômes** : Erreur CORS ou Network Error

**Solutions** :
1. Vérifiez que `VITE_API_URL` est correct sur Vercel (sans trailing slash)
2. Vérifiez que l'URL Vercel est dans `CORS_ALLOWED_ORIGINS` sur Render
3. Testez l'API directement : `https://your-api.onrender.com/actuator/health`
4. Vérifiez la console du navigateur (F12)

### Cold Start sur Render (Plan Free)

**Symptômes** : Première requête très lente (30-60s)

**Cause** : Le plan Free met le service en veille après 15 minutes d'inactivité.

**Solutions** :
1. Passer au plan Starter (7$/mois, pas de cold start)
2. Utiliser un service de "ping" pour garder l'app active
3. Ajouter un loading indicator sur le frontend

### Build Timeout sur Render

**Symptômes** : Build échoue après 15 minutes

**Solutions** :
1. Vérifier que Maven utilise bien le cache des dépendances
2. Passer au plan Starter pour augmenter le timeout
3. Optimiser le Dockerfile (multi-stage build déjà configuré)

---

## 🔒 Sécurité et Bonnes Pratiques

### Backend

✅ **À faire** :
- [ ] Changer le mot de passe admin par défaut
- [ ] Générer un `JWT_SECRET` unique et sécurisé (64+ caractères)
- [ ] Utiliser `spring.jpa.hibernate.ddl-auto=validate` en production
- [ ] Activer HTTPS only (déjà fait par Render)
- [ ] Limiter les origines CORS aux domaines de production uniquement
- [ ] Désactiver Swagger en production (optionnel)

### Frontend

✅ **À faire** :
- [ ] Vérifier que `VITE_API_URL` pointe bien vers HTTPS
- [ ] Activer les headers de sécurité (déjà configurés dans vercel.json)
- [ ] Configurer un domaine personnalisé avec certificat SSL
- [ ] Activer Vercel Analytics pour le monitoring

### Base de Données

✅ **À faire** :
- [ ] Activer les backups automatiques (selon votre provider)
- [ ] Utiliser des connexions SSL/TLS
- [ ] Limiter les accès réseau (whitelist IPs si possible)
- [ ] Surveiller les connexions actives

---

## 🔄 Mises à Jour et CI/CD

### Déploiement Automatique

Les deux plateformes supportent le déploiement automatique :

#### Render
- Push sur `main` → déploiement automatique du backend
- Configurable dans : Service Settings → Build & Deploy → Auto-Deploy

#### Vercel
- Push sur n'importe quelle branche → déploiement de preview
- Push sur `main` → déploiement en production
- Configurable dans : Project Settings → Git

### Workflow Recommandé

1. Développement local sur une branche `feature/ma-feature`
2. Push → Vercel crée un preview automatiquement
3. Review et tests
4. Merge dans `main` → déploiement en production

---

## 📞 Support

### Documentation

- [Render Docs](https://render.com/docs)
- [Vercel Docs](https://vercel.com/docs)
- [Spring Boot Docs](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Vite Docs](https://vitejs.dev/)

### Communauté

- Render Community : https://community.render.com/
- Vercel Discussions : https://github.com/vercel/vercel/discussions

---

## ✅ Checklist de Déploiement

### Backend (Render)

- [ ] Base de données MySQL créée et accessible
- [ ] Repository Git poussé avec Dockerfile
- [ ] Web Service créé sur Render
- [ ] Toutes les variables d'environnement configurées
- [ ] `JWT_SECRET` généré et sécurisé
- [ ] Déploiement réussi
- [ ] Health check accessible : `/actuator/health`
- [ ] API accessible et répond correctement

### Frontend (Vercel)

- [ ] Repository Git poussé avec configuration Vercel
- [ ] Projet créé sur Vercel
- [ ] `VITE_API_URL` configurée correctement
- [ ] Déploiement réussi
- [ ] Frontend accessible et affiche correctement
- [ ] Login fonctionne avec l'API

### Configuration Finale

- [ ] CORS configuré avec les URLs Vercel
- [ ] Compte admin créé et mot de passe changé
- [ ] Tests de bout en bout effectués
- [ ] Monitoring activé
- [ ] Domaine personnalisé configuré (optionnel)

---

## 🎉 Félicitations !

Votre application Credit Platform est maintenant déployée et accessible en ligne !

- Backend : `https://credit-platform-api.onrender.com`
- Frontend : `https://your-app.vercel.app`

N'oubliez pas de surveiller les logs et les performances régulièrement.
