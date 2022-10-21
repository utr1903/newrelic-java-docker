# Introduction

This repo is dedicated to demonstrate how to build and run a Spring Boot Java application as a Docker container with New Relic APM agent installed.

The application is a simple server which provides a `GET` endpoint for health check on port `8080`.

The goal is to pack this app into a Docker container with New Relic and observe its behavior in New Relic UI per making calls to its endpoint.

# Setup

## Download New Relic agent files

The minimal necessary files are `newrelic.jar` and `newrelic.yml` which can be found in the applications root folder (`/demo/newrelic/`) in this repo.

These files can also be downloaded as follows:

`curl -O https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip`

**Important**: The `newrelic.yml` file should be put into the same folder as the `newrelic.jar` file so that it can find it in the runtime!

## Create a Dockerfile

There are multiple ways to build a Docker image for a Java application. Wihtin this demo, everything from building to running the application is accomplished with Docker.

Thereby no JDK, JRE or Maven is required to be installed on the host on which the `docker build` and `docker run` commands are executed.

### Build the app with a base JDK image
```
FROM maven:3.6.0-jdk-11-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package
```

The snippet above;
1. grabs a maven JDK image
2. copies src files and pom.xml into the image
3. runs `mvn clean package` to create `/home/app/target/demo-0.0.1-SNAPSHOT.jar`

### Create runnable Docker image with a final JRE

```
FROM openjdk:11-jre-slim
COPY --from=build /home/app/target/demo-0.0.1-SNAPSHOT.jar /usr/local/lib/demo.jar
```

The snippet above;
1. grabs a JRE image
2. copies the built `jar` file from the build layer

```
ARG newRelicAppName
ARG newRelicLicenseKey

ENV NEW_RELIC_APP_NAME=$newRelicAppName
ENV NEW_RELIC_LICENSE_KEY=$newRelicLicenseKey
ENV NEW_RELIC_LOG_FILE_NAME="STDOUT"

ADD newrelic/newrelic.jar /usr/local/lib
ADD newrelic/newrelic.yml /usr/local/lib

RUN sed -i s/"NEW_RELIC_APP_NAME"/$newRelicAppName/g /usr/local/lib/newrelic.yml
RUN sed -i s/"NEW_RELIC_LICENSE_KEY"/$newRelicLicenseKey/g /usr/local/lib/newrelic.yml

EXPOSE 8080

ENTRYPOINT ["java","-javaagent:/usr/local/lib/newrelic.jar","-jar","/usr/local/lib/demo.jar"]
```

The snippet above;
1. accepts `newRelicAppName` and `newRelicLicenseKey` as build arguments
2. sets them as environment variables within the image
3. adds the newrelic files into the image
4. sets the `NEW_RELIC_APP_NAME` and `NEW_RELIC_LICENSE_KEY` within `newrelic.yml` per given build arguments.
5. exposes the port `8080`
6. runs the application target jar file with `newrelic.jar` as the New Relic agent

## Runner

The script `run_demo.sh` is automatically creating the setup environment and starts to make request to the application.

```
# Build Docker image
docker build \
  --build-arg newRelicAppName=$appName \
  --build-arg newRelicLicenseKey=$NEWRELIC_LICENSE_KEY \
  --tag $appName \
  "./demo/."
```

The snippet above provides the New Relic license key (which should be available as an environment variable) and the application name which you would like to see in New Relic UI as build arguments to the Docker build process.

```
# Run application as Docker container
docker run \
  -d \
  --rm \
  --name $appName \
  -p 8080:8080 \
  $appName
```

The snippet above runs the built Docker image in a container in detached mode and couples the port `8080` of the container with the port `8080` of the host.

```
# Make request every 3 seconds
while true
do
  response=$(curl http://localhost:8080)
  echo $response
  sleep 3
done
```

The snippet above automatically makes requests every 3 second to the one and only endpoint of the application so that you can see some data within the New Relic UI.
