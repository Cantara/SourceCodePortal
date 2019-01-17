#!/bin/bash


APP=source-code-portal.jar
JAVA_PARAMS="-Dlogback.configurationFile=./config_override/logback-default.xml -Xms64m -Xmx512m --illegal-access=deny -Dhttps.protocols=TLSv1,TLSv1.1,TLSv1.2" -Djdk.tls.client.protocols="TLSv1,TLSv1.1,TLSv1.2"
#JAVA_PARAMS="-Dlogback.configurationFile=./config_override/logback-default.xml -Djavax.net.debug=ssl:handshake:verbose -Djdk.internal.httpclient.disableHostnameVerification -Xms64m -Xmx512m --illegal-access=deny"


./update-service.sh --group-id=no.cantara.docsite --artifact-id=source-code-portal --version-pattern=SNAPSHOT

date +" --- RUNNING $(basename $0) %Y-%m-%d_%H:%M:%S --- "
set -x

if [ "$AWS_PARAMETER_STORE_ENABLED" = "true" ]; then
    python GetPropertiesFromParameterStore.py $AWS_PARAMETER_STORE_PATH config_override/application_override.properties > /dev/null
    if [ $? -eq 0 ]; then
        echo 'GetPropertiesFromParameterStore.py exited successfully!'
    else
        echo 'GetPropertiesFromParameterStore.py exited with error (non null exit code)'
        exit 1
    fi
fi

if [ -z "$LOGBACK_CANTARA_LEVEL" ]; then
    sed -i -e "s/{LOGBACK_CANTARA_LEVEL}/info/g" config_override/logback-default.xml
else
    sed -i -e "s/{LOGBACK_CANTARA_LEVEL}/$LOGBACK_CANTARA_LEVEL/g" config_override/logback-default.xml
fi

set +x

java $JAVA_PARAMS $JAVA_PARAMS_OVERRIDE -jar $APP
