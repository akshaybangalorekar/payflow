# ---------- build ----------
# Maven and JDK 25 both live in the image. Nothing is required on the host
# except Docker itself.
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build

# Dependency layer first: re-downloaded only when pom.xml changes.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline || true

COPY src/ src/
RUN mvn -B -q clean package -DskipTests

# ---------- run ----------
FROM eclipse-temurin:25-jre
WORKDIR /app

RUN apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/* \
 && groupadd -r app && useradd -r -g app app

COPY --from=build /build/target/*.jar app.jar
USER app

EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
