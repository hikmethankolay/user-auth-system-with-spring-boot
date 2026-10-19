@echo off
rem Runs the jar built by scripts\build.bat. Configuration is read from .env and the environment.
rem Usage: scripts\run.bat [app args...]
setlocal
cd /d "%~dp0.."
call "%~dp0lib.bat" require_env || exit /b 1

where java >nul 2>&1 || (
  echo error: Java 25+ runtime not found. 1>&2
  exit /b 1
)

set "JAR="
for %%f in (target\*.jar) do set "JAR=%%f"
if not defined JAR (
  echo error: No jar in target\. Run scripts\build.bat first. 1>&2
  exit /b 1
)

echo ==^> Running %JAR%
java -jar "%JAR%" %*
