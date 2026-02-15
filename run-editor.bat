@echo off
echo ========================================
echo Starting Real Editor Application
echo ========================================
echo.

cd /d "%~dp0"

REM Check if Driver.class exists
if not exist "bin\Driver.class" (
    echo ERROR: Application not compiled!
    echo Please compile the source code first.
    pause
    exit /b 1
)

REM Set classpath with all JAR dependencies
set CLASSPATH=bin;bin\mariadb-java-client-3.4.1.jar;bin\log4j-api-2.20.0.jar;bin\log4j-core-2.20.0.jar;bin\AlKhalilMorphoSys2.jar;bin\AlKhalilDiacritizer.jar;bin\ADAT-Lemmatization.v1.20180101.jar;bin\ADAT-Stemmer.v1.20180101.jar;bin\ADAT-Racineur.v1.20180101.jar;bin\Pos_tagger.jar

echo Starting application...
echo.

java -cp "%CLASSPATH%" Driver

if %errorlevel% neq 0 (
    echo.
    echo ========================================
    echo ERROR: Application failed to start!
    echo Please check:
    echo 1. MariaDB is running
    echo 2. Database 'realeditor' exists
    echo 3. Database credentials in config.properties
    echo ========================================
)

echo.
pause
