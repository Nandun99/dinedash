#!/usr/bin/env bash
# Builds a component image and pushes it to ECR tagged with the git SHA and "latest".
# Usage: ci/build-and-push.sh <component> <ecr-registry> <tag>
set -euo pipefail

COMPONENT="$1"
REGISTRY="$2"
TAG="$3"
IMAGE="$REGISTRY/dinedash/$COMPONENT"

if [ "$COMPONENT" = "frontend" ]; then
  docker build -t "$IMAGE:$TAG" -t "$IMAGE:latest" \
    --build-arg VITE_FIREBASE_API_KEY="${VITE_FIREBASE_API_KEY:-}" \
    --build-arg VITE_FIREBASE_AUTH_DOMAIN="${VITE_FIREBASE_AUTH_DOMAIN:-}" \
    --build-arg VITE_FIREBASE_DATABASE_URL="${VITE_FIREBASE_DATABASE_URL:-}" \
    --build-arg VITE_FIREBASE_PROJECT_ID="${VITE_FIREBASE_PROJECT_ID:-}" \
    --build-arg VITE_FIREBASE_MESSAGING_SENDER_ID="${VITE_FIREBASE_MESSAGING_SENDER_ID:-}" \
    --build-arg VITE_FIREBASE_APP_ID="${VITE_FIREBASE_APP_ID:-}" \
    --build-arg VITE_FIREBASE_VAPID_KEY="${VITE_FIREBASE_VAPID_KEY:-}" \
    frontend
else
  docker build -f services/Dockerfile --build-arg SERVICE="$COMPONENT" \
    -t "$IMAGE:$TAG" -t "$IMAGE:latest" .
fi

docker push "$IMAGE:$TAG"
docker push "$IMAGE:latest"
