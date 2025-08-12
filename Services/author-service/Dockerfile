FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# First copying wrapper files only (better cashe)
COPY .mvn/ .mvn/
COPY mvnw .
RUN chmod +x mvnw \
    && sed -i 's/\r$//' mvnw

# Copy everything to container
COPY . .

# Build the project
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Expose the application's port
EXPOSE 8082

# Run the JAR (replace with actual JAR name if needed)
ENTRYPOINT ["java", "-jar", "target/author-service-0.0.1-SNAPSHOT.jar"]
