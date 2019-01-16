#FROM statisticsnorway/alpine-jdk11-buildtools:latest as build
#MAINTAINER Totto <totto@totto.org>

# jlink is causing and issue with ssl handshake on strong crypto and use SNI certs

#RUN ["jlink", "--strip-debug", "--no-header-files", "--no-man-pages", "--compress=2", "--module-path", "/opt/jdk/jmods", "--output", "/linked",\
# "--add-modules", "jdk.unsupported,java.base,java.management,java.net.http,java.xml,java.naming,java.desktop"]

#FROM alpine:latest
FROM statisticsnorway/alpine-jdk11-buildtools:latest
MAINTAINER Totto <totto@totto.org>
#COPY --from=build /linked /opt/jdk/
ENV CHROME_BIN=/usr/bin/chromium-browser
ENV CHROME_PATH=/usr/lib/chromium/
ENV PATH=/opt/jdk/bin:$PATH

RUN apk add --update \
    groff \
    python \
    python-dev \
    py-pip \
    bash \
    unzip \
    curl \
    chromium \
    nss \
    chromium-chromedriver \
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
RUN ln -s $HOME/github-access-token.sh /github-access-token

#ADD https://chromedriver.storage.googleapis.com/2.42/chromedriver_linux64.zip /tmp
#RUN unzip /tmp/chromedriver_linux64.zip
#RUN mv chromedriver /usr/bin/chromedriver
#RUN chmod +x /usr/bin/chromedriver

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
EXPOSE 9091

USER $USER
WORKDIR /home/$USER
CMD ["./runapp.sh"]
