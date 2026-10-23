@echo off
rem Generates TypeScript types for the API from the committed OpenAPI document (api\openapi.json) with
rem openapi-typescript. Pair them with openapi-fetch for a typed client; see scripts/client.sh for an example.
rem Needs Node.js with npm.
rem Usage: scripts\client.bat [output-file]   (default: target\client\api.ts)
setlocal
cd /d "%~dp0.."

where npx >nul 2>&1
if errorlevel 1 (
  echo error: Node.js with npm is required to generate the client ^(https://nodejs.org^). 1>&2
  exit /b 1
)

set "OUTPUT=%~1"
if "%OUTPUT%"=="" set "OUTPUT=target\client\api.ts"
for %%f in ("%OUTPUT%") do if not exist "%%~dpf" mkdir "%%~dpf"
call npx --yes openapi-typescript@7.13.0 api/openapi.json --output "%OUTPUT%" || exit /b 1
echo ==^> TypeScript types: %OUTPUT% ^(use them with openapi-fetch^)
