# --- FASE 1: Build ---
# Usiamo Maven con Java 21 per compilare il progetto
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copiamo il file pom.xml e scarichiamo le dipendenze (cache)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiamo il codice sorgente e compiliamo il file .jar saltando i test
# (i test li abbiamo già fatti girare nella pipeline CI/CD!)
COPY src ./src
RUN mvn clean package -DskipTests

# --- FASE 2: Run ---
# Usiamo una versione leggera di Java 21 per l'esecuzione
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiamo solo il file .jar generato nella fase precedente
COPY --from=build /app/target/*.jar app.jar

# Esponiamo la porta 8080
EXPOSE 8080

# Comando per avviare l'applicazione
ENTRYPOINT ["java", "-jar", "app.jar"]