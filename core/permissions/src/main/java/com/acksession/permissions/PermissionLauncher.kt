package com.acksession.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/**
 * Interface for launching permission requests imperatively.
 * Similar to ActivityResultLauncher but for permissions with rationale support.
 */
@Stable
interface PermissionLauncher {
    /**
     * Whether all requested permissions are granted
     */
    val isGranted: Boolean

    /**
     * Launch the permission request flow.
     * Automatically handles: first request -> rationale -> settings based on state.
     */
    fun launch()
}

/**
 * Remember a permission launcher that can be triggered imperatively.
 *
 * This composable manages permission state and automatically shows rationale/settings dialogs
 * based on Android's permission flow. The launcher remains in composition and preserves state
 * across recompositions, avoiding the issues of conditional composition.
 *
 * **Use this for on-demand permissions** triggered by user actions (e.g., button clicks).
 * For screen-level permissions that gate entire screens, use [PermissionGate] instead.
 *
 * @param permissions List of permissions to request
 * @param rationaleTitle Title for the rationale dialog
 * @param rationaleMessage Message explaining why the permissions are needed
 * @param onResult Callback invoked when permission result is received (granted or denied)
 * @return PermissionLauncher that can be called imperatively via launch()
 *
 * Example usage:
 * ```
 * val audioPermissionLauncher = rememberPermissionLauncher(
 *     permissions = listOf(Manifest.permission.RECORD_AUDIO),
 *     rationaleTitle = "Microphone Required",
 *     rationaleMessage = "We need access to your microphone to record audio."
 * ) { granted ->
 *     if (granted) startRecording()
 * }
 *
 * Button(onClick = { audioPermissionLauncher.launch() }) {
 *     Text("Record")
 * }
 * ```
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberPermissionLauncher(
    permissions: List<String>,
    rationaleTitle: String,
    rationaleMessage: String,
    viewModel: PermissionViewModel = hiltViewModel(),
    onResult: (granted: Boolean) -> Unit = {}
): PermissionLauncher {
    // Track whether we should show dialogs
    var showRationaleDialog by rememberSaveable { mutableStateOf(false) }
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }

    // Load persistent state from ViewModel: has this permission been requested before?
    // Use first permission as the key (permissions list is typically single-item or related)
    val hasRequestedPermission by viewModel
        .hasRequestedPermission(permissions.first())
        .collectAsState(initial = false)

    // Use Accompanist's permission state for actual permission management
    val permissionState = rememberMultiplePermissionsState(
        permissions = permissions
    ) { permissionsMap ->
        val allGranted = permissionsMap.values.all { it }
        onResult(allGranted)

        // If permission was granted, hide any dialogs
        if (allGranted) {
            showRationaleDialog = false
            showSettingsDialog = false
        }
    }

    // Show permission dialogs (shared logic)
    PermissionDialogs(
        showRationale = showRationaleDialog,
        showSettings = showSettingsDialog,
        onDismissRationale = { showRationaleDialog = false },
        onConfirmRationale = {
            showRationaleDialog = false
            permissionState.launchMultiplePermissionRequest()
        },
        onDismissSettings = { showSettingsDialog = false },
        onConfirmSettings = { showSettingsDialog = false },
        rationaleTitle = rationaleTitle,
        rationaleMessage = rationaleMessage
    )

    return remember(permissionState, hasRequestedPermission) {
        object : PermissionLauncher {
            override val isGranted: Boolean
                get() = permissionState.allPermissionsGranted

            override fun launch() {
                when {
                    // Already granted - do nothing
                    permissionState.allPermissionsGranted -> {
                        // No-op: Permission already granted
                    }

                    // Should show rationale (after first denial)
                    permissionState.shouldShowRationale -> {
                        showRationaleDialog = true
                    }

                    // First request (must check BEFORE permanently denied)
                    // ViewModel uses DataStore, so state persists across app restarts
                    !hasRequestedPermission -> {
                        // Mark permission as requested via ViewModel (handles coroutine scope)
                        viewModel.markPermissionRequested(permissions.first())
                        permissionState.launchMultiplePermissionRequest()
                    }

                    // Permanently denied - show settings dialog
                    // Using revokedPermissions check prevents false positives when permission is granted but state hasn't updated
                    // hasRequestedPermission is now persistent, so this works correctly after app restart
                    hasRequestedPermission && permissionState.hasRevokedPermissions() && permissionState.isPermanentlyDenied() -> {
                        showSettingsDialog = true
                    }

                    // Fallback: request again
                    else -> {
                        permissionState.launchMultiplePermissionRequest()
                    }
                }
            }
        }
    }
}
