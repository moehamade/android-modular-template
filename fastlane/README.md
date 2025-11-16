# Fastlane Setup

## Prerequisites

1. **Install Fastlane**:
   ```bash
   # Using Homebrew (macOS)
   brew install fastlane

   # Or using RubyGems
   gem install fastlane
   ```

2. **Install Fastlane plugins**:
```bash
   cd fastlane
   fastlane install_plugins
   ```

## Required Credentials

### 1. Android Keystore

Create or use existing keystore for signing:
```bash
keytool -genkey -v -keystore myapp-release.jks \
  -alias myapp -keyalg RSA -keysize 2048 -validity 10000
```

Store path in `keystore.properties` (gitignored):
```properties
storeFile=../myapp-release.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=myapp
keyPassword=YOUR_KEY_PASSWORD
```

### 2. Play Store Service Account

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Select your project (or create one)
3. Navigate to: **IAM & Admin** → **Service Accounts**
4. Click **Create Service Account**
5. Give it a name (e.g., "Fastlane Deploy")
6. Grant role: **Service Account User**
7. Click **Create Key** → Choose **JSON**
8. Download the JSON file

Store the path in environment variable:
```bash
export SUPPLY_JSON_KEY="/path/to/service-account.json"
```

Or add to Appfile:
```ruby
json_key_file("/path/to/service-account.json")
```

### 3. Play Console API Access

1. Go to [Google Play Console](https://play.google.com/console)
2. Navigate to: **Setup** → **API access**
3. Link the service account created above
4. Grant permissions:
   - **Admin** (for full control)
   - Or specific permissions: Manage releases, View app information

## Environment Variables

Set these in your shell or CI/CD:

```bash
# Android signing
export ANDROID_KEYSTORE_PATH="/path/to/myapp-release.jks"
export KEYSTORE_PASSWORD="your_keystore_password"
export KEY_ALIAS="myapp"
export KEY_PASSWORD="your_key_password"

# Play Store
export SUPPLY_JSON_KEY="/path/to/service-account.json"

# Optional: Skip git status check
export SKIP_GIT_CHECK="true"
```

## Available Lanes

### Build Lanes

```bash
# Build debug APK for dev environment
fastlane build_dev_debug

# Build release APK for dev environment
fastlane build_dev_release

# Build production release AAB (for Play Store)
fastlane build_prod_release
```

### Testing and Quality

```bash
# Run all unit tests
fastlane test

# Run Detekt static analysis
fastlane detekt

# Run Android Lint
fastlane lint

# Run all quality checks (tests + detekt + lint)
fastlane quality
```

### Deployment Lanes

```bash
# Deploy to internal testing track
fastlane deploy_internal

# Deploy to beta (closed testing) track
fastlane deploy_beta

# Promote beta build to production
fastlane deploy_production
```

### Utility Lanes

```bash
# Capture screenshots
fastlane screenshots

# Download metadata from Play Store
fastlane download_metadata

# Clean build artifacts
fastlane clean
```

## Typical Workflow

### Internal Testing
```bash
# Run quality checks
fastlane quality

# Build and deploy to internal track
fastlane deploy_internal
```

### Beta Release
```bash
# Deploy to beta track for wider testing
fastlane deploy_beta
```

### Production Release
```bash
# Promote tested beta build to production
fastlane deploy_production
```

## Metadata Management

App metadata is stored in `fastlane/metadata/android/`:

```
metadata/android/
└── en-US/
    ├── title.txt                    # App title (max 50 chars)
    ├── short_description.txt        # Short description (max 80 chars)
    ├── full_description.txt         # Full description (max 4000 chars)
    ├── changelogs/
    │   └── default.txt              # Default changelog
    └── images/                      # Screenshots and graphics
        ├── phoneScreenshots/
        ├── sevenInchScreenshots/
        ├── tenInchScreenshots/
        ├── tvScreenshots/
        ├── wearScreenshots/
        ├── icon.png
        ├── featureGraphic.png
        └── tvBanner.png
```

### Adding More Languages

Create new language folders using [BCP-47 codes](https://support.google.com/googleplay/android-developer/answer/9844778):

```bash
mkdir -p metadata/android/es-ES
cp -r metadata/android/en-US/* metadata/android/es-ES/
# Edit Spanish translations
```

## CI/CD Integration

### GitHub Actions Example

See `.github/workflows/deploy.yml` for automated deployment workflow.

Required secrets in GitHub repository settings:
- `ANDROID_KEYSTORE_BASE64` - Base64 encoded keystore file
- `KEYSTORE_PASSWORD` - Keystore password
- `KEY_ALIAS` - Key alias
- `KEY_PASSWORD` - Key password
- `PLAY_STORE_JSON_KEY_DATA` - Service account JSON content

### Environment-specific Builds

The project uses product flavors:
- **dev** - Development environment (with `.dev` suffix)
- **prod** - Production environment (as configured in `template.properties`)

Fastlane automatically uses the correct flavor based on the lane.

## Troubleshooting

### "Signing not configured"
Ensure environment variables are set:
```bash
echo $ANDROID_KEYSTORE_PATH
echo $KEYSTORE_PASSWORD
```

### "Play Store API access denied"
Check service account permissions in Play Console:
**Setup** → **API access** → Verify service account has correct permissions

### "APK/AAB not found"
Ensure build completed successfully:
```bash
ls -la app/build/outputs/bundle/prodRelease/
```

### "Metadata validation failed"
Check character limits:
- Title: max 50 characters
- Short description: max 80 characters
- Full description: max 4000 characters

## Next Steps

1. ✅ Install Fastlane
2. ⬜ Generate/obtain Android keystore
3. ⬜ Create Play Store service account JSON
4. ⬜ Configure Play Console API access
5. ⬜ Set environment variables
6. ⬜ Customize metadata (title, description)
7. ⬜ Add screenshots to `metadata/android/en-US/images/`
8. ⬜ Test with `fastlane build_prod_release`
9. ⬜ Deploy to internal track: `fastlane deploy_internal`

## Resources

- [Fastlane Documentation](https://docs.fastlane.tools/)
- [Fastlane Android Setup](https://docs.fastlane.tools/getting-started/android/setup/)
- [Play Store Deployment](https://docs.fastlane.tools/actions/upload_to_play_store/)
- [Screengrab (Screenshots)](https://docs.fastlane.tools/actions/screengrab/)
