#!/usr/bin/env bash
# Compila o APK de depuração e publica como Release no GitHub.
# Uso: bash scripts/release.sh v0.3.0 "Etapa 3: tema e telas"
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."

TAG="${1:-}"
if [ -z "$TAG" ]; then
  echo "Uso: bash scripts/release.sh v0.3.0 \"Título do release\""
  exit 1
fi
TITULO="${2:-EduPonto $TAG (teste)}"

if [ -n "$(git status --porcelain)" ]; then
  echo "Há alterações sem commit. Faça: git add -A && git commit -m \"mensagem\""
  exit 1
fi

if ! command -v gh >/dev/null 2>&1; then
  echo "==> Instalando o GitHub CLI (gh)"
  sudo apt-get update -y && sudo apt-get install -y gh || {
    echo "Não foi possível instalar o gh. Crie o release pelo site, em"
    echo "github.com/SEU-USUARIO/eduponto/releases/new, anexando o APK."
    exit 1
  }
fi

echo "==> Enviando commits para o GitHub"
git push

bash scripts/build-apk.sh

mkdir -p build/release
APK="build/release/EduPonto-$TAG.apk"
cp app/build/outputs/apk/debug/app-debug.apk "$APK"

echo "==> Criando o release $TAG"
gh release create "$TAG" "$APK" --title "$TITULO" --notes "APK de depuração para testes."

echo
echo "Release publicado. Veja em: aba Releases do repositório."
