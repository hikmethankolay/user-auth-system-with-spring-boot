@echo off
rem Generates the Java reference (Javadoc) into target\reports\apidocs. Fails on any Javadoc warning,
rem including undocumented public, protected and package-private code.
rem Usage: scripts\docs.bat
setlocal
cd /d "%~dp0.."
call "%~dp0lib.bat" require_jdk || exit /b 1

call mvnw.cmd -B -q javadoc:javadoc || exit /b 1
echo ==^> Documentation: target\reports\apidocs\index.html
