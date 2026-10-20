@echo off
rem Formats the code, then runs unit and integration tests (integration tests start PostgreSQL and
rem Redis with Testcontainers, which needs Docker Desktop or Podman with its Docker API enabled).
rem Usage: scripts\test.bat [--unit] [maven args...]
rem   scripts\test.bat --unit                          unit tests only (*Test), no containers
rem   scripts\test.bat --unit -Dtest=UserServiceTest   one unit test class
rem   scripts\test.bat -Dit.test=UserIntegrationTest   unit tests and one integration test class
setlocal
cd /d "%~dp0.."
call "%~dp0lib.bat" require_jdk || exit /b 1

call mvnw.cmd -B -q spotless:apply || exit /b 1

set "ARGS=%*"
if "%~1"=="--unit" (
  call set "ARGS=%%ARGS:*--unit=%%"
  call mvnw.cmd -B test %%ARGS%% || exit /b 1
  exit /b 0
)

rem A filtered run covers only part of the code, so it would fail the coverage minimums.
set "EXTRA="
echo %* | findstr /c:"-Dtest=" /c:"-Dit.test=" >nul && set "EXTRA=-Djacoco.haltOnFailure=false"
call mvnw.cmd -B verify %* %EXTRA% || exit /b 1
echo ==^> Coverage report: target\site\jacoco\index.html
