@echo off
setlocal enabledelayedexpansion

:: Set project directories
set PROJECT_ROOT=%~dp0
set SRC_DIR=%PROJECT_ROOT%src
set BIN_DIR=%PROJECT_ROOT%bin
set LIB_DIR=%PROJECT_ROOT%lib
set RESOURCES_DIR=%PROJECT_ROOT%\src\main\resources

:: Set main class
set MAIN_CLASS=Main

echo.
echo ===================================
echo Building Price Comparison Application
echo ===================================
echo.

:: Clean existing compiled files
echo Cleaning previous build...
if exist "%BIN_DIR%" rmdir /s /q "%BIN_DIR%"
mkdir "%BIN_DIR%"
echo Done.
echo.

:: Build class path with all JAR files in lib directory
set CLASSPATH=%BIN_DIR%
if exist "%LIB_DIR%" (
    for %%f in ("%LIB_DIR%\*.jar") do (
        set CLASSPATH=!CLASSPATH!;%%f
    )
)

:: Compile the source files
echo Compiling source files...
:: First, find all Java files and compile them
dir /s /b "%SRC_DIR%\*.java" > sources.txt
javac -d "%BIN_DIR%" -cp "%CLASSPATH%" @sources.txt
if %ERRORLEVEL% neq 0 (
    echo Error: Compilation failed!
    del sources.txt
    exit /b %ERRORLEVEL%
)
del sources.txt
echo Compilation successful.
echo.

:: Copy resource files
echo Copying resource files...
if exist "%RESOURCES_DIR%" (
    xcopy /E /Y "%RESOURCES_DIR%" "%BIN_DIR%"
)
echo Done.
echo.

:: Run the application
echo Running application...
echo ===================================
java -cp "%CLASSPATH%" %MAIN_CLASS%
echo ===================================

echo.
echo Build process completed.
exit /b 0