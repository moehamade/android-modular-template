# GitHub Actions Workflows

This directory contains CI/CD workflows for the Android project.

## Workflows

### 1. CI Workflow (`ci.yml`)

**Triggers**:
- Push to `main` branch
- Pull requests to `main` branch
- Manual trigger via `workflow_dispatch`

**Jobs**:

#### Build & Test
- Builds all modules
- Runs unit tests
- Runs Detekt static analysis
- Uploads build reports on failure

#### Lint Check
- Runs Android Lint
- Uploads lint reports

#### Assemble Release
- Builds dev release APK
- Builds prod release APK
- Uploads both APKs as artifacts (14 day retention)

**Requirements**:
- None for basic CI
- Optional: `GOOGLE_SERVICES_JSON` secret for Firebase builds

### 2. Deploy Workflow (`deploy.yml`)

**Triggers**:
- Manual trigger via `workflow_dispatch` with track selection:
  - `internal` - Internal testing track
  - `beta` - Closed testing (beta) track
  - `production` - Production release

**Options**:
- `promote-from-beta`: Skip build and promote existing beta to production

**Jobs**:

#### Deploy
- Decodes Android keystore
- Builds signed production AAB
- Uploads to Play Store using Fastlane
- Uploads Crashlytics mapping files
- Creates git tags for production releases
- Creates GitHub releases

**Requirements** (see Secrets section below):
- `ANDROID_KEYSTORE_BASE64`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`
- `PLAY_STORE_SERVICE_ACCOUNT`
- `GOOGLE_SERVICES_JSON` (optional, commented out)

## Required Secrets

Configure these in GitHub repository settings: **Settings** → **Secrets and variables** → **Actions**

### Android Signing Secrets

#### 1. `ANDROID_KEYSTORE_BASE64`
Base64-encoded keystore file.

**How to create**:
```bash
# Create keystore if you don't have one
keytool -genkey -v -keystore myapp-release.jks \
  -alias myapp -keyalg RSA -keysize 2048 -validity 10000

# Encode to base64
base64 -i myapp-release.jks | pbcopy  # macOS
base64 -w 0 myapp-release.jks | xclip  # Linux
```

#### 2. `KEYSTORE_PASSWORD`
Password for the keystore file.

#### 3. `KEY_ALIAS`
Alias of the key in the keystore (e.g., `myapp`).

#### 4. `KEY_PASSWORD`
Password for the specific key.

### Play Store Secrets

#### 5. `PLAY_STORE_SERVICE_ACCOUNT`
Google Play service account JSON key content.

**How to create**:
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Navigate to: **IAM & Admin** → **Service Accounts**
3. Create service account with **Service Account User** role
4. Create JSON key
5. Copy entire JSON content to this secret

**Alternative**: Use `PLAY_STORE_SERVICE_ACCOUNT_FILE` if you prefer file path.

### Firebase Secrets (Optional)

#### 6. `GOOGLE_SERVICES_JSON`
Content of `google-services.json` file from Firebase Console.

**How to create**:
1. Download `google-services.json` from Firebase Console
2. Copy entire JSON content to this secret

**Note**: Currently commented out in workflows. Uncomment when Firebase is fully configured.

## Environment Variables

No additional environment variables needed - all configuration is through secrets.

## Usage Examples

### Run CI Manually

1. Go to **Actions** tab
2. Select **CI** workflow
3. Click **Run workflow**
4. Select branch
5. Click **Run workflow**

### Deploy to Internal Track

1. Go to **Actions** tab
2. Select **Deploy to Play Store** workflow
3. Click **Run workflow**
4. Select:
   - Branch: `main`
   - Track: `internal`
   - Promote from beta: `false`
5. Click **Run workflow**

### Promote Beta to Production

1. Go to **Actions** tab
2. Select **Deploy to Play Store** workflow
3. Click **Run workflow**
4. Select:
   - Branch: `main`
   - Track: `production`
   - Promote from beta: `true` ✅
5. Click **Run workflow**

This will promote the existing beta build without creating a new build.

## Workflow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                         Push to main                        │
│                              │                              │
│                              ▼                              │
│                    ┌─────────────────┐                      │
│                    │   CI Workflow   │                      │
│                    └─────────────────┘                      │
│                              │                              │
│              ┌───────────────┼───────────────┐              │
│              ▼               ▼               ▼              │
│         ┌────────┐    ┌──────────┐   ┌──────────┐           │
│         │ Build  │    │   Lint   │   │ Assemble │           │
│         │ & Test │    │   Check  │   │ Release  │           │
│         └────────┘    └──────────┘   └──────────┘           │
│                                                             │
│                      Manual Trigger                         │
│                              │                              │
│                              ▼                              │
│                  ┌──────────────────────┐                   │
│                  │  Deploy Workflow     │                   │
│                  └──────────────────────┘                   │
│                              │                              │
│              ┌───────────────┼───────────────┐              │
│              ▼               ▼               ▼              │
│         ┌─────────┐   ┌──────────┐   ┌────────────┐         │
│         │Internal │   │   Beta   │   │ Production │         │
│         │  Track  │   │  Track   │   │   Track    │         │
│         └─────────┘   └──────────┘   └────────────┘         │
│                                              │              │
│                                              ▼              │
│                                    ┌──────────────────┐     │
│                                    │ Git Tag & GitHub │     │
│                                    │     Release      │     │
│                                    └──────────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

## Customization

### Auto-deploy on Tag Push

Uncomment in `deploy.yml`:
```yaml
on:
  push:
    tags:
      - 'v*.*.*'
```

This will auto-deploy to internal track when you push a version tag.

### Add Slack Notifications

Uncomment the notification step in `deploy.yml` and add `SLACK_WEBHOOK` secret:

```yaml
- name: Notify deployment status
  if: always()
  uses: 8398a7/action-slack@v3
  with:
    status: ${{ job.status }}
    webhook_url: ${{ secrets.SLACK_WEBHOOK }}
```

### Enable Firebase Crashlytics Mapping Upload

Uncomment in `deploy.yml`:
```yaml
- name: Upload mapping to Crashlytics
  run: |
    ./gradlew :app:uploadCrashlyticsMappingFileProdRelease
```

## Troubleshooting

### Build fails with "google-services.json not found"

**Solution**: Add `GOOGLE_SERVICES_JSON` secret and uncomment the step in workflows:
```yaml
- name: Create google-services.json
  run: |
    echo "${{ secrets.GOOGLE_SERVICES_JSON }}" > app/google-services.json
```

### Deployment fails with "Signing configuration not found"

**Solution**: Verify all signing secrets are configured:
- `ANDROID_KEYSTORE_BASE64`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

### "Version code has already been used"

**Solution**: Bump version before deploying:
```bash
./scripts/bump_version.sh patch
git add version.properties
git commit -m "chore: Bump version"
git push
```

### "Play Store API access denied"

**Solution**: Verify service account has correct permissions in Play Console:
1. Go to Play Console → **Setup** → **API access**
2. Find your service account
3. Grant **Admin** permission or specific release permissions

## Best Practices

1. **Always run CI before deploying** - Ensure tests pass
2. **Use internal track first** - Test builds before beta/production
3. **Promote beta to production** - Don't skip beta testing
4. **Tag production releases** - Workflow creates tags automatically
5. **Monitor deployments** - Check Play Console after deployment
6. **Keep secrets secure** - Never commit secrets to repo

## Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Fastlane Android Setup](https://docs.fastlane.tools/getting-started/android/setup/)
- [Play Store Publishing](https://developer.android.com/studio/publish)
