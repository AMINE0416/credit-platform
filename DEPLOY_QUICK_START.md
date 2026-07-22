# 🚀 Déploiement Rapide - Credit Platform

Guide rapide pour déployer l'application en 15 minutes.

## 📦 Fichiers Créés

Les fichiers suivants ont été préparés pour le déploiement :

### Backend (Render)
- ✅ `Dockerfile` - Image Docker multi-stage optimisée
- ✅ `.dockerignore` - Exclut les fichiers inutiles du build
- ✅ `render.yaml` - Configuration Infrastructure as Code
- ✅ `src/main/resources/application-prod.properties` - Config production
- ✅ `pom.xml` - Ajout de Spring Boot Actuator pour health checks

### Frontend (Vercel)
- ✅ `frontend/vercel.json` - Configuration Vercel
- ✅ `frontend/.env.development` - Variables pour développement local
- ✅ `frontend/.env.production` - Variables pour production
- ✅ `frontend/.env.example` - Template des variables
- ✅ Services mis à jour pour utiliser `VITE_API_URL`

## 🎯 Déploiement en 5 Étapes

### 1️⃣ Générer le JWT Secret

**Windows PowerShell :**
```powershell
cd scripts
.\generate-jwt-secret.ps1
```

**Linux/Mac :**
```bash
cd scripts
chmod +x generate-jwt-secret.sh
./generate-jwt-secret.sh
```

Copiez le secret généré (128 caractères).

---

### 2️⃣ Préparer la Base de Données

**Option A : Render Database (Recommandé)**

1. Sur [Render Dashboard](https://dashboard.render.com/)
2. New + → MySQL
3. Notez les infos de connexion

**Option B : Base existante**

Préparez l'URL JDBC :
```
jdbc:mysql://HOST:PORT/credit_platform?useSSL=true&serverTimezone=UTC
```

---

### 3️⃣ Déployer le Backend sur Render

1. **Pusher le code :**
   ```bash
   git add .
   git commit -m "Add deployment configuration"
   git push origin main
   ```

2. **Sur Render Dashboard :**
   - New + → Web Service
   - Connecter votre repo Git
   - Runtime : **Docker**
   - Plan : Free (ou Starter pour éviter cold start)

3. **Ajouter les variables d'environnement :**

   | Variable | Valeur |
   |----------|--------|
   | `SPRING_DATASOURCE_URL` | `jdbc:mysql://...` (votre URL DB) |
   | `DB_USERNAME` | Votre username DB |
   | `DB_PASSWORD` | Votre password DB |
   | `JWT_SECRET` | Le secret généré à l'étape 1 |
   | `JWT_EXPIRATION` | `7200000` (2 heures) |
   | `CORS_ALLOWED_ORIGINS` | `https://your-app.vercel.app,https://your-app-*.vercel.app` |
   | `SERVER_PORT` | `8081` |
   | `JAVA_OPTS` | `-Xmx512m -Xms256m -Dspring.profiles.active=prod` |

4. **Déployer** → Attendre 5-10 minutes

5. **Tester :**
   ```
   https://your-api.onrender.com/actuator/health
   ```
   Doit retourner : `{"status":"UP"}`

---

### 4️⃣ Déployer le Frontend sur Vercel

1. **Sur [Vercel Dashboard](https://vercel.com/dashboard) :**
   - Add New → Project
   - Importer votre repo Git
   - Framework : **Vite**
   - Root Directory : **`frontend`**
   - Build Command : `npm run build`
   - Output Directory : `dist`

2. **Ajouter la variable d'environnement :**
   - Dans Project Settings → Environment Variables
   - Nom : `VITE_API_URL`
   - Valeur : `https://your-api.onrender.com` (SANS le `/` final)
   - Environnements : Production + Preview

3. **Déployer** → Attendre 1-3 minutes

---

### 5️⃣ Finaliser la Configuration

1. **Mettre à jour CORS sur Render :**
   
   Retournez sur Render → votre service → Environment
   
   Modifiez `CORS_ALLOWED_ORIGINS` avec votre vraie URL Vercel :
   ```
   https://your-app.vercel.app,https://your-app-*.vercel.app
   ```

2. **Redéployer le backend :**
   
   Manual Deploy → Deploy latest commit

3. **Tester l'application complète :**
   - Ouvrez `https://your-app.vercel.app`
   - Connectez-vous avec `admin` / `admin123`
   - Changez immédiatement le mot de passe !

---

## ✅ Checklist Rapide

### Avant de déployer
- [ ] Code commité et pushé sur Git
- [ ] JWT Secret généré
- [ ] Base de données MySQL prête

### Backend Render
- [ ] Web Service créé
- [ ] Variables d'environnement configurées
- [ ] Health check répond : `/actuator/health`

### Frontend Vercel
- [ ] Projet créé avec root directory `frontend`
- [ ] `VITE_API_URL` configurée
- [ ] Frontend accessible

### Après déploiement
- [ ] CORS mis à jour avec URLs Vercel réelles
- [ ] Login fonctionne
- [ ] Mot de passe admin changé

---

## 🆘 Problèmes Courants

### ❌ Backend : "Connection refused" dans les logs

**Solution :** Vérifiez `SPRING_DATASOURCE_URL`, `DB_USERNAME`, `DB_PASSWORD`

### ❌ Frontend : "Network Error" ou erreur CORS

**Solution :** 
1. Vérifiez que `VITE_API_URL` est correct sur Vercel (sans `/`)
2. Vérifiez que votre URL Vercel est dans `CORS_ALLOWED_ORIGINS` sur Render
3. Redéployez le backend après modification

### ❌ Backend : Cold Start (première requête lente)

**Cause :** Plan Free de Render met le service en veille

**Solutions :**
- Passer au plan Starter (7$/mois)
- Ajouter un loading indicator
- Utiliser un service de ping (ex: UptimeRobot)

### ❌ Build Failed sur Render

**Solution :** 
1. Vérifiez les logs de build
2. Assurez-vous que le Dockerfile est bien à la racine
3. Vérifiez que Maven peut télécharger les dépendances

---

## 📚 Documentation Complète

Pour plus de détails, consultez **[DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md)**

---

## 🎉 C'est Fait !

Votre application est maintenant en ligne :
- 🔹 Backend : `https://your-api.onrender.com`
- 🔸 Frontend : `https://your-app.vercel.app`

**Prochaines étapes :**
1. Configurer un domaine personnalisé
2. Activer les backups de base de données
3. Configurer le monitoring
4. Inviter les utilisateurs

Bon déploiement ! 🚀
