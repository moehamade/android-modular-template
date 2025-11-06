# Architecture Decision Records (ADRs)

This directory contains Architecture Decision Records documenting important architectural decisions made in the Zencastr project.

## What is an ADR?

Architecture Decision Records (ADRs) are documents that capture important architectural decisions along with their context and consequences. They help team members understand why certain choices were made and provide historical context for future decisions.

---

## Authentication & Token Management

The authentication system consists of three interconnected ADRs:

```
┌──────────────────────────────────────────────────────────────┐
│                  Authentication System                       │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌─────────────────────────────────────────────────────┐     │
│  │ ADR-005: Encrypted Token Storage                    │     │
│  │ ├─ Google Tink (AES-256-GCM-HKDF)                   │     │
│  │ ├─ DataStore Preferences                            │     │
│  │ ├─ In-memory cache (AtomicReference)                │     │
│  │ └─ Hardware-backed keys (Android Keystore)          │     │
│  └─────────────────────────────────────────────────────┘     │
│                          ▲                                   │
│                          │ stores/retrieves                  │
│                          │                                   │
│  ┌─────────────────────────────────────────────────────┐     │
│  │ ADR-003: Token Refresh Strategy                     │     │
│  │ ├─ TokenAuthenticator (handles 401)                 │     │
│  │ ├─ TokenRefreshCallback (DIP pattern)               │     │
│  │ ├─ Automatic retry with new token                   │     │
│  │ └─ No circular dependencies                         │     │
│  └─────────────────────────────────────────────────────┘     │
│                          ▲                                   │
│                          │ triggers when expired             │
│                          │                                   │
│  ┌─────────────────────────────────────────────────────┐     │
│  │ ADR-006: Token Expiration Strategy                  │     │
│  │ ├─ Proactive refresh (check before request)         │     │
│  │ ├─ 5-minute buffer window                           │     │
│  │ ├─ Saves 200-300ms latency                          │     │
│  │ └─ AuthInterceptor (network layer)                  │     │
│  └─────────────────────────────────────────────────────┘     │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

**How they work together:**
1. **Storage (ADR-005)**: Tokens encrypted at rest with Tink, cached in memory for fast access
2. **Expiration (ADR-006)**: AuthInterceptor checks if token expired → triggers synthetic 401
3. **Refresh (ADR-003)**: TokenAuthenticator catches 401 → refreshes token → retries request

**Recommended reading order**: ADR-005 → ADR-003 → ADR-006

---

## All ADRs

### Architecture & Modules
1. **[ADR-001: Multi-Module Architecture](ADR-001-multi-module-architecture.md)**
   - Module structure, dependency rules, separation of concerns

2. **[ADR-002: Navigation3 Adoption](ADR-002-navigation3-adoption.md)**
   - Type-safe navigation, modular route definitions, deep linking

3. **[ADR-004: Convention Plugins System](ADR-004-convention-plugins.md)**
   - Gradle convention plugins, centralized configuration, build optimization

### Authentication & Security
4. **[ADR-003: Token Refresh Strategy](ADR-003-token-refresh-strategy.md)**
   - Automatic token refresh, DIP pattern, avoiding circular dependencies

5. **[ADR-005: Encrypted Token Storage](ADR-005-encrypted-storage.md)**
   - AES-256-GCM encryption, DataStore, in-memory cache, Tink migration

6. **[ADR-006: Token Expiration Strategy](ADR-006-token-expiration-strategy.md)**
   - Proactive vs reactive refresh, network layer optimization, 5-minute buffer

---

## Format

Each ADR follows this structure:
- **Title**: Short descriptive name
- **Status**: Proposed, Accepted, Deprecated, Superseded
- **Context**: The situation leading to the decision
- **Decision**: The chosen approach
- **Consequences**: Trade-offs, impacts, and implications
- **Alternatives Considered**: Other options that were evaluated

