package com.acksession.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/**
 * A composable that gates content behind runtime permissions.
 *
 * This component acts as a permission gate - it only displays the content when all requested
 * permissions are granted. Until then, it automatically handles the permission request flow
 * (first request → rationale dialog → settings dialog).
 *
 * **Use this for screen-level permissions** where the entire screen requires permissions to function.
 * For on-demand permissions (e.g., triggered by a button click), use [rememberPermissionLauncher] instead.
 *
 * @param permissions List of permissions to request
 * @param rationaleTitle Title for the rationale dialog
 * @param rationaleMessage Message explaining why the permissions are needed
 * @param onPermissionsResult Callback invoked when all permissions are granted or denied
 * @param content Content to display when all permissions are granted
 *
 * Example usage:
 * ```
 * PermissionGate(
 *     permissions = listOf(Manifest.permission.CAMERA),
 *     rationaleTitle = "Camera Required",
 *     rationaleMessage = "This app needs camera access to show preview."
 * ) {
 *     CameraPreview()
 * }
 * ```
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionGate(
    permissions: List<String>,
    rationaleTitle: String,
    rationaleMessage: String,
    viewModel: PermissionViewModel = hiltViewModel(),
    onPermissionsResult: (allGranted: Boolean) -> Unit = {},
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    // State variables to control which dialog (if any) should be shown
    var showRationaleDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Local state to track if we've initiated the request in THIS composition
    // Prevents re-triggering LaunchedEffect when DataStore updates
    var hasInitiatedRequest by remember { mutableStateOf(false) }

    // Persistent state from DataStore (for restart scenarios)
    // This persists across app restarts and tells us if permission was ever requested
    val hasRequestedPermissions by viewModel
        .hasRequestedPermission(permissions.first())
        .collectAsState(initial = false)

    val permissionsState = rememberMultiplePermissionsState(permissions) { permissionsMap ->
        val allGranted = permissionsMap.values.all { it }
        onPermissionsResult(allGranted)
    }

    // Determine which dialog should show based on permission state
    // This LaunchedEffect ensures atomic state updates before rendering
    // NOTE: hasRequestedPermissions is NOT in dependencies to prevent re-trigger
    LaunchedEffect(
        permissionsState.allPermissionsGranted,
        permissionsState.shouldShowRationale,
        permissionsState.revokedPermissions.size
    ) {
        when {
            // Permission granted - hide all dialogs
            permissionsState.allPermissionsGranted -> {
                showRationaleDialog = false
                showSettingsDialog = false
            }

            // Should show rationale (after first denial)
            permissionsState.shouldShowRationale -> {
                showRationaleDialog = true
                showSettingsDialog = false
            }

            // First time in THIS session - check BOTH local AND persistent state
            !hasInitiatedRequest && !hasRequestedPermissions -> {
                // Set local flag FIRST to prevent re-trigger when DataStore updates
                hasInitiatedRequest = true
                // Mark permission as requested in DataStore (for restart persistence)
                viewModel.markPermissionRequested(permissions.first())
                // Launch the system permission dialog
                permissionsState.launchMultiplePermissionRequest()
                // Don't show any custom dialogs
                showRationaleDialog = false
                showSettingsDialog = false
            }

            // Permanently denied - show settings dialog
            // MUST check hasRequestedPermissions to avoid false positive on first launch
            // (isPermanentlyDenied() returns true both for never-requested AND permanently-denied)
            hasRequestedPermissions && permissionsState.hasRevokedPermissions() && permissionsState.isPermanentlyDenied() -> {
                showRationaleDialog = false
                showSettingsDialog = true
            }
        }
    }

    // Render content or dialogs based on state
    // Only ONE of these will be true at any time, preventing flashing
    when {
        // All permissions granted - show content
        permissionsState.allPermissionsGranted -> {
            content()
        }

        // Show rationale dialog
        showRationaleDialog -> {
            PermissionRationaleDialog(
                title = rationaleTitle,
                message = rationaleMessage,
                onDismiss = { showRationaleDialog = false },
                onConfirm = {
                    showRationaleDialog = false
                    permissionsState.launchMultiplePermissionRequest()
                }
            )
        }

        // Show settings dialog
        showSettingsDialog -> {
            PermissionRationaleDialog(
                title = "Permission Required",
                message = "$rationaleMessage\n\nPlease grant the required permissions in app settings.",
                onDismiss = { showSettingsDialog = false },
                onConfirm = {
                    showSettingsDialog = false
                    context.openAppSettings()
                },
                confirmButtonText = "Open Settings"
            )
        }
    }
}
