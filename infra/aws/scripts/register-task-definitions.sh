#!/usr/bin/env bash
# Renders and registers the initial Fargate task definition for each component from
# ecs/services.json. Secrets are read from SSM Parameter Store under /dinedash/<NAME>.
# "PARAM:ENV" in the secrets list maps parameter /dinedash/PARAM to env var ENV.
set -euo pipefail
cd "$(dirname "$0")/.."

REGION="${AWS_REGION:-ap-south-1}"
ACCOUNT="$(aws sts get-caller-identity --query Account --output text)"
OUT="$(mktemp -d)"

for name in $(jq -r 'keys[]' ecs/services.json); do
  cfg=$(jq --arg n "$name" '.[$n]' ecs/services.json)
  sed -e "s/__NAME__/$name/g" -e "s/__ACCOUNT__/$ACCOUNT/g" -e "s/__REGION__/$REGION/g" \
      -e "s/__CPU__/$(jq -r .cpu <<<"$cfg")/" -e "s/__MEMORY__/$(jq -r .memory <<<"$cfg")/" \
      -e "s/__PORT__/$(jq -r .port <<<"$cfg")/" -e "s/__TASK_ROLE__/$(jq -r .taskRole <<<"$cfg")/" \
      ecs/task-definition.template.json \
  | jq --argjson cfg "$cfg" --arg prefix "arn:aws:ssm:$REGION:$ACCOUNT:parameter/dinedash/" '
      .containerDefinitions[0].environment =
        ([{name: "SERVER_PORT", value: ($cfg.port | tostring)}]
         + ($cfg.environment | to_entries | map({name: .key, value: .value})))
      | .containerDefinitions[0].secrets =
        ($cfg.secrets | map(split(":") as $p | {name: ($p[1] // $p[0]), valueFrom: ($prefix + $p[0])}))
    ' > "$OUT/$name.json"
  aws ecs register-task-definition --cli-input-json "file://$OUT/$name.json" --region "$REGION" \
    --query 'taskDefinition.taskDefinitionArn' --output text
done
