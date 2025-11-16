# Production Setup Guide

This guide covers everything needed to prepare the app for production release.

## ✅ Prerequisites

- [ ] Backend API is implemented and deployed
- [ ] Google Play Developer account created
- [ ] Signing keystore generated
- [ ] Privacy policy and terms of service published
- [ ] Firebase project created (for Crashlytics)

---

## 🔐 Step 1: Generate Release Keystore

```bash
keytool -genkey -v -keystore myapp-release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias myapp
```

**Store the keystore securely!**

### Create `keystore.properties`

Create this file in the project root (it's gitignored):

```properties
storeFile=../myapp-release.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=myapp
keyPassword=YOUR_KEY_PASSWORD
```

### Enable Signing in `app/build.gradle.kts`

Uncomment these lines:
```kotlin
signingConfigs {
    create("release") {
        val keystorePropertiesFile = rootProject.file("keystore.properties")
        if (keystorePropertiesFile.exists()) {
            val keystoreProperties = java.util.Properties()
            keystoreProperties.load(java.io.FileInputStream(keystorePropertiesFile))
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }
}

buildTypes {
    release {
        signingConfig = signingConfigs.getByName("release") // Uncomment this
    }
}
```

---

## 🔧 Step 2: Configure API Base URL

Update in `app/build.gradle.kts`:

```kotlin
defaultConfig {
    buildConfigField("String", "API_BASE_URL", "\"https://api.example.com/\"")
}
```

For staging/production variants:
```kotlin
buildTypes {
    debug {
        buildConfigField("String", "API_BASE_URL", "\"https://staging.example.com/\"")
    }
    release {
        buildConfigField("String", "API_BASE_URL", "\"https://api.example.com/\"")
    }
}
```

---

## 🧪 Step 3: Test Release Build

```bash
# Build release APK
./gradlew assembleRelease

# Install on device
adb install app/build/outputs/apk/release/app-release.apk

# Test thoroughly:
# - Authentication flow
# - Token refresh handling
# - Network error handling
# - Offline behavior
# - All critical user flows
```

### Verify ProGuard

Check that ProGuard is working:
```bash
# Build should show:
# > Task :app:minifyReleaseWithR8
```

If release build crashes:
1. Check `app/build/outputs/mapping/release/mapping.txt` for obfuscation map
2. Use this to deobfuscate stack traces
3. Add missing ProGuard rules to `app/proguard-rules.pro`

---

## 📊 Step 4: Add Crashlytics (Recommended)

### 4.1 Add Firebase to Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create new project or use existing
3. Add Android app with package matching your `template.properties`
4. Download `google-services.json` → place in `app/`

### 4.2 Update Dependencies

In `gradle/libs.versions.toml`:
```toml
[versions]
firebase-bom = "33.1.0"

[libraries]
firebase-bom = { module = "com.google.firebase:firebase-bom", version.ref = "firebase-bom" }
firebase-crashlytics = { module = "com.google.firebase:firebase-crashlytics-ktx" }
firebase-analytics = { module = "com.google.firebase:firebase-analytics-ktx" }

[plugins]
google-services = { id = "com.google.gms.google-services", version = "4.4.2" }
firebase-crashlytics = { id = "com.google.firebase.crashlytics", version = "3.0.2" }
```

In `app/build.gradle.kts`:
```kotlin
plugins {
    // ...existing plugins
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

dependencies {
    // ...existing dependencies
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
}
```

### 4.3 Initialize in Application Class

```kotlin
// In Application class
class MyAppApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            // Production logging (only errors to Crashlytics)
            Timber.plant(CrashlyticsTree())
        }
    }
}

// Create CrashlyticsTree
class CrashlyticsTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.ERROR || priority == Log.WARN) {
            FirebaseCrashlytics.getInstance().log("$tag: $message")
            t?.let { FirebaseCrashlytics.getInstance().recordException(it) }
        }
    }
}
```

---

## 📱 Step 5: Prepare Play Store Listing

### Required Assets

- [ ] **App Icon** - 512x512 PNG
- [ ] **Feature Graphic** - 1024x500 PNG
- [ ] **Screenshots** - At least 2, up to 8 (phone and tablet)
- [ ] **Privacy Policy URL**
- [ ] **App Description** (short and full)
- [ ] **Promotional video** (optional)

### Content Rating

Complete the content rating questionnaire in Play Console.

### App Category

Choose appropriate category (e.g., "Productivity" or "Music & Audio").

---

## 🚀 Step 6: Build Release Bundle

```bash
# Generate signed app bundle (AAB) for Play Store
./gradlew bundleRelease

# Output: app/build/outputs/bundle/release/app-release.aab
```

### Test Bundle Before Upload

```bash
# Use bundletool to test
bundletool build-apks --bundle=app/build/outputs/bundle/release/app-release.aab \
  --output=myapp.apks \
  --ks=myapp-release.jks \
  --ks-key-alias=myapp

# Install on connected device
bundletool install-apks --apks=myapp.apks
```

---

## 📝 Step 7: Upload to Play Console

1. Go to [Google Play Console](https://play.google.com/console)
2. Create new app (or select existing)
3. Go to **Production** → **Create new release**
4. Upload `app-release.aab`
5. Fill in release notes
6. Review and rollout

### Release Types

- **Internal testing** - Up to 100 testers, instant updates
- **Closed testing** - Larger group, opt-in via link
- **Open testing** - Public beta, anyone can join
- **Production** - Live on Play Store

Start with **Internal testing**, then **Closed → Open → Production**.

---

## 🔍 Step 8: Monitor Release

### First 24 Hours

- [ ] Check Crashlytics for crashes
- [ ] Monitor Play Console vitals (ANRs, crash rate)
- [ ] Watch user reviews
- [ ] Check analytics for anomalies

### Key Metrics

- **Crash-free rate** - Should be > 99%
- **ANR rate** - Should be < 0.5%
- **App startup time** - Should be < 2s
- **Battery usage** - Should be minimal

---

## 🛠️ Troubleshooting

### ProGuard Issues

If release build crashes:
```bash
# Get stack trace
adb logcat -d > crash.txt

# Deobfuscate
retrace.sh app/build/outputs/mapping/release/mapping.txt crash.txt
```

Add missing keep rules to `app/proguard-rules.pro`.

### Signing Issues

If upload fails with "Signature mismatch":
- You're using a different keystore than previous uploads
- Use the same keystore for all releases
- If keystore is lost, you must create a new app listing

---

## 📋 Pre-Launch Checklist

- [ ] All TODOs in code resolved
- [ ] API base URL points to production
- [ ] ProGuard enabled and tested
- [ ] App signed with release keystore
- [ ] Crashlytics configured
- [ ] Privacy policy published
- [ ] Play Store listing complete
- [ ] Internal testing passed
- [ ] Beta testing passed
- [ ] All critical bugs fixed
- [ ] Performance tested on low-end devices
- [ ] Battery drain tested
- [ ] Network error handling tested
- [ ] Offline mode tested

---

## 🎉 Post-Launch

1. **Monitor closely** for first week
2. **Respond to reviews** promptly
3. **Fix critical bugs** immediately
4. **Plan updates** based on feedback
5. **Iterate and improve**

---

## 🆘 Emergency Procedures

### Critical Bug in Production

1. **Stop rollout** immediately in Play Console
2. **Fix bug** in code
3. **Build hotfix** release
4. **Test thoroughly**
5. **Upload new version**
6. **Resume rollout**

### Data Breach

1. **Revoke all refresh tokens** on backend
2. **Force app update** via Play Console
3. **Notify users** per GDPR requirements
4. **Investigate** root cause
5. **Patch vulnerability**

---

## 📚 Additional Resources

- [Android Launch Checklist](https://developer.android.com/distribute/best-practices/launch/launch-checklist)
- [Play Console Help](https://support.google.com/googleplay/android-developer)
- [Firebase Crashlytics](https://firebase.google.com/docs/crashlytics)
- [ProGuard Manual](https://www.guardsquare.com/manual/home)

