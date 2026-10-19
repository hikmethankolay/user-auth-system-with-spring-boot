@echo off
rem Generates the Doxygen HTML documentation into target\doxygen\html.
rem Needs doxygen and perl on PATH (perl runs the Doxygen input filter; Git for Windows or Strawberry Perl provide it).
rem Usage: scripts\docs.bat [--strict]   (--strict fails on any Doxygen warning, including undocumented code)
setlocal
cd /d "%~dp0.."

where doxygen >nul 2>&1 || (
  echo error: Doxygen not found. Install it, e.g. "winget install DimitriVanHeesch.Doxygen". 1>&2
  exit /b 1
)
where perl >nul 2>&1 || (
  echo error: perl not found. Install Strawberry Perl or add Git for Windows' usr\bin to PATH. 1>&2
  exit /b 1
)

if not exist "target\doxygen" mkdir "target\doxygen"
if /i "%~1"=="--strict" (
  (type Doxyfile & echo WARN_AS_ERROR = FAIL_ON_WARNINGS) | doxygen -
  if errorlevel 1 (
    echo error: Documentation warnings found, see target\doxygen\warnings.log 1>&2
    type target\doxygen\warnings.log
    exit /b 1
  )
) else (
  doxygen Doxyfile || exit /b 1
)

echo ==^> Documentation: target\doxygen\html\index.html
