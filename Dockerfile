# ─────────────────────────────────────────────
# Stage 1: Build
# Usa un'immagine Maven con JDK 21 per compilare
# e produrre il fat JAR.
# ─────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copia prima solo il pom.xml e scarica le dipendenze.
# Docker mette in cache questo layer — se il pom.xml non cambia,
# le dipendenze non vengono riscaricate ad ogni build.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia i sorgenti e compila saltando i test.
COPY src ./src
RUN mvn package -DskipTests -B

# ─────────────────────────────────────────────
# Stage 2: Runtime
# Immagine minimale JRE — non include Maven né JDK.
# ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Utente non-root per sicurezza
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /app/target/user-management-*.jar app.jar

EXPOSE 8080

# -XX:+UseContainerSupport: rispetta i limiti di memoria del container
# -XX:MaxRAMPercentage: usa al massimo il 75% della RAM disponibile
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]