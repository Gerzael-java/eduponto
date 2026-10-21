#!/usr/bin/env bash
# Prepara o ambiente: Gradle 9.4.1, Android SDK (cmdline-tools, plataforma 36,
# build-tools 36.0.0) e o Gradle Wrapper do projeto. Pode ser executado de novo
# com segurança: o que já existe não é baixado outra vez.
set -euo pipefail

GRADLE_VERSION="9.4.1"
CMDLINE_BUILD="14742923"
ANDROID_PLATFORM="android-36"
BUILD_TOOLS="36.0.0"

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TOOLS_DIR="$HOME/tools"
export ANDROID_HOME="$HOME/android-sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"

echo "==> Verificando Java"
if ! command -v java >/dev/null 2>&1; then
  echo "ERRO: Java não encontrado. Reconstrua o container (F1 > Codespaces: Rebuild Container)."
  exit 1
fi
java -version

for cmd in curl unzip; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "==> Instalando $cmd"
    sudo apt-get update -y && sudo apt-get install -y "$cmd"
  fi
done

mkdir -p "$TOOLS_DIR"

echo "==> Gradle $GRADLE_VERSION"
GRADLE_BIN="$TOOLS_DIR/gradle-$GRADLE_VERSION/bin/gradle"
if [ ! -x "$GRADLE_BIN" ]; then
  curl -fL -o /tmp/gradle.zip "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
  unzip -q -o /tmp/gradle.zip -d "$TOOLS_DIR"
  rm -f /tmp/gradle.zip
fi

echo "==> Android command-line tools"
SDKMANAGER="$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager"
if [ ! -x "$SDKMANAGER" ]; then
  mkdir -p "$ANDROID_HOME/cmdline-tools"
  curl -fL -o /tmp/cmdline-tools.zip "https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_BUILD}_latest.zip"
  unzip -q -o /tmp/cmdline-tools.zip -d "$ANDROID_HOME/cmdline-tools"
  rm -rf "$ANDROID_HOME/cmdline-tools/latest"
  mv "$ANDROID_HOME/cmdline-tools/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest"
  rm -f /tmp/cmdline-tools.zip
fi

echo "==> Aceitando licenças do Android SDK e instalando pacotes"
yes | "$SDKMANAGER" --licenses >/dev/null || true
"$SDKMANAGER" "platform-tools" "platforms;${ANDROID_PLATFORM}" "build-tools;${BUILD_TOOLS}"

echo "==> local.properties"
echo "sdk.dir=$ANDROID_HOME" > "$PROJECT_DIR/local.properties"

echo "==> Gradle Wrapper"
if [ ! -f "$PROJECT_DIR/gradle/wrapper/gradle-wrapper.jar" ]; then
  TMP_WRAPPER="$(mktemp -d)"
  ( cd "$TMP_WRAPPER" && "$GRADLE_BIN" wrapper --gradle-version "$GRADLE_VERSION" --no-daemon )
  mkdir -p "$PROJECT_DIR/gradle/wrapper"
  cp "$TMP_WRAPPER/gradlew" "$TMP_WRAPPER/gradlew.bat" "$PROJECT_DIR/"
  cp "$TMP_WRAPPER"/gradle/wrapper/* "$PROJECT_DIR/gradle/wrapper/"
  chmod +x "$PROJECT_DIR/gradlew"
  rm -rf "$TMP_WRAPPER"
fi

echo "==> Variáveis de ambiente nos próximos terminais"
if ! grep -q "ANDROID_HOME=" "$HOME/.bashrc" 2>/dev/null; then
  {
    echo "export ANDROID_HOME=\"$ANDROID_HOME\""
    echo "export ANDROID_SDK_ROOT=\"$ANDROID_HOME\""
    echo "export PATH=\"\$PATH:$ANDROID_HOME/platform-tools\""
  } >> "$HOME/.bashrc"
fi

echo
echo "Ambiente pronto. Para compilar: bash scripts/build-apk.sh"
