# 🔧 Troubleshooting Railway - Échec de Compilation

> **Guide pour résoudre les erreurs de déploiement Railway**

---

## ❌ Erreur : "Healthcheck failure"

### Diagnostic

L'erreur "healthcheck failure" signifie que Railway ne peut pas atteindre `/actuator/health` après le déploiement.

### Causes Possibles

1. **Base de données non connectée**
2. **Variables d'environnement manquantes**
3. **Tables de base de données non créées**
4. **Port mal configuré**

---

## ✅ Solution : Checklist de Vérification

### 1. Vérifier que DATABASE_URL est Liée

Sur Railway :

1. Allez dans votre **service backend**
2. Cliquez sur l'onglet **"Variables"**
3. Vérifiez qu'il y a une variable **`DATABASE_URL`**
   - Si elle n'existe pas, ajoutez-la :
     - **Settings** → **Service Variables** → **+ Variable Reference**
     - Sélectionnez votre base **PostgreSQL**
     - Choisissez **`DATABASE_URL`**

### 2. Vérifier TOUTES les Variables d'Environnement

Dans l'onglet **Variables** de votre backend Railway, vous DEVEZ avoir :

```bash
# Automatiques (si DATABASE_URL liée)
DATABASE_URL=postgresql://...

# À AJOUTER MANUELLEMENT
PORT=8081
JWT_SECRET=[VOTRE_SECRET_128_CARACTERES]
JWT_EXPIRATION=7200000
CORS_ALLOWED_ORIGINS=https://*.vercel.app
SPRING_PROFILES_ACTIVE=prod
JAVA_OPTS=-Xmx512m -Xms256m
```

⚠️ **IMPORTANT :** Ne mettez PAS `DB_USERNAME` et `DB_PASSWORD` séparément sur Railway. Utilisez uniquement `DATABASE_URL`.

### 3. Vérifier le Profil Spring

Assurez-vous que `SPRING_PROFILES_ACTIVE=prod` est bien défini.

---

## 🔄 Solution Appliquée

J'ai modifié `application-prod.properties` :

### Avant (Problème)
```properties
spring.jpa.hibernate.ddl-auto=validate
```

### Après (Solution)
```properties
spring.jpa.hibernate.ddl-auto=update
```

**Pourquoi ?**
- `validate` : Vérifie que les tables existent → ❌ Échoue si DB vide
- `update` : Crée les tables automatiquement → ✅ Fonctionne au premier déploiement

---

## 🚀 Actions Immédiates

### Étape 1 : Commit le Changement

```bash
cd c:\Users\asus\Documents\credit-platform
git add src/main/resources/application-prod.properties
git commit -m "Fix: Use update instead of validate for first deployment"
git push origin main
```

### Étape 2 : Vérifier les Variables Railway

1. **Railway Dashboard** → Votre service backend → **Variables**
2. Vérifiez que toutes les variables sont présentes (voir liste ci-dessus)
3. **Si DATABASE_URL manque**, ajoutez-la via **Settings** → **Service Variables** → **+ Variable Reference**

### Étape 3 : Redéployer

1. Railway → Votre service → **Deployments**
2. Le redéploiement devrait se faire automatiquement après le push Git
3. **OU** cliquez sur **"Redeploy"** manuellement

### Étape 4 : Surveiller les Logs

1. Railway → Votre service → **Deployments** → Dernier déploiement
2. Regardez les logs en temps réel
3. Cherchez :
   - ✅ "Started CreditPlatformApplication" → Succès !
   - ❌ Erreurs de connexion DB → Vérifier DATABASE_URL
   - ❌ JWT errors → Vérifier JWT_SECRET

---

## 🔍 Erreurs Courantes et Solutions

### ❌ Error: "spring.datasource.url is null"

**Cause :** `DATABASE_URL` non définie ou mal liée

**Solution :**
1. Railway → Service backend → **Settings** → **Service Variables**
2. **+ Variable Reference** → PostgreSQL → `DATABASE_URL`
3. Attendre 1-2 min que le service redémarre

---

### ❌ Error: "Encoded password does not look like BCrypt"

**Cause :** `JWT_SECRET` manquant ou invalide

**Solution :**
1. Générez un nouveau secret :
   ```powershell
   cd scripts
   .\generate-jwt-secret.ps1
   ```
2. Railway → Variables → Modifier `JWT_SECRET`

---

### ❌ Error: "Port 8081 is already in use"

**Cause :** Railway utilise la variable `PORT` dynamiquement

**Solution :**
- Assurez-vous que la variable `PORT=8081` est définie
- Ou laissez Railway définir `PORT` automatiquement (Railway choisira un port)
- Votre config `server.port=${PORT:${SERVER_PORT:8081}}` devrait gérer ça automatiquement

---

### ❌ Healthcheck Timeout

**Cause :** Application prend trop de temps à démarrer

**Solution :**
1. Vérifiez que `JAVA_OPTS=-Xmx512m -Xms256m` est défini
2. Attendez 2-3 minutes (première compilation Java est lente)
3. Si ça persiste, augmentez la mémoire : `JAVA_OPTS=-Xmx768m -Xms384m`

---

### ❌ Build Failed: Maven Dependencies

**Cause :** Maven ne peut pas télécharger les dépendances

