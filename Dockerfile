# Dockerfile multi-stage pour Spring Boot
FROM maven:3.9-eclipse-temurin-17 AS build

# Définir le répertoire de travail
WORKDIR /app

# Copier les fichiers de configuration Maven
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Télécharger les dépendances (mise en cache si pom.xml n'a pas changé)
RUN mvn dependency:go-offline -B

# Copier le code source
COPY src ./src

# Builder l'application
RUN mvn clean package -DskipTests

# Stage de production
FROM eclipse-temurin:17-jre-alpine

# Créer un utilisateur non-root pour la sécurité
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# Copier le JAR depuis le stage de build
COPY --from=build /app/target/*.jar app.jar

# Exposer le port
EXPOSE 8081

# Variables d'environnement par défaut (seront surchargées par Render)
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SERVER_PORT=8081

# Point d'entrée
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
