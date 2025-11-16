# Getting Started with Android Modular Template

This guide will walk you through setting up the Android Modular Template for local development, CI/CD, and eventual Play Store deployment.

---

## Quick Status Check

**For Current Development** (Required Now):
- [ ] Firebase configured (`google-services.json` added)
- [ ] Local build working (`./gradlew :app:assembleDevDebug`)
- [ ] GitHub secrets configured (for CI/CD)
- [ ] Build Release APKs workflow working (unsigned APKs)

**For Play Store Deployment** (When Ready):
- [ ] Keystore generated (for signed release builds)
- [ ] Play Store service account (for deployment)
- [ ] Deploy workflow enabled

---

## Prerequisites

Before you begin, ensure you have:

- **Android Studio** Ladybug (2024.2.1) or newer
- **JDK** 11 or higher
- **Android SDK** (API 30+)
- **Git** installed
- **Firebase account** (free tier is fine)
- **Google Play Developer account** (only when ready to publish - $25 one-time fee)

---

## Step 1: Local Development Setup (Required Now)

### 1.1 Clone and Rebrand the Template

**Option A: Quick Rebrand (Recommended)**

```bash
# Clone the template
git clone https://github.com/yourusername/android-modular-template.git MyApp
cd MyApp

# Interactive rebrand
./rebrand.sh

# Or specify values directly
./rebrand.sh --project-name MyApp \
             --package-name com.mycompany.myapp \
             --app-name "My Awesome App"

# Preview changes first (dry run)
./rebrand.sh --project-name MyApp \
             --package-name com.mycompany.myapp \
             --dry-run
```

**Option B: Manual Setup**

```bash
git clone https://github.com/yourusername/android-modular-template.git
cd android-modular-template
# Manually edit template.properties and rebuild
```

### 1.2 Firebase Configuration

**This is required - the app will NOT build without it.**

#### Step 1: Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Click **"Add project"** or select an existing project
3. Name it **"MyApp"** (or your preferred name)
4. Follow the wizard (Google Analytics is optional)

#### Step 2: Add Android Apps to Firebase Project

**Important**: This template uses product flavors (dev/prod) and build types (debug/release), which create different package names. You need to add **all 4 package variants** to Firebase for full functionality.

**Add these 4 Android apps to your Firebase project:**

1. **Production Release** (main app for Play Store):
   - Click **Android icon** (⚙️ → Add app)
   - Package name: **`com.example.myapp`** (base package from `template.properties`)
   - Nickname: **"MyApp - Production"**
   - Click **"Register app"**

2. **Production Debug** (for testing production config):
   - Click **Add another app** → **Android icon**
   - Package name: **`com.example.myapp.debug`** (base + `.debug`)
   - Nickname: **"MyApp - Prod Debug"**
   - Click **"Register app"**

3. **Dev Release** (for development environment):
   - Click **Add another app** → **Android icon**
   - Package name: **`com.example.myapp.dev`** (base + `.dev`)
   - Nickname: **"MyApp - Dev"**
   - Click **"Register app"**

4. **Dev Debug** (for day-to-day development):
   - Click **Add another app** → **Android icon**
   - Package name: **`com.example.myapp.dev.debug`** (base + `.dev.debug`)
   - Nickname: **"MyApp - Dev Debug"**
   - Click **"Register app"**

**Why 4 apps?**
- Product flavors (`dev`, `prod`) let you test against different backend environments
- Build types (`debug`, `release`) control optimization and debug tools
- Each combination gets a unique package name (via `applicationIdSuffix`)
- Firebase services (Analytics, Crashlytics, FCM) are isolated per package
- This prevents dev/debug data from polluting production metrics

#### Step 3: Download google-services.json

**Important**: After adding all 4 apps, Firebase will generate a **single** `google-services.json` file that contains configurations for all of them.

1. Go to **Project Settings** → **Your apps** → Select any of the apps
2. Click **"Download google-services.json"** button
3. Place it in the `app/` directory:
   ```bash
   mv ~/Downloads/google-services.json app/google-services.json
   ```
4. **DO NOT commit this file** - it's already in `.gitignore`

**Note**: The Google Services plugin automatically selects the correct configuration based on your build variant's `applicationId`.

#### Step 4: Enable Firebase Services (Recommended)

In Firebase Console, enable these services:

