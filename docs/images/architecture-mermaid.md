# Android Modular Template - Architecture Diagrams

These Mermaid diagrams can be viewed directly on GitHub or rendered in any Markdown viewer.

## Module Dependency Graph

```mermaid
graph TD
    %% App Module
    App[":app<br/>Main Application"]

    %% Feature Modules
    Recording[":feature:recording"]
    RecordingAPI[":feature:recording:api"]
    Profile[":feature:profile"]
    ProfileAPI[":feature:profile:api"]
    Settings[":feature:settings"]
    SettingsAPI[":feature:settings:api"]

    %% Core Modules
    UI[":core:ui<br/>Design System"]
    Common[":core:common<br/>Infrastructure"]
    Navigation[":core:navigation<br/>Navigation3"]
    Network[":core:network<br/>Retrofit + OkHttp"]
    Data[":core:data<br/>Repositories"]
    Domain[":core:domain<br/>Use Cases"]
    Preferences[":core:datastore:preferences<br/>Encrypted Storage"]
    Proto[":core:datastore:proto"]
    Analytics[":core:analytics<br/>Firebase"]
    Notifications[":core:notifications<br/>FCM"]
    RemoteConfig[":core:remoteconfig<br/>Feature Flags"]

    %% App Dependencies
    App --> Recording
    App --> RecordingAPI
    App --> Profile
    App --> ProfileAPI
    App --> Settings
    App --> SettingsAPI
    App --> UI
    App --> Navigation
    App --> Network
    App --> Data
    App --> Analytics
    App --> Notifications
    App --> RemoteConfig

    %% Feature Dependencies
    Recording --> UI
    Recording --> Domain
    Recording --> Data
    Recording --> Navigation
    Recording --> ProfileAPI
    Recording --> SettingsAPI

    Profile --> UI
    Profile --> Domain
    Profile --> Data
    Profile --> Navigation

    Settings --> UI
    Settings --> Domain
    Settings --> Data
    Settings --> Navigation

    %% Core Dependencies
    Data --> Network
    Data --> Domain
    Data --> Preferences
    Data --> Common

    Network --> Preferences

    Domain --> Common

    Preferences --> Common
    Preferences --> Proto

    %% Styling
    classDef appModule fill:#e1f5ff,stroke:#01579b,stroke-width:3px
    classDef featureModule fill:#f3e5f5,stroke:#4a148c,stroke-width:2px
    classDef apiModule fill:#fff3e0,stroke:#e65100,stroke-width:2px
    classDef coreModule fill:#e8f5e9,stroke:#1b5e20,stroke-width:2px

    class App appModule
    class Recording,Profile,Settings featureModule
    class RecordingAPI,ProfileAPI,SettingsAPI apiModule
    class UI,Common,Navigation,Network,Data,Domain,Preferences,Proto,Analytics,Notifications,RemoteConfig coreModule
```

## Clean Architecture Layers

```mermaid
graph TB
    subgraph "Presentation Layer"
        UI1[":feature:recording<br/>RecordingScreen"]
        UI2[":feature:profile<br/>ProfileScreen"]
        UI3[":feature:settings<br/>SettingsScreen"]
        Components[":core:ui<br/>Shared Components"]
    end

    subgraph "Domain Layer"
        UC1["Use Cases<br/>(Business Logic)"]
        Models["Domain Models"]
        Repo["Repository Interfaces"]
    end

    subgraph "Data Layer"
        RepoImpl["Repository<br/>Implementations"]
        Remote["Remote Data Source<br/>(Retrofit)"]
        Local["Local Data Source<br/>(Room + DataStore)"]
        Cache["In-Memory Cache"]
    end

    subgraph "Infrastructure"
        Network[":core:network<br/>API Services"]
        DB[":core:data<br/>Room Database"]
        Storage[":core:datastore:preferences<br/>Encrypted Storage"]
        Firebase["Firebase<br/>(Analytics, Crashlytics, FCM)"]
    end

    %% Dependencies
    UI1 --> UC1
    UI2 --> UC1
    UI3 --> UC1
    UI1 --> Components
    UI2 --> Components
    UI3 --> Components

    UC1 --> Models
    UC1 --> Repo

    RepoImpl -.implements.-> Repo
    RepoImpl --> Remote
    RepoImpl --> Local
    RepoImpl --> Cache

    Remote --> Network
    Local --> DB
    Local --> Storage

    %% Styling
    classDef presentation fill:#e1f5ff,stroke:#01579b,stroke-width:2px
    classDef domain fill:#fff3e0,stroke:#e65100,stroke-width:2px
    classDef data fill:#f3e5f5,stroke:#4a148c,stroke-width:2px
    classDef infrastructure fill:#e8f5e9,stroke:#1b5e20,stroke-width:2px

    class UI1,UI2,UI3,Components presentation
    class UC1,Models,Repo domain
    class RepoImpl,Remote,Local,Cache data
    class Network,DB,Storage,Firebase infrastructure
```

