FROM maven:3.9.9-eclipse-temurin-21-alpine AS maven-builder

COPY .m2/settings.xml /root/.m2/settings.xml

COPY pom.xml /app/pom.xml

RUN mvn -f /app/pom.xml dependency:go-offline # Download dependencies before copying source

COPY src /app/src

RUN mvn -f /app/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy

COPY --from=maven-builder app/target/backend-0.0.1-SNAPSHOT.jar /app-service/backend-0.0.1-SNAPSHOT.jar

WORKDIR /app-service

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "backend-0.0.1-SNAPSHOT.jar"]