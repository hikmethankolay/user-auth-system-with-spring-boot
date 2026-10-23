@echo off
rem One-time setup: checks prerequisites, creates .env with generated secrets and compiles the project.
rem Usage: scripts\setup.bat [--force]   (--force recreates .env)
setlocal
cd /d "%~dp0.."

echo ==^> Checking prerequisites
call "%~dp0lib.bat" require_jdk || exit /b 1
call "%~dp0lib.bat" detect_compose >nul 2>&1
if errorlevel 1 (
  echo warning: Docker or Podman not found: db.bat, docker.bat and integration tests need one. 1>&2
) else (
  echo ==^> Container runtime found
)

if exist ".env" if /i not "%~1"=="--force" (
  echo ==^> .env already exists ^(use --force to recreate it^)
  goto compile
)

echo ==^> Creating .env from .env.example with generated secrets
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "function Secret([int]$n) { $b = New-Object byte[] $n; [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b); [Convert]::ToBase64String($b) -replace '[+/=]','' };" ^
  "$admin = 'Admin#' + (Secret 12);" ^
  "(Get-Content .env.example) -replace '^JWT_SECRET=.*', ('JWT_SECRET=' + (Secret 48)) -replace '^MFA_ENCRYPTION_KEY=.*', ('MFA_ENCRYPTION_KEY=' + (Secret 48)) -replace '^ADMIN_PASSWORD=.*', ('ADMIN_PASSWORD=' + $admin) | Set-Content -Encoding ascii .env;" ^
  "Write-Host ('==> Initial admin password: ' + $admin + ' (stored in .env; change it after the first login)')"
if errorlevel 1 exit /b 1

:compile
echo ==^> Compiling (downloads dependencies on first run)
call mvnw.cmd -B -q -DskipTests compile || exit /b 1

echo ==^> Done. Next: scripts\dev.bat to start the database and the app.
