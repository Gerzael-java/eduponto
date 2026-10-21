#!/usr/bin/env bash
# Compila o APK de depuração e mostra onde ele está.
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."

if [ ! -x ./gradlew ]; then
  echo "gradlew não encontrado. Execute antes: bash .devcontainer/setup.sh"
  exit 1
fi

./gradlew assembleDebug --console=plain

APK="app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$APK" ]; then
  echo
  echo "APK gerado em: $(pwd)/$APK"
  ls -lh "$APK"
else
  echo "O build terminou, mas o APK não foi encontrado em $APK"
  exit 1
fi
