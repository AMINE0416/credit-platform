# 🔄 Migration MySQL → PostgreSQL - Guide Complet

## ✅ Changements Effectués

La migration de MySQL vers PostgreSQL a été complétée. Voici ce qui a changé :

### 1. Dépendance Maven (pom.xml)

**Avant (MySQL)** :
```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

**Après (PostgreSQL)** :
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 2. Configuration Développement (application.properties)

**Avant (MySQL)** :
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/credit_platform
spring.datasource.username=${DB_USERNAME:root}
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

**Après (PostgreSQL)** :
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/credit_platform
spring.datasource.username=${DB_USERNAME:postgres}
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### 3. Configuration Production (application-prod.properties)

**Avant (MySQL)** :
```properties
spring.datasource.url=${SPRING_DATASOURCE_URL}
```

**Après (PostgreSQL)** :
```properties
spring.datasource.url=${DATABASE_URL}
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

> **Note** : Render PostgreSQL fournit automatiquement `DATABASE_URL` en format complet.

---

## 🚀 Déploiement avec PostgreSQL sur Render

### Option 1 : Via render.yaml (Recommandé - Infrastructure as Code)

Le fichier `render.yaml` a été mis à jour pour créer automatiquement la base PostgreSQL.

**Étapes** :

1. **Pusher le code** :
   ```bash
   git add .
   git commit -m "Migrate from MySQL to PostgreSQL"
   git push origin main
   ```

