FROM openjdk:17-jdk-slim

COPY build/libs/YogaYoBack-0.0.1-SNAPSHOT.jar yogayo.jar

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
CMD ["java", "-jar", "/yogayo.jar"]

