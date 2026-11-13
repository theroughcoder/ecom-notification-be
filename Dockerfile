FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# ecom-events is a shared, unpublished library — build+install it into the
# local .m2 cache first so ecom-notification-be's pom can resolve it as a normal dependency.
COPY ecom-events ecom-events
RUN --mount=type=cache,target=/root/.m2 mvn -f ecom-events/pom.xml -q install -DskipTests

COPY ecom-notification-be/pom.xml ecom-notification-be/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -f ecom-notification-be/pom.xml -q dependency:go-offline

COPY ecom-notification-be/src ecom-notification-be/src
RUN --mount=type=cache,target=/root/.m2 mvn -f ecom-notification-be/pom.xml -q package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/ecom-notification-be/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
