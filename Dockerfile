FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -g 1000 spring && adduser -D -u 1000 -G spring spring
COPY --from=build /build/target/tramitea-admin-be.jar app.jar
USER spring

EXPOSE 8080
# Sin esto la JVM toma como techo el 25% de la RAM del HOST (~1,9 GB). Con
# mem_limit en el compose, el heap queda acotado al 75% del límite.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
