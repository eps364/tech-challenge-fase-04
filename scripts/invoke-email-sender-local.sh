#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

cd "${PROJECT_ROOT}"

BODY_FILE="${1:-docs/API/email-avaliacao-criada.json}"
awslocal lambda invoke \
  --function-name email-sender \
  --payload "{\"Records\":[{\"messageId\":\"local-test-1\",\"body\":$(jq -c . "${BODY_FILE}")}]}" \
  /tmp/email-response.json
cat /tmp/email-response.json
