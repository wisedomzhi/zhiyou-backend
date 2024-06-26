FROM openjdk:17-jdk-slim

VOLUME /tmp

ADD ./zhiyou-backend-0.0.1-SNAPSHOT.jar zhiyou-backend-0.0.1-SNAPSHOT.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "zhiyou-backend-0.0.1-SNAPSHOT.jar", "--spring.profiles.active=prod"]