**Crashlytics** (Crash Reporting):
- Go to: **Build → Crashlytics**
- Click **"Enable Crashlytics"**

**Analytics** (User Analytics):
- Automatically enabled when project is created
- Go to: **Analytics → Dashboard** to verify

**Cloud Messaging** (Push Notifications):
- Automatically enabled when project is created
- Go to: **Build → Cloud Messaging** to verify

**Remote Config** (Feature Flags):
- Go to: **Build → Remote Config**
- Click **"Get started"**

**Performance Monitoring** (Performance Tracking):
- Go to: **Build → Performance Monitoring**
- Click **"Get started"**

#### Step 5: Verify Local Build

```bash
./gradlew :app:assembleDevDebug
```

If successful, you'll see:
```
BUILD SUCCESSFUL in 2m 15s
```

---

## Step 2: CI/CD Setup (Required for GitHub Actions)

### 2.1 Add Firebase Config to GitHub Secrets

**Without these secrets, CI builds will fail.**

#### Step 1: Convert to Base64

```bash
# macOS/Linux
cat app/google-services.json | base64 | pbcopy

# On Linux without pbcopy
cat app/google-services.json | base64 | xclip -selection clipboard
```

#### Step 2: Add to GitHub

1. Go to your GitHub repo
2. Navigate to: **Settings → Secrets and variables → Actions**
3. Click **"New repository secret"**
4. Name: `GOOGLE_SERVICES_JSON`
5. Value: Paste the base64 string from clipboard
6. Click **"Add secret"**

#### Step 3: Verify CI Build

Push any change to trigger CI:
```bash
git add .
git commit -m "test: Verify CI with Firebase config"
git push origin main
```

Check: **Actions** tab → Your workflow should now build successfully

---

## Step 3: Building Release APKs (No Signing Required!)

**Good news!** You don't need a keystore yet. Use the **Build Release APKs** workflow for development and testing.

### What You Can Do Right Now:

✅ Build unsigned release APKs via GitHub Actions
✅ Test app on devices without signing
✅ Create GitHub releases with downloadable APKs
✅ Use CI/CD for builds, tests, and lint checks

### Option A: Via GitHub Actions (Recommended)

1. Go to your GitHub repo → **Actions** tab
2. Select **"Build Release APKs"** workflow
3. Click **"Run workflow"**
4. Choose flavor: `dev`, `prod`, or `both`
5. Wait for build to complete (~5-10 minutes)
6. Download APKs from **Artifacts** section
7. Install: `adb install -r app-dev-release.apk`

### Option B: Build Locally

```bash
# Dev release (unsigned)
./gradlew :app:assembleDevRelease

# Prod release (unsigned)
./gradlew :app:assembleProdRelease

# Find APKs in:
# app/build/outputs/apk/dev/release/*.apk
# app/build/outputs/apk/prod/release/*.apk
```

**These unsigned APKs are perfect for:**
- Internal testing
- Beta testing with team
- Device testing (real devices, Firebase Test Lab)
- QA before Play Store submission

---

## Step 4: Release Builds Setup (When Ready for Play Store)

**Only complete this step when you're ready to publish to Google Play Store.**

### 4.1 Generate Release Keystore

#### Step 1: Create Keystore

```bash
keytool -genkey -v \
  -keystore myapp-release.jks \
  -alias myapp \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

You'll be prompted for:
- **Keystore password**: Choose a strong password (save it!)
- **Key password**: Can be same as keystore password
- **Name, organization, etc.**: Fill in your details

**CRITICAL**:
- 🔒 **Backup this file** - if you lose it, you can't update your app!
- 🔒 **Save passwords securely** (use a password manager)
- 🔒 **Never commit the keystore to git**

#### Step 2: Move Keystore to Safe Location

```bash
# Move to parent directory (outside git repo)
mv myapp-release.jks ../myapp-release.jks
```

#### Step 3: Create keystore.properties

Create this file in the project root (it's gitignored):

```bash
cat > keystore.properties << EOF
storeFile=../myapp-release.jks
storePassword=YOUR_KEYSTORE_PASSWORD
keyAlias=myapp
keyPassword=YOUR_KEY_PASSWORD
EOF
```

Replace `YOUR_KEYSTORE_PASSWORD` and `YOUR_KEY_PASSWORD` with actual passwords.

#### Step 4: Uncomment Signing Config in app/build.gradle.kts

Find this section (around line 68) and uncomment:

```kotlin
// Signing configuration for release builds
// Uncomment and configure when ready to sign release builds
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

