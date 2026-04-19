@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "ROOT=%~dp0.."
set "BUILD_DIR=%ROOT%\build"
set "CLASSES_DIR=%BUILD_DIR%\classes"
set "SOURCES_FILE=%BUILD_DIR%\sources.txt"
set "TOMCAT_HOME=%CATALINA_HOME%"
if "%TOMCAT_HOME%"=="" set "TOMCAT_HOME=C:\apache-tomcat-9.0.117"
set "CATALINA_HOME=%TOMCAT_HOME%"
set "APP_NAME=AJT"
set "WEBAPP_DIR=%TOMCAT_HOME%\webapps\%APP_NAME%"

if "%JAVA_HOME%"=="" if "%JRE_HOME%"=="" (
    for /d %%i in ("C:\Program Files\Java\jdk*") do set "JAVA_HOME=%%i"
    if "!JAVA_HOME!"=="" for /d %%i in ("C:\Program Files\Java\jre*") do set "JRE_HOME=%%i"
    if not "!JAVA_HOME!"=="" echo Automatically set JAVA_HOME to !JAVA_HOME!
    if not "!JRE_HOME!"=="" echo Automatically set JRE_HOME to !JRE_HOME!
)

echo [1/6] Using Tomcat home: %TOMCAT_HOME%
if not exist "%TOMCAT_HOME%\bin\startup.bat" (
    echo ERROR: Tomcat not found at %TOMCAT_HOME%
    echo Set CATALINA_HOME or edit scripts\deploy-tomcat.bat
    exit /b 1
)

echo [2/6] Collecting Java sources...
if not exist "%BUILD_DIR%" mkdir "%BUILD_DIR%"
if not exist "%CLASSES_DIR%" mkdir "%CLASSES_DIR%"
type nul > "%SOURCES_FILE%"
for /r "%ROOT%\src" %%f in (*.java) do (
    set "JAVA_FILE=%%f"
    set "JAVA_FILE=!JAVA_FILE:\=/!"
    echo "!JAVA_FILE!" >> "%SOURCES_FILE%"
)

echo [3/6] Compiling project classes...
javac -cp "%ROOT%\WebContent\WEB-INF\lib\*" -d "%CLASSES_DIR%" @"%SOURCES_FILE%"
if errorlevel 1 (
    echo ERROR: Compilation failed.
    exit /b 1
)

echo [4/6] Copying web content...
if not exist "%WEBAPP_DIR%" mkdir "%WEBAPP_DIR%"
xcopy /E /Y /I "%ROOT%\WebContent\*" "%WEBAPP_DIR%\" > nul
if errorlevel 1 (
    echo ERROR: Failed to copy WebContent.
    exit /b 1
)

echo [5/6] Copying compiled classes...
if not exist "%WEBAPP_DIR%\WEB-INF\classes" mkdir "%WEBAPP_DIR%\WEB-INF\classes"
xcopy /E /Y /I "%CLASSES_DIR%\*" "%WEBAPP_DIR%\WEB-INF\classes\" > nul
if errorlevel 1 (
    echo ERROR: Failed to copy compiled classes.
    exit /b 1
)

echo [6/6] Restarting Tomcat...
call "%TOMCAT_HOME%\bin\shutdown.bat" > nul 2>&1
call "%TOMCAT_HOME%\bin\startup.bat"
if errorlevel 1 (
    echo ERROR: Tomcat startup failed.
    exit /b 1
)

echo.
echo Deployment complete.
echo Open: http://localhost:8080/%APP_NAME%/login

endlocal
