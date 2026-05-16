#!/usr/bin/env bash
set -euo pipefail

LOCALSTACK_HOST_URL="http://localhost:4566/_localstack/health"
LOCALSTACK_DOMAIN_URL="http://localhost.localstack.cloud:4566/_localstack/health"

print_status() {
  local label="$1"
  local url="$2"

  if curl -fsS "$url" >/dev/null 2>&1; then
    echo "OK: ${label} -> ${url}"
  else
    echo "FALHA: ${label} -> ${url}"
  fi
}

is_wsl() {
  grep -qiE "microsoft|wsl" /proc/version 2>/dev/null
}

wsl_ip() {
  hostname -I | awk '{print $1}'
}

echo "Diagnostico de acesso LocalStack via browser"
echo
print_status "health localhost" "${LOCALSTACK_HOST_URL}"
print_status "health localhost.localstack.cloud" "${LOCALSTACK_DOMAIN_URL}"

echo
if is_wsl; then
  IP_WSL="$(wsl_ip)"
  echo "Ambiente detectado: WSL"
  echo "IP da distro WSL: ${IP_WSL}"
  echo
  echo "Teste no browser do Windows:"
  echo "1) http://localhost:4566/_localstack/health"
  echo "2) http://${IP_WSL}:4566/_localstack/health"
  echo
  echo "Use no app.localstack.cloud (endpoint da instância local):"
  echo "- http://localhost:4566"
  echo "- http://${IP_WSL}:4566 (fallback para WSL)"
  echo
  cat <<'EOF'
Se localhost nao funcionar no browser do Windows, execute no PowerShell (Administrador):

# Substitua <WSL_IP> pelo valor exibido acima
netsh interface portproxy delete v4tov4 listenaddress=127.0.0.1 listenport=4566
netsh interface portproxy add v4tov4 listenaddress=127.0.0.1 listenport=4566 connectaddress=<WSL_IP> connectport=4566

# Opcional: liberar firewall local para a porta
netsh advfirewall firewall add rule name="LocalStack 4566" dir=in action=allow protocol=TCP localport=4566
EOF
else
  echo "Ambiente detectado: Linux nativo"
  echo "No app.localstack.cloud use: http://localhost:4566"
fi
