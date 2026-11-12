#!/usr/bin/env bash
# Rolls a new image out to an ECS service: copies the current task definition,
# swaps the image, registers a new revision and waits until the service is stable.
# Usage: deploy-ecs.sh <component> <image-uri>
set -euo pipefail

COMPONENT="$1"
IMAGE="$2"
CLUSTER="${ECS_CLUSTER:-dinedash}"
FAMILY="dinedash-$COMPONENT"
SERVICE="$COMPONENT"

current=$(aws ecs describe-task-definition --task-definition "$FAMILY" --query taskDefinition)

new=$(jq --arg image "$IMAGE" --arg name "$COMPONENT" '
  .containerDefinitions |= map(if .name == $name then .image = $image else . end)
  | del(.taskDefinitionArn, .revision, .status, .requiresAttributes, .compatibilities,
        .registeredAt, .registeredBy)' <<<"$current")

revision=$(aws ecs register-task-definition --cli-input-json "$new" \
  --query 'taskDefinition.taskDefinitionArn' --output text)
echo "Registered $revision"

aws ecs update-service --cluster "$CLUSTER" --service "$SERVICE" --task-definition "$revision" \
  --query 'service.deployments[0].id' --output text

# Fails the pipeline if the new tasks never become healthy (the deployment circuit breaker rolls back)
aws ecs wait services-stable --cluster "$CLUSTER" --services "$SERVICE"
echo "$SERVICE is running $IMAGE"
