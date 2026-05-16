#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

# shellcheck source=/dev/null
source "${SCRIPT_DIR}/localstack-cli.sh"

cd "${PROJECT_ROOT}"

docker compose up -d

echo "Aguardando LocalStack ficar saudavel..."
for _ in $(seq 1 30); do
	if curl -fsS http://localhost.localstack.cloud:4566/_localstack/health >/dev/null 2>&1; then
		echo "LocalStack pronto."
		exit 0
	fi
	sleep 2
done

echo "Erro: LocalStack nao respondeu ao healthcheck em tempo habil." >&2
exit 1