// In buildTypes.release, add:
buildTypes {
    release {
        signingConfig = signingConfigs.getByName("release")
        // ... existing config ...
    }
}
```

#### Step 5: Test Signed Build

```bash
./gradlew :app:assembleProdRelease
```

Find APK at: `app/build/outputs/apk/prod/release/app-prod-release.apk`

### 4.2 Add Keystore Secrets to GitHub

**For automated deployment via GitHub Actions:**

#### Step 1: Convert Keystore to Base64

```bash
cat ../myapp-release.jks | base64 | pbcopy
```

#### Step 2: Add Secrets to GitHub

Go to: **Settings → Secrets and variables → Actions**

Add these secrets:

| Secret Name | Value | Description |
|-------------|-------|-------------|
| `ANDROID_KEYSTORE_BASE64` | Paste base64 keystore | Encoded keystore file |
| `KEYSTORE_PASSWORD` | Your keystore password | Password for keystore |
| `KEY_ALIAS` | `myapp` | Alias from keytool command |
| `KEY_PASSWORD` | Your key password | Password for key |

---

## Step 5: Play Store Deployment Setup (When Ready to Publish)

### 5.1 Create Play Store Service Account

**Without this, you cannot deploy to Google Play Store via CI/CD.**

#### Step 1: Create Service Account in Google Cloud Console

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Select your project (or create one linked to Play Console)
3. Navigate to: **IAM & Admin → Service Accounts**
4. Click **"Create Service Account"**
   - Name: `myapp-github-actions`
   - Description: `Service account for GitHub Actions deployment`
5. Click **"Create and Continue"**

#### Step 2: Grant Permissions

- Role: **Service Account User**
- Click **"Continue"** → **"Done"**

#### Step 3: Create JSON Key

1. Find your service account in the list
2. Click the **⋮** menu → **"Manage keys"**
3. Click **"Add Key" → "Create new key"**
4. Choose **JSON** format
5. Click **"Create"** - file will download automatically

#### Step 4: Link to Play Console

1. Go to [Google Play Console](https://play.google.com/console/)
2. Navigate to: **Setup → API access**
3. Scroll to **Service accounts**
4. Click **"Link Google Cloud project"** (if not already linked)
5. Grant access to the service account you created:
   - Click **"Grant access"** next to your service account
   - Permissions: **Admin (all permissions)** or **Release manager**
   - Click **"Apply"**

#### Step 5: Add to GitHub Secrets

```bash
# Copy JSON content
cat ~/Downloads/your-service-account-key.json | pbcopy
```

Add to GitHub:
- Name: `PLAY_STORE_SERVICE_ACCOUNT`
- Value: Paste entire JSON content
- Click **"Add secret"**

### 5.2 Enable Deploy Workflow

```bash
# The deploy workflow is currently disabled
mv .github/workflows/deploy.yml.disabled .github/workflows/deploy.yml
git add .github/workflows/deploy.yml
git commit -m "feat: Enable Play Store deployment workflow"
git push
```

### 5.3 Deploy via GitHub Actions

1. Go to **Actions** tab → **"Deploy to Play Store"**
2. Click **"Run workflow"**
3. Choose track: `internal`, `beta`, or `production`
4. Wait for deployment to complete
5. Check Play Console to verify

### 5.4 Deploy via Fastlane (Local Alternative)

```bash
fastlane deploy_internal     # Internal testing
fastlane deploy_beta         # Closed testing
fastlane deploy_production   # Production release
```

---

## Quick Commands Reference

### Build Commands

```bash
# Development builds
./gradlew :app:assembleDevDebug       # Dev debug (with Chucker, LeakCanary)
./gradlew :app:assembleDevRelease     # Dev release (unsigned)

# Production builds (requires keystore setup)
./gradlew :app:assembleProdRelease    # Production release APK
./gradlew :app:bundleProdRelease      # Production AAB (for Play Store)

# Run all tests
./gradlew test

# Code quality checks
./gradlew detekt                      # Static analysis
./gradlew lint                        # Android lint

