#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

# shellcheck source=/dev/null
source "${SCRIPT_DIR}/localstack-cli.sh"

cd "${PROJECT_ROOT}"

require_localstack_running

PAYLOAD_FILE="${1:-docs/API/avaliacao-request.json}"
REQUEST_BODY="$(jq -c . "${PAYLOAD_FILE}" | jq -Rs .)"
localstack_cli lambda invoke \
  --function-name avaliador \
  --payload "{\"body\":${REQUEST_BODY}}" \
  /tmp/avaliador-response.json
cat /tmp/avaliador-response.json
