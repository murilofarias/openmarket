# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Create logs directory
RUN mkdir -p /app/logs

# Download Elastic APM Java Agent
ADD https://repo1.maven.org/maven2/co/elastic/apm/elastic-apm-agent/1.52.1/elastic-apm-agent-1.52.1.jar /app/elastic-apm-agent.jar

# Copy the built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the application port
EXPOSE 8080 5005

# Run the application with APM agent and debug enabled
# APM configuration is done via environment variables in docker-compose.yml
ENTRYPOINT ["java", \
    "-javaagent:/app/elastic-apm-agent.jar", \
    "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", \
    "-jar", "app.jar"]
