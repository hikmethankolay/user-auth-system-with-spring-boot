@echo off
rem Builds the executable jar into target\. Tests run unless --skip-tests is given.
rem Usage: scripts\build.bat [--skip-tests]
setlocal
cd /d "%~dp0.."
call "%~dp0lib.bat" require_jdk || exit /b 1

if /i "%~1"=="--skip-tests" (
  call mvnw.cmd -B clean package -DskipTests || exit /b 1
) else (
  call mvnw.cmd -B clean verify || exit /b 1
)

for %%f in (target\*.jar) do echo ==^> Built: %%f
