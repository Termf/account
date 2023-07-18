FROM openjdk:8-alpine

ENV JAVA_APM_ENABLE=true
ENV APP_NAME=account
ENV SKYWALKING_ADD=172.18.3.13:11800


ADD app.jar  /

ENV JAVA_OPTS="-Denv=DEV -Dactive=dev"
ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar