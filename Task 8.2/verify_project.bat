@echo off
echo =================================
echo Job Achiever AI - Project Verification
echo =================================
echo.

echo Checking project structure...
echo.

REM Check root files
if exist "build.gradle" (
    echo ✓ Root build.gradle found
) else (
    echo ✗ Root build.gradle missing
)

if exist "settings.gradle" (
    echo ✓ settings.gradle found
) else (
    echo ✗ settings.gradle missing
)

if exist "gradlew.bat" (
    echo ✓ Gradle wrapper found
) else (
    echo ✗ Gradle wrapper missing
)

echo.
echo Checking app module...

if exist "app\build.gradle" (
    echo ✓ App build.gradle found
) else (
    echo ✗ App build.gradle missing
)

if exist "app\src\main\AndroidManifest.xml" (
    echo ✓ AndroidManifest.xml found
) else (
    echo ✗ AndroidManifest.xml missing
)

if exist "app\google-services.json" (
    echo ✓ Firebase config found
) else (
    echo ✗ Firebase config missing
)

echo.
echo Checking key activities...

if exist "app\src\main\java\com\example\task82\MainActivity.java" (
    echo ✓ MainActivity found
) else (
    echo ✗ MainActivity missing
)

if exist "app\src\main\java\com\example\task82\activities\SplashActivity.java" (
    echo ✓ SplashActivity found
) else (
    echo ✗ SplashActivity missing
)

if exist "app\src\main\java\com\example\task82\activities\LoginActivity.java" (
    echo ✓ LoginActivity found
) else (
    echo ✗ LoginActivity missing
)

if exist "app\src\main\java\com\example\task82\activities\InterviewActivity.java" (
    echo ✓ InterviewActivity found
) else (
    echo ✗ InterviewActivity missing
)

echo.
echo Checking fragments...

if exist "app\src\main\java\com\example\task82\fragments\HomeFragment.java" (
    echo ✓ HomeFragment found
) else (
    echo ✗ HomeFragment missing
)

if exist "app\src\main\java\com\example\task82\fragments\DashboardFragment.java" (
    echo ✓ DashboardFragment found
) else (
    echo ✗ DashboardFragment missing
)

if exist "app\src\main\java\com\example\task82\fragments\FeedbackFragment.java" (
    echo ✓ FeedbackFragment found
) else (
    echo ✗ FeedbackFragment missing
)

if exist "app\src\main\java\com\example\task82\fragments\ProfileFragment.java" (
    echo ✓ ProfileFragment found
) else (
    echo ✗ ProfileFragment missing
)

echo.
echo Checking AI service...

if exist "app\src\main\java\com\example\task82\services\LlamaAIService.java" (
    echo ✓ LlamaAIService found
) else (
    echo ✗ LlamaAIService missing
)

echo.
echo Checking key layouts...

if exist "app\src\main\res\layout\activity_main.xml" (
    echo ✓ Main activity layout found
) else (
    echo ✗ Main activity layout missing
)

if exist "app\src\main\res\layout\activity_splash.xml" (
    echo ✓ Splash activity layout found
) else (
    echo ✗ Splash activity layout missing
)

if exist "app\src\main\res\layout\activity_login.xml" (
    echo ✓ Login activity layout found
) else (
    echo ✗ Login activity layout missing
)

if exist "app\src\main\res\layout\activity_interview.xml" (
    echo ✓ Interview activity layout found
) else (
    echo ✗ Interview activity layout missing
)

echo.
echo Checking resources...

if exist "app\src\main\res\values\strings.xml" (
    echo ✓ Strings resource found
) else (
    echo ✗ Strings resource missing
)

if exist "app\src\main\res\values\colors.xml" (
    echo ✓ Colors resource found
) else (
    echo ✗ Colors resource missing
)

if exist "app\src\main\res\values\themes.xml" (
    echo ✓ Themes resource found
) else (
    echo ✗ Themes resource missing
)

echo.
echo Checking documentation...

if exist "README.md" (
    echo ✓ README.md found
) else (
    echo ✗ README.md missing
)

if exist "PROJECT_SUMMARY.md" (
    echo ✓ PROJECT_SUMMARY.md found
) else (
    echo ✗ PROJECT_SUMMARY.md missing
)

if exist "BUILD_INSTRUCTIONS.md" (
    echo ✓ BUILD_INSTRUCTIONS.md found
) else (
    echo ✗ BUILD_INSTRUCTIONS.md missing
)

echo.
echo =================================
echo Verification Complete!
echo =================================
echo.
echo Next steps:
echo 1. Set up Java environment (see BUILD_INSTRUCTIONS.md)
echo 2. Open project in Android Studio
echo 3. Wait for Gradle sync
echo 4. Build and run the application
echo.
pause 