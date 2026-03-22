@echo off
setlocal

set "JDK17_HOME=C:\Program Files\Java\jdk-17"
if not exist "%JDK17_HOME%\bin\java.exe" (
    echo ERROR: JDK 17 not found at "%JDK17_HOME%"
    exit /b 1
)

set "JAVA_HOME=%JDK17_HOME%"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "ROOT=%~dp0.."

if /I "%~1"=="--check" (
    echo JAVA_HOME=%JAVA_HOME%
    java -version
    exit /b 0
)

echo Launching VS Code with JAVA_HOME=%JAVA_HOME%
code "%ROOT%"

endlocal