2. **Sur Render Dashboard** :
   - Aller sur [Render Dashboard](https://dashboard.render.com/)
   - Cliquer **"New"** → **"Blueprint"**
   - Connecter ton repository
   - Render va créer automatiquement :
     - La base PostgreSQL `credit-platform-db`
     - Le service web `credit-platform-api`
     - Les connexions entre les deux

3. **Ajouter les variables manquantes** :
   
   Dans le service web créé, aller dans **Environment** et ajouter :
   
   ```
   JWT_SECRET=[TON_SECRET_128_CARACTERES]
   JWT_EXPIRATION=7200000
   CORS_ALLOWED_ORIGINS=https://your-app.vercel.app,https://your-app-*.vercel.app
   ```

4. **Déployer** :
   - Render va automatiquement builder et déployer
   - La base PostgreSQL sera créée et connectée automatiquement

---

### Option 2 : Création Manuelle (Alternative)

Si tu préfères créer manuellement :

#### Étape 1 : Créer la Base PostgreSQL

1. Sur [Render Dashboard](https://dashboard.render.com/)
2. **New +** → **PostgreSQL**
3. Configure :
   - **Name** : `credit-platform-db`
   - **Database** : `credit_platform`
   - **User** : `credit_user`
   - **Region** : Choisir (ex: Frankfurt)
   - **Plan** : **Free** (ou Starter)
4. **Create Database**
5. Attendre 2-3 minutes

#### Étape 2 : Créer le Web Service

1. **New +** → **Web Service**
2. Connecte ton repository Git
3. Configure :
   - **Name** : `credit-platform-api`
   - **Runtime** : **Docker**
   - **Region** : **Même que la DB**
   - **Plan** : **Free** (ou Starter)

#### Étape 3 : Configurer les Variables d'Environnement

Dans l'onglet **Environment** du web service :

| Variable | Valeur | Description |
|----------|--------|-------------|
| `DATABASE_URL` | Copier depuis PostgreSQL Dashboard → **Internal Database URL** | URL complète PostgreSQL |
| `JWT_SECRET` | [Ton secret 128 caractères] | Secret JWT généré |
| `JWT_EXPIRATION` | `7200000` | 2 heures |
| `CORS_ALLOWED_ORIGINS` | `https://your-app.vercel.app,https://your-app-*.vercel.app` | URLs Vercel |
| `SERVER_PORT` | `8081` | Port serveur |
| `JAVA_OPTS` | `-Xmx512m -Xms256m -Dspring.profiles.active=prod` | Options JVM |

> **💡 Astuce** : Render PostgreSQL fournit automatiquement `DATABASE_URL` dans le bon format. Tu n'as pas besoin de le construire manuellement !

#### Étape 4 : Déployer

Cliquer **"Create Web Service"** et attendre 5-10 minutes.

---

## 💻 Développement Local avec PostgreSQL

### Installation PostgreSQL

#### Windows
```powershell
# Via Chocolatey
choco install postgresql

# Ou télécharger depuis
# https://www.postgresql.org/download/windows/
```

#### Linux (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
```

#### macOS
```bash
# Via Homebrew
brew install postgresql@15
brew services start postgresql@15
```

### Créer la Base de Données Locale

```bash
# Se connecter à PostgreSQL
psql -U postgres

# Créer la base
CREATE DATABASE credit_platform;

# Créer un utilisateur (optionnel)
CREATE USER credit_user WITH PASSWORD 'aminux';
GRANT ALL PRIVILEGES ON DATABASE credit_platform TO credit_user;

# Quitter
\q
```

### Démarrer l'Application

```bash
# Depuis la racine du projet
./mvnw spring-boot:run
```

Spring Boot créera automatiquement les tables au démarrage.

---

## 🔍 Différences MySQL vs PostgreSQL

### Ce qui NE CHANGE PAS

✅ Tes entités JPA (`@Entity`, `@Table`, etc.)  
✅ Tes repositories (`JpaRepository`, `@Query`)  
✅ Tes services et controllers  
✅ La logique métier  
✅ Les requêtes JPQL  
✅ Les transactions  

### Ce qui change (automatiquement géré par Hibernate)

| Aspect | MySQL | PostgreSQL |
|--------|-------|------------|
| Port par défaut | 3306 | 5432 |
| Auto-increment | `AUTO_INCREMENT` | `SERIAL` ou `SEQUENCE` |
| Boolean | `TINYINT(1)` | `BOOLEAN` |
| Texte long | `TEXT`, `LONGTEXT` | `TEXT` |
| Date/Time | `DATETIME` | `TIMESTAMP` |

> **Important** : Hibernate gère automatiquement ces différences grâce au dialecte. Tu n'as rien à changer dans ton code !

---

## ⚠️ Points d'Attention

### 1. Requêtes SQL Natives

Si tu as des requêtes SQL natives (avec `@Query(nativeQuery = true)`), vérifie qu'elles sont compatibles :

- ✅ **Standards SQL** : Fonctionnent sur les deux
- ⚠️ **Fonctions spécifiques** : Peuvent différer

**Exemple dans ton code** :

```java
// ✅ OK - Standard SQL
@Query("SELECT d FROM Dossier d WHERE d.status = :status")

// ⚠️ Vérifier si tu utilises des fonctions MySQL spécifiques
// MySQL: CONCAT_WS, DATE_FORMAT, IFNULL
// PostgreSQL: CONCAT_WS existe, TO_CHAR, COALESCE
```

### 2. Types de Données

JPA/Hibernate gère automatiquement la conversion, mais si tu as des `@Column(columnDefinition = "...")` spécifiques à MySQL :

```java
// ❌ Avant (MySQL spécifique)
@Column(columnDefinition = "DATETIME")
private LocalDateTime createdAt;

// ✅ Après (portable)
@Column
private LocalDateTime createdAt;
```

### 3. Charset et Collation

PostgreSQL utilise UTF-8 par défaut. Si tu avais configuré `utf8mb4` sur MySQL, c'est déjà géré.

---

## 🧪 Tests de Migration

### 1. Vérifier les Dépendances

```bash
./mvnw dependency:tree | grep -i postgres
```

Tu devrais voir : `org.postgresql:postgresql`

### 2. Démarrer Localement

```bash
./mvnw clean spring-boot:run
```

Vérifie dans les logs :
```
Hibernate: ... PostgreSQLDialect ...
HikariPool-1 - Starting...
Started CreditPlatformApplication in X seconds
```

### 3. Tester l'API

```bash
# Health check
curl http://localhost:8081/actuator/health

# Login
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

---

## 📊 Avantages PostgreSQL vs MySQL

### Pour Render

✅ **Base PostgreSQL gratuite native** (pas MySQL)  
✅ **Pas de cold start** même sur plan Free  
✅ **1 GB de stockage gratuit**  
✅ **Backups automatiques** (plan Starter)  
✅ **Connexion interne rapide** avec le web service  

### Pour ton Application

✅ **Plus performant** pour les requêtes complexes  
✅ **Meilleure gestion de la concurrence**  
✅ **Standards SQL plus stricts**  
✅ **Types de données avancés** (JSON, Arrays, etc.)  
✅ **Extensions puissantes** (PostGIS, pg_trgm, etc.)  

---

## 🔄 Rollback vers MySQL (si besoin)

Si tu veux revenir à MySQL, il suffit de :

1. Dans `pom.xml` : Remplacer `postgresql` par `mysql-connector-j`
2. Dans `application.properties` : Changer l'URL et le dialect
3. Redémarrer l'application

Les données ne sont pas affectées (les bases sont séparées).

---

## 📚 Documentation PostgreSQL

- [PostgreSQL Official](https://www.postgresql.org/docs/)
- [Spring Boot + PostgreSQL](https://spring.io/guides/gs/accessing-data-postgresql/)
- [Hibernate PostgreSQL Dialect](https://docs.jboss.org/hibernate/orm/6.0/userguide/html_single/Hibernate_User_Guide.html#database-dialect)
- [Render PostgreSQL](https://render.com/docs/databases)

---

## ✅ Checklist Migration

- [x] Dépendance PostgreSQL ajoutée dans `pom.xml`
- [x] Dépendance MySQL retirée
- [x] `application.properties` mis à jour
- [x] `application-prod.properties` mis à jour
- [x] `.env.example` mis à jour
- [x] `render.yaml` mis à jour
- [ ] PostgreSQL installé localement (si développement local)
- [ ] Base locale créée
- [ ] Application testée localement
- [ ] Déployée sur Render
- [ ] Tests de bout en bout réussis

---

## 🎉 Conclusion

La migration est **terminée** ! Ton application est maintenant prête pour PostgreSQL.

### Prochaines Étapes

1. **Tester localement** (si tu as PostgreSQL installé)
2. **Commiter et pusher** les changements
3. **Déployer sur Render** avec PostgreSQL
4. **Tester en production**

---

**La migration vers PostgreSQL est fluide avec Spring Boot. Aucun changement de code métier nécessaire !** 🚀
