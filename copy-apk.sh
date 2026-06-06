#!/usr/bin/env bash
# ============================================================
#  Set ANDROID_PROJECT to the root folder of your Android app
# ============================================================
ANDROID_PROJECT="/c/workspaces/AndroidStudioProjects/Taxi"

# Copies app-debug.apk built by the Android project into the apks/ folder of this project.
set -euo pipefail

APK_SRC="$ANDROID_PROJECT/app/debug/app-debug.apk"
APK_DST="$(cd "$(dirname "$0")" && pwd)/apks/app-debug.apk"

if [[ ! -f "$APK_SRC" ]]; then
  echo "[ERROR] APK not found at: $APK_SRC"
  echo "Build the project in Android Studio first (Build > Build APK)."
  exit 1
fi

cp -f "$APK_SRC" "$APK_DST"
echo "[OK] app-debug.apk copied to apks/"
