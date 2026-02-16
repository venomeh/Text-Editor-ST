@echo off
title Boundary Test Suite - Compiler
echo ========================================
echo   COMPILING BOUNDARY TESTS
echo ========================================
echo.

cd /d "%~dp0"

REM Set classpath for compilation
set CLASSPATH=lib\junit-4.13.2.jar;lib\mockito-core-3.12.4.jar;lib\hamcrest-core-1.3.jar;lib\byte-buddy-1.11.13.jar;lib\objenesis-3.2.jar;..\bin;..\bin\mariadb-java-client-3.4.1.jar;..\bin\log4j-api-2.20.0.jar;..\bin\log4j-core-2.20.0.jar;..\bin\AlKhalilMorphoSys2.jar

echo Compiling test files...
echo.

REM Compile DTO tests (if any)
REM javac -cp "%CLASSPATH%" dto\*.java

REM Compile BLL tests
echo [1/4] Compiling BLL tests...
javac -cp "%CLASSPATH%" bll\*.java
if %errorlevel% neq 0 (
    echo [ERROR] BLL tests compilation failed!
    pause
    exit /b 1
)

REM Compile DAL tests
echo [2/4] Compiling DAL tests...
javac -cp "%CLASSPATH%" dal\*.java
if %errorlevel% neq 0 (
    echo [ERROR] DAL tests compilation failed!
    pause
    exit /b 1
)

REM Compile Integration tests
echo [3/4] Compiling Integration tests...
javac -cp "%CLASSPATH%" integration\*.java
if %errorlevel% neq 0 (
    echo [ERROR] Integration tests compilation failed!
    pause
    exit /b 1
)

REM Compile Test Runners
echo [4/4] Compiling Test Runners...
javac -cp "%CLASSPATH%" testrunner\*.java
if %errorlevel% neq 0 (
    echo [ERROR] Test runner compilation failed!
    pause
    exit /b 1
)

echo.
echo ========================================
echo Compilation successful!
echo ========================================
echo.
pause
