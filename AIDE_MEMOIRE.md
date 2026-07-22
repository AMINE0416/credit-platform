# 📝 Aide-Mémoire - Déploiement Credit Platform

> **Référence rapide pour le déploiement - À imprimer ou garder ouvert**

---

## 🔑 Informations Clés

### JWT Secret Généré
```
EE805AAE05476552C53DA212F1C902EB866FCAE6552E3436997B085067E2485BB28B058BA71440BDC063D0342B02FECAA59A736F37472C2F3AFAA8B6ECED9DED
```

### Credentials par Défaut
```
Username : admin
Password : admin123
⚠️ À CHANGER immédiatement après déploiement !
```

---

## 📋 Variables d'Environnement

### Backend Render (6 variables)

| Variable | Valeur | Source |
|----------|--------|--------|
| `DATABASE_URL` | `postgresql://...` | Copier depuis PostgreSQL Dashboard |
| `JWT_SECRET` | Voir ci-dessus | Script generate-jwt-secret.ps1 |
| `JWT_EXPIRATION` | `7200000` | 2 heures en millisecondes |
| `CORS_ALLOWED_ORIGINS` | Voir ci-dessous | URLs Vercel |
| `SERVER_PORT` | `8081` | Port par défaut |
| `JAVA_OPTS` | `-Xmx512m -Xms256m -Dspring.profiles.active=prod` | Options JVM |

### Frontend Vercel (1 variable)

| Variable | Valeur | Source |
|----------|--------|--------|
| `VITE_API_URL` | `https://[nom-service].onrender.com` | URL backend Render |

---

## 🔗 URLs Importantes

### Dashboards
- **Render** : https://dashboard.render.com/
- **Vercel** : https://vercel.com/dashboard

### Après Déploiement (à compléter)
```
Backend Render    : https://______________________.onrender.com
Frontend Vercel   : https://______________________.vercel.app
Health Check      : https://______________________.onrender.com/actuator/health
```

### CORS à Mettre à Jour
```
https://[ton-app].vercel.app,https://[ton-app]-*.vercel.app
```

---

## ⚡ Commandes Rapides

### Générer JWT Secret
```powershell
# Windows
.\scripts\generate-jwt-secret.ps1

# Linux/Mac
./scripts/generate-jwt-secret.sh
```

### Vérifier Configuration
```powershell
# Windows
.\scripts\pre-deploy-check.ps1
```

### Git - Commiter et Pusher
```bash
git add .
git commit -m "Ready for deployment"
git push origin main
```

### Test Health Check
```bash
curl https://[ton-backend].onrender.com/actuator/health
```

---

## 📊 Plans et Prix

### Gratuit (0€/mois)
```
Render Backend (Free)      : Cold start 30-60s
Render PostgreSQL (Free)   : 1 GB
Vercel Frontend (Hobby)    : Illimité
```

### Recommandé (7€/mois)
```
Render Backend (Starter)   : Pas de cold start ✅
Render PostgreSQL (Free)   : 1 GB
Vercel Frontend (Hobby)    : Illimité
```

### Pro (14€/mois)
```
Render Backend (Starter)   : Pas de cold start ✅
Render PostgreSQL (Starter): Backups auto ✅
Vercel Frontend (Hobby)    : Illimité
```

---

## 🎯 Étapes de Déploiement (Résumé)

### 1. Préparation (5 min)
- [x] JWT Secret généré ✅
- [ ] Code commité et pushé
- [ ] Comptes Render et Vercel créés

### 2. PostgreSQL sur Render (2 min)
1. New → PostgreSQL
2. Name: `credit-platform-db`
3. Plan: Free
4. Copier Internal Database URL

### 3. Backend sur Render (10 min)
1. New → Web Service → Docker
2. Name: `credit-platform-api`
3. Configurer 6 variables (voir tableau)
4. Deploy et attendre 5-10 min

### 4. Frontend sur Vercel (5 min)
1. New → Project
2. Root Directory: `frontend`
3. Variable: `VITE_API_URL`
4. Deploy et attendre 1-3 min

### 5. Finalisation (5 min)
1. Mettre à jour CORS avec URL Vercel réelle
2. Tester connexion
3. Changer mot de passe admin

