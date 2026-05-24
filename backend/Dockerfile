FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -B -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache curl

WORKDIR /app

RUN addgroup -S atendeja && adduser -S atendeja -G atendeja

COPY --from=build /workspace/target/*.jar app.jar

USER atendeja

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
