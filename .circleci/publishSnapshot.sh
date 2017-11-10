#!/usr/bin/env bash

set -e

if [ "x$CIRCLE_PULL_REQUESTS" != "x" ]; then
  echo "this is a PR build - not reporting publishing a snapshot"
  exit 0
fi

echo "TBD - snapshot builds"