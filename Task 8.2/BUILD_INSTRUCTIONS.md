# Job Achiever AI - Build Instructions

## Prerequisites

### 1. Java Development Kit (JDK)
- **Required**: JDK 11 or later
- **Recommended**: JDK 17 (LTS)

#### Download and Install JDK:
1. **Oracle JDK**: https://www.oracle.com/java/technologies/downloads/
2. **OpenJDK**: https://adoptium.net/ (Free alternative)
3. **Amazon Corretto**: https://aws.amazon.com/corretto/ (Free alternative)

#### Set JAVA_HOME Environment Variable:

**Windows:**
1. Open System Properties → Advanced → Environment Variables
2. Add new system variable:
   - Variable Name: `JAVA_HOME`
   - Variable Value: `C:\Program Files\Java\jdk-17` (adjust path to your JDK installation)
3. Add to PATH: `%JAVA_HOME%\bin`

**Alternative Windows Command:**
```cmd
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%
```

### 2. Android Studio
- **Download**: https://developer.android.com/studio
- **Version**: Latest stable version
- **SDK**: Android SDK API 24+ (included with Android Studio)

## Build Process

### Option 1: Using Android Studio (Recommended)
1. Open Android Studio
2. Choose "Open an existing Android Studio project"
3. Navigate to the project directory: `Task 8.2`
4. Wait for Gradle sync to complete
5. Click "Build" → "Make Project" or press `Ctrl+F9`
6. Run the app by clicking the green play button

### Option 2: Command Line Build
```cmd
# Navigate to project directory
cd "Task 8.2"

# Make gradlew executable (Linux/Mac)
chmod +x gradlew

# Build the project
gradlew build

# Install and run on connected device
gradlew installDebug
```

### Option 3: If Java Issues Persist
```cmd
# Use Android Studio's embedded JDK
set JAVA_HOME=C:\Users\%USERNAME%\AppData\Local\Android\Sdk\jdk\jdk-version
gradlew build
```

## Project Structure Verification

After successful build, verify these key files exist:
- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/intermediates/` (build artifacts)

## Troubleshooting

### Common Issues:

1. **"JAVA_HOME is not set"**
   - Solution: Set JAVA_HOME environment variable as described above

2. **"Build was configured to prefer settings repositories"**
   - ✅ **FIXED**: Repositories moved to `settings.gradle`

3. **"SDK location not found"**
   - Create `local.properties` file in root directory:
   ```properties
   sdk.dir=C:\\Users\\%USERNAME%\\AppData\\Local\\Android\\Sdk
   ```

4. **Gradle version conflicts**
   - Use the wrapper: `gradlew` instead of global `gradle`

5. **Firebase dependencies missing**
   - Ensure `google-services.json` is in `app/` directory
   - Internet connection required for first build

## Firebase Setup (Optional for Full Functionality)

1. Go to https://console.firebase.google.com
2. Create a new project or use existing
3. Add Android app with package name: `com.example.task82`
4. Download `google-services.json`
5. Replace the placeholder file in `app/google-services.json`

## Testing the Application

### Features to Test:
1. **Splash Screen**: App loads with branding
2. **Authentication**: Login/signup functionality
3. **Home Screen**: Navigation and feature cards
4. **Interview Practice**: Speech recognition and AI feedback
5. **Dashboard**: Charts and analytics display
6. **Feedback**: Form submission
7. **Profile**: User statistics

### Required Permissions:
- **Microphone**: For speech recognition during interviews
- **Internet**: For Firebase and future AI API calls

## Performance Expectations

- **Build Time**: 2-5 minutes (first build)
- **App Size**: ~15MB
- **Min Android Version**: 7.0 (API 24)
- **Target Android Version**: Latest (API 35)

## Success Indicators

✅ **Build Successful** when you see:
```
BUILD SUCCESSFUL in Xs
```

✅ **App Ready** when:
- APK file is generated in `app/build/outputs/apk/`
- No compilation errors in Android Studio
- App installs and runs on device/emulator

## Support

If you encounter issues:
1. Check this troubleshooting guide
2. Review Android Studio build logs
3. Ensure all prerequisites are installed
4. Try "Clean Project" and "Rebuild Project" in Android Studio

---

**Note**: The application is fully functional even without Firebase configuration, using local data storage for demonstration purposes. 