@echo off
@setlocal enableextensions
@cd /d "%~dp0"

echo Checking if Chocolatey is installed...
if exist "%ProgramData%\Chocolatey\bin\choco.exe" (
    echo Chocolatey is already installed.
) else (
    echo Installing Chocolatey...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString(\"https://community.chocolatey.org/install.ps1\"))"
    if %errorlevel% neq 0 (
        echo Error: Failed to install Chocolatey.
        pause
        exit /b 1
    )
    echo Adding Chocolatey to PATH...
    setx PATH "%ProgramData%\Chocolatey\bin;%PATH%" >nul
    if %errorlevel% neq 0 (
        echo Error: Failed to update PATH for Chocolatey.
        pause
        exit /b 1
    )
)

echo Checking if Scoop is installed...
where scoop >nul 2>&1
if %errorlevel%==0 (
    echo Scoop is already installed.
) else (
    echo Scoop is not installed. Installing Scoop...
    powershell -NoProfile -ExecutionPolicy RemoteSigned -Command "[System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString(\"https://get.scoop.sh\")); Invoke-Scoop -Administrator"
    if %errorlevel% neq 0 (
        echo Error: Failed to install Scoop.
        pause
        exit /b 1
    )
)

echo Installation checks complete.
pause
