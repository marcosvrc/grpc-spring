#!/usr/bin/env bash
# Gera um certificado autoassinado para uso local do servidor gRPC com TLS
# (perfil Spring "tls"). NAO use este certificado em producao - e apenas
# para desenvolvimento/testes na sua maquina.
set -euo pipefail

CERT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/certs"
mkdir -p "$CERT_DIR"

openssl req -x509 -newkey rsa:2048 -nodes \
  -keyout "$CERT_DIR/server-key.pem" \
  -out "$CERT_DIR/server-cert.pem" \
  -days 3650 \
  -subj "/CN=localhost" \
  -addext "subjectAltName=DNS:localhost,IP:127.0.0.1"

echo
echo "Certificado gerado em:"
echo "  - $CERT_DIR/server-cert.pem"
echo "  - $CERT_DIR/server-key.pem"
echo
echo "Rode a aplicacao com o perfil 'tls' para habilitar TLS:"
echo "  ./mvnw spring-boot:run -Dspring-boot.run.profiles=tls"
