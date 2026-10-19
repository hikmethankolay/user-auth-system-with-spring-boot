@echo off
rem Shared helpers for the .bat scripts in this directory.
rem Usage: call "%~dp0lib.bat" <function>   (functions: require_jdk, require_env, detect_compose)
rem Variables set here (COMPOSE, ...) are visible to the caller.
call :%*
exit /b %errorlevel%

:require_jdk
set "JAVAC=javac"
if defined JAVA_HOME set "JAVAC=%JAVA_HOME%\bin\javac"
call "%JAVAC%" -version >nul 2>&1
if errorlevel 1 (
  echo error: JDK 25+ not found ^(javac missing^). Install one, e.g. "winget install EclipseAdoptium.Temurin.25.JDK", or set JAVA_HOME. 1>&2
  exit /b 1
)
for /f "tokens=2" %%v in ('call "%JAVAC%" -version 2^>^&1') do set "JAVAC_VERSION=%%v"
for /f "delims=." %%m in ("%JAVAC_VERSION%") do set "JAVAC_MAJOR=%%m"
if %JAVAC_MAJOR% LSS 25 (
  echo error: JDK 25+ required, found %JAVAC_VERSION%. 1>&2
  exit /b 1
)
exit /b 0

:require_env
if not exist ".env" (
  echo error: .env not found. Run scripts\setup.bat first. 1>&2
  exit /b 1
)
exit /b 0

:detect_compose
docker info >nul 2>&1
if not errorlevel 1 (
  set "COMPOSE=docker compose"
  exit /b 0
)
podman info >nul 2>&1
if not errorlevel 1 (
  set "COMPOSE=podman compose"
  exit /b 0
)
echo error: Docker or Podman is required ^(and must be running^). 1>&2
exit /b 1
