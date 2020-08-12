FROM navikt/java:8-appdynamics

ARG app_name

ENV LC_ALL="no_NB.UTF-8"
ENV LANG="no_NB.UTF-8"
ENV TZ="Europe/Oslo"
ENV APPD_ENABLED=true
ENV JAVA_OPTS="${JAVA_OPTS} -XX:MaxRAMPercentage=65.0"
COPY java-debug.sh /init-scripts/08-java-debug.sh

COPY build/libs/$app_name-all.jar app.jar
