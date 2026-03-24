#!/usr/bin/env bash
set -euo pipefail

APP_NAME="Grid Padel Generator"
APP_VERSION="0.0.1"
MAIN_JAR="grid-padel-ui-${APP_VERSION}.jar"
VENDOR="GridPadel"
DESCRIPTION="Desktop application for managing padel tournament brackets"

echo "============================================"
echo " Building ${APP_NAME} v${APP_VERSION}"
echo "============================================"

echo ""
echo "[1/3] Building project with Maven..."
mvn clean package -DskipTests -q

echo "[2/3] Preparing staging directory..."
rm -rf staging
mkdir -p staging dist
cp "grid-padel-ui/target/${MAIN_JAR}" staging/

echo "[3/3] Creating Linux application..."

rm -rf "dist/${APP_NAME}"

jpackage \
  --type app-image \
  --name "${APP_NAME}" \
  --app-version "${APP_VERSION}" \
  --vendor "${VENDOR}" \
  --description "${DESCRIPTION}" \
  --input staging \
  --main-jar "${MAIN_JAR}" \
  --main-class org.springframework.boot.loader.launch.JarLauncher \
  --dest dist \
  --java-options "--enable-native-access=ALL-UNNAMED" \
  --java-options "-Xmx512m"

rm -rf staging

echo ""
echo "============================================"
echo " SUCCESS! Application built at:"
echo " dist/${APP_NAME}/bin/${APP_NAME}"
echo "============================================"
