@echo off
echo ========================================
echo Setting up RealEditor Database
echo ========================================
echo.
echo This will create the 'realeditor' database and all required tables.
echo Please enter the MariaDB root password when prompted (default: taqi123)
echo.
pause

"C:\Program Files\MariaDB 11.5\bin\mysql.exe" -u root -p < "resource\Database\EditorDBQuery.sql"

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo Database setup completed successfully!
    echo ========================================
) else (
    echo.
    echo ========================================
    echo ERROR: Database setup failed!
    echo Please check if MariaDB is installed and running.
    echo ========================================
)

echo.
pause
