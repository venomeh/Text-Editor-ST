@echo off
echo ========================================
echo  BOUNDARY TEST SUITE - SETUP SCRIPT
echo ========================================
echo.

REM Create lib directory for test dependencies
if not exist "lib" mkdir lib

echo Checking for required test libraries...
echo.

REM Check for JUnit
if exist "lib\junit-4.13.2.jar" (
    echo [OK] JUnit 4.13.2 found
) else (
    echo [MISSING] junit-4.13.2.jar
    echo Please download from: https://search.maven.org/artifact/junit/junit/4.13.2/jar
)

REM Check for Mockito
if exist "lib\mockito-core-3.12.4.jar" (
    echo [OK] Mockito Core 3.12.4 found
) else (
    echo [MISSING] mockito-core-3.12.4.jar
    echo Please download from: https://search.maven.org/artifact/org.mockito/mockito-core/3.12.4/jar
)

REM Check for Hamcrest
if exist "lib\hamcrest-core-1.3.jar" (
    echo [OK] Hamcrest Core 1.3 found
) else (
    echo [MISSING] hamcrest-core-1.3.jar
    echo Please download from: https://search.maven.org/artifact/org.hamcrest/hamcrest-core/1.3/jar
)

REM Check for Byte Buddy
if exist "lib\byte-buddy-1.11.13.jar" (
    echo [OK] Byte Buddy 1.11.13 found
) else (
    echo [MISSING] byte-buddy-1.11.13.jar
    echo Please download from: https://search.maven.org/artifact/net.bytebuddy/byte-buddy/1.11.13/jar
)

REM Check for Objenesis
if exist "lib\objenesis-3.2.jar" (
    echo [OK] Objenesis 3.2 found
) else (
    echo [MISSING] objenesis-3.2.jar
    echo Please download from: https://search.maven.org/artifact/org.objenesis/objenesis/3.2/jar
)

echo.
echo ========================================
echo Setup complete! See README.md for usage.
echo ========================================
pause
