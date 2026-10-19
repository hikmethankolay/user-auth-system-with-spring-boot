@echo off
rem Runs unit and integration tests (integration tests start PostgreSQL with Testcontainers,
rem which needs Docker Desktop or Podman with its Docker API enabled).
rem Usage: scripts\test.bat [maven args...]   e.g. scripts\test.bat -Dtest=UserServiceTest
setlocal
cd /d "%~dp0.."
call "%~dp0lib.bat" require_jdk || exit /b 1

call mvnw.cmd -B verify %* || exit /b 1
echo ==^> Coverage report: target\site\jacoco\index.html
