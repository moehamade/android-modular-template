# Core Domain Module

Pure Kotlin business logic and domain models with no Android dependencies.

## Purpose

Contains the core business rules, entities, and use cases that define what the application does. This module is framework-agnostic and can be tested without Android.

## What Lives Here

- **Domain Models**: Core business entities (User, Recording, Session)
- **Use Cases**: Single-purpose business operations
- **Business Logic**: Validation, calculations, transformations
- **Repository Interfaces**: Contracts for data access (implemented in `:core:data`)

## Principles

- **Pure Kotlin**: No Android framework dependencies
- **Framework Independent**: Can be used in any Kotlin project
- **Testable**: Easy to unit test without mocking Android components
- **Focused**: Each class has a single responsibility

## Structure

```
com.acksession.domain
├── model/          # Domain entities
├── usecase/        # Business operations
└── repository/     # Repository contracts (interfaces)
```

## Domain Models

Keep them simple and focused on business concepts:

```kotlin
data class Recording(
    val id: String,
    val title: String,
    val duration: Duration,
    val createdAt: Instant,
    val participants: List<Participant>,
    val status: RecordingStatus
)

enum class RecordingStatus {
    DRAFT, IN_PROGRESS, COMPLETED, FAILED
}
```

## Use Cases

Each use case represents a single business operation:

```kotlin
class StartRecordingUseCase @Inject constructor(
    private val recordingRepository: RecordingRepository,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(sessionId: String): Result<Recording> {
        // Validate session exists
        val session = sessionManager.getSession(sessionId)
            ?: return Result.failure(SessionNotFoundException())
        
        // Check prerequisites
        if (session.participants.isEmpty()) {
            return Result.failure(NoParticipantsException())
        }
        
        // Create recording
        return recordingRepository.createRecording(session)
    }
}
```

## Repository Interfaces

Define contracts without implementation details:

```kotlin
interface RecordingRepository {
    suspend fun createRecording(session: Session): Result<Recording>
    suspend fun getRecording(id: String): Result<Recording>
    suspend fun updateRecording(recording: Recording): Result<Unit>
    suspend fun deleteRecording(id: String): Result<Unit>
}
```

The actual implementation lives in `:core:data`.

## Business Logic Example

Encapsulate validation and business rules:

```kotlin
class RecordingValidator {
    fun validateTitle(title: String): ValidationResult {
        return when {
            title.isBlank() -> ValidationResult.Error("Title cannot be empty")
            title.length < 3 -> ValidationResult.Error("Title too short")
            title.length > 100 -> ValidationResult.Error("Title too long")
            else -> ValidationResult.Success
        }
    }
    
    fun canStartRecording(session: Session): Boolean {
        return session.participants.isNotEmpty() && 
               session.status == SessionStatus.READY
    }
}

sealed class ValidationResult {
    data object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}
```

## Testing

Domain logic is straightforward to test:

```kotlin
class StartRecordingUseCaseTest {
    private lateinit var useCase: StartRecordingUseCase
    private lateinit var mockRepository: RecordingRepository
    
    @Before
    fun setup() {
        mockRepository = mock()
        useCase = StartRecordingUseCase(mockRepository, mockSessionManager)
    }
    
    @Test
    fun `should fail when session has no participants`() = runTest {
        val emptySession = Session(id = "123", participants = emptyList())
        whenever(mockSessionManager.getSession("123")).thenReturn(emptySession)
        
        val result = useCase("123")
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NoParticipantsException)
    }
}
```

## Dependencies

**None.** This module should only depend on:
- Kotlin standard library
- Kotlinx Coroutines (for suspend functions)
- Kotlinx DateTime (for time handling)

Do not add Android dependencies, Retrofit, Room, or any framework-specific libraries.

## Best Practices

- Keep models immutable (use `data class` with `val`)
- Use sealed classes for state/result types
- One use case per business operation
- Inject dependencies through constructors
- Return `Result<T>` for operations that can fail
- Use meaningful exceptions for domain errors

## Why Pure Kotlin?

By keeping this module framework-independent:
- **Faster Tests**: No Android framework means tests run instantly
- **Reusability**: Logic can be shared with backend or other platforms
- **Clarity**: Forces separation of business logic from infrastructure
- **Flexibility**: Easy to change frameworks without rewriting business rules

