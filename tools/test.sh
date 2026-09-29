#!/usr/bin/env bash
# Unit tests for the logic that needs no camera. Plain JDK, no Android SDK.
set -euo pipefail
cd "$(dirname "$0")/.."
rm -rf out/test && mkdir -p out/test
javac -encoding UTF-8 -d out/test src/com/artec/evftoggle/Display.java test/com/artec/evftoggle/DisplayTest.java
java -cp out/test com.artec.evftoggle.DisplayTest