# Clean build
./gradlew clean build
```

### Version Management

```bash
./scripts/bump_version.sh patch  # 1.0.0 → 1.0.1 (bug fixes)
./scripts/bump_version.sh minor  # 1.0.0 → 1.1.0 (new features)
./scripts/bump_version.sh major  # 1.0.0 → 2.0.0 (breaking changes)
```

### Create New Feature Module

```bash
./gradlew createFeature -PfeatureName=dashboard
```

This automatically creates:
- `:feature:dashboard` - Feature implementation
- `:feature:dashboard:api` - Navigation routes
- Build files, manifests, and boilerplate

### Install Git Hooks

```bash
./install-hooks.sh
```

Pre-commit hooks run:
- Detekt static analysis
- Unit tests

---

## Troubleshooting

### Build Fails: "File google-services.json is missing"

**Solution**: Download from Firebase Console and place in `app/google-services.json`

See [Step 1.2: Firebase Configuration](#12-firebase-configuration)

### Build Fails: "No matching client found for package name"

**Solution**: This error means you're missing one of the 4 required package variants in Firebase.

**Example error:**
```
No matching client found for package name 'com.example.myapp.dev.debug'
```

**Fix:**
1. Check which variant is failing (look at the package name in the error)
2. Go to Firebase Console → Project Settings → Your apps
3. Verify you have all 4 apps registered:
   - `com.example.myapp` (prod release)
   - `com.example.myapp.debug` (prod debug)
   - `com.example.myapp.dev` (dev release)
   - `com.example.myapp.dev.debug` (dev debug)
4. If missing, add the missing app variant (see [Step 1.2](#step-2-add-android-apps-to-firebase-project))
5. Download the updated `google-services.json` and replace the old one
6. Sync Gradle and rebuild

**Alternative (if package changed after rebranding):**
- Re-run `./rebrand.sh` with your new package name
- Delete all apps from Firebase and re-add with new package names

### CI Fails: "GOOGLE_SERVICES_JSON secret not found"

**Solution**: Add secret in GitHub Settings → Secrets and variables → Actions

See [Step 2.1: Add Firebase Config to GitHub Secrets](#21-add-firebase-config-to-github-secrets)

### Release Build Fails: "Keystore file not found"

**Solution**:
1. Check `keystore.properties` has correct `storeFile` path
2. Verify `myapp-release.jks` exists at that path
3. Ensure path is relative to project root (e.g., `../myapp-release.jks`)

### Deployment Fails: "Unauthorized service account"

**Solution**:
1. Verify service account is linked in Play Console
2. Verify it has "Admin" or "Release manager" permissions
3. Verify JSON key is correct in GitHub secret
4. Check that the service account email matches the one in Play Console

### Firebase Crashlytics: "Crash reports not appearing"

**Solution**:
1. Verify Crashlytics is enabled in Firebase Console
2. Trigger a test crash in the app
3. Wait 5-10 minutes for first crash to appear
4. Ensure you're running a release build (Crashlytics disabled in debug)

### Deep Links Not Working

**Solution**:
1. Verify intent filters in `AndroidManifest.xml`
2. For custom scheme (`myapp://`):
   ```bash
   adb shell am start -W -a android.intent.action.VIEW \
     -d "myapp://content/123" com.example.myapp.dev
   ```
3. For App Links (`https://`):
   - Verify `assetlinks.json` is accessible at `https://yourdomain.com/.well-known/assetlinks.json`
   - Get SHA-256 fingerprint: `keytool -list -v -keystore ../myapp-release.jks`
   - Test: `adb shell am start -W -a android.intent.action.VIEW -d "https://example.com/content/123"`

### Import/Sync Issues in Android Studio

```bash
# Stop Gradle daemon
./gradlew --stop

# Clean and rebuild
./gradlew clean build

# If still failing, in Android Studio:
# File → Invalidate Caches → Invalidate and Restart
```

### KSP/Hilt Issues (Generated Code Not Found)

```bash
# Stop Gradle daemon (fixes most KSP issues)
./gradlew --stop

# Rebuild
./gradlew clean build

# Ensure KSP plugin is applied in module's build.gradle.kts
```

### Git Hooks Not Running

```bash
./install-hooks.sh
```

To bypass (not recommended):
```bash
git commit --no-verify
```

---

## Verification Checklist

### ✅ Local Development

