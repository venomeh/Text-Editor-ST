@echo off
title Real Editor Launcher
color 0A
echo.
echo ========================================
echo    REAL EDITOR - Arabic Text Editor
echo ========================================
echo.

cd /d "%~dp0"

REM Check if MariaDB is running
netstat -ano | findstr ":3306" >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] MariaDB is running
) else (
    echo [WARNING] MariaDB is not running!
    echo.
    echo Starting MariaDB server...
    
    REM Try to start MariaDB service
    net start MySQL >nul 2>&1
    if %errorlevel% equ 0 (
        echo [OK] MariaDB service started
        timeout /t 2 >nul
    ) else (
        echo.
        echo MariaDB service not found. Starting manually...
        echo A new window will open with MariaDB server.
        echo KEEP THAT WINDOW OPEN while using the editor!
        echo.
        start "MariaDB Server" /MIN powershell -Command "& 'C:\Program Files\MariaDB 11.5\bin\mysqld.exe' --console"
        timeout /t 5 >nul
    )
)

echo.
echo [OK] Launching Real Editor...
echo.

REM Launch the application
start "Real Editor" javaw -cp "bin;bin\mariadb-java-client-3.4.1.jar;bin\log4j-api-2.20.0.jar;bin\log4j-core-2.20.0.jar;bin\AlKhalilMorphoSys2.jar;bin\AlKhalilDiacritizer.jar;bin\ADAT-Lemmatization.v1.20180101.jar;bin\ADAT-Stemmer.v1.20180101.jar;bin\ADAT-Racineur.v1.20180101.jar;bin\Pos_tagger.jar" Driver

timeout /t 2 >nul

echo.
echo ========================================
echo Real Editor is now running!
echo You can close this window.
echo ========================================
echo.

timeout /t 3 >nul
exit
