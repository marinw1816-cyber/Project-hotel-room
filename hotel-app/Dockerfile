# Stage 1: build the JAR with Maven and JDK 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
# Working directory inside the build container
WORKDIR /app
# Copy the build file first
COPY pom.xml .
# Copy the source code
COPY src ./src
# Build the JAR (tests already run in CI)
RUN mvn -B clean package -DskipTests

# Stage 2: small runtime image with only the Java 21 JRE
FROM eclipse-temurin:21-jre
# Working directory inside the runtime container
WORKDIR /app
# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar /app/hotel-app.jar
# Copy source code and pom.xml so they can be extracted later with docker cp
COPY --from=build /app/src /app/src
COPY --from=build /app/pom.xml /app/pom.xml
# Start the app when the container runs
ENTRYPOINT ["java","-jar","/app/hotel-app.jar"]
