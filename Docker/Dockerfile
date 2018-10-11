FROM statisticsnorway/alpine-jdk11-buildtools:latest
MAINTAINER Totto <totto@totto.org>

RUN apk add --update \
    groff \
    python \
    python-dev \
    py-pip \
    bash \
  && pip install awscli boto3 \
  && pip install --upgrade awscli \
  && rm -rf /var/cache/apk/*

ENV USER=sourcecodeportal
ENV HOME=/home/$USER
RUN adduser -S $USER

#Builds latest by default, which means it downloads latest jar/snapshot from mvnrepo
ARG DOCKER_TAG=latest

COPY config_override/* $HOME/config_override/
COPY toRoot/* $HOME/

ADD https://raw.githubusercontent.com/Cantara/devops/master/scripted_deploy/update-service.sh $HOME/
RUN chmod 755 $HOME/update-service.sh


RUN if [ $DOCKER_TAG = "latest" ]; then \
      $HOME/update-service.sh --group-id=no.cantara.docsite --artifact-id=source-code-portal --version-pattern=SNAPSHOT; \
    else \
      $HOME/update-service.sh --group-id=no.cantara.docsite --artifact-id=source-code-portal --version-pattern=$(echo $DOCKER_TAG) ; \
    fi

## Create directory for holding application logs and configure permissions
## Configure permissions
RUN mkdir -p $HOME/logs && \
    mkdir -p $HOME/db && \
    chmod 755 $HOME/*.sh && \
    chown -R $USER $HOME

## Map data volume container
VOLUME ["$HOME/config_override", "$HOME/logs", "$HOME/db"]

## Expose application port
EXPOSE 9090

USER $USER
WORKDIR /home/$USER
CMD ["./runapp.sh"]