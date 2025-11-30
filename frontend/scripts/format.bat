@echo off
REM Format Kotlin code using ktlint + spotless
cd /d "%~dp0\.."
call gradlew.bat spotlessApply