- [ ] Firebase project created
- [ ] `google-services.json` downloaded and placed in `app/`
- [ ] Debug build successful: `./gradlew :app:assembleDevDebug`
- [ ] App runs on emulator/device
- [ ] Firebase services enabled (Crashlytics, Analytics, FCM, Remote Config)

### ✅ CI/CD

- [ ] `GOOGLE_SERVICES_JSON` secret added to GitHub
- [ ] CI builds passing on GitHub Actions
- [ ] Unsigned release APKs building successfully

### ✅ Release Builds (For Play Store)

- [ ] Keystore generated (`myapp-release.jks`)
- [ ] Keystore backed up securely (external drive, password manager)
- [ ] `keystore.properties` created
- [ ] Signing config uncommented in `app/build.gradle.kts`
- [ ] Release build successful: `./gradlew :app:assembleProdRelease`

### ✅ Deployment (For Play Store)

- [ ] GitHub secrets added:
  - `ANDROID_KEYSTORE_BASE64`
  - `KEYSTORE_PASSWORD`
  - `KEY_ALIAS`
  - `KEY_PASSWORD`
  - `PLAY_STORE_SERVICE_ACCOUNT`
- [ ] Play Store service account created and linked
- [ ] Deploy workflow enabled (`.github/workflows/deploy.yml`)
- [ ] Deployment workflow tested

---

## What's Next?

### Current Phase: Development & Testing

**You can do this right now:**

1. **Local Development**:
   ```bash
   ./gradlew :app:assembleDevDebug
   ```

2. **Build Unsigned Release APKs** (via GitHub Actions):
   - Go to **Actions** tab → **"Build Release APKs"**
   - Click **"Run workflow"** → Choose flavor
   - Download from artifacts

3. **Test on Devices**:
   ```bash
   adb install -r app-dev-release.apk
   ```

4. **Develop Features**:
   - Full CI/CD support
   - Automatic testing and linting
   - Firebase Analytics and Crashlytics
   - Push notifications

### Future Phase: Play Store Release

**When ready to publish:**

1. **Generate Keystore** (see [Step 4.1](#41-generate-release-keystore))

2. **Create Signed Builds**:
   ```bash
   ./gradlew :app:assembleProdRelease  # Signed APK
   ./gradlew :app:bundleProdRelease    # Signed AAB for Play Store
   ```

3. **Setup Play Store** (see [Step 5](#step-5-play-store-deployment-setup-when-ready-to-publish)):
   - Create app listing
   - Upload screenshots, description, privacy policy
   - Complete content rating questionnaire
   - Upload first release to internal testing

4. **Enable Automated Deployment**:
   - Add GitHub secrets
   - Enable deploy workflow
   - Deploy via GitHub Actions or Fastlane

---

## Additional Resources

### Documentation

- **[README.md](README.md)** - Project overview and features
- **[CONTRIBUTING.md](CONTRIBUTING.md)** - Contribution guidelines
- **[CLAUDE.md](CLAUDE.md)** - Architecture and technical details
- **[docs/architecture/](docs/architecture/)** - Architecture Decision Records (ADRs)
- **[docs/api/](docs/api/)** - API endpoint documentation

### Specific Guides

- **[scripts/README.md](scripts/README.md)** - Version management scripts
- **[.github/workflows/README.md](.github/workflows/README.md)** - CI/CD workflows
- **[fastlane/README.md](fastlane/README.md)** - Fastlane deployment
- **[build-logic/README.md](build-logic/README.md)** - Convention plugins

### External Resources

- [Android Launch Checklist](https://developer.android.com/distribute/best-practices/launch/launch-checklist)
- [Firebase Documentation](https://firebase.google.com/docs)
- [Play Console Help](https://support.google.com/googleplay/android-developer)
- [Modern Android Development](https://developer.android.com/series/mad-skills)

---

## Need Help?

- **Build issues**: See [Troubleshooting](#troubleshooting) above
- **CI/CD issues**: Check `.github/workflows/README.md`
- **Architecture questions**: Read `CLAUDE.md` and `docs/architecture/`
- **Contributing**: See `CONTRIBUTING.md`
- **Open an issue**: [GitHub Issues](https://github.com/yourusername/android-modular-template/issues)

---

**Congratulations!** 🎉 You're all set to start building amazing Android apps with this template.
