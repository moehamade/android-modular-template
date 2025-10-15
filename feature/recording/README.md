# Feature: Recording

Audio/video recording functionality with session management.

## Purpose

Handles the core recording features of the app - creating sessions, managing participants, recording audio/video, and saving recordings.

## Features

- Create recording sessions
- Invite and manage participants
- Record audio with high quality
- Real-time recording status
- Pause/resume functionality
- Save and upload recordings

## Module Structure

```
:feature:recording
├── ui/
│   ├── RecordingScreen.kt          # Main recording screen
│   ├── SessionSetupScreen.kt       # Pre-recording setup
│   └── components/                 # Recording-specific UI
├── viewmodel/
│   ├── RecordingViewModel.kt       # Recording state management
│   └── SessionSetupViewModel.kt
└── navigation/
    └── RecordingNavigation.kt      # Navigation registration

:feature:recording:api
└── RecordingRoute.kt               # Navigation routes
```

## Navigation

Routes defined in `:feature:recording:api`:

```kotlin
@Serializable
sealed interface RecordingRoute : NavKey {
    @Serializable
    data class Setup(val sessionId: String?) : RecordingRoute
    
    @Serializable
    data class Record(val sessionId: String) : RecordingRoute
}

fun Navigator.navigateToRecordingSetup(sessionId: String? = null) {
    navigateTo(RecordingRoute.Setup(sessionId))
}

fun Navigator.navigateToRecording(sessionId: String) {
    navigateTo(RecordingRoute.Record(sessionId))
}
```

## State Management

```kotlin
data class RecordingState(
    val status: RecordingStatus = RecordingStatus.IDLE,
    val duration: Duration = Duration.ZERO,
    val participants: List<Participant> = emptyList(),
    val error: String? = null
)

enum class RecordingStatus {
    IDLE, RECORDING, PAUSED, PROCESSING, COMPLETED
}
```

## Key Components

### RecordingViewModel

Manages recording state and orchestrates recording operations:

```kotlin
@HiltViewModel
class RecordingViewModel @Inject constructor(
    private val startRecordingUseCase: StartRecordingUseCase,
    private val stopRecordingUseCase: StopRecordingUseCase,
    private val recordingRepository: RecordingRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(RecordingState())
    val state = _state.asStateFlow()
    
    fun startRecording(sessionId: String) {
        viewModelScope.launch {
            startRecordingUseCase(sessionId)
                .onSuccess { recording ->
                    _state.update { it.copy(status = RecordingStatus.RECORDING) }
                }
                .onFailure { error ->
                    _state.update { it.copy(error = error.message) }
                }
        }
    }
}
```

### RecordingScreen

Main UI for active recording:

```kotlin
@Composable
fun RecordingScreen(
    sessionId: String,
    navigator: Navigator,
    viewModel: RecordingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    RecordingContent(
        state = state,
        onPause = viewModel::pauseRecording,
        onResume = viewModel::resumeRecording,
        onStop = { 
            viewModel.stopRecording()
            navigator.navigateBack()
        }
    )
}
```

## Permissions

Required permissions (declared in AndroidManifest.xml):

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

Handle permission requests in the ViewModel:

```kotlin
fun checkPermissions(): Boolean {
    // Permission checking logic
}
```

## Dependencies

- `:core:ui` - Shared UI components
- `:core:domain` - Recording use cases
- `:core:data` - Recording repository
- `:core:navigation` - Navigation infrastructure
- Media3 or ExoPlayer - Audio recording
- Hilt - Dependency injection

## Testing

Example ViewModel test:

```kotlin
@Test
fun `should update status when recording starts`() = runTest {
    val viewModel = RecordingViewModel(mockStartUseCase, mockStopUseCase, mockRepo)
    
    viewModel.startRecording("session-123")
    advanceUntilIdle()
    
    assertEquals(RecordingStatus.RECORDING, viewModel.state.value.status)
}
```

## Future Enhancements

- Multi-track recording
- Video recording support
- Live waveform visualization
- Auto-save drafts
- Cloud backup integration

