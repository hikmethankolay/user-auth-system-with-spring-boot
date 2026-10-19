@echo off
rem End-to-end smoke test against a running app: health, register, login, /me, refresh, logout.
rem Usage: scripts\smoke.bat [base-url]   (default http://localhost:8080)
setlocal
set "BASE_URL=%~1"
if "%BASE_URL%"=="" set "BASE_URL=http://localhost:8080"

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$ErrorActionPreference = 'Stop'; $base = '%BASE_URL%'; $user = 'smoke_' + [DateTimeOffset]::UtcNow.ToUnixTimeSeconds(); $pw = 'Smoke#12345';" ^
  "function Check($method, $path, $expected, $body, $token) {" ^
  "  $headers = @{}; if ($token) { $headers['Authorization'] = 'Bearer ' + $token };" ^
  "  $params = @{ Method = $method; Uri = $base + $path; Headers = $headers; UseBasicParsing = $true; ContentType = 'application/json' };" ^
  "  if ($body) { $params['Body'] = $body };" ^
  "  try { $r = Invoke-WebRequest @params; $status = [int]$r.StatusCode; $content = $r.Content }" ^
  "  catch { if (-not $_.Exception.Response) { throw \"$method $path: connection failed (is the app running at $base?)\" }; $status = [int]$_.Exception.Response.StatusCode; $content = '' };" ^
  "  if ($status -ne $expected) { throw \"$method $path: expected $expected, got $status\" };" ^
  "  Write-Host \"==> $method $path -> $status\"; return $content };" ^
  "Check GET '/actuator/health' 200 | Out-Null;" ^
  "Check POST '/api/v1/auth/register' 201 (@{ username = $user; email = \"$user@example.com\"; password = $pw } | ConvertTo-Json) | Out-Null;" ^
  "$token = (Check POST '/api/v1/auth/login' 200 (@{ identifier = $user; password = $pw } | ConvertTo-Json) | ConvertFrom-Json).accessToken;" ^
  "Check GET '/api/v1/users/me' 200 $null $token | Out-Null;" ^
  "Check POST '/api/v1/auth/refresh' 200 $null $token | Out-Null;" ^
  "Check GET '/api/v1/users' 403 $null $token | Out-Null;" ^
  "Check GET '/api/v1/users/me' 401 | Out-Null;" ^
  "Check POST '/api/v1/auth/logout' 204 | Out-Null;" ^
  "Write-Host \"==> Smoke test passed against $base\""
exit /b %errorlevel%
