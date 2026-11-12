#!/usr/bin/env bash
# One-time creation of the ECS cluster and Fargate services, wired together with
# ECS Service Connect so services reach each other by name (e.g. http://order-service:8083).
# Requires: SUBNETS (comma separated private subnets), SECURITY_GROUP, GATEWAY_TARGET_GROUP_ARN,
#           FRONTEND_TARGET_GROUP_ARN (ALB target groups for public traffic).
set -euo pipefail
cd "$(dirname "$0")/.."

CLUSTER="${ECS_CLUSTER:-dinedash}"
aws ecs create-cluster --cluster-name "$CLUSTER" \
  --service-connect-defaults namespace=dinedash --settings name=containerInsights,value=enabled >/dev/null

for name in $(jq -r 'keys[]' ecs/services.json); do
  port=$(jq -r --arg n "$name" '.[$n].port' ecs/services.json)
  lb=()
  if [ "$name" = "api-gateway" ]; then
    lb=(--load-balancers "targetGroupArn=$GATEWAY_TARGET_GROUP_ARN,containerName=$name,containerPort=$port")
  elif [ "$name" = "frontend" ]; then
    lb=(--load-balancers "targetGroupArn=$FRONTEND_TARGET_GROUP_ARN,containerName=$name,containerPort=$port")
  fi
  aws ecs create-service --cluster "$CLUSTER" --service-name "$name" \
    --task-definition "dinedash-$name" --desired-count 1 --launch-type FARGATE \
    --network-configuration "awsvpcConfiguration={subnets=[$SUBNETS],securityGroups=[$SECURITY_GROUP],assignPublicIp=DISABLED}" \
    --deployment-configuration "deploymentCircuitBreaker={enable=true,rollback=true},minimumHealthyPercent=100,maximumPercent=200" \
    --service-connect-configuration "{\"enabled\":true,\"namespace\":\"dinedash\",\"services\":[{\"portName\":\"$name\",\"clientAliases\":[{\"port\":$port,\"dnsName\":\"$name\"}]}]}" \
    "${lb[@]}" >/dev/null
  echo "Created service $name"
done
