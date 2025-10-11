package com.acksession.permissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acksession.datastore.preferences.PermissionPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing permission request state with persistent storage.
 *
 * This ViewModel wraps the PermissionPreferencesRepository to provide:
 * - Automatic lifecycle management via ViewModel scope
 * - Proper coroutine handling for suspend functions
 * - Consistent DI pattern with other ViewModels in the app
 *
 * The repository persists permission state across app restarts, solving the issue where
 * permission state is lost when using compose remember state.
 *
 * **Architecture:**
 * - Follows the same @HiltViewModel pattern as RecordingViewModel
 * - Hilt automatically injects the repository singleton
 * - No manual dependency threading needed
 *
 * **Usage:**
 * ```
 * @Composable
 * fun PermissionGate(
 *     viewModel: PermissionViewModel = hiltViewModel()
 * ) {
 *     val hasRequested by viewModel
 *         .hasRequestedPermission("android.permission.CAMERA")
 *         .collectAsState(initial = false)
 *
 *     if (!hasRequested) {
 *         viewModel.markPermissionRequested("android.permission.CAMERA")
 *     }
 * }
 * ```
 */
@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val permissionPreferences: PermissionPreferencesRepository
) : ViewModel() {

    /**
     * Returns a Flow indicating whether the given permission has been requested.
     *
     * This Flow emits true if the permission has been requested at least once,
     * false otherwise. The state persists across app restarts via DataStore.
     *
     * The Flow is reactive and updates when the underlying DataStore value changes.
     *
     * @param permission The Android permission string (e.g., android.permission.CAMERA)
     * @return Flow<Boolean> - true if permission has been requested, false otherwise
     *
     * Example:
     * ```
     * val hasRequestedCamera by viewModel
     *     .hasRequestedPermission(Manifest.permission.CAMERA)
     *     .collectAsState(initial = false)
     * ```
     */
    fun hasRequestedPermission(permission: String): Flow<Boolean> {
        return permissionPreferences.hasRequestedPermission(permission)
    }

    /**
     * Marks the given permission as requested.
     *
     * This should be called when the permission request dialog is shown to the user
     * for the first time. The state persists across app restarts.
     *
     * This function launches a coroutine in the viewModelScope, ensuring proper
     * lifecycle management and automatic cancellation when the ViewModel is cleared.
     *
     * @param permission The Android permission string (e.g., android.permission.CAMERA)
     *
     * Example:
     * ```
     * LaunchedEffect(Unit) {
     *     viewModel.markPermissionRequested(Manifest.permission.CAMERA)
     *     permissionsState.launchMultiplePermissionRequest()
     * }
     * ```
     */
    fun markPermissionRequested(permission: String) {
        viewModelScope.launch {
            permissionPreferences.markPermissionRequested(permission)
        }
    }

    /**
     * Clears the request state for the given permission.
     *
     * Useful for testing or resetting permission flows. In production, permissions
     * should rarely need to be cleared.
     *
     * This function launches a coroutine in the viewModelScope.
     *
     * @param permission The Android permission string (e.g., android.permission.CAMERA)
     */
    fun clearPermissionState(permission: String) {
        viewModelScope.launch {
            permissionPreferences.clearPermissionState(permission)
        }
    }
}
