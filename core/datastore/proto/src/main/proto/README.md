# Proto DataStore

This module is reserved for future Proto DataStore implementations.

## When to use Proto DataStore

Use Proto DataStore when you need:
- Type-safe, structured data storage
- Schema evolution (versioning)
- Complex nested objects
- Strong validation requirements

## Examples of future use cases

- User settings (theme, language, accessibility preferences)
- App configuration
- Feature flags
- Complex user profiles

## Setup required

When implementing, you'll need to:
1. Add `com.google.protobuf` plugin to build.gradle.kts
2. Add `datastore` and `protobuf-javalite` dependencies
3. Define `.proto` schema files in this directory
4. Configure protobuf code generation

## References

- [Proto DataStore documentation](https://developer.android.com/topic/libraries/architecture/datastore#proto-datastore)
- [Protocol Buffers](https://protobuf.dev/)
