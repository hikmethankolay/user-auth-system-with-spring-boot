@echo off
rem Generates an API client from the committed OpenAPI document (api\openapi.json) with OpenAPI Generator, run in
rem its container image (needs Docker or Podman); see scripts/client.sh.
rem Default: typescript-fetch, a TypeScript client on the browser's fetch. --generator picks another language
rem (kotlin, swift5, dart, java, python, ...).
rem Usage: scripts\client.bat [--generator NAME] [output-dir]   (default: typescript-fetch, target\client\NAME)
setlocal
cd /d "%~dp0.."

set "GENERATOR=typescript-fetch"
if /i not "%~1"=="--generator" goto output
if "%~2"=="" (
  echo error: Usage: scripts\client.bat [--generator NAME] [output-dir] 1>&2
  exit /b 1
)
set "GENERATOR=%~2"
shift
shift

:output
set "CONTAINER="
docker info >nul 2>&1 && set "CONTAINER=docker"
if not defined CONTAINER podman info >nul 2>&1 && set "CONTAINER=podman"
if not defined CONTAINER (
  echo error: Docker or Podman is required ^(and must be running^). 1>&2
  exit /b 1
)
set "OUTPUT=%~1"
if "%OUTPUT%"=="" set "OUTPUT=target\client\%GENERATOR%"
if not exist "%OUTPUT%" mkdir "%OUTPUT%"
for %%i in ("%OUTPUT%") do set "OUTPUT=%%~fi"
%CONTAINER% run --rm -v "%CD%\api:/spec:ro" -v "%OUTPUT%:/out" docker.io/openapitools/openapi-generator-cli:v7.26.0 generate -i /spec/openapi.json -g "%GENERATOR%" -o /out || exit /b 1
echo ==^> %GENERATOR% client: %OUTPUT%
