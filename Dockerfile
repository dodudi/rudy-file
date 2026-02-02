# Build stage
FROM gradle:8.11-jdk21-alpine AS builder

WORKDIR /app

# Copy gradle files
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Copy source code
COPY src ./src

# Build the application
RUN chmod +x gradlew && ./gradlew build -x test --no-daemon

# Production stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy built jar from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8084

CMD ["java", "-jar", "app.jar"]
