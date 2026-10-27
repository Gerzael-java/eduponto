#!/usr/bin/env bash
# Grava a URL e a chave PÚBLICA do Supabase em local.properties (arquivo que NÃO vai para o GitHub).
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."

read -r -p "URL do projeto (ex.: https://abcdefgh.supabase.co): " URL
read -r -p "Chave pública (publishable ou anon): " CHAVE

URL="${URL%/}"
if [ -z "$URL" ] || [ -z "$CHAVE" ]; then
  echo "A URL e a chave são obrigatórias."
  exit 1
fi
case "$CHAVE" in
  sb_secret_*)
    echo "ERRO: isto é uma chave SECRETA. Ela nunca pode ir no app. Use a chave publishable/anon."
    exit 1
    ;;
esac

touch local.properties
grep -v -E '^(supabase\.url|supabase\.publishableKey)=' local.properties > local.properties.tmp || true
{
  cat local.properties.tmp
  echo "supabase.url=$URL"
  echo "supabase.publishableKey=$CHAVE"
} > local.properties
rm -f local.properties.tmp

echo "Configurado em local.properties (não vai para o GitHub)."
echo "Agora compile: bash scripts/build-apk.sh"
