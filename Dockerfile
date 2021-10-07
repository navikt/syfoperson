FROM navikt/java:11-appdynamics

COPY build/libs/app.jar app.jar

ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=remote"
