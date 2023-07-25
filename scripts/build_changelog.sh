#!/bin/bash

# Copyright (c) 2023 wefhy

#TAG=$(git describe --tags)
TAG=$1
CHANGELOG=$(awk -v tag="$TAG" '
  /^## \[/ {
    if(match($0, "\\["tag"\\]")) {
      found=1
      next
    } else {
      found=0
    }
  }
  found {
    print
  }
' CHANGELOG.md)

if [ -n "$CHANGELOG" ]; then
  echo "$CHANGELOG"
else
  echo ""
fi
