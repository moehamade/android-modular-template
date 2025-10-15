# Feature: Profile

User profile management and settings.

## Purpose

Handles user profile viewing, editing, and account settings. Displays user information and provides access to app preferences.

## Features

- View user profile
- Edit profile information
- Update avatar
- Account settings
- Logout functionality

## Module Structure

```
:feature:profile
├── ui/
│   ├── ProfileScreen.kt           # Main profile display
│   ├── EditProfileScreen.kt       # Profile editing
│   └── components/                # Profile-specific UI components
├── viewmodel/
│   ├── ProfileViewModel.kt
│   └── EditProfileViewModel.kt
└── navigation/
    └── ProfileNavigation.kt

:feature:profile:api
└── ProfileRoute.kt                # Navigation routes
```

## Navigation

Routes for cross-feature navigation:

```kotlin
@Serializable
sealed interface ProfileRoute : NavKey {
    @Serializable
    data class Profile(val userId: String) : ProfileRoute
    
    @Serializable
    data object EditProfile : ProfileRoute
    
    @Serializable
    data object Settings : ProfileRoute
}

fun Navigator.navigateToProfile(userId: String) {
    navigateTo(ProfileRoute.Profile(userId))
}

fun Navigator.navigateToEditProfile() {
    navigateTo(ProfileRoute.EditProfile)
}
```

## State Management

```kotlin
data class ProfileState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class EditProfileState(
    val name: String = "",
    val bio: String = "",
    val avatarUrl: String? = null,
    val isSaving: Boolean = false,
    val validationErrors: Map<String, String> = emptyMap()
)
```

## Key Components

### ProfileViewModel

```kotlin
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val userId: String = savedStateHandle["userId"] ?: ""
    
    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()
    
    init {
        loadProfile()
    }
    
    private fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getUserUseCase(userId)
                .onSuccess { user ->
                    _state.update { it.copy(user = user, isLoading = false) }
                }
                .onFailure { error ->
                    _state.update { it.copy(error = error.message, isLoading = false) }
                }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }
}
```

### ProfileScreen

```kotlin
@Composable
fun ProfileScreen(
    userId: String,
    navigator: Navigator,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    IconButton(onClick = { navigator.navigateToEditProfile() }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingIndicator()
            state.error != null -> ErrorMessage(state.error!!)
            state.user != null -> ProfileContent(
                user = state.user!!,
                onLogout = viewModel::logout
            )
        }
    }
}
```

## Form Validation

```kotlin
class ProfileValidator {
    fun validateName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Error("Name required")
            name.length < 2 -> ValidationResult.Error("Name too short")
            name.length > 50 -> ValidationResult.Error("Name too long")
            else -> ValidationResult.Success
        }
    }
    
    fun validateBio(bio: String): ValidationResult {
        return if (bio.length > 500) {
            ValidationResult.Error("Bio too long (max 500 characters)")
        } else {
            ValidationResult.Success
        }
    }
}
```

## Image Upload

Handle avatar upload:

```kotlin
suspend fun uploadAvatar(uri: Uri): Result<String> {
    return runCatching {
        val file = uriToFile(uri)
        val response = profileApi.uploadAvatar(file.asRequestBody())
        response.avatarUrl
    }
}
```

## Dependencies

- `:core:ui` - Shared UI components
- `:core:domain` - User domain models and use cases
- `:core:data` - User repository
- `:core:navigation` - Navigation
- Coil - Image loading
- Hilt - Dependency injection

## Testing

```kotlin
@Test
fun `should load profile on init`() = runTest {
    val testUser = User(id = "123", name = "Test User")
    whenever(mockGetUserUseCase("123")).thenReturn(Result.success(testUser))
    
    val viewModel = ProfileViewModel(mockGetUserUseCase, mockLogoutUseCase, mockSavedState)
    advanceUntilIdle()
    
    assertEquals(testUser, viewModel.state.value.user)
    assertFalse(viewModel.state.value.isLoading)
}
```

## UI Components

Reusable profile components:

- `AvatarImage` - Circular avatar with fallback
- `ProfileHeader` - User info header
- `ProfileField` - Editable text field
- `SettingItem` - Settings list item

## Cross-Feature Integration

Other features can navigate to profile:

```kotlin
// In :feature:recording
dependencies {
    implementation(project(":feature:profile:api"))
}

// Navigate to user profile
Button(onClick = { navigator.navigateToProfile(userId) }) {
    Text("View Profile")
}
```

