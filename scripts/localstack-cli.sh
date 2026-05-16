#!/usr/bin/env bash
set -euo pipefail

CONTAINER_NAME="tech-challenge-localstack"

require_localstack_running() {
  if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo "Erro: container ${CONTAINER_NAME} nao esta em execucao." >&2
    echo "Execute: docker compose up -d localstack" >&2
    return 1
  fi
}

localstack_cli() {
  if command -v awslocal >/dev/null 2>&1; then
    awslocal "$@"
    return 0
  fi

  require_localstack_running

  # lambda invoke writes the response payload to the last positional argument (a local file path).
  # When running via docker exec the path resolves inside the container, not on the host.
  # Intercept that argument, use a temp path inside the container, then copy the result out.
  if [[ "${1:-}" == "lambda" && "${2:-}" == "invoke" ]]; then
    local args=("$@")
    local last_idx=$(( ${#args[@]} - 1 ))
    local local_output="${args[$last_idx]}"
    local container_output="/tmp/lambda-invoke-output-$$.json"
    args[$last_idx]="$container_output"
    docker exec -i -w /workspace "${CONTAINER_NAME}" awslocal "${args[@]}"
    docker exec "${CONTAINER_NAME}" cat "$container_output" > "$local_output"
    docker exec "${CONTAINER_NAME}" rm -f "$container_output"
    return 0
  fi

  docker exec -i -w /workspace "${CONTAINER_NAME}" awslocal "$@"
}

if [[ "${BASH_SOURCE[0]}" == "$0" ]]; then
  localstack_cli "$@"
fi
