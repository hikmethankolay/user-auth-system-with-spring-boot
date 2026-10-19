@echo off
rem Runs the whole stack (PostgreSQL + app image) with Docker/Podman Compose.
rem Usage: scripts\docker.bat up^|down^|logs^|ps
setlocal
cd /d "%~dp0.."
call "%~dp0lib.bat" require_env || exit /b 1
call "%~dp0lib.bat" detect_compose || exit /b 1

set "CMD=%~1"
if "%CMD%"=="" set "CMD=up"

if /i "%CMD%"=="up" (
  echo ==^> Building the image and starting the stack
  %COMPOSE% --profile app up -d --build --wait || exit /b 1
  echo ==^> App running on http://localhost:8080
) else if /i "%CMD%"=="down" (
  %COMPOSE% --profile app down
) else if /i "%CMD%"=="logs" (
  %COMPOSE% --profile app logs -f app
) else if /i "%CMD%"=="ps" (
  %COMPOSE% --profile app ps
) else (
  echo error: Unknown command '%CMD%'. Use: up^|down^|logs^|ps 1>&2
  exit /b 1
)
exit /b %errorlevel%
