#!/usr/bin/env bash
set -euo pipefail

PAYLOAD_FILE="${1:-docs/API/avaliacao-request.json}"
awslocal lambda invoke \
  --function-name avaliador \
  --payload "{\"body\":$(jq -c . "${PAYLOAD_FILE}")}" \
  /tmp/avaliador-response.json
cat /tmp/avaliador-response.json
