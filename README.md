# Zencastr

A modern Android podcast recording application built with Jetpack Compose and Clean Architecture.

## Features

- 🎙️ High-quality audio recording
- 👥 Multi-participant session management
- 🎨 Material3 design with dark/light themes
- 📱 Modern Compose UI
- 🔐 Secure JWT authentication
- ☁️ Cloud sync capabilities

## Tech Stack

- **UI**: Jetpack Compose with Material3
- **Architecture**: Multi-module Clean Architecture
- **Dependency Injection**: Hilt
- **Navigation**: Navigation3 with type-safe routing
- **Networking**: Retrofit + OkHttp
- **Local Storage**: DataStore
- **Build System**: Gradle with Kotlin DSL + Convention Plugins

## Project Structure

The project uses a modular architecture for scalability and maintainability:

```
zencastr/
├── app/                    # Main application module
├── core/
│   ├── ui/                # Shared UI components and theme
│   ├── navigation/        # Type-safe navigation setup
│   ├── network/          # Network configuration
│   ├── data/             # Data layer and repositories
│   ├── domain/           # Business logic (pure Kotlin)
│   └── datastore/        # Local data persistence
├── feature/
│   ├── recording/        # Recording functionality
│   └── profile/          # User profile management
└── build-logic/          # Gradle convention plugins
```

## Getting Started

### Prerequisites

- Android Studio Jellyfish (2023.3.1) or newer
- JDK 11 or higher
- Android SDK (API 30+)

### Setup

1. Clone the repository:
```bash
git clone https://github.com/yourusername/zencastr.git
cd zencastr
```

2. Open the project in Android Studio

3. Let Gradle sync complete

4. Build the project:
```bash
./gradlew build
```

5. Run the app on an emulator or device

### Configuration

Create a `local.properties` file in the root directory and add:
```properties
sdk.dir=/path/to/your/android/sdk
```

## Building

```bash
# Build all modules
./gradlew build

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run tests
./gradlew test

# Clean build
./gradlew clean build
```

## Architecture Highlights

### Multi-Module Design

Each module has a specific responsibility:

- **Core modules** provide shared functionality (UI, networking, data)
- **Feature modules** are isolated and independently developable
- **Convention plugins** eliminate build configuration boilerplate

### Type-Safe Navigation

Features expose navigation routes through `:api` modules:

```kotlin
@Serializable
sealed interface RecordingRoute : NavKey {
    @Serializable
    data class Record(val sessionId: String) : RecordingRoute
}
```

This enables cross-feature navigation without tight coupling.

### Clean Network Layer

The network module handles authentication transparently:

- JWT tokens stored securely in DataStore
- Automatic token refresh on 401 responses
- No circular dependencies using Dependency Inversion

## Contributing

We welcome contributions! Please read [CONTRIBUTING.md](CONTRIBUTING.md) for:

- Commit message conventions
- Code style guidelines
- Pull request process
- How to create new modules

Quick commit format:
```
<type>(<scope>): <description>

Example: feat(recording): add pause/resume functionality
```

## Development Workflow

### Creating a New Feature

Use the scaffolding task:
```bash
./gradlew createFeature -PfeatureName=myfeature
```

This automatically creates the module structure with proper configuration.

### Running Tests

```bash
# Unit tests
./gradlew test

# Specific module
./gradlew :feature:recording:test

# UI tests (requires device)
./gradlew connectedAndroidTest
```

### Troubleshooting

If you encounter build issues:
```bash
./gradlew --stop
./gradlew clean build
```

See [CONTRIBUTING.md](CONTRIBUTING.md) for more troubleshooting tips.

## Documentation

- [CONTRIBUTING.md](CONTRIBUTING.md) - Contribution guidelines
- [CLAUDE.md](CLAUDE.md) - AI assistant development guide
- Module READMEs - Documentation for each module

## License

[Your License Here]

## Contact

[Your Contact Information]

---

Made with ❤️ using Jetpack Compose

