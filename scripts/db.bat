@echo off
rem Manages the local PostgreSQL container.
rem Usage: scripts\db.bat up^|down^|reset^|logs^|psql^|status
setlocal
cd /d "%~dp0.."
call "%~dp0lib.bat" detect_compose || exit /b 1

set "CMD=%~1"
if "%CMD%"=="" set "CMD=up"

if /i "%CMD%"=="up" (
  echo ==^> Starting PostgreSQL
  %COMPOSE% up -d --wait db
) else if /i "%CMD%"=="down" (
  %COMPOSE% stop db
) else if /i "%CMD%"=="reset" (
  set /p "ANSWER=This deletes all local database data. Continue? [y/N] "
  call :reset
) else if /i "%CMD%"=="logs" (
  %COMPOSE% logs -f db
) else if /i "%CMD%"=="psql" (
  %COMPOSE% exec db sh -c "psql -U \"$POSTGRES_USER\" -d \"$POSTGRES_DB\""
) else if /i "%CMD%"=="status" (
  %COMPOSE% ps db
) else (
  echo error: Unknown command '%CMD%'. Use: up^|down^|reset^|logs^|psql^|status 1>&2
  exit /b 1
)
exit /b %errorlevel%

:reset
if /i not "%ANSWER%"=="y" exit /b 0
%COMPOSE% down -v
%COMPOSE% up -d --wait db
exit /b %errorlevel%
