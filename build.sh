#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/qrapp/android-sdk}"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
[ -x ./gradlew ] || { echo "No ./gradlew yet. Run: bash setup-wrapper.sh"; exit 1; }
./gradlew --version
./gradlew testDebugUnitTest assembleDebug
find app/build/outputs/apk -type f -name '*.apk' -ls
