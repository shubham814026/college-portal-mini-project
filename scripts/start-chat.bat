@echo off
setlocal EnableDelayedExpansion

set "ROOT=%~dp0.."
set "BUILD_DIR=%ROOT%\build"
set "CLASSES_DIR=%BUILD_DIR%\classes"
set "SOURCES_FILE=%BUILD_DIR%\sources.txt"

if not exist "%BUILD_DIR%" mkdir "%BUILD_DIR%"
if not exist "%CLASSES_DIR%" mkdir "%CLASSES_DIR%"

echo Collecting Java sources...
type nul > "%SOURCES_FILE%"
for /r "%ROOT%\src" %%f in (*.java) do (
	set "JAVA_FILE=%%f"
	set "JAVA_FILE=!JAVA_FILE:\=/!"
	echo "!JAVA_FILE!" >> "%SOURCES_FILE%"
)

echo Compiling project classes...
javac -cp "%ROOT%\WebContent\WEB-INF\lib\*" -d "%CLASSES_DIR%" @"%SOURCES_FILE%"
if errorlevel 1 (
	echo Compilation failed. Please fix compile errors and retry.
	exit /b 1
)

echo Starting Chat server...
java -cp "%CLASSES_DIR%;%ROOT%\WebContent\WEB-INF\lib\*" com.college.chat.ChatServer
