FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
# Only the Spring Boot runnable jar is produced (the plain jar is disabled in build.gradle.kts),
# so this glob is unambiguous.
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
