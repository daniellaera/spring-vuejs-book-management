FROM maven:3.9.9-eclipse-temurin-21-alpine AS maven-builder

COPY .m2/settings.xml /root/.m2/settings.xml

COPY pom.xml /app/pom.xml

RUN mvn -f /app/pom.xml dependency:go-offline # Download dependencies before copying source

COPY src /app/src

# Accept version as build arg
ARG APP_VERSION=dev
RUN mvn -f /app/pom.xml clean package -DskipTests -Drevision=${APP_VERSION}

FROM eclipse-temurin:21-jre-jammy

COPY --from=maven-builder app/target/backend-*.jar /app-service/backend.jar

WORKDIR /app-service

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "backend.jar"]