@echo off
setlocal EnableExtensions
cd /d "%~dp0"
title Aggrey DayFlow - Build APK
echo ============================================================
echo   Aggrey DayFlow - build the debug APK
echo ============================================================
echo.

rem ---------- 1. Are we in the right folder, and is the Gradle wrapper here? ----------
if not exist "app\build.gradle.kts" goto :wrong_folder
if not exist "gradlew.bat" goto :no_wrapper
if not exist "gradle\wrapper\gradle-wrapper.jar" goto :no_wrapper
if not exist "gradle\wrapper\gradle-wrapper.properties" goto :no_wrapper
echo [1/5] Project files found.

rem ---------- 2. Find Java 17 or newer ----------
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" goto :java_home_ok
set "JAVA_HOME="
for %%P in ("%ProgramFiles%\Android\Android Studio\jbr" "%LOCALAPPDATA%\Programs\Android Studio\jbr") do (
    if not defined JAVA_HOME if exist "%%~P\bin\java.exe" set "JAVA_HOME=%%~P"
)
if defined JAVA_HOME goto :java_home_ok
where java >nul 2>nul
if errorlevel 1 goto :no_java
goto :java_version
:java_home_ok
set "PATH=%JAVA_HOME%\bin;%PATH%"
:java_version
set "JV="
set "JMAJ="
for /f "tokens=3" %%V in ('java -version 2^>^&1 ^| findstr /i "version"') do if not defined JV set "JV=%%~V"
if not defined JV goto :no_java
for /f "delims=." %%M in ("%JV%") do set "JMAJ=%%M"
if not defined JMAJ goto :no_java
if %JMAJ% LSS 17 goto :old_java
echo [2/5] Java %JV% found.

rem ---------- 3. Find the Android SDK ----------
if exist "local.properties" goto :sdk_ok
if defined ANDROID_HOME goto :sdk_ok
if defined ANDROID_SDK_ROOT goto :sdk_ok
if exist "%LOCALAPPDATA%\Android\Sdk\platforms" goto :sdk_write
goto :no_sdk
:sdk_write
set "SDKP=%LOCALAPPDATA%\Android\Sdk"
set "SDKF=%SDKP:\=/%"
> local.properties echo sdk.dir=%SDKF%
echo        Created local.properties pointing at %SDKP%
:sdk_ok
echo [3/5] Android SDK located.

rem ---------- 4. Build ----------
echo [4/5] Building. The first run downloads Gradle and libraries and can take 5 to 15 minutes.
echo        An internet connection is required for the first build.
echo.
call gradlew.bat assembleDebug --console=plain
if errorlevel 1 goto :build_failed

rem ---------- 5. Verify the APK really exists ----------
set "APK=%~dp0app\build\outputs\apk\debug\app-debug.apk"
if not exist "%APK%" goto :apk_missing
set "SZ=0"
for %%F in ("%APK%") do set "SZ=%%~zF"
if "%SZ%"=="0" goto :apk_missing
echo.
echo [5/5] Running the unit tests. The APK above is already built either way.
call gradlew.bat testDebugUnitTest --console=plain
if errorlevel 1 (set "TESTS=FAILED - read the messages above") else (set "TESTS=PASSED")

echo.
echo ============================================================
echo   BUILD SUCCESSFUL
echo   APK:   %APK%
echo   Size:  %SZ% bytes
echo   Unit tests: %TESTS%
echo ============================================================
echo.
echo Copy that file to your phone, then open it on the phone to install.
echo See README_APK_INSTALL.md for the steps.
explorer /select,"%APK%"
goto :done

:wrong_folder
echo ERROR: This file must stay in the AggreyDayFlow project folder,
echo        next to the folder named app. Move it back and run it again.
goto :fail

:no_wrapper
echo ERROR: The Gradle wrapper files are missing.
echo        Needed: gradlew.bat and gradle\wrapper\gradle-wrapper.jar
echo.
echo        Fix: re-extract the latest AggreyDayFlow zip, which includes them.
echo        Or open the folder in Android Studio, wait for Gradle sync to finish,
echo        then build with the menu: Build - Build Bundle or APK - Build APK.
goto :fail

:no_java
echo ERROR: Java was not found.
echo        Easiest fix: install Android Studio from https://developer.android.com/studio
echo        It includes the right Java. Then run this file again.
goto :fail

:old_java
echo ERROR: Java %JV% is too old. This project needs Java 17 or newer.
echo        Easiest fix: install Android Studio. This script then uses its built-in Java.
goto :fail

:no_sdk
echo ERROR: The Android SDK was not found.
echo        1. Install and open Android Studio.
echo        2. Open the project once and let it finish Gradle sync.
echo           When asked, install Android SDK Platform 34.
echo        3. Run this file again.
goto :fail

:build_failed
echo.
echo ============================================================
echo   BUILD FAILED  -  no APK was created by this run
echo ============================================================
echo Scroll up and find the FIRST line that says error or What went wrong.
echo Send that part to your developer assistant. Nothing was changed on your phone.
goto :fail

:apk_missing
echo.
echo ERROR: Gradle finished but the APK is missing or empty:
echo        %APK%
echo        Treat this build as FAILED and send the output above for help.
goto :fail

:fail
echo.
if not defined NOPAUSE pause
exit /b 1

:done
echo.
if not defined NOPAUSE pause
exit /b 0
