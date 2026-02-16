@echo off
title BLL Test Suite Runner
echo ========================================
echo   RUNNING BLL TESTS ONLY
echo ========================================
echo.

cd /d "%~dp0"

set CLASSPATH=lib\junit-4.13.2.jar;lib\mockito-core-3.12.4.jar;lib\hamcrest-core-1.3.jar;lib\byte-buddy-1.11.13.jar;lib\objenesis-3.2.jar;.;..\bin;..\bin\mariadb-java-client-3.4.1.jar;..\bin\log4j-api-2.20.0.jar;..\bin\log4j-core-2.20.0.jar

echo Running Business Logic Layer Tests...
echo.

java -cp "%CLASSPATH%" org.junit.runner.JUnitCore testrunner.BLLTestSuite

echo.
echo ========================================
echo BLL Test execution complete!
echo ========================================
pause
