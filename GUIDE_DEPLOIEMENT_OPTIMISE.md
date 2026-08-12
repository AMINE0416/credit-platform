# Guide de Déploiement Optimisé - Credit Platform

## 📋 Table des Matières
1. [Optimisations Backend (Java/Spring Boot)](#optimisations-backend)
2. [Optimisations Frontend (React)](#optimisations-frontend)
3. [Optimisations Base de Données](#optimisations-base-de-données)
4. [Configuration de Production](#configuration-de-production)
5. [Procédure de Déploiement](#procédure-de-déploiement)
6. [Vérifications Post-Déploiement](#vérifications-post-déploiement)

---

## 🚀 Optimisations Backend

### 1. Configuration JVM pour Production

**Créer**: `src/main/resources/application-prod.properties`
```properties
# Configuration JVM optimisée
server.tomcat.threads.max=200
server.tomcat.threads.min-spare=10
server.tomcat.connection-timeout=20000
server.tomcat.max-connections=10000

# Compression des réponses HTTP
server.compression.enabled=true
server.compression.mime-types=text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json
server.compression.min-response-size=1024

# Cache HTTP
spring.web.resources.cache.cachecontrol.max-age=31536000
spring.web.resources.cache.cachecontrol.cache-public=true

# Pool de connexions optimisé
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
spring.datasource.hikari.leak-detection-threshold=60000

# JPA/Hibernate Performance
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.jdbc.batch_versioned_data=true
spring.jpa.properties.hibernate.query.in_clause_parameter_padding=true

# Cache de second niveau
spring.jpa.properties.hibernate.cache.use_second_level_cache=true
spring.jpa.properties.hibernate.cache.use_query_cache=true
spring.jpa.properties.hibernate.cache.region.factory_class=org.hibernate.cache.jcache.JCacheRegionFactory

# Logging optimisé pour production
logging.level.root=WARN
logging.level.org.eqdom.creditplatform=INFO
logging.level.org.springframework.web=WARN
logging.level.org.hibernate.SQL=WARN
```

### 2. Ajouter les dépendances de cache dans `pom.xml`

```xml
<!-- Cache -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
<dependency>
    <groupId>org.ehcache</groupId>
    <artifactId>ehcache</artifactId>
</dependency>
<dependency>
    <groupId>javax.cache</groupId>
    <artifactId>cache-api</artifactId>
</dependency>
```

---

## ⚛️ Optimisations Frontend

### 1. Configuration Vite pour Production

**Mettre à jour**: `frontend/vite.config.ts`
```typescript
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  build: {
    // Minification optimale
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true, // Supprimer les console.log en production
        drop_debugger: true,
      },
    },
    // Code splitting optimisé
    rollupOptions: {
      output: {
        manualChunks: {
          'vendor-react': ['react', 'react-dom', 'react-router-dom'],
          'vendor-ui': ['lucide-react'],
          'vendor-utils': ['axios', 'date-fns'],
        },
      },
    },
    // Chunk size optimisé
    chunkSizeWarningLimit: 1000,
    // Compression
    reportCompressedSize: true,
  },
  // Optimisations de développement
  server: {
    port: 5173,
  },
  // Pre-bundling optimisé
  optimizeDeps: {
    include: ['react', 'react-dom', 'react-router-dom', 'axios'],
  },
});
```

### 2. Lazy Loading des Routes

**Mettre à jour**: `frontend/src/App.tsx`
```typescript
import { lazy, Suspense } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';

// Lazy loading des composants
const Login = lazy(() => import('./pages/Login'));
const AdminLayout = lazy(() => import('./layouts/AdminLayout'));
const AdminHome = lazy(() => import('./pages/AdminHome'));
const AdminDossiers = lazy(() => import('./pages/AdminDossiers'));
const DossierDetail = lazy(() => import('./pages/DossierDetail'));
const AdminGestionMensuelle = lazy(() => import('./pages/AdminGestionMensuelle'));
const AdminImport = lazy(() => import('./pages/AdminImport'));
const AgentDashboard = lazy(() => import('./pages/AgentDashboard'));

// Composant de chargement
const LoadingFallback = () => (
  <div style={{ 
    display: 'flex', 
    justifyContent: 'center', 
    alignItems: 'center', 
    height: '100vh',
    fontSize: '1.2rem',
    color: '#666'
  }}>
    Chargement...
  </div>
);

function App() {
  return (
    <Router>
      <Suspense fallback={<LoadingFallback />}>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/admin" element={<AdminLayout />}>
            <Route index element={<AdminHome />} />
            <Route path="dossiers" element={<AdminDossiers />} />
            <Route path="dossiers/:id" element={<DossierDetail />} />
            <Route path="gestion-mensuelle" element={<AdminGestionMensuelle />} />
            <Route path="import" element={<AdminImport />} />
          </Route>
          <Route path="/agent" element={<AgentDashboard />} />
          <Route path="/" element={<Login />} />
        </Routes>
      </Suspense>
    </Router>
  );
}

export default App;
```

### 3. Optimisation des Images et Assets

**Créer**: `frontend/public/.htaccess` (si serveur Apache)
```apache
# Compression GZIP
<IfModule mod_deflate.c>
  AddOutputFilterByType DEFLATE text/html text/plain text/xml text/css text/javascript application/javascript application/json
</IfModule>

# Cache des assets statiques
<IfModule mod_expires.c>
  ExpiresActive On
  ExpiresByType image/jpg "access plus 1 year"
  ExpiresByType image/jpeg "access plus 1 year"
  ExpiresByType image/png "access plus 1 year"
  ExpiresByType image/gif "access plus 1 year"
  ExpiresByType image/svg+xml "access plus 1 year"
  ExpiresByType text/css "access plus 1 month"
  ExpiresByType text/javascript "access plus 1 month"
  ExpiresByType application/javascript "access plus 1 month"
</IfModule>
```

### 4. Optimisation des Services API

**Mettre à jour**: `frontend/src/services/axiosConfig.ts`
```typescript
import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const axiosInstance = axios.create({
  baseURL: API_URL,
  timeout: 30000, // 30 secondes
  headers: {
    'Content-Type': 'application/json',
  },
});

// Cache simple pour les requêtes GET
const cache = new Map<string, { data: any; timestamp: number }>();
const CACHE_DURATION = 60000; // 1 minute

// Intercepteur de requête
axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // Vérifier le cache pour les requêtes GET
    if (config.method === 'get' && config.url) {
      const cached = cache.get(config.url);
      if (cached && Date.now() - cached.timestamp < CACHE_DURATION) {
        config.adapter = () => Promise.resolve({
          data: cached.data,
          status: 200,
          statusText: 'OK (cached)',
          headers: {},
          config,
        });
      }
    }

    return config;
  },
  (error) => Promise.reject(error)
);

// Intercepteur de réponse
axiosInstance.interceptors.response.use(
  (response) => {
    // Mettre en cache les réponses GET réussies
    if (response.config.method === 'get' && response.config.url) {
      cache.set(response.config.url, {
        data: response.data,
        timestamp: Date.now(),
      });
    }

    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Fonction pour nettoyer le cache
export const clearCache = () => {
  cache.clear();
};

export default axiosInstance;
```

---

## 🗄️ Optimisations Base de Données

### 1. Index sur les colonnes fréquemment utilisées

**Créer**: `scripts/optimisation_indexes.sql`
```sql
-- Index pour améliorer les performances des requêtes

-- Index sur les colonnes de recherche fréquentes
CREATE INDEX IF NOT EXISTS idx_dossier_statut ON dossier(statut);
CREATE INDEX IF NOT EXISTS idx_dossier_agent ON dossier(agent_id);
CREATE INDEX IF NOT EXISTS idx_dossier_numero_contrat ON dossier(numero_contrat);
CREATE INDEX IF NOT EXISTS idx_dossier_created_at ON dossier(created_at);

-- Index sur les colonnes de jointure
CREATE INDEX IF NOT EXISTS idx_client_dossier ON client(dossier_id);
CREATE INDEX IF NOT EXISTS idx_promesse_dossier ON promesse(dossier_id);
CREATE INDEX IF NOT EXISTS idx_historique_dossier ON historique_dossier(dossier_id);
CREATE INDEX IF NOT EXISTS idx_log_archivage_dossier ON log_archivage(dossier_id);

-- Index composites pour les requêtes complexes
CREATE INDEX IF NOT EXISTS idx_dossier_statut_agent ON dossier(statut, agent_id);
CREATE INDEX IF NOT EXISTS idx_dossier_date_statut ON dossier(created_at, statut);
CREATE INDEX IF NOT EXISTS idx_promesse_date_statut ON promesse(date_promesse, statut);

-- Index pour les recherches textuelles (optionnel, PostgreSQL)
CREATE INDEX IF NOT EXISTS idx_client_nom_prenom ON client USING gin(to_tsvector('french', nom || ' ' || prenom));

-- Statistiques de la base de données
ANALYZE dossier;
ANALYZE client;
ANALYZE promesse;
ANALYZE historique_dossier;
ANALYZE log_archivage;

-- Vérifier les index créés
SELECT 
    schemaname,
    tablename,
    indexname,
    indexdef
FROM pg_indexes 
WHERE schemaname = 'public'
ORDER BY tablename, indexname;
```

### 2. Nettoyage et Maintenance

**Créer**: `scripts/maintenance_database.sql`
```sql
-- Nettoyage des données obsolètes
-- Supprimer les logs d'archivage de plus de 6 mois
DELETE FROM log_archivage 
WHERE date_archivage < NOW() - INTERVAL '6 months';

-- Nettoyer l'historique trop ancien (garder 2 ans)
DELETE FROM historique_dossier 
WHERE date_modification < NOW() - INTERVAL '2 years';

-- VACUUM pour récupérer l'espace
VACUUM FULL ANALYZE dossier;
VACUUM FULL ANALYZE client;
VACUUM FULL ANALYZE promesse;
VACUUM FULL ANALYZE historique_dossier;
VACUUM FULL ANALYZE log_archivage;

-- Réindexation pour optimiser les performances
REINDEX TABLE dossier;
REINDEX TABLE client;
REINDEX TABLE promesse;
REINDEX TABLE historique_dossier;
```

---

## ⚙️ Configuration de Production

### 1. Variables d'Environnement Backend

**Mettre à jour**: `.env` (pour Railway/Render)
```env
# Database
DATABASE_URL=postgresql://user:password@host:5432/dbname
SPRING_DATASOURCE_URL=${DATABASE_URL}

# JWT
JWT_SECRET=votre_secret_jwt_genere_avec_le_script
JWT_EXPIRATION=86400000

# CORS
CORS_ALLOWED_ORIGINS=https://votre-frontend.vercel.app,https://www.votre-domaine.com

# JVM Options
JAVA_OPTS=-Xmx512m -Xms256m -XX:+UseG1GC -XX:MaxGCPauseMillis=200

# Spring Profile
SPRING_PROFILES_ACTIVE=prod

# Logging
LOGGING_LEVEL_ROOT=WARN
LOGGING_LEVEL_ORG_EQDOM=INFO
```

### 2. Variables d'Environnement Frontend

**Mettre à jour**: `frontend/.env.production`
```env
VITE_API_URL=https://votre-backend.railway.app/api
VITE_APP_NAME=Credit Platform
VITE_APP_VERSION=1.0.0
```

---

## 📦 Procédure de Déploiement

### Étape 1: Préparation

```powershell
# 1. Vérifier que tout fonctionne localement
cd c:\Users\asus\Documents\credit-platform

# 2. Tester le build backend
.\mvnw clean package -DskipTests

# 3. Tester le build frontend
cd frontend
npm run build
cd ..

# 4. Committer tous les changements
git add .
git commit -m "feat: optimisation des performances pour la production"
git push origin main
```

### Étape 2: Optimiser la Base de Données

```powershell
# Se connecter à votre base de données Railway/Render
# Via Railway CLI ou directement avec psql

# Exécuter les optimisations
psql $DATABASE_URL -f scripts/optimisation_indexes.sql
```

### Étape 3: Déploiement Backend (Railway)

```powershell
# 1. Installer Railway CLI (si pas déjà fait)
npm install -g @railway/cli

# 2. Se connecter
railway login

# 3. Lier le projet
railway link

# 4. Définir les variables d'environnement
railway variables set SPRING_PROFILES_ACTIVE=prod
railway variables set JWT_SECRET=(votre secret)
railway variables set CORS_ALLOWED_ORIGINS=https://votre-frontend.vercel.app

# 5. Déployer
railway up
```

### Étape 4: Déploiement Frontend (Vercel)

```powershell
# 1. Installer Vercel CLI (si pas déjà fait)
npm install -g vercel

# 2. Se connecter
vercel login

# 3. Déployer depuis le dossier frontend
cd frontend
vercel --prod

# 4. Configurer les variables d'environnement sur Vercel Dashboard
# VITE_API_URL=https://votre-backend.railway.app/api
```

### Étape 5: Configuration DNS (Optionnel)

Si vous avez un domaine personnalisé:

**Railway (Backend)**:
1. Aller dans Settings > Networking
2. Ajouter votre domaine: `api.votre-domaine.com`
3. Configurer le DNS avec les valeurs fournies

**Vercel (Frontend)**:
1. Aller dans Settings > Domains
2. Ajouter votre domaine: `www.votre-domaine.com`
3. Configurer le DNS avec les valeurs fournies

---

## ✅ Vérifications Post-Déploiement

### 1. Tests de Performance Backend

```powershell
# Test de temps de réponse
curl -w "@curl-format.txt" -o /dev/null -s https://votre-backend.railway.app/api/health

# Créer curl-format.txt
@"
    time_namelookup:  %{time_namelookup}s
       time_connect:  %{time_connect}s
    time_appconnect:  %{time_appconnect}s
   time_pretransfer:  %{time_pretransfer}s
      time_redirect:  %{time_redirect}s
 time_starttransfer:  %{time_starttransfer}s
                    ----------
         time_total:  %{time_total}s
"@ | Out-File -FilePath curl-format.txt -Encoding utf8
```

### 2. Tests de Performance Frontend

**Créer**: `scripts/test-performance.ps1`
```powershell
# Test de performance Frontend

Write-Host "=== Test de Performance Frontend ===" -ForegroundColor Cyan

# URL de votre application
$URL = "https://votre-frontend.vercel.app"

Write-Host "`nTest 1: Temps de chargement initial..." -ForegroundColor Yellow
$response = Measure-Command { Invoke-WebRequest -Uri $URL -UseBasicParsing }
Write-Host "Temps: $($response.TotalSeconds)s" -ForegroundColor Green

Write-Host "`nTest 2: Taille des assets..." -ForegroundColor Yellow
$page = Invoke-WebRequest -Uri $URL -UseBasicParsing
Write-Host "Taille HTML: $($page.Content.Length / 1KB) KB" -ForegroundColor Green

Write-Host "`nTest 3: Vérification HTTPS..." -ForegroundColor Yellow
if ($URL.StartsWith("https://")) {
    Write-Host "✓ HTTPS activé" -ForegroundColor Green
} else {
    Write-Host "✗ HTTPS non activé" -ForegroundColor Red
}

Write-Host "`nTest 4: Headers de cache..." -ForegroundColor Yellow
$headers = $page.Headers
if ($headers.'Cache-Control') {
    Write-Host "✓ Cache-Control: $($headers.'Cache-Control')" -ForegroundColor Green
} else {
    Write-Host "⚠ Pas de Cache-Control" -ForegroundColor Yellow
}

Write-Host "`n=== Recommandations ===" -ForegroundColor Cyan
Write-Host "1. Tester avec Google Lighthouse: https://pagespeed.web.dev/"
Write-Host "2. Tester avec GTmetrix: https://gtmetrix.com/"
Write-Host "3. Vérifier les Core Web Vitals dans Google Search Console"
```

### 3. Monitoring et Logs

```powershell
# Voir les logs Railway
railway logs

# Voir les logs Vercel
vercel logs

# Surveiller les erreurs
railway logs --filter error
```

### 4. Checklist de Déploiement

```markdown
## ✅ Checklist Post-Déploiement

### Backend
- [ ] Application démarre sans erreur
- [ ] Connexion à la base de données fonctionne
- [ ] Les endpoints API répondent correctement
- [ ] JWT et authentification fonctionnent
- [ ] CORS configuré correctement
- [ ] Logs ne montrent pas d'erreurs critiques
- [ ] Temps de réponse < 500ms pour les requêtes simples

### Frontend
- [ ] Application se charge en < 3 secondes
- [ ] Toutes les pages sont accessibles
- [ ] Login fonctionne
- [ ] Les appels API aboutissent
- [ ] Pas d'erreurs dans la console
- [ ] Design responsive fonctionne
- [ ] HTTPS activé

### Base de Données
- [ ] Index créés correctement
- [ ] Statistiques à jour (ANALYZE)
- [ ] Backup configuré
- [ ] Connexions pool optimisé

### Performance
- [ ] Score Lighthouse > 80
- [ ] First Contentful Paint < 2s
- [ ] Time to Interactive < 3.5s
- [ ] Pas de ressources bloquantes
```

---

## 🚨 Dépannage

### Problème: Application lente

**Solution**:
```sql
-- Vérifier les requêtes lentes
SELECT 
    query,
    calls,
    total_time,
    mean_time,
    max_time
FROM pg_stat_statements
WHERE mean_time > 100
ORDER BY mean_time DESC
LIMIT 10;
```

### Problème: Timeout sur les requêtes

**Solution**: Augmenter le timeout dans `application-prod.properties`
```properties
spring.datasource.hikari.connection-timeout=60000
server.tomcat.connection-timeout=60000
```

### Problème: Erreurs CORS

**Solution**: Vérifier dans Railway Dashboard
```properties
CORS_ALLOWED_ORIGINS=https://votre-frontend.vercel.app,https://www.votre-domaine.com
```

---

## 📊 Objectifs de Performance

| Métrique | Objectif | Critique |
|----------|----------|----------|
| Temps de chargement initial | < 3s | < 5s |
| Temps de réponse API | < 500ms | < 1s |
| First Contentful Paint | < 2s | < 3s |
| Time to Interactive | < 3.5s | < 5s |
| Score Lighthouse | > 80 | > 60 |

---

## 📝 Notes Importantes

1. **Toujours tester en local avant de déployer**
2. **Faire un backup de la base de données avant les migrations**
3. **Surveiller les logs pendant les premières heures après déploiement**
4. **Utiliser un domaine HTTPS pour la production**
5. **Configurer des alertes pour surveiller les performances**

---

## 🔄 Maintenance Continue

### Quotidien
- Vérifier les logs d'erreur
- Surveiller le temps de réponse

### Hebdomadaire
- Analyser les métriques de performance
- Vérifier les backups de la base de données

### Mensuel
- Exécuter `scripts/maintenance_database.sql`
- Mettre à jour les dépendances
- Revoir les optimisations

---

**Créé le**: $(Get-Date -Format "yyyy-MM-dd")
**Dernière mise à jour**: $(Get-Date -Format "yyyy-MM-dd")
