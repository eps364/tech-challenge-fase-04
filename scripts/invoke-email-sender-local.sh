#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

cd "${PROJECT_ROOT}"

BODY_FILE="${1:-docs/API/email-avaliacao-critica.json}"
MESSAGE_BODY="$(jq -c . "${BODY_FILE}" | jq -Rs .)"
awslocal lambda invoke \
  --function-name email-sender \
  --payload "{\"Records\":[{\"messageId\":\"local-test-1\",\"body\":${MESSAGE_BODY}}]}" \
  /tmp/email-response.json
cat /tmp/email-response.json
