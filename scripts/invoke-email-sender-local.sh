#!/usr/bin/env bash
set -euo pipefail

BODY_FILE="${1:-docs/API/email-avaliacao-criada.json}"
awslocal lambda invoke \
  --function-name email-sender \
  --payload "{\"Records\":[{\"messageId\":\"local-test-1\",\"body\":$(jq -c . "${BODY_FILE}")}]}" \
  /tmp/email-response.json
cat /tmp/email-response.json
