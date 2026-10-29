#!/usr/bin/env bash
# Creates the Gradle 8.9 wrapper using your global Gradle, run once.
# (AGP 8.7 needs Gradle 8.9.x; your global Gradle 9.x is only used to write the wrapper files.)
set -e
cd "$(dirname "$0")"
tmp="$(mktemp -d)"
( cd "$tmp" && gradle wrapper --gradle-version 8.9 --distribution-type bin )
cp "$tmp/gradlew" "$tmp/gradlew.bat" .
mkdir -p gradle && cp -r "$tmp/gradle/wrapper" gradle/
chmod +x gradlew
rm -rf "$tmp"
echo "Wrapper ready:"; cat gradle/wrapper/gradle-wrapper.properties
