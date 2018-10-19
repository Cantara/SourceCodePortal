Docker image for running SourceCode Dashboard Portal.
=======================================
This image will by default pack the latest SourceCodePortal jar file (SNAPSHOT).
--build-arg DOCKER_TAG=SourceCodePortal-0.5.1 may be used to pack a specific release-version. The format matches the git tag format.
The curl logic may be replaced by simple copies to get local jar-files into the image locally.

Building with latest SNAPSHOT version
```
docker build -t cantara/sourcecodeportal .
```

Build with specific application version
```
docker build -t cantara/sourcecodeportal . --build-arg DOCKER_TAG=sourcecodeportal-0.8-beta-12
```

## Configuration

#### Logging
Where to put logs. See config_override/logback.xml
* LOGBACK_CANTARA_LEVEL (Loglevel of no.cantara logs. Defaults to info if not set)

#### Application properties
The configuration can be overridden by passing a file with the `--env-file` command when running the image.
E.g:
```
--env-file application_override.properties
```
Alternatively, the properties can be overridden by passing them one by one.
E.g:
```
-e github.oauth2.client.clientId=clientId -e github.oauth2.client.clientSecret=clientSecret

sudo docker run -e SCP_github.oauth2.client.clientId=clientId -e SCP_github.oauth2.client.clientSecret=clientSecret  -it --rm -p 80:9090 cantara/sourcecodeportal
```

