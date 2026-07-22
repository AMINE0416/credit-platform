# ✅ Checklist Complète de Déploiement

Utilise cette checklist pour suivre ta progression dans le déploiement.

---

## 📋 Phase 1 : Préparation (5-10 min)

### Vérification des Fichiers
- [ ] Tous les fichiers de configuration sont présents (exécuter `.\scripts\pre-deploy-check.ps1`)
- [ ] Code commité localement
- [ ] Code pushé sur GitHub/GitLab/Bitbucket

### Génération des Secrets
- [ ] JWT Secret généré (128 caractères) avec `.\scripts\generate-jwt-secret.ps1`
- [ ] JWT Secret copié et sauvegardé en lieu sûr

### Repository Git
- [ ] Repository accessible en ligne
- [ ] Branche `main` à jour
- [ ] Pas de secrets commités (vérifier `.gitignore`)

---

## 🗄️ Phase 2 : Base de Données (5-10 min)

### Création de la Base
- [ ] Compte créé sur [Render](https://dashboard.render.com/) (ou autre provider)
- [ ] Base de données MySQL créée
- [ ] Nom de la base : `credit_platform`
- [ ] Région choisie (même que celle du backend)

### Informations de Connexion
- [ ] URL de connexion notée (Internal Database URL)
- [ ] Username noté : `___________________`
- [ ] Password noté (en lieu sûr) : `___________________`
- [ ] Port noté (généralement 3306) : `___________________`

### Format JDBC
- [ ] URL JDBC formatée correctement :
  ```
  jdbc:mysql://[HOST]:[PORT]/credit_platform?useSSL=true&serverTimezone=UTC
  ```

---

## 🔧 Phase 3 : Backend Render (10-15 min)

### Création du Service
- [ ] Sur [Render Dashboard](https://dashboard.render.com/)
- [ ] "New +" → "Web Service" cliqué
- [ ] Repository Git connecté
- [ ] Branche `main` sélectionnée
- [ ] Runtime : **Docker** sélectionné
- [ ] Region : **Même que la DB** choisie
- [ ] Plan choisi : Free ☐ ou Starter ☐

### Variables d'Environnement (8 au total)

#### Connexion Base de Données
- [ ] `SPRING_DATASOURCE_URL` = `jdbc:mysql://...`
- [ ] `DB_USERNAME` = `credit_user` (ou ton username)
- [ ] `DB_PASSWORD` = `[ton password DB]` (🔒 marqué comme sensible)

#### Sécurité JWT
- [ ] `JWT_SECRET` = `[128 caractères hex générés]` (🔒 marqué comme sensible)
- [ ] `JWT_EXPIRATION` = `7200000`

#### CORS (temporaire, à mettre à jour après Vercel)
- [ ] `CORS_ALLOWED_ORIGINS` = `https://localhost:3000,https://*.vercel.app`

#### Configuration Serveur
- [ ] `SERVER_PORT` = `8081`
- [ ] `JAVA_OPTS` = `-Xmx512m -Xms256m -Dspring.profiles.active=prod`

### Déploiement
- [ ] "Create Web Service" cliqué
- [ ] Build en cours... (attendre 5-10 min)
- [ ] Build réussi ✅
- [ ] Service démarré ✅

### Vérification Backend
- [ ] URL notée : `https://__________________.onrender.com`
- [ ] Health check testé : `https://[URL]/actuator/health`
- [ ] Réponse reçue : `{"status":"UP"}` ✅
- [ ] Logs vérifiés (pas d'erreurs)

---

## 🎨 Phase 4 : Frontend Vercel (5-10 min)

### Création du Projet
- [ ] Sur [Vercel Dashboard](https://vercel.com/dashboard)
- [ ] "Add New..." → "Project" cliqué
- [ ] Repository Git importé
- [ ] Branche `main` sélectionnée

### Configuration Build
- [ ] Framework Preset : **Vite** sélectionné
- [ ] **Root Directory** : `frontend` ⚠️ IMPORTANT
- [ ] Build Command : `npm run build` (vérifié)
- [ ] Output Directory : `dist` (vérifié)
- [ ] Install Command : `npm install` (vérifié)

### Variables d'Environnement
- [ ] Variable ajoutée : `VITE_API_URL`
- [ ] Valeur : `https://[TON_URL_RENDER].onrender.com` (SANS `/` à la fin)
- [ ] Appliquée à : **Production** ☑ et **Preview** ☑

### Déploiement
- [ ] "Deploy" cliqué
- [ ] Build en cours... (attendre 1-3 min)
- [ ] Build réussi ✅
- [ ] Déploiement réussi ✅

### Vérification Frontend
- [ ] URL notée : `https://__________________.vercel.app`
- [ ] Site accessible dans le navigateur ✅
- [ ] Page de login s'affiche ✅
- [ ] Pas d'erreurs dans la console du navigateur (F12) ✅

---

## 🔄 Phase 5 : Configuration Finale (5 min)

### Mise à Jour CORS
- [ ] Retour sur Render Dashboard
- [ ] Service backend ouvert
- [ ] Onglet "Environment" sélectionné
- [ ] Variable `CORS_ALLOWED_ORIGINS` modifiée avec URL Vercel réelle :
  ```
  https://[TON_APP].vercel.app,https://[TON_APP]-*.vercel.app
  ```
- [ ] "Save Changes" cliqué
- [ ] Redéploiement automatique lancé (attendre 1-2 min)
- [ ] Redéploiement terminé ✅

### Test de Bout en Bout
- [ ] Ouvrir `https://[TON_APP].vercel.app`
- [ ] Aller sur la page de login
- [ ] Saisir : `admin` / `admin123`
- [ ] Cliquer "Se connecter"
- [ ] ✅ Connexion réussie → Redirection vers le dashboard
- [ ] ❌ Erreur → Consulter section Troubleshooting

---

## 🔒 Phase 6 : Sécurité (IMPORTANT - 5 min)

### Changement du Mot de Passe Admin
- [ ] Connecté avec `admin` / `admin123`
- [ ] Aller dans Paramètres du profil / Utilisateurs
- [ ] Modifier le mot de passe admin
- [ ] Nouveau mot de passe sécurisé saisi
- [ ] Mot de passe changé ✅
- [ ] Déconnexion et reconnexion avec nouveau mot de passe testée ✅

### Vérifications Sécurité
- [ ] `JWT_SECRET` est unique (pas celui de l'exemple)
- [ ] `JWT_SECRET` n'est pas commité dans Git
- [ ] `DB_PASSWORD` marqué comme sensible sur Render
- [ ] CORS restreint aux URLs Vercel uniquement
- [ ] HTTPS activé (automatique sur Render et Vercel) ✅

---

## 🧪 Phase 7 : Tests Fonctionnels (10 min)

### Tests Authentification
- [ ] Connexion admin fonctionne
- [ ] Déconnexion fonctionne
- [ ] Connexion avec mauvais mot de passe refuse l'accès

### Tests Gestion Utilisateurs (Admin)
- [ ] Créer un nouvel utilisateur agent
- [ ] Lister les utilisateurs
- [ ] Modifier un utilisateur
- [ ] (Optionnel) Supprimer un utilisateur test

### Tests Gestion Dossiers
- [ ] Créer un nouveau dossier
- [ ] Lister les dossiers
- [ ] Voir le détail d'un dossier
- [ ] Modifier le statut d'un dossier
- [ ] (Optionnel) Affecter un dossier à un agent

### Tests Import/Export
- [ ] Importer un fichier Excel (si disponible)
- [ ] Exporter des données
- [ ] Vérifier le fichier exporté

### Tests Dashboard
- [ ] KPIs s'affichent correctement
- [ ] Graphiques (si présents) se chargent
- [ ] Filtres fonctionnent

---

## 📊 Phase 8 : Monitoring (5 min)

### Vérification des Logs

#### Backend (Render)
- [ ] Dashboard → Service → Logs ouvert
- [ ] Pas d'erreurs critiques ✅
- [ ] Application démarre correctement ✅

#### Frontend (Vercel)
- [ ] Dashboard → Project → Deployments ouvert
- [ ] Dernier déploiement : Status "Ready" ✅
- [ ] Build logs vérifiés (pas d'erreurs) ✅

### Configuration Monitoring (Optionnel)
- [ ] UptimeRobot ou autre service de ping configuré
- [ ] Vercel Analytics activé
- [ ] Alertes configurées

---

## 📝 Phase 9 : Documentation (5 min)

### URLs Finales
Documente tes URLs ici pour référence :

```
Backend Render : https://_________________________.onrender.com
Frontend Vercel : https://_________________________.vercel.app
Health Check : https://_________________________.onrender.com/actuator/health
Swagger (si activé) : https://_________________________.onrender.com/swagger-ui.html
```

### Credentials Production
⚠️ À sauvegarder en lieu sûr (gestionnaire de mots de passe) :

```
Admin Username : admin
Admin Password : _________________________ (nouveau mot de passe)

DB Host : _________________________
DB Name : credit_platform
DB User : _________________________
DB Password : _________________________

JWT Secret : _________________________ (128 caractères)
```

### Informations Techniques
```
Plan Render Backend : Free ☐ / Starter ☐
Plan Render Database : Free ☐ / Starter ☐
Plan Vercel : Hobby ☐ / Pro ☐

Region Backend : _________________________
Region Database : _________________________
```

---

## 🎉 Phase 10 : Validation Finale

### Checklist Globale
- [ ] ✅ Backend déployé et accessible
- [ ] ✅ Frontend déployé et accessible
- [ ] ✅ Base de données configurée et connectée
- [ ] ✅ CORS configuré correctement
- [ ] ✅ Health check répond
- [ ] ✅ Login fonctionne
- [ ] ✅ Mot de passe admin changé
- [ ] ✅ Tests fonctionnels réussis
- [ ] ✅ Logs vérifiés (pas d'erreurs)
- [ ] ✅ URLs documentées
- [ ] ✅ Credentials sauvegardés

### Communication
- [ ] Équipe informée du déploiement
- [ ] URLs partagées avec les parties prenantes
- [ ] Documentation accessible

---

## ⚠️ Troubleshooting

Si tu rencontres un problème à une étape, consulte :

1. **DEPLOY_QUICK_START.md** → Section "Problèmes Courants"
2. **DEPLOYMENT_GUIDE.md** → Section "Troubleshooting"
3. **Logs Render** : Dashboard → Service → Logs
4. **Logs Vercel** : Dashboard → Project → Deployments → Logs
5. **Console navigateur** : F12 → Console (erreurs frontend)

### Problèmes Fréquents et Solutions Rapides

| Problème | Solution Rapide |
|----------|-----------------|
| Backend : Connection refused | Vérifie `SPRING_DATASOURCE_URL`, username, password |
| Frontend : Network Error | Vérifie `VITE_API_URL` sur Vercel + CORS sur Render |
| Backend : Lent au démarrage | Normal sur plan Free (cold start 60s) |
| Frontend : 404 sur refresh | Vérifie `vercel.json` (rewrites configurés) |
| CORS Error dans console | URL Vercel doit être dans `CORS_ALLOWED_ORIGINS` |

---

## 🚀 Prochaines Étapes (Post-Déploiement)

Une fois tout validé :

### Court Terme (Semaine 1)
- [ ] Configurer un domaine personnalisé (optionnel)
- [ ] Ajouter des utilisateurs agents
- [ ] Importer les premiers dossiers
- [ ] Former les utilisateurs finaux

### Moyen Terme (Mois 1)
- [ ] Passer au plan Starter si cold start gênant
- [ ] Configurer des backups automatiques de DB
- [ ] Mettre en place un monitoring avancé
- [ ] Optimiser les performances

### Long Terme (Trimestre 1)
- [ ] Créer un environnement staging
- [ ] Mettre en place CI/CD avec tests automatiques
- [ ] Implémenter de nouvelles fonctionnalités
- [ ] Analyser les métriques d'usage

---

## 📞 Support

- 📚 **Documentation** : Consulte tous les fichiers `DEPLOYMENT_*.md`
- 💬 **Communauté** : [Render Community](https://community.render.com/) · [Vercel Discussions](https://github.com/vercel/vercel/discussions)
- 🐛 **Issues** : Ouvre une issue sur ton repository Git

---

## ✅ Statut Final

Une fois toutes les phases complétées :

```
🎉 DÉPLOIEMENT RÉUSSI !

✅ Backend opérationnel
✅ Frontend opérationnel
✅ Base de données connectée
✅ Sécurité configurée
✅ Tests validés

L'application Credit Platform est maintenant en production ! 🚀
```

Date du déploiement : ___________________

Déployé par : ___________________

---

**Imprime ou sauvegarde cette checklist pour suivre ta progression !**