**Total : 20-30 minutes**

---

## 🐛 Troubleshooting Rapide

| Problème | Solution |
|----------|----------|
| Backend "Connection refused" | Vérifier `DATABASE_URL` |
| Frontend "Network Error" | Vérifier `VITE_API_URL` et CORS |
| CORS Error | Ajouter URL Vercel dans `CORS_ALLOWED_ORIGINS` |
| Cold start lent | Normal sur Free (passer à Starter) |
| Build failed Render | Vérifier logs dans Dashboard |
| 404 sur refresh Vercel | Vérifier `vercel.json` (rewrites) |

---

## 📱 Checklist Post-Déploiement

### Sécurité
- [ ] Mot de passe admin changé
- [ ] JWT_SECRET unique et sécurisé
- [ ] CORS restreint aux domaines production
- [ ] HTTPS activé (auto sur Render/Vercel)

### Tests
- [ ] Health check répond
- [ ] Login fonctionne
- [ ] Création utilisateur fonctionne
- [ ] Gestion dossiers fonctionne
- [ ] Import/Export fonctionnent

### Documentation
- [ ] URLs notées (ci-dessus)
- [ ] Credentials sauvegardés (gestionnaire MDP)
- [ ] Équipe informée

---

## 📞 Contacts Utiles

### Support
- Render Community : https://community.render.com/
- Vercel Discussions : https://github.com/vercel/vercel/discussions

### Documentation
- Render Docs : https://render.com/docs
- Vercel Docs : https://vercel.com/docs
- PostgreSQL : https://www.postgresql.org/docs/

---

## 🎓 Guides à Consulter

| Guide | Quand l'utiliser |
|-------|------------------|
| **DEPLOYER_MAINTENANT.md** | Déployer maintenant (20 min) |
| **TOUT_EST_PRET.md** | Vue d'ensemble complète |
| **POSTGRESQL_READY.md** | Comprendre PostgreSQL |
| **NEXT_STEPS.md** | Instructions détaillées |
| **DEPLOYMENT_CHECKLIST.md** | Suivre progression pas à pas |

---

## 💡 Notes Importantes

### PostgreSQL vs MySQL
- ✅ PostgreSQL gratuit sur Render (1 GB)
- ✅ Plus simple : 6 variables au lieu de 8
- ✅ DATABASE_URL fournie automatiquement
- ✅ Pas de cold start DB

### Sécurité
- ⚠️ Ne jamais commiter `.env` ou secrets
- ⚠️ Changer password admin dès la première connexion
- ⚠️ Utiliser HTTPS uniquement (automatique)
- ⚠️ JWT_SECRET doit être unique (128 caractères)

### Performance
- Plan Free : Cold start 30-60s après 15 min inactivité
- Plan Starter : Pas de cold start, toujours rapide
- Compression HTTP : Activée automatiquement
- Cache statique : Configuré dans vercel.json

---

## 🔄 Workflows Utiles

### Redéployer Backend
```
Render Dashboard → Service → Manual Deploy → Deploy latest commit
```

### Redéployer Frontend
```
Vercel Dashboard → Project → Deployments → Redeploy
```

### Voir les Logs
```
Render : Dashboard → Service → Logs
Vercel : Dashboard → Project → Deployments → Logs
```

### Changer Variables
```
Render : Dashboard → Service → Environment → Edit
Vercel : Dashboard → Project → Settings → Environment Variables
```

---

## 📊 Métriques de Succès

### Performance
- ⏱️ Health check : < 500ms
- ⏱️ Page load : < 2s
- ⏱️ API response : < 1s

### Disponibilité
- 🟢 Uptime : > 99%
- 🟢 Health check : Status UP
- 🟢 Errors : < 1%

---

## ✅ Validation Finale

Tout est OK quand :
- [ ] `https://[backend]/actuator/health` retourne `{"status":"UP"}`
- [ ] `https://[frontend]` affiche la page login
- [ ] Login avec admin/admin123 fonctionne
- [ ] Dashboard s'affiche
- [ ] Pas d'erreurs dans console navigateur (F12)
- [ ] Mot de passe admin changé

---

**🎉 Tu as toutes les infos pour réussir !**

**Garde ce fichier ouvert pendant le déploiement** 📌
