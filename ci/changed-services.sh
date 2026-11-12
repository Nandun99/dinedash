#!/usr/bin/env bash
# Prints the deployable components touched since the given commit, one per line.
# A change to the shared module or the parent POM rebuilds every backend service.
set -euo pipefail

BASE="${1:-HEAD~1}"
ALL_SERVICES="api-gateway auth-user-service restaurant-service order-service payment-service delivery-service notification-service"

if ! git rev-parse --verify --quiet "$BASE" >/dev/null; then
  # First build or shallow clone: build everything
  printf '%s\n' $ALL_SERVICES frontend
  exit 0
fi

changed=$(git diff --name-only "$BASE" HEAD)

if echo "$changed" | grep -qE '^(pom\.xml|shared/|services/Dockerfile)'; then
  printf '%s\n' $ALL_SERVICES
else
  for svc in $ALL_SERVICES; do
    if echo "$changed" | grep -q "^services/$svc/"; then
      echo "$svc"
    fi
  done
fi

if echo "$changed" | grep -q '^frontend/'; then
  echo frontend
fi
