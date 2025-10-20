# Deploy Workflow - Currently Disabled

## Why is deploy.yml disabled?

The `deploy.yml` workflow is renamed to `deploy.yml.disabled` because it requires:

1. **Android Keystore** - For signing release builds
2. **Play Store Service Account** - For uploading to Google Play Store
3. **GitHub Secrets** - `ANDROID_KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`, `PLAY_STORE_SERVICE_ACCOUNT`

Since the app is not yet on the Play Store, these are not needed right now.

---

## Current Build Workflow

Use **`build-release.yml`** instead:
- ✅ Builds **unsigned** release APKs
- ✅ Works without keystore or Play Store setup
- ✅ Perfect for development and testing
- ✅ Creates GitHub releases with APK artifacts

### How to use:

**Manual Trigger**:
1. Go to **Actions** tab on GitHub
2. Select **"Build Release APKs"** workflow
3. Click **"Run workflow"**
4. Choose flavor: `dev`, `prod`, or `both`

**Automatic on Version Tags**:
```bash
git tag v1.0.0
git push origin v1.0.0
```

### Download APKs:

After workflow completes:
1. Go to workflow run page
2. Scroll to **Artifacts** section
3. Download `dev-release-apk` or `prod-release-apk`
4. Install: `adb install -r app-dev-release.apk`

---

## When to Enable deploy.yml

Re-enable when you're ready to publish to Play Store:

### Step 1: Generate Keystore

```bash
keytool -genkey -v \
  -keystore zencastr-release.jks \
  -alias zencastr \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

**⚠️ CRITICAL**: Backup this file securely!

### Step 2: Create Play Store Service Account

1. Go to [Google Play Console](https://play.google.com/console/)
2. Create your app listing
3. Go to [Google Cloud Console](https://console.cloud.google.com/)
4. Create service account with Play Store API access
5. Download JSON key

### Step 3: Add GitHub Secrets

Add these in **Settings → Secrets and variables → Actions**:

| Secret Name | How to Get |
|-------------|------------|
| `ANDROID_KEYSTORE_BASE64` | `cat zencastr-release.jks \| base64 \| pbcopy` |
| `KEYSTORE_PASSWORD` | Password from keytool |
| `KEY_ALIAS` | `zencastr` (from keytool) |
| `KEY_PASSWORD` | Password from keytool |
| `PLAY_STORE_SERVICE_ACCOUNT` | Paste entire JSON from service account |

### Step 4: Uncomment Signing Config

Edit `app/build.gradle.kts` lines 46-70:
- Uncomment keystore loading code
- Uncomment `signingConfig = signingConfigs.getByName("release")`

### Step 5: Re-enable Workflow

```bash
mv .github/workflows/deploy.yml.disabled .github/workflows/deploy.yml
git add .github/workflows/deploy.yml
git commit -m "feat: Enable Play Store deployment workflow"
git push
```

### Step 6: Test Deployment

1. Go to **Actions** tab
2. Select **"Deploy to Play Store"** workflow
3. Click **"Run workflow"**
4. Choose track: `internal` (for first deployment)

---

## Detailed Setup Guide

See [`MANUAL_SETUP_REQUIRED.md`](../../MANUAL_SETUP_REQUIRED.md) for complete step-by-step instructions.

---

## Need Help?

- **Build issues**: Check `build-release.yml` workflow logs
- **Keystore generation**: See `MANUAL_SETUP_REQUIRED.md#3`
- **Play Store setup**: See `MANUAL_SETUP_REQUIRED.md#5`
- **CI/CD issues**: Check `.github/workflows/README.md`
