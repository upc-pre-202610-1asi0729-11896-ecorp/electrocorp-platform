FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw

COPY src src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S electrocorp && adduser -S electrocorp -G electrocorp

COPY --from=build /workspace/target/electrocorp-platform-0.0.1-SNAPSHOT.jar /app/electrocorp-platform.jar

USER electrocorp

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/electrocorp-platform.jar"]
