@echo off
setlocal EnableExtensions

set "ROOT=%~dp0.."
set "SCHEMA=%ROOT%\db\schema.sql"

REM Try common MySQL Server install path first
set "MYSQL_EXE=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"

if not exist "%MYSQL_EXE%" (
  echo ERROR: Could not find mysql.exe at:
  echo   %MYSQL_EXE%
  echo.
  echo Fix options:
  echo 1^) Install MySQL Server (includes mysql client)
  echo 2^) Or edit this script to point MYSQL_EXE to your mysql.exe
  echo 3^) Or add MySQL bin to PATH and run mysql directly
  exit /b 1
)

if not exist "%SCHEMA%" (
  echo ERROR: Could not find schema file:
  echo   %SCHEMA%
  exit /b 1
)

echo Using mysql:
echo   %MYSQL_EXE%
echo Using schema:
echo   %SCHEMA%
echo.
echo It will prompt for MySQL password...
echo.

"%MYSQL_EXE%" -u root -p college_db < "%SCHEMA%"

if errorlevel 1 (
  echo.
  echo ERROR: Schema import failed.
  echo Common causes:
  echo - Wrong MySQL password
  echo - MySQL service not running
  echo - Database permissions
  exit /b 1
)

echo.
echo Schema import completed successfully.
endlocal
