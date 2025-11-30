#!/bin/bash
# Check Kotlin code formatting using ktlint + spotless
cd "$(dirname "$0")/.."
./gradlew spotlessCheck
