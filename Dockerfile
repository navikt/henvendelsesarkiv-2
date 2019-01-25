FROM navikt/java:8-appdynamics

ARG app_name

ENV LC_ALL="no_NB.UTF-8"
ENV LANG="no_NB.UTF-8"
ENV TZ="Europe/Oslo"

COPY appdynamics.sh /init-scripts/

COPY build/libs/$app_name-all.jar app.jar