**Solution :**
1. Vérifiez votre `pom.xml` est valide
2. Railway → Logs → Cherchez l'erreur Maven spécifique
3. Localement, testez : `./mvnw clean package -DskipTests`

---

## 📋 Checklist de Débogage

Suivez cette checklist dans l'ordre :

### Niveau 1 : Variables d'Environnement
- [ ] `DATABASE_URL` existe et commence par `postgresql://`
- [ ] `JWT_SECRET` existe et fait 128+ caractères
- [ ] `SPRING_PROFILES_ACTIVE=prod` est défini
- [ ] `PORT=8081` est défini
- [ ] `CORS_ALLOWED_ORIGINS` est défini
- [ ] `JWT_EXPIRATION=7200000` est défini
- [ ] `JAVA_OPTS=-Xmx512m -Xms256m` est défini

### Niveau 2 : Configuration Base de Données
- [ ] PostgreSQL créée sur Railway
- [ ] PostgreSQL est dans le même projet que le backend
- [ ] DATABASE_URL est liée via **Service Variables**
- [ ] PostgreSQL est démarrée (statut "Active")

### Niveau 3 : Code
- [ ] `application-prod.properties` a `ddl-auto=update`
- [ ] Code compilé localement : `./mvnw clean package -DskipTests`
- [ ] Dockerfile existe à la racine
- [ ] Code pushé sur GitHub

### Niveau 4 : Railway
- [ ] Bon repository GitHub sélectionné
- [ ] Branch `main` (ou votre branche principale)
- [ ] Build détecte le Dockerfile
- [ ] Healthcheck path = `/actuator/health` (automatique)

---

## 🎯 Commandes de Vérification Locale

Avant de redéployer, testez localement :

### 1. Compiler le Backend

```bash
cd c:\Users\asus\Documents\credit-platform
./mvnw clean package -DskipTests
```

✅ Doit compiler sans erreurs

### 2. Tester le Dockerfile Localement (Optionnel)

Si vous avez Docker Desktop installé :

```bash
docker build -t credit-platform-test .
docker run -p 8081:8081 \
  -e DATABASE_URL="jdbc:postgresql://localhost:5432/credit_platform" \
  -e JWT_SECRET="test_secret_123" \
  -e SPRING_PROFILES_ACTIVE=prod \
  credit-platform-test
```

### 3. Vérifier PostgreSQL Local

```bash
psql -U postgres
\l
# Doit montrer credit_platform
\c credit_platform
\dt
# Doit montrer vos tables si ddl-auto=update
```

---

## 📊 Logs Railway - Que Chercher ?

### Logs de Build (5-8 min)

```
✅ [INFO] Building credit-platform 1.0.0
✅ [INFO] BUILD SUCCESS
✅ Successfully built image
```

### Logs de Démarrage (1-2 min)

```
✅ Started CreditPlatformApplication in X.XXX seconds
✅ Tomcat started on port(s): 8081 (http)
```

### Logs d'Erreur Communs

```
❌ spring.datasource.url is null
   → DATABASE_URL manquant

❌ Access denied for user
   → Mauvais credentials DB

❌ Encoded password does not look like BCrypt
   → JWT_SECRET manquant

❌ Table 'xxx' doesn't exist
   → Utiliser ddl-auto=update
```

---

## 🔄 Après la Correction

### Étape 1 : Push le Code

```bash
git add .
git commit -m "Fix Railway deployment configuration"
git push origin main
```

### Étape 2 : Attendre le Redéploiement

- Railway détecte automatiquement le push
- Nouveau build démarre (5-8 min)
- Surveillez les logs

### Étape 3 : Tester le Healthcheck

```
https://votre-app.up.railway.app/actuator/health
```

Doit retourner :
```json
{
  "status": "UP"
}
```

### Étape 4 : Si Ça Marche

1. **Félicitations ! 🎉**
2. Continuez avec le frontend sur Vercel
3. Suivez **RAILWAY_QUICK_START.md** à partir de l'étape 5

---

## 🆘 Toujours Bloqué ?

### Ressources

1. **Railway Logs** - La réponse est probablement là
2. **Railway Discord** - https://discord.gg/railway
3. **Documentation Railway** - https://docs.railway.app/

### Informations à Fournir

Si vous demandez de l'aide, fournissez :
- ✅ Capture des logs Railway (Build + Deploy)
- ✅ Liste de vos variables d'environnement (masquez les secrets)
- ✅ Screenshot de votre configuration Railway
- ✅ Message d'erreur exact

---

## ✅ Résumé de la Solution

### Le Problème
```
❌ Healthcheck failure
❌ spring.jpa.hibernate.ddl-auto=validate
❌ Tables n'existent pas encore
```

### La Solution
```
✅ spring.jpa.hibernate.ddl-auto=update
✅ Vérifier DATABASE_URL liée
✅ Vérifier toutes les variables
✅ Redeployer
```

---

## 🎯 Prochaines Étapes

Une fois que le backend fonctionne :

1. [ ] Backend accessible sur Railway
2. [ ] Healthcheck retourne `{"status":"UP"}`
3. [ ] Déployer frontend sur Vercel
4. [ ] Mettre à jour CORS avec URL Vercel
5. [ ] Tester l'application complète

---

**Le déploiement va réussir ! 💪**

*Guide créé suite à votre erreur de healthcheck*
