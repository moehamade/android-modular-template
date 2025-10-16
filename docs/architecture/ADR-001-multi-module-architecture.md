# ADR-001: Multi-Module Architecture

**Status**: Accepted

**Date**: 2025-10-15

## Context

Android projects can be structured as a single monolithic module or split into multiple modules. As Zencastr is intended to be a company-wide base project with potential for open-source contributions, we needed a scalable architecture that supports:

- Multiple feature teams working in parallel
- Independent feature development and testing
- Clear separation of concerns
- Reduced build times through module-level compilation
- Reusable components across features

## Decision

We adopted a **multi-module Clean Architecture** with the following structure:

```
:app                        # Application orchestration
:core:*                     # Shared infrastructure
:feature:*                  # Feature modules
:feature:*:api              # Navigation contracts
```

### Module Types

1. **`:core` modules** - Shared infrastructure:
   - `:core:ui` - Design system and reusable UI components
   - `:core:navigation` - Navigation3 wrapper and NavKey interfaces
   - `:core:network` - Network configuration (Retrofit, OkHttp)
   - `:core:data` - Data layer (repositories, Room)
   - `:core:domain` - Business logic (pure Kotlin, no Android deps)
   - `:core:datastore:*` - Local data persistence

2. **`:feature` modules** - Feature implementations:
   - `:feature:recording` - Recording functionality
   - `:feature:profile` - User profile management
   - Each feature is self-contained with its own UI, ViewModels, and logic

3. **`:feature:*:api` modules** - Navigation contracts:
   - Contain only route definitions (sealed interfaces)
   - Enable cross-feature navigation without coupling
   - Other features depend on APIs, not implementations

### Dependency Rules

```
:app → :feature:*, :core:*
:feature:* → :feature:*:api, :core:*
:feature:*:api → :core:navigation only
:core:data → :core:network, :core:domain, :core:datastore:*
:core:network → :core:datastore:preferences (for token storage)
:core:domain → No Android dependencies (pure Kotlin)
```

## Consequences

### Positive
- ✅ **Parallel development**: Teams can work on different features without conflicts
- ✅ **Faster builds**: Gradle only rebuilds changed modules
- ✅ **Testability**: Each module can be tested in isolation
- ✅ **Reusability**: Core modules can be extracted to separate libraries
- ✅ **Clear boundaries**: Enforced separation prevents spaghetti dependencies
- ✅ **Feature flags**: Easy to enable/disable features at build time

### Negative
- ⚠️ **Initial complexity**: More files and build scripts to manage
- ⚠️ **Learning curve**: Team needs to understand module boundaries
- ⚠️ **Overhead**: Small projects might not benefit from this structure

### Mitigations
- Convention plugins minimize boilerplate in each module
- Clear documentation (this ADR) explains the structure
- Feature scaffolding task automates new module creation

## Alternatives Considered

1. **Single module monolith**
   - Rejected: Doesn't scale for multiple teams or large codebases
   - Build times grow linearly with code size

2. **Feature-based modules only (no core)**
   - Rejected: Leads to code duplication across features
   - No clear place for shared infrastructure

3. **Layer-based modules (ui, domain, data)**
   - Rejected: Doesn't support parallel feature development
   - Still couples unrelated features together

## References

- [Guide to Android app modularization](https://developer.android.com/topic/modularization)
- [Now in Android app architecture](https://github.com/android/nowinandroid)

