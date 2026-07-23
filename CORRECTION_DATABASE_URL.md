# 🔧 Correction DATABASE_URL pour Railway

## 🎯 Problème Identifié

Même avec le service Postgres provisionné sur Railway et `DATABASE_URL` configurée correctement dans les variables d'environnement, l'application crashait au démarrage avec une erreur de connexion à la base de données.

### Cause racine

1. **`application.properties` codé en dur** :
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/credit_platform
   ```
   → Spring Boot ignorait complètement `DATABASE_URL` et tentait de se connecter à `localhost:5432` (inexistant dans le conteneur Railway)

2. **Format incompatible** :
   - Railway fournit `DATABASE_URL` au format : `postgres://user:pass@host:port/db`
   - Driver JDBC PostgreSQL exige : `jdbc:postgresql://host:port/db`
   - Même si on référençait la variable, il faudrait la convertir

## ✅ Solution Implémentée

### 1. Classe `DataSourceConfig.java`

Créé une classe de configuration qui :
- Parse automatiquement `DATABASE_URL` (format Railway/Heroku)
- Convertit au format JDBC PostgreSQL
- Fournit un fallback pour le développement local

**Fichier** : `src/main/java/org/eqdom/creditplatform/config/DataSourceConfig.java`

```java
@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");

        // Fallback pour le dev local (pas de DATABASE_URL)
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return DataSourceBuilder.create()
                    .url("jdbc:postgresql://localhost:5432/credit_platform")
                    .username(System.getenv().getOrDefault("DB_USERNAME", "postgres"))
                    .password(System.getenv().getOrDefault("DB_PASSWORD", "postgres123"))
                    .driverClassName("org.postgresql.Driver")
                    .build();
        }

        try {
            // Parse DATABASE_URL au format postgres://user:pass@host:port/db
            URI dbUri = new URI(databaseUrl);
            String[] userInfo = dbUri.getUserInfo().split(":", 2);
            String username = userInfo[0];
            String password = userInfo.length > 1 ? userInfo[1] : "";

            // Convertit en format JDBC
            String jdbcUrl = "jdbc:postgresql://" + dbUri.getHost() + ":" 
                           + dbUri.getPort() + dbUri.getPath();

            return DataSourceBuilder.create()
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .driverClassName("org.postgresql.Driver")
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de parser DATABASE_URL : " + databaseUrl, e);
        }
    }
}
```

### 2. Nettoyage `application.properties`

**Supprimé** (lignes en conflit) :
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/credit_platform
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:postgres123}
```

**Conservé** (toujours nécessaires) :
```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
server.port=8081
# ... reste de la configuration
```

## 🚀 Comportement

### En local (développement)
- `DATABASE_URL` non défini → fallback sur `localhost:5432` avec `DB_USERNAME`/`DB_PASSWORD`
- Fonctionne comme avant

### Sur Railway (production)
- `DATABASE_URL` fourni par le service Postgres → parse et convertit automatiquement
- Utilise la base Postgres provisionnée

## 🔍 Point de Vérification Secondaire

Si le healthcheck échoue après ce fix :

1. **Vérifier le port exposé** :
   - Railway doit router le trafic public vers le port `8081`
   - Settings → Service → Networking
   - La variable `SERVER_PORT=8081` correspond déjà

2. **Vérifier le lien DATABASE_URL** :
   - Variables → `DATABASE_URL` = `${{Postgres.DATABASE_URL}}` ✅
   - (déjà fait, visible dans ta capture d'écran)

## 📦 Prochaines Étapes

1. **Commit et push** :
   ```bash
   git add .
   git commit -m "fix: parse DATABASE_URL pour Railway - support format postgres://"
   git push origin main
   ```

2. **Railway redéploie automatiquement** après le push

3. **Vérifier les logs** sur Railway :
   - Voir "Successfully connected to database" ou équivalent
   - Plus d'erreur "Connection to localhost:5432 refused"

## ✅ Validation

Code compilé avec succès :
```
[INFO] BUILD SUCCESS
[INFO] Compiling 56 source files
```

Cette correction résout le problème fondamental de la connexion DB sur Railway ! 🎉
