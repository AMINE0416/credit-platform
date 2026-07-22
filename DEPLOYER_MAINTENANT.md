# 🚀 DÉPLOYER MAINTENANT - 3 Étapes Ultra-Rapides

> **Tu veux déployer TOUT DE SUITE sans lire de longue documentation ?**

---

## 📋 Avant de Commencer (2 min)

Tu as besoin de :
- [ ] Compte [Render](https://dashboard.render.com/) (gratuit - créer si besoin)
- [ ] Compte [Vercel](https://vercel.com/dashboard) (gratuit - créer si besoin)
- [ ] Ton code sur GitHub/GitLab/Bitbucket

---

## ⚡ Étape 1 : Générer le JWT Secret (1 min)

```powershell
# Windows PowerShell
cd scripts
.\generate-jwt-secret.ps1
```

**✏️ COPIE** le secret généré (128 caractères) - tu en auras besoin !

---

## ⚡ Étape 2 : Pusher le Code (1 min)

```bash
git add .
git commit -m "Add deployment configuration"
git push origin main
```

---

## ⚡ Étape 3 : Déployer Backend sur Render (10 min)

### 3.1 Créer la Base de Données PostgreSQL
1. Va sur [Render Dashboard](https://dashboard.render.com/)
2. **New +** → **PostgreSQL**
3. Remplis :
   - Name : `credit-platform-db`
   - Database : `credit_platform`
   - User : `credit_user`
   - Plan : **Free**
4. **Create Database**
5. ✏️ **NOTE** l'**Internal Database URL** (commence par `postgresql://...`)

### 3.2 Créer le Web Service
1. Sur Render Dashboard : **New +** → **Web Service**
2. Connecte ton repository Git
3. Remplis :
   - Name : `credit-platform-api`
   - Runtime : **Docker**
   - Plan : **Free**
4. Clique **Environment** et ajoute ces 6 variables :

```
DATABASE_URL = [L_INTERNAL_DATABASE_URL_DE_LETAPE_3.1]
JWT_SECRET = [LE_SECRET_DE_LETAPE_1]
JWT_EXPIRATION = 7200000
CORS_ALLOWED_ORIGINS = https://*.vercel.app
SERVER_PORT = 8081
JAVA_OPTS = -Xmx512m -Xms256m -Dspring.profiles.active=prod
```

> 💡 **Astuce** : Tu peux aussi lier directement la base en utilisant l'option "Add from database"

5. **Create Web Service** → Attendre 5-10 min

6. ✏️ **NOTE** l'URL : `https://[TON_SERVICE].onrender.com`

---

## ⚡ Étape 4 : Déployer Frontend sur Vercel (5 min)

1. Va sur [Vercel Dashboard](https://vercel.com/dashboard)
2. **Add New...** → **Project**
3. Importe ton repository Git
4. Configure :
   - Framework : **Vite**
   - **Root Directory** : `frontend` ⚠️ IMPORTANT
   - Build Command : `npm run build`
   - Output Directory : `dist`
5. Dans **Environment Variables**, ajoute :

```
VITE_API_URL = https://[TON_SERVICE_RENDER].onrender.com
```

6. **Deploy** → Attendre 1-3 min

7. ✏️ **NOTE** l'URL : `https://[TON_APP].vercel.app`

---

## ⚡ Étape 5 : Finaliser (2 min)

1. Retourne sur **Render** → ton service → **Environment**
2. Modifie `CORS_ALLOWED_ORIGINS` avec ton URL Vercel réelle :

```
https://[TON_APP].vercel.app,https://[TON_APP]-*.vercel.app
```

3. **Save Changes** → Attendre 1 min (redéploiement auto)

---

## ✅ Étape 6 : Tester (2 min)

1. Ouvre `https://[TON_APP].vercel.app`
2. Login avec :
   - Username : `admin`
   - Password : `admin123`
3. **SI ÇA MARCHE** :
   - ✅ Va dans paramètres
   - ✅ Change le mot de passe admin MAINTENANT
   - ✅ C'est terminé ! 🎉

4. **SI ÇA NE MARCHE PAS** :
   - Ouvre la console du navigateur (F12)
   - Vérifie les erreurs
   - Ouvre [NEXT_STEPS.md](./NEXT_STEPS.md) section "Troubleshooting"

---

## 🎉 C'est Fait !

Ton application est maintenant en ligne !

- 🌐 **Frontend** : `https://[TON_APP].vercel.app`
- 🔧 **Backend** : `https://[TON_SERVICE].onrender.com`
- 📊 **Health** : `https://[TON_SERVICE].onrender.com/actuator/health`

---

## ⚠️ Important

### Plan Free de Render
- Le service se met en **veille après 15 min** d'inactivité
- **Cold start** : première requête prend 30-60 secondes
- 💡 Solution : Passer au plan **Starter (7$/mois)** pour pas de veille

### Sécurité
- ⚠️ **Change le mot de passe admin** dès que possible
- ⚠️ Le JWT_SECRET doit être **unique** (pas celui de l'exemple)
- ⚠️ Ne partage **jamais** les secrets

---

## 📚 Plus de Détails ?

Si tu veux comprendre en profondeur ou si tu as des problèmes :

| Document | Usage |
|----------|-------|
| [NEXT_STEPS.md](./NEXT_STEPS.md) | Instructions détaillées étape par étape |
| [DEPLOYMENT_CHECKLIST.md](./DEPLOYMENT_CHECKLIST.md) | Checklist complète |
| [DEPLOY_QUICK_START.md](./DEPLOY_QUICK_START.md) | Guide rapide 5 étapes |
| [DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md) | Guide complet |

---

## 🆘 Problèmes ?

### Backend ne démarre pas
→ Vérifie `SPRING_DATASOURCE_URL`, username, password dans Render

### Frontend "Network Error"
→ Vérifie que `VITE_API_URL` est correct sur Vercel

### Erreur CORS
→ Vérifie que ton URL Vercel est dans `CORS_ALLOWED_ORIGINS` sur Render

### Cold Start (lent)
→ Normal sur plan Free (60s). Passe au Starter pour éviter ça.

---

**Bon déploiement ! 🚀**

*Si tu suis ces étapes, ton app sera en ligne en ~20 minutes !*
