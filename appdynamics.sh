#!/bin/bash
if test -r "/opt/appdynamics/javaagent.jar";
then
    JAVA_OPTS="${JAVA_OPTS} -javaagent:/opt/appdynamics/javaagent.jar"
    JAVA_OPTS="${JAVA_OPTS} -Dappdynamics.agent.applicationName=${APP_NAME}"
    JAVA_OPTS="${JAVA_OPTS} -Dappdynamics.agent.tierName=${APP_ENVIRONMENT}-${APP_NAME}"
    JAVA_OPTS="${JAVA_OPTS} -Dappdynamics.agent.reuse.nodeName=true"
    JAVA_OPTS="${JAVA_OPTS} -Dappdynamics.agent.reuse.nodeName.prefix=${APP_ENVIRONMENT}_${APP_NAME}_"
    JAVA_OPTS="${JAVA_OPTS} -Dappdynamics.jvm.shutdown.mark.node.as.historical=true"
    export JAVA_OPTS
fi
