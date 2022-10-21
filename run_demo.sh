#!/bin/bash

# Set application name
appName="demo"

# Build Docker image
docker build \
  --build-arg newRelicAppName=$appName \
  --build-arg newRelicLicenseKey=$NEWRELIC_LICENSE_KEY \
  --tag $appName \
  "./demo/."

# Run application as Docker container
docker run \
  -d \
  --rm \
  --name $appName \
  -p 8080:8080 \
  $appName

# Make request every 3 seconds
while true
do
  response=$(curl http://localhost:8080)
  echo $response
  sleep 3
done
