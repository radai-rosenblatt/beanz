#!/usr/bin/env bash

set -e

echo "CIRCLE_PULL_REQUESTS is $CIRCLE_PULL_REQUESTS"
echo "CIRCLE_PR_NUMBER is $CIRCLE_PR_NUMBER"

if [ "x$CIRCLE_PULL_REQUESTS" != "x" ]; then
  echo "this is a PR build"
  exit 0
fi

echo "this is a commit build"