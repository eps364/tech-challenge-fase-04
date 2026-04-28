#!/usr/bin/env bash
set -euo pipefail

ENVIRONMENT="${1:-dev}"

./scripts/package.sh
cd infra/terraform
terraform init
terraform apply -auto-approve -var-file="environments/${ENVIRONMENT}.tfvars"
