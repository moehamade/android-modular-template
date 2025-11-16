# Manual Setup Required

This document contains **step-by-step instructions** for all manual configuration tasks required to build and deploy the Android app.

---

## 📋 Quick Status Check

**For Current Development** (Required Now):
- [ ] Firebase configured (`google-services.json` added)
- [ ] Local build working (`./gradlew :app:assembleDevDebug`)
- [ ] GitHub secrets configured (for CI/CD)
- [ ] **Build Release APKs** workflow working (unsigned APKs)

**For Play Store Deployment** (When Ready):
- [ ] Keystore generated (for signed release builds)
- [ ] Play Store service account (for deployment)
- [ ] Deploy workflow enabled

---

## 🏗️ PRE-PLAY STORE WORKFLOW

**Good news!** You don't need keystore or Play Store setup yet. Use the **Build Release APKs** workflow for development and testing.

### What You Can Do Right Now:

✅ Build unsigned release APKs via GitHub Actions
✅ Test app on devices without signing
✅ Create GitHub releases with downloadable APKs
✅ Use CI/CD for builds, tests, and lint checks

### How to Build Release APKs (No Signing Required):

1. Go to your GitHub repo → **Actions** tab
2. Select **"Build Release APKs"** workflow
3. Click **"Run workflow"**
4. Choose flavor: `dev`, `prod`, or `both`
5. Wait for build to complete (~5-10 minutes)
6. Download APKs from **Artifacts** section
7. Install: `adb install -r app-dev-release.apk`

**These APKs are perfect for**:
- Internal testing
- Beta testing with testers
- Device testing (real devices or Firebase Test Lab)
- QA before Play Store submission

---

## 🚀 REQUIRED NOW - Local Development

### 1. Firebase Configuration

**Without this, the app will NOT build at all.**

#### Step 1: Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Click **"Add project"** or select an existing project
3. Name it **"MyApp"** (or your preferred name)
4. Follow the wizard (Google Analytics is optional)

#### Step 2: Add Android App to Firebase Project

1. In your Firebase project, click the **Android icon** (⚙️ → Add app)
2. Enter package name: **`com.example.myapp`** (match your package from `template.properties`)
3. (Optional) App nickname: **"MyApp Android"**
4. (Optional) Debug signing certificate SHA-1 (for App Links - can be added later)
5. Click **"Register app"**

#### Step 3: Download `google-services.json`

1. Download the `google-services.json` file
2. Place it in the `app/` directory:
   ```bash
   mv ~/Downloads/google-services.json app/google-services.json
   ```
3. **DO NOT commit this file** - it's already in `.gitignore`

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

## 🤖 REQUIRED FOR CI - GitHub Actions

**Without these secrets, CI builds will fail.**

### 2. Add Firebase Config to GitHub Secrets

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

## 🔐 REQUIRED FOR RELEASE BUILDS - Android Keystore

**Without this, you cannot create signed release builds for the Play Store.**

### 3. Generate Release Keystore

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

#### Step 3: Create `keystore.properties`

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

#### Step 4: Uncomment Signing Config in `app/build.gradle.kts`

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

---

## 🔑 REQUIRED FOR DEPLOYMENT - GitHub Secrets

**Without these secrets, automated deployment to Play Store will fail.**

### 4. Add Keystore Secrets to GitHub

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

## 🏪 REQUIRED FOR PLAY STORE - Service Account

**Without this, you cannot deploy to Google Play Store via CI/CD.**

### 5. Create Play Store Service Account

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

---

## 🧪 Optional - Deep Link Testing

### 6. Test Deep Links (After App is Installed)

#### Custom Scheme (`myapp://session/123`)

```bash
adb shell am start -W -a android.intent.action.VIEW \
  -d "myapp://session/123" com.example.myapp.dev
```

#### App Links (`https://example.com/session/123`)

**Requires**: `assetlinks.json` file hosted on your domain

1. Create `assetlinks.json`:
```json
[{
  "relation": ["delegate_permission/common.handle_all_urls"],
  "target": {
    "namespace": "android_app",
    "package_name": "com.example.myapp",
    "sha256_cert_fingerprints": ["YOUR_SHA256_FINGERPRINT"]
  }
}]
```

2. Get SHA-256 fingerprint:
```bash
keytool -list -v -keystore ../myapp-release.jks -alias myapp
```

