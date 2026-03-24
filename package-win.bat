@echo off
setlocal

set APP_NAME=Grid Padel Generator
set APP_VERSION=0.0.1
set MAIN_JAR=grid-padel-ui-%APP_VERSION%.jar
set VENDOR=GridPadel
set DESCRIPTION=Desktop application for managing padel tournament brackets

echo ============================================
echo  Building %APP_NAME% v%APP_VERSION%
echo ============================================

echo.
echo [1/3] Building project with Maven...
call mvn clean package -DskipTests -q
if errorlevel 1 (
    echo ERROR: Maven build failed!
    exit /b 1
)

echo [2/3] Preparing staging directory...
if exist staging rmdir /s /q staging
mkdir staging
copy grid-padel-ui\target\%MAIN_JAR% staging\

echo [3/3] Creating Windows application...

REM Build app-image (standalone directory with .exe launcher)
if exist dist\%APP_NAME% rmdir /s /q "dist\%APP_NAME%"

jpackage ^
  --type app-image ^
  --name "%APP_NAME%" ^
  --app-version %APP_VERSION% ^
  --vendor "%VENDOR%" ^
  --description "%DESCRIPTION%" ^
  --input staging ^
  --main-jar %MAIN_JAR% ^
  --main-class org.springframework.boot.loader.launch.JarLauncher ^
  --dest dist ^
  --java-options "--enable-native-access=ALL-UNNAMED" ^
  --java-options "-Xmx512m"

if errorlevel 1 (
    echo ERROR: jpackage failed!
    rmdir /s /q staging
    exit /b 1
)

rmdir /s /q staging

echo.
echo ============================================
echo  SUCCESS! Application built at:
echo  dist\%APP_NAME%\%APP_NAME%.exe
echo ============================================
echo.
echo To create an installer (.exe or .msi), install
echo WiX Toolset (https://wixtoolset.org/) and run:
echo   package-win-installer.bat
echo.

endlocal
