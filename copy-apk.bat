@echo off
REM ============================================================
REM  Set ANDROID_PROJECT to the root folder of your Android app
REM ============================================================
set ANDROID_PROJECT=C:\workspaces\AndroidStudioProjects\Taxi

REM Copies app-debug.apk built by the Android project into the apks/ folder of this project.
set APK_SRC=%ANDROID_PROJECT%\app\debug\app-debug.apk
set APK_DST=%~dp0apks\app-debug.apk

if not exist "%APK_SRC%" (
    echo [ERROR] APK not found at: %APK_SRC%
    echo Build the project in Android Studio first ^(Build ^> Build APK^).
    exit /b 1
)

copy /Y "%APK_SRC%" "%APK_DST%"
echo [OK] app-debug.apk copied to apks\
