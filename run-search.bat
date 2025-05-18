@echo off
echo ===========================================
echo SearchCLI Development Tool
echo This is a separate tool from the main program
echo ===========================================
echo.

:: Build the project
echo Building project...
call mvn clean compile

:: Run the SearchCLI
echo.
echo Starting SearchCLI...
echo (Type 'exit' to quit)
echo.
call mvn exec:java

pause 