## Navigation Flow

```mermaid
graph LR
    subgraph "App Entry"
        MainActivity["MainActivity<br/>(Compose)"]
    end

    subgraph "Navigation Graph"
        NavHost["NavHost<br/>(Navigation3)"]
    end

    subgraph "Feature Routes"
        RecordingRoute["RecordingRoute<br/>@Serializable"]
        ProfileRoute["ProfileRoute<br/>@Serializable"]
        SettingsRoute["SettingsRoute<br/>@Serializable"]
    end

    subgraph "Feature Screens"
        RecordingScreen["RecordingScreen<br/>(Camera)"]
        ProfileScreen["ProfileScreen<br/>(User Info)"]
        SettingsScreen["SettingsScreen<br/>(Preferences)"]
    end

    MainActivity --> NavHost
    NavHost --> RecordingRoute
    NavHost --> ProfileRoute
    NavHost --> SettingsRoute

    RecordingRoute --> RecordingScreen
    ProfileRoute --> ProfileScreen
    SettingsRoute --> SettingsScreen

    RecordingScreen -.navigate to.-> ProfileRoute
    ProfileScreen -.navigate to.-> SettingsRoute
    SettingsScreen -.navigate to.-> RecordingRoute

    %% Styling
    classDef entry fill:#e1f5ff,stroke:#01579b,stroke-width:2px
    classDef navigation fill:#fff3e0,stroke:#e65100,stroke-width:2px
    classDef route fill:#f3e5f5,stroke:#4a148c,stroke-width:2px
    classDef screen fill:#e8f5e9,stroke:#1b5e20,stroke-width:2px

    class MainActivity entry
    class NavHost navigation
    class RecordingRoute,ProfileRoute,SettingsRoute route
    class RecordingScreen,ProfileScreen,SettingsScreen screen
```

## Data Flow (Token Refresh Example)

```mermaid
sequenceDiagram
    participant UI as ViewModel
    participant UseCase as LoginUseCase
    participant Repo as AuthRepository
    participant API as AuthApiService
    participant Storage as TinkAuthStorage
    participant Network as OkHttpClient

    UI->>UseCase: login(email, password)
    UseCase->>Repo: login(email, password)
    Repo->>API: POST /auth/login
    API->>Network: HTTP Request
    Network-->>API: 200 OK + tokens
    API-->>Repo: AuthResponse
    Repo->>Storage: saveTokens(access, refresh)
    Storage->>Storage: Encrypt with Tink (AES-256-GCM)
    Storage-->>Repo: Success
    Repo-->>UseCase: Success
    UseCase-->>UI: Success

    Note over Network,Storage: Later... Access token expires

    UI->>UseCase: getProfile()
    UseCase->>Repo: getProfile()
    Repo->>API: GET /user/profile
    API->>Network: HTTP Request + Access Token
    Network-->>API: 401 Unauthorized

    Note over Network: TokenAuthenticator intercepts

    Network->>Storage: getRefreshToken()
    Storage-->>Network: Decrypted refresh token
    Network->>API: POST /auth/refresh
    API-->>Network: 200 OK + new tokens
    Network->>Storage: saveTokens(newAccess, newRefresh)
    Storage-->>Network: Success
    Network->>API: Retry GET /user/profile + new token
    API-->>Network: 200 OK + profile data
    Network-->>Repo: Profile data
    Repo-->>UseCase: Profile
    UseCase-->>UI: Profile
```

## Build Variants Structure

