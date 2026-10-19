@echo off
rem Starts PostgreSQL and runs the app from source (Ctrl+C to stop). Extra arguments go to Maven.
rem Usage: scripts\dev.bat [maven args...]
setlocal
cd /d "%~dp0.."
call "%~dp0lib.bat" require_jdk || exit /b 1
call "%~dp0lib.bat" require_env || exit /b 1

call "%~dp0db.bat" up || exit /b 1

echo ==^> Starting the application on http://localhost:8080 (Swagger UI: /swagger-ui.html)
call mvnw.cmd -B spring-boot:run %*
