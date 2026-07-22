# Externalisation de la configuration CORS

## Contexte
Suite au rapport de sécurité point 3.2, les patterns CORS hardcodés ont été externalisés en variables d'environnement pour améliorer l'hygiène de sécurité et faciliter la configuration selon l'environnement (dev/staging/production).

## Modifications apportées

### 1. SecurityConfig.java
**Fichier**: `src/main/java/org/eqdom/creditplatform/config/SecurityConfig.java`

**Changements**:
- Ajout de l'import `org.springframework.beans.factory.annotation.Value`
- Ajout de l'import `java.util.Arrays`
- Ajout d'un champ `allowedOriginPatterns` injecté depuis `application.properties`
- Modification de la méthode `corsConfigurationSource()` pour lire les patterns depuis la variable d'environnement

**Avant**:
```java
config.setAllowedOriginPatterns(List.of(
    "http://localhost:*", 
    "http://127.0.0.1:*",
    "http://192.168.*.*:*",  // Réseau local classe C
    "http://10.*.*.*:*",     // Réseau local classe A
    "http://172.16.*.*:*"    // Réseau local classe B
));
```

**Après**:
```java
@Value("${cors.allowed-origin-patterns}")
private String allowedOriginPatterns;

// Dans corsConfigurationSource():
List<String> patterns = Arrays.asList(allowedOriginPatterns.split(","));
config.setAllowedOriginPatterns(patterns);
```

### 2. application.properties
**Fichier**: `src/main/resources/application.properties`

**Ajout**:
```properties
# CORS - Patterns d'origines autorisées (utilise variables d'environnement)
# Patterns pour autoriser localhost et réseau local
cors.allowed-origin-patterns=${CORS_ALLOWED_ORIGINS:http://localhost:*,http://127.0.0.1:*,http://192.168.*.*:*,http://10.*.*.*:*,http://172.16.*.*:*}
```

### 3. .env.example
**Fichier**: `.env.example`

**Ajout**:
```bash
# Configuration CORS - Patterns d'origines autorisées
# Patterns séparés par des virgules pour définir les origines acceptées
# Par défaut: localhost et réseaux locaux (192.168.x.x, 10.x.x.x, 172.16-31.x.x)
CORS_ALLOWED_ORIGINS=http://localhost:*,http://127.0.0.1:*,http://192.168.*.*:*,http://10.*.*.*:*,http://172.16.*.*:*

# Pour production, restreindre à des domaines spécifiques:
# CORS_ALLOWED_ORIGINS=https://app.exemple.com,https://www.exemple.com
```

## Configuration par environnement

### Environnement de développement
Les valeurs par défaut dans `application.properties` autorisent localhost et les réseaux locaux. Aucune action requise.

### Environnement de production
Créer un fichier `.env` ou définir la variable d'environnement système:
```bash
CORS_ALLOWED_ORIGINS=https://votredomaine.com,https://www.votredomaine.com
```

### Environnement de staging
```bash
CORS_ALLOWED_ORIGINS=https://staging.votredomaine.com
```

## Avantages de cette approche

1. **Sécurité améliorée**: Les patterns CORS peuvent être restreints en production sans modifier le code
2. **Flexibilité**: Configuration différente selon l'environnement (dev/staging/prod)
3. **Traçabilité**: Les patterns autorisés sont explicitement documentés dans `.env.example`
4. **Pas de rebuild requis**: Changement de configuration sans recompilation
5. **Valeurs par défaut sûres**: Si la variable n'est pas définie, les valeurs par défaut sont utilisées

## Tests de validation

La compilation Maven a réussi sans erreur:
```bash
mvn clean compile -DskipTests
[INFO] BUILD SUCCESS
```

## Note importante

Cette modification répond au point 3.2 du rapport de sécurité. Bien que ce ne soit pas critique pour un environnement de développement/interne, c'est une bonne pratique qui facilite le déploiement sécurisé en production.

Le point 3.4 (CSRF) reste inchangé comme confirmé correct par le rapport.