3. Upload to: `https://example.com/.well-known/assetlinks.json`

4. Test:
```bash
adb shell am start -W -a android.intent.action.VIEW \
  -d "https://example.com/session/123" com.example.myapp
```

---

## 📊 Verification Checklist

### Local Development ✅

- [ ] Firebase project created
- [ ] `google-services.json` downloaded and placed in `app/`
- [ ] Debug build successful: `./gradlew :app:assembleDevDebug`
- [ ] App runs on emulator/device

### CI/CD ✅

- [ ] `GOOGLE_SERVICES_JSON` secret added to GitHub
- [ ] CI builds passing on GitHub Actions

### Release Builds ✅

- [ ] Keystore generated (`MyApp-release.jks`)
- [ ] Keystore backed up securely
- [ ] `keystore.properties` created
- [ ] Signing config uncommented in `app/build.gradle.kts`
- [ ] Release build successful: `./gradlew :app:assembleProdRelease`

### Deployment ✅

- [ ] GitHub secrets added:
  - `ANDROID_KEYSTORE_BASE64`
  - `KEYSTORE_PASSWORD`
  - `KEY_ALIAS`
  - `KEY_PASSWORD`
  - `PLAY_STORE_SERVICE_ACCOUNT`
- [ ] Play Store service account created and linked
- [ ] Deployment workflow tested

---

## 🆘 Troubleshooting

### Build Fails: "File google-services.json is missing"

**Solution**: Download from Firebase Console and place in `app/google-services.json`

### Build Fails: "No matching client found for package name"

**Solution**: Package name in Firebase must match your package from `template.properties`

### CI Fails: "GOOGLE_SERVICES_JSON secret not found"

**Solution**: Add secret in GitHub Settings → Secrets and variables → Actions

### Release Build Fails: "Keystore file not found"

**Solution**:
1. Check `keystore.properties` has correct `storeFile` path
2. Verify `myapp-release.jks` exists at that path

### Deployment Fails: "Unauthorized service account"

**Solution**:
1. Verify service account is linked in Play Console
2. Verify it has "Admin" or "Release manager" permissions
3. Verify JSON key is correct in GitHub secret

### Firebase Crashlytics: "Crash reports not appearing"

**Solution**:
1. Verify Crashlytics is enabled in Firebase Console
2. Trigger a test crash in the app
3. Wait 5-10 minutes for first crash to appear
4. Check ProGuard mapping files uploaded: `.github/workflows/deploy.yml:139-142`

### Deep Links Not Working

**Solution**:
1. Verify intent filters in `AndroidManifest.xml`
2. For App Links: Verify `assetlinks.json` is accessible at `https://yourdomain.com/.well-known/assetlinks.json`
3. Test with `adb shell am start` commands above

---

## 📚 Additional Resources

- **Fastlane Deployment**: `fastlane/README.md`
- **CI/CD Workflows**: `.github/workflows/README.md`
- **Version Management**: `scripts/README.md`
- **Production Checklist**: `NEXT_STEPS.md`

---

## 🎯 Next Steps Based on Your Stage

### Right Now (Development Phase):

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

### When Ready for Play Store:

1. **Generate Keystore** (see [#3 above](#3-generate-release-keystore))

2. **Create Signed Builds**:
   ```bash
   ./gradlew :app:assembleProdRelease  # Signed APK
   ./gradlew :app:bundleProdRelease    # Signed AAB for Play Store
   ```

3. **Enable Deploy Workflow**:
   ```bash
   mv .github/workflows/deploy.yml.disabled .github/workflows/deploy.yml
   git add . && git commit -m "feat: Enable Play Store deployment"
   git push
   ```

4. **Deploy via GitHub Actions**:
   - Go to **Actions** tab → **"Deploy to Play Store"**
   - Click **"Run workflow"**
   - Choose track: `internal`, `beta`, or `production`

5. **Or Deploy via Fastlane** (local):
   ```bash
   fastlane deploy_internal     # Internal testing
   fastlane deploy_beta         # Closed testing
   fastlane deploy_production   # Production release
   ```

---

**Current Status**:
- ✅ **Development Ready** - You can build, test, and create unsigned APKs
- ⏳ **Play Store Ready** - Complete keystore + service account setup when needed
