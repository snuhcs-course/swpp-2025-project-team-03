#!/bin/bash
# Format Kotlin code using ktlint + spotless
cd "$(dirname "$0")/.."
./gradlew spotlessApply
