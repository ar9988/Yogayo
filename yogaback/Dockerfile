FROM openjdk:17-jdk-slim

RUN apt-get update && apt-get install -y wget && \
    wget https://github.com/jwilder/dockerize/releases/download/v0.7.0/dockerize-linux-amd64-v0.7.0.tar.gz && \
    tar -C /usr/local/bin -xvzf dockerize-linux-amd64-v0.7.0.tar.gz && \
    rm dockerize-linux-amd64-v0.7.0.tar.gz

COPY build/libs/YogaYoBack-0.0.1-SNAPSHOT.jar yogayo.jar

EXPOSE 8080

CMD ["dockerize", "-wait", "tcp://mysql:3306", "-timeout", "60s", "java", "-jar", "/yogayo.jar"]

