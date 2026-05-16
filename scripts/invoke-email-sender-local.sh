#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

# shellcheck source=/dev/null
source "${SCRIPT_DIR}/localstack-cli.sh"

cd "${PROJECT_ROOT}"

require_localstack_running

BODY_FILE="${1:-docs/API/email-avaliacao-critica.json}"
MESSAGE_BODY="$(jq -c . "${BODY_FILE}" | jq -Rs .)"
localstack_cli lambda invoke \
  --function-name email-sender \
  --payload "{\"Records\":[{\"messageId\":\"local-test-1\",\"body\":${MESSAGE_BODY}}]}" \
  /tmp/email-response.json
cat /tmp/email-response.json