```mermaid
graph TD
    subgraph "Product Flavors"
        Dev["dev<br/>Development Environment"]
        Prod["prod<br/>Production Environment"]
    end

    subgraph "Build Types"
        Debug["debug<br/>Debug Build"]
        Release["release<br/>Release Build"]
    end

    subgraph "Build Variants"
        DevDebug["devDebug<br/>com.example.myapp.dev.debug<br/>• Debug tools enabled<br/>• Dev API endpoint<br/>• LeakCanary + Chucker"]
        DevRelease["devRelease<br/>com.example.myapp.dev<br/>• Optimized build<br/>• Dev API endpoint<br/>• ProGuard enabled"]
        ProdDebug["prodDebug<br/>com.example.myapp.debug<br/>• Debug tools enabled<br/>• Prod API endpoint<br/>• For testing prod config"]
        ProdRelease["prodRelease<br/>com.example.myapp<br/>• Play Store release<br/>• Prod API endpoint<br/>• Fully optimized + signed"]
    end

    Dev --> DevDebug
    Dev --> DevRelease
    Prod --> ProdDebug
    Prod --> ProdRelease

    Debug --> DevDebug
    Debug --> ProdDebug
    Release --> DevRelease
    Release --> ProdRelease

    %% Styling
    classDef flavor fill:#e1f5ff,stroke:#01579b,stroke-width:2px
    classDef buildType fill:#fff3e0,stroke:#e65100,stroke-width:2px
    classDef variant fill:#e8f5e9,stroke:#1b5e20,stroke-width:2px

    class Dev,Prod flavor
    class Debug,Release buildType
    class DevDebug,DevRelease,ProdDebug,ProdRelease variant
```

## Firebase Integration

```mermaid
graph TB
    subgraph "App"
        MainApp["MainApplication<br/>(Application class)"]
    end

    subgraph "Firebase Modules"
        Analytics[":core:analytics<br/>AnalyticsTracker"]
        Notifications[":core:notifications<br/>NotificationManager"]
        RemoteConfig[":core:remoteconfig<br/>FeatureFlagManager"]
    end

    subgraph "Firebase Services"
        FA["Firebase Analytics<br/>Event Tracking"]
        FC["Firebase Crashlytics<br/>Crash Reporting"]
        FP["Firebase Performance<br/>Performance Monitoring"]
        FCM["Firebase Cloud Messaging<br/>Push Notifications"]
        FRC["Firebase Remote Config<br/>Feature Flags"]
    end

    subgraph "App Features"
        Recording[":feature:recording"]
        Profile[":feature:profile"]
        Settings[":feature:settings"]
    end

    MainApp --> Analytics
    MainApp --> Notifications
    MainApp --> RemoteConfig

    Analytics --> FA
    Analytics --> FC
    Analytics --> FP

    Notifications --> FCM

    RemoteConfig --> FRC

    Recording --> Analytics
    Recording --> Notifications
    Profile --> Analytics
    Settings --> Analytics
    Settings --> RemoteConfig

    %% Styling
    classDef app fill:#e1f5ff,stroke:#01579b,stroke-width:3px
    classDef module fill:#f3e5f5,stroke:#4a148c,stroke-width:2px
    classDef service fill:#fff3e0,stroke:#e65100,stroke-width:2px
    classDef feature fill:#e8f5e9,stroke:#1b5e20,stroke-width:2px

    class MainApp app
    class Analytics,Notifications,RemoteConfig module
    class FA,FC,FP,FCM,FRC service
    class Recording,Profile,Settings feature
```

## CI/CD Pipeline

```mermaid
graph LR
    subgraph "Git Events"
        Push["Push to main"]
        PR["Pull Request"]
        Tag["Version Tag<br/>(v*.*.*)"]
    end

    subgraph "CI Workflow"
        Checkout["Checkout Code"]
        Setup["Setup JDK 17"]
        Decode["Decode google-services.json"]
        Build["Build All Modules"]
        Test["Run Unit Tests"]
        Lint["Lint + Detekt"]
        Assemble["Assemble APKs"]
    end

    subgraph "Build Release Workflow"
        BuildAPK["Build Unsigned APKs"]
        Upload["Upload Artifacts"]
        Release["Create GitHub Release"]
    end

    subgraph "Deploy Workflow"
        Sign["Sign with Keystore"]
        Bundle["Build AAB"]
        Deploy["Deploy to Play Store"]
    end

    Push --> Checkout
    PR --> Checkout

    Checkout --> Setup
    Setup --> Decode
    Decode --> Build
    Build --> Test
    Test --> Lint
    Lint --> Assemble

    Tag --> BuildAPK
    BuildAPK --> Upload
    Upload --> Release

    Tag -.when ready.-> Sign
    Sign --> Bundle
    Bundle --> Deploy

    %% Styling
    classDef event fill:#e1f5ff,stroke:#01579b,stroke-width:2px
    classDef ci fill:#e8f5e9,stroke:#1b5e20,stroke-width:2px
    classDef build fill:#fff3e0,stroke:#e65100,stroke-width:2px
    classDef deploy fill:#f3e5f5,stroke:#4a148c,stroke-width:2px

    class Push,PR,Tag event
    class Checkout,Setup,Decode,Build,Test,Lint,Assemble ci
    class BuildAPK,Upload,Release build
    class Sign,Bundle,Deploy deploy
```
