#!/usr/bin/env bash
set -euo pipefail

awslocal lambda invoke \
  --function-name reports-generator \
  --payload '{}' \
  /tmp/reports-response.json
cat /tmp/reports-response.json
