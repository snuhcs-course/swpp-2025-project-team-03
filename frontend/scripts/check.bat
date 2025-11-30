@echo off
REM Check Kotlin code formatting using ktlint + spotless
cd /d "%~dp0\.."
call gradlew.bat spotlessCheck
