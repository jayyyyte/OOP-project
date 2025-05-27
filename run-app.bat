@echo off
echo ===========================================
echo Main Application
echo ===========================================
echo.

:: Build the project
echo Building project...
call mvn clean compile

:: Run the main application
echo.
echo Starting Main Application...
echo.
call mvn javafx:run

pause 