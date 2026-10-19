@echo off
rem Turns this template into your own project (Maven coordinates, application name, base package).
rem Runs scripts/rename-project.sh with Git Bash, which ships with Git for Windows.
rem Usage: scripts\rename-project.bat <groupId> <artifactId> [basePackage]
rem Example: scripts\rename-project.bat com.acme order-service com.acme.orders
setlocal
cd /d "%~dp0.."

set "BASH="
for %%p in ("%ProgramFiles%\Git\bin\bash.exe" "%ProgramFiles(x86)%\Git\bin\bash.exe" "%LocalAppData%\Programs\Git\bin\bash.exe") do (
  if not defined BASH if exist "%%~p" set "BASH=%%~p"
)
if not defined BASH (
  echo error: Git Bash not found. Install Git for Windows ^(winget install Git.Git^) or run scripts/rename-project.sh in WSL. 1>&2
  exit /b 1
)

"%BASH%" scripts/rename-project.sh %*
