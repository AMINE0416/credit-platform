# 🚂 Railway - Démarrage Ultra-Rapide (10 min)

> **Déployez en 10 minutes chrono ! Suivez ces 6 étapes.**

---

## ⚡ ÉTAPE 1 : Générer JWT Secret (1 min)

```powershell
cd c:\Users\asus\Documents\credit-platform\scripts
.\generate-jwt-secret.ps1
```

**✏️ COPIEZ** le secret généré → vous en aurez besoin !

---

## ⚡ ÉTAPE 2 : Pousser sur Git (1 min)

```bash
cd c:\Users\asus\Documents\credit-platform
git add .
git commit -m "Config Railway"
git push origin main
```

---

## ⚡ ÉTAPE 3 : Créer Projet Railway (2 min)

1. Allez sur https://railway.app/ → Login avec GitHub
2. **New Project** → **Provision PostgreSQL** → Attendez 30s ✅
3. Dans le même projet : **+ New** → **GitHub Repo** → Sélectionnez `credit-platform` → **Deploy Now**

✅ Vous avez maintenant : PostgreSQL + Backend en cours de build !

---

## ⚡ ÉTAPE 4 : Configurer Variables (3 min)

### 4.1 Lier la Base de Données

1. Cliquez sur votre **service backend**
2. **Settings** → **Service Variables** → **+ Variable Reference**
3. Sélectionnez **PostgreSQL** → Choisissez **`DATABASE_URL`** → ✅

### 4.2 Ajouter les Variables

Dans **Variables**, cliquez **+ New Variable** et ajoutez :

```bash
PORT=8081
JWT_SECRET=[VOTRE_SECRET_ETAPE_1]
JWT_EXPIRATION=7200000
CORS_ALLOWED_ORIGINS=https://*.vercel.app
SPRING_PROFILES_ACTIVE=prod
JAVA_OPTS=-Xmx512m -Xms256m
```

### 4.3 Générer le Domaine

1. **Settings** → **Networking** → **Generate Domain** → ✅
2. **📝 COPIEZ** l'URL : `https://votre-app.up.railway.app`

### 4.4 Attendre le Build

- Allez dans **Deployments** → Suivez les logs (5-8 min)
- Quand vous voyez "Started CreditPlatformApplication" → ✅

### 4.5 Tester

Ouvrez : `https://votre-app.up.railway.app/actuator/health`

Vous devez voir : `{"status":"UP"}` → ✅ Backend OK !

---

## ⚡ ÉTAPE 5 : Déployer Frontend Vercel (2 min)

1. https://vercel.com/dashboard → **Add New...** → **Project**
2. Importez votre repo GitHub
3. Configuration :
   - Framework : **Vite**
   - **Root Directory : `frontend`** ⚠️ IMPORTANT
   - Build : `npm run build`
   - Output : `dist`
4. **Environment Variables** :
   ```
   VITE_API_URL=https://votre-app.up.railway.app
   ```
   ⚠️ Remplacez par votre vraie URL Railway
5. **Deploy** → Attendez 1-2 min
6. **📝 COPIEZ** l'URL : `https://votre-app.vercel.app`

---

## ⚡ ÉTAPE 6 : Finaliser CORS (1 min)

1. Retour sur **Railway** → service backend → **Variables**
2. **Modifiez** `CORS_ALLOWED_ORIGINS` :
   ```
   https://votre-app.vercel.app,https://votre-app-*.vercel.app
   ```
   ⚠️ Remplacez par votre vraie URL Vercel
3. Attendez 1-2 min que Railway redéploie

---

## 🎉 C'est Prêt !

1. Ouvrez `https://votre-app.vercel.app`
2. Login : `admin` / `admin123`
3. **CHANGEZ LE MOT DE PASSE** immédiatement ! ⚠️

---

## 🆘 Problèmes ?

### ❌ "Network Error"
→ Vérifiez `VITE_API_URL` sur Vercel et `CORS_ALLOWED_ORIGINS` sur Railway

### ❌ Backend ne démarre pas
→ Vérifiez que `DATABASE_URL` est bien liée dans Railway

### ❌ 404 Frontend
→ Vérifiez que Root Directory = `frontend` sur Vercel

---

## 📊 Surveiller l'Usage

Railway Dashboard → Votre projet → **Usage** (haut droite)

Vous avez **500h gratuites/mois** = environ 20 jours 24/7

---

## 📚 Plus de Détails ?

Consultez **[DEPLOYMENT_RAILWAY.md](./DEPLOYMENT_RAILWAY.md)** pour le guide complet.

---

**Félicitations ! Votre app est en ligne ! 🚀**

*Temps total : ~10 minutes*
