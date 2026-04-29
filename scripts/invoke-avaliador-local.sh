#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

cd "${PROJECT_ROOT}"

PAYLOAD_FILE="${1:-docs/API/avaliacao-request.json}"
awslocal lambda invoke \
  --function-name avaliador \
  --payload "{\"body\":$(jq -c . "${PAYLOAD_FILE}")}" \
  /tmp/avaliador-response.json
cat /tmp/avaliador-response.json
