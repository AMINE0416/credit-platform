# 🚀 Guide de Déploiement Rapide

## Checklist Pré-Déploiement

### ✅ Étape 1: Vérifications Locales (5 min)

```powershell
# 1. Vérifier que le backend compile
cd c:\Users\asus\Documents\credit-platform
.\mvnw clean package -DskipTests

# 2. Vérifier que le frontend build
cd frontend
npm run build
cd ..

# 3. Vérifier les variables d'environnement
# S'assurer que tous les fichiers .env sont correctement configurés
```

### ✅ Étape 2: Optimiser la Base de Données (2 min)

```powershell
# Se connecter à votre base de données Railway/Render
# Puis exécuter le script d'optimisation

# Via Railway CLI:
railway connect postgresql

# Puis dans psql:
\i scripts/optimisation_indexes.sql
```

Ou directement avec psql:
```bash
psql $DATABASE_URL -f scripts/optimisation_indexes.sql
```

### ✅ Étape 3: Commit et Push (2 min)

```powershell
git add .
git commit -m "feat: optimisation des performances pour la production"
git push origin main
```

---

## 🎯 Déploiement Backend (Railway)

### Option A: Déploiement Automatique (Recommandé)

Railway détecte automatiquement les changements sur `main`:

1. **Vérifier les variables d'environnement dans Railway Dashboard**:
   - `SPRING_PROFILES_ACTIVE=prod`
   - `JWT_SECRET=votre_secret`
   - `CORS_ALLOWED_ORIGINS=https://votre-frontend.vercel.app`
   - `DATABASE_URL` (doit être automatiquement configuré)

2. **Attendre le déploiement** (2-5 min)

3. **Vérifier les logs**:
```powershell
railway logs
```

### Option B: Déploiement Manuel

```powershell
# Installer Railway CLI (si nécessaire)
npm install -g @railway/cli

# Se connecter
railway login

# Lier le projet
railway link

# Déployer
railway up
```

---

## 🌐 Déploiement Frontend (Vercel)

### Option A: Déploiement Automatique (Recommandé)

Vercel détecte automatiquement les changements sur `main`:

1. **Vérifier les variables d'environnement dans Vercel Dashboard**:
   - `VITE_API_URL=https://votre-backend.railway.app/api`

2. **Attendre le déploiement** (1-3 min)

### Option B: Déploiement Manuel

```powershell
# Installer Vercel CLI (si nécessaire)
npm install -g vercel

# Se connecter
vercel login

# Déployer depuis le dossier frontend
cd frontend
vercel --prod
```

---

## 🧪 Tests Post-Déploiement (5 min)

### Test 1: Disponibilité

```powershell
# Tester le backend
curl https://votre-backend.railway.app/api/health

# Tester le frontend
curl https://votre-frontend.vercel.app
```

### Test 2: Performance

```powershell
# Exécuter le script de test
.\scripts\test-performance.ps1

# Ou manuellement:
# Aller sur https://pagespeed.web.dev/
# Entrer votre URL frontend
# Vérifier que le score est > 80
```

### Test 3: Fonctionnalités

1. **Ouvrir l'application**: https://votre-frontend.vercel.app
2. **Se connecter** avec un compte test
3. **Vérifier les fonctions principales**:
   - ✅ Liste des dossiers se charge
   - ✅ Détails d'un dossier s'affichent
   - ✅ Création d'une promesse fonctionne
   - ✅ Import de données fonctionne

---

## 📊 Monitoring (Continu)

### Vérifier les Logs Backend

```powershell
# Railway
railway logs

# Ou dans le dashboard Railway
# https://railway.app/dashboard
```

### Vérifier les Logs Frontend

```powershell
# Vercel
vercel logs

# Ou dans le dashboard Vercel
# https://vercel.com/dashboard
```

### Métriques de Performance à Surveiller

| Métrique | Objectif | Critique |
|----------|----------|----------|
| Temps de chargement | < 3s | < 5s |
| Temps réponse API | < 500ms | < 1s |
| Taux d'erreur | < 1% | < 5% |
| Score Lighthouse | > 80 | > 60 |

---

## 🔧 Dépannage Rapide

### Problème: Application ne démarre pas

**Backend**:
```powershell
# Vérifier les logs
railway logs --filter error

# Vérifier les variables d'environnement
railway variables
```

**Frontend**:
```powershell
# Vérifier les logs de build
vercel logs

# Rebuild manuel
vercel --prod --force
```

### Problème: Erreur de base de données

```sql
-- Se connecter à la base
railway connect postgresql

-- Vérifier les tables
\dt

-- Vérifier les index
SELECT indexname FROM pg_indexes WHERE schemaname = 'public';

-- Si nécessaire, réexécuter les migrations
\i scripts/optimisation_indexes.sql
```

### Problème: Erreurs CORS

```powershell
# Mettre à jour la variable d'environnement dans Railway
railway variables set CORS_ALLOWED_ORIGINS=https://votre-frontend.vercel.app

# Redémarrer l'application
railway restart
```

### Problème: Application lente

1. **Exécuter la maintenance DB**:
```sql
\i scripts/maintenance_database.sql
```

2. **Vérifier les métriques**:
```powershell
.\scripts\test-performance.ps1
```

3. **Analyser avec Lighthouse**:
   - Ouvrir: https://pagespeed.web.dev/
   - Analyser les recommandations

---

## 🎨 Optimisations Appliquées

### ✅ Backend
- [x] Pool de connexions HikariCP optimisé
- [x] Batch insert/update Hibernate
- [x] Compression HTTP activée
- [x] Threads Tomcat optimisés
- [x] Logging optimisé pour production
- [x] Index de base de données créés

### ✅ Frontend
- [x] Lazy loading des composants
- [x] Code splitting optimisé
- [x] Minification avec Terser
- [x] Suppression des console.log
- [x] Cache des appels API
- [x] Compression des assets

### ✅ Base de Données
- [x] Index sur colonnes fréquentes
- [x] Index composites
- [x] Statistiques à jour (ANALYZE)
- [x] VACUUM configuré

---

## 📋 Commandes Utiles

### Railway (Backend)

```powershell
# Connexion
railway login

# Logs en temps réel
railway logs

# Variables d'environnement
railway variables

# Redémarrer
railway restart

# Se connecter à la DB
railway connect postgresql
```

### Vercel (Frontend)

```powershell
# Connexion
vercel login

# Déployer
cd frontend
vercel --prod

# Logs
vercel logs

# Liste des déploiements
vercel list
```

### Base de Données

```powershell
# Via Railway
railway connect postgresql

# Via psql direct
psql $DATABASE_URL

# Exécuter un script
psql $DATABASE_URL -f scripts/nom_script.sql
```

---

## 🎯 Résumé: Déploiement en 5 Minutes

```powershell
# 1. Optimiser la DB (30s)
psql $DATABASE_URL -f scripts/optimisation_indexes.sql

# 2. Commit et push (30s)
git add . && git commit -m "feat: optimisations" && git push

# 3. Attendre les déploiements automatiques (3-4 min)
# Railway et Vercel déploient automatiquement

# 4. Tester (1 min)
.\scripts\test-performance.ps1

# ✅ TERMINÉ!
```

---

## 📞 Support

En cas de problème:
1. Consulter le fichier `GUIDE_DEPLOIEMENT_OPTIMISE.md` pour les détails
2. Vérifier les logs: `railway logs` et `vercel logs`
3. Tester localement d'abord
4. Vérifier les variables d'environnement

---

**Dernière mise à jour**: $(Get-Date -Format "yyyy-MM-dd HH:mm")
