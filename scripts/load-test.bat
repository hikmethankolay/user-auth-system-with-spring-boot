@echo off
rem Runs the k6 load test (load-tests\api.js) against a running app, using the k6 container image.
rem All requests come from one IP, so start the target app with RATE_LIMIT_ENABLED=false
rem (and REQUIRE_VERIFIED_EMAIL=false).
rem Usage: scripts\load-test.bat [base-url]   (default http://host.docker.internal:8080)
rem Env:   VUS (virtual users, default 20), DURATION (default 30s)
setlocal
cd /d "%~dp0.."

set "CONTAINER="
docker info >nul 2>&1 && set "CONTAINER=docker"
if not defined CONTAINER podman info >nul 2>&1 && set "CONTAINER=podman"
if not defined CONTAINER (
  echo error: Docker or Podman is required ^(and must be running^). 1>&2
  exit /b 1
)

set "BASE_URL=%~1"
if "%BASE_URL%"=="" set "BASE_URL=http://host.docker.internal:8080"
if not defined VUS set "VUS=20"
if not defined DURATION set "DURATION=30s"

echo ==^> Load testing %BASE_URL% with %VUS% virtual users for %DURATION%
%CONTAINER% run --rm -v "%CD%\load-tests:/scripts:ro" -e BASE_URL=%BASE_URL% -e VUS=%VUS% -e DURATION=%DURATION% docker.io/grafana/k6:1.4.0 run /scripts/api.js
