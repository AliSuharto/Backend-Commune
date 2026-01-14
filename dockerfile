# Stage 1 : Build
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copier les fichiers Maven
COPY pom.xml .
COPY src ./src

# Construire l'application
RUN apt-get update && apt-get install -y maven
RUN mvn clean package -DskipTests

# Stage 2 : Runtime
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copier le JAR depuis le stage de build
COPY --from=build /app/target/Dev-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]