# 🚂 Déploiement sur Railway - Guide Complet

> **Déployez votre application en 15 minutes sur Railway.app (gratuit, sans CB)**

Railway offre 500 heures gratuites par mois - largement suffisant pour un projet en production !

---

## 📋 Ce Dont Vous Avez Besoin

- [ ] Compte [Railway](https://railway.app/) (gratuit, connexion via GitHub)
- [ ] Compte [Vercel](https://vercel.com/) (gratuit, connexion via GitHub)
- [ ] Code pushé sur GitHub
- [ ] 15 minutes de votre temps ⏱️

---

## 🎯 Architecture de Déploiement

```
┌─────────────────────────────────────────────┐
│  Frontend (Vercel)                          │
│  https://votre-app.vercel.app              │
└──────────────┬──────────────────────────────┘
               │ API calls
               ▼
┌─────────────────────────────────────────────┐
│  Backend (Railway)                          │
│  https://votre-app.up.railway.app          │
│  - Spring Boot API (Docker)                 │
│  - Health checks: /actuator/health          │
└──────────────┬──────────────────────────────┘
               │ Database connection
               ▼
┌─────────────────────────────────────────────┐
│  PostgreSQL (Railway)                       │
│  - Base de données managée                  │
│  - Backups automatiques                     │
└─────────────────────────────────────────────┘
```

---

## 🚀 ÉTAPE 1 : Préparation (3 min)

### 1.1 Générer le JWT Secret

```powershell
cd c:\Users\asus\Documents\credit-platform\scripts
.\generate-jwt-secret.ps1
```

**📝 IMPORTANT :** Copiez le secret généré (128 caractères) dans un fichier texte.

### 1.2 Vérifier Git

```bash
cd c:\Users\asus\Documents\credit-platform
git status
git add .
git commit -m "Configuration pour Railway"
git push origin main
```

---

## 🗄️ ÉTAPE 2 : Créer la Base de Données PostgreSQL (2 min)

1. **Allez sur** https://railway.app/
2. **Connectez-vous** avec GitHub
3. **Cliquez** sur "New Project"
4. **Sélectionnez** "Provision PostgreSQL"
5. **Attendez** 30 secondes → Base créée ! ✅

### 2.1 Récupérer les Informations de Connexion

1. Cliquez sur votre base PostgreSQL
2. Allez dans l'onglet **"Variables"**
3. Vous verrez automatiquement ces variables :
   - `DATABASE_URL` (URL complète PostgreSQL)
   - `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD`, `PGDATABASE`

**📝 NOTE :** Railway génère automatiquement `DATABASE_URL` - vous n'avez rien à copier pour l'instant !

---

## 🔧 ÉTAPE 3 : Déployer le Backend (5 min)

### 3.1 Créer le Service Backend

1. Dans votre projet Railway, cliquez **"+ New"**
2. Sélectionnez **"GitHub Repo"**
3. **Autorisez** Railway à accéder à vos repos (si première fois)
4. **Sélectionnez** votre repository `credit-platform`
5. Cliquez **"Deploy Now"**

### 3.2 Configurer les Variables d'Environnement

1. Cliquez sur votre service backend
2. Allez dans l'onglet **"Variables"**
3. Cliquez **"+ New Variable"** et ajoutez :

```bash
# Port (Railway utilise PORT automatiquement)
PORT=8081

# JWT Configuration
JWT_SECRET=[VOTRE_SECRET_DE_L_ETAPE_1]
JWT_EXPIRATION=7200000

# CORS (on mettra l'URL Vercel plus tard)
CORS_ALLOWED_ORIGINS=https://*.vercel.app

# Spring Profile
SPRING_PROFILES_ACTIVE=prod

# Java Options
JAVA_OPTS=-Xmx512m -Xms256m
```

### 3.3 Lier la Base de Données

1. Dans votre service backend, cliquez sur l'onglet **"Settings"**
2. Scrollez jusqu'à **"Service Variables"**
3. Cliquez **"+ Variable Reference"**
4. Sélectionnez votre base PostgreSQL
5. Choisissez **`DATABASE_URL`**
6. Railway créera automatiquement la variable `DATABASE_URL` dans votre backend ! 🎉

### 3.4 Configurer le Déploiement Docker

1. Dans **Settings** → **Deploy**
2. Assurez-vous que **"Dockerfile"** est détecté
3. Si pas détecté, allez dans **"Root Directory"** et laissez vide (racine du projet)

### 3.5 Attendre le Build

- Le build prendra **5-8 minutes** (première fois)
- Suivez les logs dans l'onglet **"Deployments"**
- Quand vous voyez "Started CreditPlatformApplication" → ✅ C'est bon !

### 3.6 Obtenir l'URL du Backend

1. Dans l'onglet **"Settings"**
2. Sous **"Networking"** → **"Public Networking"**
3. Cliquez **"Generate Domain"**
4. **📝 COPIEZ** l'URL : `https://votre-app.up.railway.app`

### 3.7 Tester le Backend

Ouvrez dans votre navigateur :
```
https://votre-app.up.railway.app/actuator/health
```

Vous devriez voir :
```json
{
  "status": "UP"
}
```

✅ **Backend déployé avec succès !**

---

## 🎨 ÉTAPE 4 : Déployer le Frontend sur Vercel (3 min)

### 4.1 Créer le Projet Vercel

1. Allez sur https://vercel.com/dashboard
2. Cliquez **"Add New..."** → **"Project"**
3. **Importez** votre repository GitHub
4. Configuration **IMPORTANTE** :
   - **Framework Preset:** Vite
   - **Root Directory:** `frontend` ⚠️ **CRUCIAL !**
   - **Build Command:** `npm run build`
   - **Output Directory:** `dist`

### 4.2 Configurer la Variable d'Environnement

1. Dans **"Environment Variables"**, ajoutez :

```bash
VITE_API_URL=https://votre-app.up.railway.app
```

⚠️ **REMPLACEZ** `votre-app.up.railway.app` par votre vraie URL Railway de l'étape 3.6

2. Sélectionnez : **Production**, **Preview**, et **Development**
3. Cliquez **"Deploy"**

### 4.3 Attendre le Déploiement

- Prend **1-2 minutes**
- Quand terminé, vous verrez un écran de congratulations

### 4.4 Obtenir l'URL du Frontend

**📝 COPIEZ** l'URL Vercel : `https://votre-app.vercel.app`

---

## 🔗 ÉTAPE 5 : Finaliser la Configuration CORS (2 min)

### 5.1 Mettre à Jour les CORS sur Railway

1. Retournez sur **Railway** → votre service backend
2. Allez dans **"Variables"**
3. **Modifiez** `CORS_ALLOWED_ORIGINS` :

```bash
https://votre-app.vercel.app,https://votre-app-*.vercel.app
```

⚠️ **REMPLACEZ** `votre-app` par votre vrai nom de domaine Vercel

4. Le service va automatiquement redéployer (1-2 min)

---

## 🎉 ÉTAPE 6 : Tester l'Application (2 min)

1. **Ouvrez** `https://votre-app.vercel.app`
2. **Connectez-vous** avec :
   - Username : `admin`
   - Password : `admin123`

### ✅ Si ça marche :
- Félicitations ! 🎉
- **Changez le mot de passe admin IMMÉDIATEMENT**
- Testez les fonctionnalités principales

### ❌ Si ça ne marche pas :
- Ouvrez la console (F12)
- Regardez les erreurs
- Consultez la section "Troubleshooting" ci-dessous

---

## 🔍 Troubleshooting

### ❌ Erreur : "Network Error" ou "Failed to fetch"

**Cause :** Problème de CORS ou URL incorrecte

**Solutions :**
1. Vérifiez que `VITE_API_URL` sur Vercel est correct (sans `/` à la fin)
2. Vérifiez que votre URL Vercel est dans `CORS_ALLOWED_ORIGINS` sur Railway
3. Attendez 2 minutes que Railway redéploie après modification des variables

### ❌ Backend : "Connection refused" dans les logs

**Cause :** Problème de connexion à la base de données

**Solutions :**
1. Vérifiez que `DATABASE_URL` est bien liée depuis PostgreSQL
2. Dans Railway, allez dans PostgreSQL → Variables → copiez `DATABASE_URL`
3. Collez-la manuellement dans les variables du backend si besoin

### ❌ Build Failed sur Railway

**Cause :** Problème dans le Dockerfile ou dépendances Maven

**Solutions :**
1. Vérifiez les logs de build dans Railway
2. Assurez-vous que le Dockerfile est à la racine
3. Vérifiez que Maven peut télécharger les dépendances

### ❌ Frontend : Page blanche ou 404

**Cause :** Root directory mal configuré

**Solutions :**
1. Sur Vercel → Settings → General
2. Vérifiez que **Root Directory** = `frontend`
3. Redéployez si nécessaire

### ❌ Backend lent au démarrage

**Cause :** Normal pour un cold start avec le plan gratuit

**Solutions :**
- Première requête peut prendre 10-30 secondes
- C'est normal sur le plan gratuit
- Soyez patient lors du premier accès

---

## 📊 Monitoring et Maintenance

### Surveiller les Logs

**Railway :**
1. Allez dans votre service
2. Cliquez sur l'onglet **"Deployments"**
3. Sélectionnez le dernier déploiement
4. Consultez les logs en temps réel

**Vercel :**
1. Allez dans votre projet
2. Onglet **"Deployments"**
3. Cliquez sur un déploiement pour voir les logs

### Surveiller l'Utilisation

**Railway :**
1. Dashboard → votre projet
2. En haut à droite : **Usage**
3. Vous verrez vos heures utilisées / 500h gratuites

### Base de Données

**Accéder à PostgreSQL :**
1. Railway → PostgreSQL → **Data**
2. Vous pouvez exécuter des requêtes SQL directement !

**Ou connecter avec pgAdmin :**
1. Railway → PostgreSQL → Variables
2. Copiez les valeurs de `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD`, `PGDATABASE`
3. Configurez une nouvelle connexion dans pgAdmin

---

## 🔐 Sécurité Post-Déploiement

### ✅ Checklist Sécurité

- [ ] Mot de passe admin changé
- [ ] JWT_SECRET unique et secret
- [ ] Variables d'environnement jamais commitées dans Git
- [ ] CORS configuré avec vos domaines exacts (pas de wildcards en prod)
- [ ] Backups PostgreSQL activés sur Railway

### Changer le Mot de Passe Admin

1. Connectez-vous avec `admin` / `admin123`
2. Allez dans les paramètres utilisateur
3. Changez le mot de passe **immédiatement**

---

## 💰 Plan Gratuit Railway - Limites

### Ce qui est inclus (gratuit) :
- ✅ 500 heures d'exécution/mois
- ✅ PostgreSQL avec 1 GB de stockage
- ✅ 100 GB de bande passante sortante
- ✅ Tous les services managés

### Calcul des heures :
- **500h = ~20 jours** de fonctionnement continu
- Si votre app est active 24/7 : **500h ÷ 720h/mois = 69% du mois**
- **Recommandation :** Pour une prod 24/7, passez au plan Hobby (5$/mois)

### Quand mettre à niveau :
- Si vous dépassez 500h/mois
- Si vous voulez plus de stockage DB (> 1GB)
- Si vous avez besoin de ressources CPU/RAM garanties

---

## 📚 Commandes Utiles

### Voir les Logs en Direct (Railway CLI)

```bash
# Installer Railway CLI
npm install -g @railway/cli

# Login
railway login

# Link projet
railway link

# Voir les logs
railway logs
```

### Redéployer Manuellement

**Railway :**
1. Service → Deployments
2. Cliquez sur **"Redeploy"** sur le dernier déploiement

**Vercel :**
1. Projet → Deployments
2. Cliquez sur **"..."** → **"Redeploy"**

---

## 🎯 Prochaines Étapes

### Améliorations Recommandées

1. **Domaine Personnalisé**
   - Railway : Settings → Networking → Custom Domain
   - Vercel : Settings → Domains → Add Domain

2. **Monitoring**
   - Configurer des alertes sur Railway
   - Utiliser Vercel Analytics

3. **Backups Automatiques**
   - Activer les backups PostgreSQL sur Railway
   - Planifier des exports réguliers

4. **CI/CD Avancé**
   - Configurer des environnements de staging
   - Tests automatiques avant déploiement

---

## 🆘 Besoin d'Aide ?

### Documentation Officielle
- [Railway Docs](https://docs.railway.app/)
- [Vercel Docs](https://vercel.com/docs)

### Support
- Railway Discord : https://discord.gg/railway
- Railway Help : support@railway.app

---

## ✅ Checklist Finale

- [ ] JWT Secret généré
- [ ] Code pushé sur GitHub
- [ ] PostgreSQL créée sur Railway
- [ ] Backend déployé sur Railway
- [ ] Variables d'environnement configurées
- [ ] DATABASE_URL liée
- [ ] Backend accessible (`/actuator/health`)
- [ ] Frontend déployé sur Vercel
- [ ] VITE_API_URL configurée
- [ ] CORS mis à jour avec URL Vercel réelle
- [ ] Application testée et fonctionnelle
- [ ] Mot de passe admin changé

---

## 🎉 Félicitations !

Votre application est maintenant déployée en production !

- 🌐 **Frontend :** `https://votre-app.vercel.app`
- 🔧 **Backend :** `https://votre-app.up.railway.app`
- 🗄️ **Database :** Managée sur Railway
- 📊 **Health :** `https://votre-app.up.railway.app/actuator/health`

**Bon déploiement ! 🚀**

---

*Guide créé pour le projet Credit Platform - Déploiement sur Railway.app*
