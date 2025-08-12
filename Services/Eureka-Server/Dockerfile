FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy everything to container
COPY . .

# Build the project
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Expose the application's port
EXPOSE 8761

# Run the JAR (replace with actual JAR name if needed)
ENTRYPOINT ["java", "-jar", "target/Eureka-Server-0.0.1-SNAPSHOT.jar"]
