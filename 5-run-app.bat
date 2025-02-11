@echo off
@setlocal enableextensions
@cd /d "%~dp0"

echo Running Application
java -jar user-auth-system/target/user-auth-system-1.0.jar

echo Operation Completed!
pause