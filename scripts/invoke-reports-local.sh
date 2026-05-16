#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

# shellcheck source=/dev/null
source "${SCRIPT_DIR}/localstack-cli.sh"

cd "${PROJECT_ROOT}"

require_localstack_running

localstack_cli lambda invoke \
  --function-name reports-generator \
  --payload '{}' \
  /tmp/reports-response.json
cat /tmp/reports-response.json
