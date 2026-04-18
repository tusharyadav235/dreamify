# ── Use a lightweight JRE for the final image ──
FROM eclipse-temurin:17-jre-alpine

# Create a non-root user for security
RUN addgroup -S dreamify && adduser -S dreamify -G dreamify

WORKDIR /app

# ── THE CRITICAL CHANGE ──
# We stop building INSIDE Docker. 
# We COPY the fresh JAR that Maven just built in the GitHub Action runner.
COPY target/*.jar app.jar

# Fix permissions for the non-root user
RUN chown -R dreamify:dreamify /app
USER dreamify

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
