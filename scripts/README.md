# Version Management Scripts

## Semantic Versioning

This project uses semantic versioning: `MAJOR.MINOR.PATCH`

- **MAJOR**: Breaking changes, incompatible API changes
- **MINOR**: New features, backwards-compatible
- **PATCH**: Bug fixes, backwards-compatible

Version information is stored in `version.properties` at the project root.

## Bumping Versions

### Usage

```bash
# Bump patch version (1.0.0 -> 1.0.1)
./scripts/bump_version.sh patch

# Bump minor version (1.0.0 -> 1.1.0)
./scripts/bump_version.sh minor

# Bump major version (1.0.0 -> 2.0.0)
./scripts/bump_version.sh major
```

### What it does

1. Reads current version from `version.properties`
2. Increments the appropriate version component
3. Always increments `VERSION_CODE` (for Play Store)
4. Updates `version.properties`
5. Shows next steps (commit, tag, push)

### Example

```bash
$ ./scripts/bump_version.sh minor

📦 Current version: 1.0.5 (code: 6)
⬆️  Bumping MINOR version
✅ New version: 1.1.0 (code: 7)
📝 Updated version.properties

Next steps:
  1. Review changes: git diff version.properties
  2. Commit: git add version.properties && git commit -m 'chore: Bump version to 1.1.0'
  3. Tag: git tag v1.1.0
  4. Push: git push && git push --tags
```

## How Versions Are Used

### Build Files

The root `build.gradle.kts` loads `version.properties` and makes it available to all modules:

```kotlin
// Root build.gradle.kts
extra["versionMajor"] = 1
extra["versionMinor"] = 1
extra["versionPatch"] = 0
extra["versionCode"] = 7
extra["versionName"] = "1.1.0"
```

### App Module

The `:app` module uses these values in `defaultConfig`:

```kotlin
// app/build.gradle.kts
defaultConfig {
    versionCode = rootProject.extra["versionCode"] as Int
    versionName = rootProject.extra["versionName"] as String
}
```

### Product Flavors

Version suffixes are added automatically based on flavor:

- **dev**: `versionName = "1.1.0-dev"`
- **prod**: `versionName = "1.1.0"`

### Build Types

Debug builds add suffix:
- **debug**: `versionName = "1.1.0-dev-debug"`

## Version Code

The `VERSION_CODE` in `version.properties` **must always increase** for Play Store releases.

- It increments automatically with every version bump
- Play Store requires each new release to have a higher version code
- Never manually decrease version code

## Integration with Fastlane

Fastlane lanes can access version info:

```ruby
# In Fastfile
lane :current_version do
  version_properties = File.read("../version.properties")
  major = version_properties[/VERSION_MAJOR=(\d+)/, 1]
  minor = version_properties[/VERSION_MINOR=(\d+)/, 1]
  patch = version_properties[/VERSION_PATCH=(\d+)/, 1]
  code = version_properties[/VERSION_CODE=(\d+)/, 1]

  UI.message("Current version: #{major}.#{minor}.#{patch} (#{code})")
end
```

## Git Tagging Convention

After bumping version, create a git tag:

```bash
# Format: v{MAJOR}.{MINOR}.{PATCH}
git tag v1.1.0
git push origin v1.1.0

# Or push all tags
git push --tags
```

## CI/CD Integration

### GitHub Actions

```yaml
- name: Bump version
  run: |
    ./scripts/bump_version.sh patch

- name: Get version
  id: version
  run: |
    VERSION=$(grep "VERSION_NAME=" version.properties | cut -d'=' -f2)
    echo "version=$VERSION" >> $GITHUB_OUTPUT

- name: Create Release
  uses: actions/create-release@v1
  with:
    tag_name: v${{ steps.version.outputs.version }}
    release_name: Release ${{ steps.version.outputs.version }}
```

## Changelog Management

Changelogs for Play Store are stored in:
```
fastlane/metadata/android/en-US/changelogs/
```

Create version-specific changelog:
```bash
# After bumping to version code 7
echo "• New features
• Bug fixes
• Performance improvements" > fastlane/metadata/android/en-US/changelogs/7.txt
```

The version code (not version name) is used for changelog filenames.

## Manual Version Update

If you need to manually update the version:

1. Edit `version.properties`
2. Ensure `VERSION_CODE` only increases (never decreases)
3. Update `VERSION_MAJOR`, `VERSION_MINOR`, `VERSION_PATCH`
4. Sync Gradle project in Android Studio

## Troubleshooting

### "Version code has already been used"

This means you're trying to upload a build with a version code that already exists on Play Store.

**Solution**: Bump version again:
```bash
./scripts/bump_version.sh patch
```

### Version not updating in build

**Solution**: Sync Gradle in Android Studio or clean build:
```bash
./gradlew clean
./gradlew :app:assembleProdRelease
```

### Script permission denied

**Solution**: Make script executable:
```bash
chmod +x scripts/bump_version.sh
```
