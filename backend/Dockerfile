FROM maven:3.9.9-eclipse-temurin-21-alpine as maven-builder

COPY .m2/settings.xml /root/.m2/settings.xml

# Copy pom.xml first to leverage caching for dependency downloads
COPY pom.xml /app/pom.xml

RUN mvn -f /app/pom.xml dependency:go-offline # Download dependencies before copying source

COPY src /app/src

RUN mvn -f /app/pom.xml clean package -DskipTests

FROM openjdk:21-jdk-slim

COPY --from=maven-builder app/target/backend-0.0.1-SNAPSHOT.jar /app-service/backend-0.0.1-SNAPSHOT.jar

WORKDIR /app-service

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "backend-0.0.1-SNAPSHOT.jar"]