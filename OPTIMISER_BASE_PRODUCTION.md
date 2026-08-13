# 🔧 Optimisation Base de Données - Instructions

## ⚠️ IMPORTANT : À faire UNE SEULE FOIS

Cette étape optimise la base de données PostgreSQL en production pour rendre l'import **8-10x plus rapide**.

---

## 📋 Prérequis

Vous avez besoin de :
- L'URL de connexion à votre base PostgreSQL en production
- `psql` installé OU accès à pgAdmin

---

## 🚀 Option 1 : Via Railway CLI (Recommandé)

Si vous utilisez Railway :

```bash
# 1. Installer Railway CLI (si pas encore fait)
# Windows: winget install railway
# OU télécharger depuis https://railway.app/cli

# 2. Se connecter
railway login

# 3. Lier au projet
railway link

# 4. Se connecter à PostgreSQL
railway run psql

# 5. Dans psql, exécuter :
\i scripts/optimisation_indexes_import.sql

# 6. Vérifier les index créés
SELECT tablename, indexname 
FROM pg_indexes 
WHERE tablename IN ('clients', 'credits', 'users', 'dossiers')
ORDER BY tablename;

# 7. Quitter
\q
```

---

## 🚀 Option 2 : Via DATABASE_URL directement

Si vous avez l'URL de la base :

```bash
# 1. Récupérer DATABASE_URL depuis Railway/Render
#    Dashboard → Variables → DATABASE_URL
#    Format : postgresql://user:password@host:port/database

# 2. Se connecter avec psql
psql "postgresql://user:password@host:port/database"

# 3. Dans psql, exécuter :
\i scripts/optimisation_indexes_import.sql

# 4. Vérifier
SELECT count(*) FROM pg_indexes 
WHERE tablename IN ('clients', 'credits', 'users', 'dossiers');
-- Devrait retourner au moins 15

# 5. Quitter
\q
```

---

## 🚀 Option 3 : Via pgAdmin (Interface graphique)

Si vous préférez une interface :

1. **Ouvrir pgAdmin**

2. **Créer une nouvelle connexion** :
   - Host : (depuis DATABASE_URL)
   - Port : (depuis DATABASE_URL)
   - Database : (depuis DATABASE_URL)
   - Username : (depuis DATABASE_URL)
   - Password : (depuis DATABASE_URL)

3. **Exécuter le script** :
   - Clic droit sur votre base → Query Tool
   - Fichier → Ouvrir : `scripts/optimisation_indexes_import.sql`
   - Cliquer sur ▶️ Execute (F5)

4. **Vérifier les résultats** :
   - Chercher les messages "CREATE INDEX"
   - Vous devriez voir environ 15 index créés

---

## 📊 Vérification

Après avoir exécuté le script, vérifiez que tout fonctionne :

```sql
-- Compter les index créés
SELECT count(*) FROM pg_indexes 
WHERE schemaname = 'public' 
  AND tablename IN ('clients', 'credits', 'users', 'dossiers');

-- Lister tous les index
SELECT tablename, indexname 
FROM pg_indexes 
WHERE schemaname = 'public' 
  AND tablename IN ('clients', 'credits', 'users', 'dossiers')
ORDER BY tablename, indexname;
```

**Résultat attendu** : Au moins 15 index listés

---

## ✅ Index qui seront créés

Le script crée ces index :

### Clients
- `idx_client_cin` → Recherche par CIN
- `idx_client_ville` → Filtrage par ville

### Crédits
- `idx_credit_reference` → Recherche par référence
- `idx_credit_client` → Lien avec clients
- `idx_credit_score` → Tri par score

### Dossiers
- `idx_dossier_credit` → Lien avec crédits
- `idx_dossier_agent` → Filtrage par agent
- `idx_dossier_statut` → Filtrage par statut
- `idx_dossier_mois` → Filtrage par mois
- `idx_dossier_recurrent` → Filtrage récurrents
- `idx_dossier_statut_agent` → Composite
- `idx_dossier_mois_statut` → Composite

### Users
- `idx_user_email` → Recherche par email
- `idx_user_actif` → Filtrage actifs

### Autres tables
- Index sur actions, historique, mois_precedent

---

## 🎯 Impact sur les Performances

**Sans ces index** :
- Import 3000 lignes = 3-5 minutes
- Requêtes lentes sur clients/crédits

**Avec ces index** :
- Import 3000 lignes = **15-20 secondes** ⚡
- Requêtes instantanées

---

## 🐛 Dépannage

### Erreur "permission denied"

Assurez-vous que l'utilisateur a les droits :
```sql
-- Vérifier les droits
\du

-- Si nécessaire, utiliser un super-utilisateur
```

### Erreur "relation already exists"

Index déjà créé, c'est normal. Le script utilise `IF NOT EXISTS`.

### psql non trouvé

Installer PostgreSQL client :
- Windows : `winget install PostgreSQL.PostgreSQL`
- Ou télécharger depuis https://www.postgresql.org/download/

---

## ✅ Après l'optimisation

1. **Tester l'import** :
   - Aller sur Admin → Import Excel
   - Choisir "Import optimisé (rapide)"
   - Importer 3000 lignes
   - Vérifier : terminé en 15-20 secondes !

2. **Surveiller les logs** :
   ```bash
   # Railway
   railway logs | grep "Import terminé"
   
   # Render
   # Dashboard → Logs
   ```

3. **Vérifier les performances** :
   - Chercher "🎉 Import terminé en X secondes"
   - X devrait être < 30 secondes pour 3000 lignes

---

## 🎉 C'est fait !

Une fois le script exécuté :

✅ Base de données optimisée  
✅ Import 8-10x plus rapide  
✅ Prêt pour la production  

**Profitez de votre import ultra-rapide !** 🚀
