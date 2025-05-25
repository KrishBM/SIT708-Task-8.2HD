# Authentication Guide - Job Achiever AI

## 🚀 Quick Start (Demo Mode)

Your Job Achiever AI app now includes **Demo Mode** for easy testing and demonstration without requiring Firebase configuration.

## How Authentication Works

### Demo Mode (Currently Active)
- **No signup required** - Click the green "🚀 Try Demo" button
- **Instant access** - Bypass all authentication steps
- **Full functionality** - Access all app features immediately
- **Local storage** - Demo login state is saved locally

### Regular Authentication (Firebase)
- **Email/Password signup** - Create real accounts
- **Firebase backend** - Secure cloud authentication
- **Password recovery** - Forgot password functionality
- **Persistent login** - Stay signed in across app restarts

## Current Setup

The app is currently configured in **Demo Mode** for easy demonstration:

```java
// In LoginActivity.java
private static final boolean DEMO_MODE = true;
```

## Using the App

### Option 1: Demo Mode (Recommended for Testing)
1. Launch the app
2. Click "🚀 Try Demo (No Signup Required)" button
3. Instantly access all features

### Option 2: Regular Login (With Demo Fallback)
1. Enter any email address (e.g., `test@example.com`)
2. Enter any password (minimum 6 characters)
3. Click "Sign In" or "Sign Up"
4. If Firebase is not configured, app automatically switches to demo mode

### Option 3: Direct Firebase Integration
1. Set `DEMO_MODE = false` in LoginActivity.java
2. Replace `google-services.json` with your Firebase project file
3. Configure Firebase Authentication in Firebase Console
4. Enable Email/Password authentication method

## Features Available After Login

✅ **AI Mock Interviews** - Practice with 4 job categories  
✅ **Speech Recognition** - Real-time voice transcription  
✅ **AI Feedback** - Contextual interview feedback  
✅ **Analytics Dashboard** - Performance tracking with charts  
✅ **Progress Tracking** - Monitor improvement over time  
✅ **Modern UI** - Material Design 3 interface  

## Signing Out

- Go to **Profile** tab
- Click "Sign Out" button
- Returns to login screen
- Clears both demo and Firebase login states

## Firebase Configuration (Optional)

To enable real Firebase authentication:

1. Create Firebase project at https://console.firebase.google.com
2. Add Android app with package name: `com.example.task82`
3. Download `google-services.json` and replace the placeholder file
4. Enable Authentication > Email/Password in Firebase Console
5. Set `DEMO_MODE = false` in LoginActivity.java

## Troubleshooting

**Q: Sign in/up buttons not working?**  
A: The app includes demo mode fallback. If Firebase fails, it automatically switches to demo mode.

**Q: Can't access the app?**  
A: Use the green "🚀 Try Demo" button for instant access.

**Q: Want to test real authentication?**  
A: Configure Firebase as described above, or use demo mode for full functionality testing.

## Security Notes

- Demo mode is for testing/demonstration only
- Production apps should use proper Firebase configuration
- Demo login state is stored locally and cleared on sign out
- All app features work identically in both demo and Firebase modes

---

**Ready to start?** Launch the app and click "🚀 Try Demo" for instant access to all features! 