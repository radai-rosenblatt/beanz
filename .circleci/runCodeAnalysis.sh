#!/usr/bin/env bash

set -e

if [ "x$CIRCLE_PULL_REQUESTS" != "x" ]; then
  echo "this is a PR build - not reporting to sonarcloud"
  exit 0
fi

./gradlew sonarqube -Dsonar.organization=radai-rosenblatt-github -Dsonar.login=$SONAR_KEY