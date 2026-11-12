#!/usr/bin/env bash
# One-time setup of the IAM roles and media bucket used by DineDash.
# Requires an admin profile; Jenkins itself only gets jenkins-ci-policy.json.
set -euo pipefail
cd "$(dirname "$0")/.."

REGION="${AWS_REGION:-ap-south-1}"
BUCKET="${S3_BUCKET:-dinedash-media}"

aws iam create-role --role-name dinedash-ecs-task-execution \
  --assume-role-policy-document file://iam/ecs-tasks-trust-policy.json
aws iam put-role-policy --role-name dinedash-ecs-task-execution \
  --policy-name dinedash-task-execution --policy-document file://iam/ecs-task-execution-policy.json

# Only restaurant-service needs AWS API access (S3 uploads); other tasks get no permissions
aws iam create-role --role-name dinedash-restaurant-task \
  --assume-role-policy-document file://iam/ecs-tasks-trust-policy.json
aws iam put-role-policy --role-name dinedash-restaurant-task \
  --policy-name dinedash-restaurant-s3 --policy-document file://iam/restaurant-task-policy.json
aws iam create-role --role-name dinedash-default-task \
  --assume-role-policy-document file://iam/ecs-tasks-trust-policy.json

aws iam create-policy --policy-name dinedash-jenkins-ci --policy-document file://iam/jenkins-ci-policy.json

aws s3api create-bucket --bucket "$BUCKET" --region "$REGION" \
  --create-bucket-configuration LocationConstraint="$REGION"
aws s3api put-public-access-block --bucket "$BUCKET" --public-access-block-configuration \
  BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=false,RestrictPublicBuckets=false
aws s3api put-bucket-policy --bucket "$BUCKET" --policy file://s3/media-bucket-policy.json
aws s3api put-bucket-cors --bucket "$BUCKET" --cors-configuration file://s3/media-bucket-cors.json

for repo in api-gateway auth-user-service restaurant-service order-service payment-service \
            delivery-service notification-service frontend; do
  aws ecr create-repository --repository-name "dinedash/$repo" --region "$REGION" \
    --image-scanning-configuration scanOnPush=true >/dev/null || true
done
echo "IAM roles, media bucket and ECR repositories are ready"
