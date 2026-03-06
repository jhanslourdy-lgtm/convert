# Étape 1 : Construction (Build)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copier le fichier de configuration des dépendances
COPY pom.xml .

# Copier le code source
COPY src ./src

# Compiler le projet et générer le JAR (en ignorant les tests pour gagner du temps)
RUN mvn clean package -DskipTests

# Étape 2 : Exécution (Runtime)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copier le JAR généré depuis l'étape précédente
# Note : Vérifie que le nom du jar correspond à ton artifactId dans pom.xml
COPY --from=build /app/target/*.jar app.jar

# Créer un dossier temporaire pour les conversions Aspose
RUN mkdir -p /tmp/aspose_conversions && chmod 777 /tmp/aspose_conversions

# Exposer le port configuré dans ton application.properties (8081)
EXPOSE 8081

# Lancer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]