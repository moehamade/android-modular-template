package com.acksession.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
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
    onPermissionsResult: (allGranted: Boolean) -> Unit = {},
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    var hasRequestedPermissions by rememberSaveable { mutableStateOf(false) }

    val permissionsState = rememberMultiplePermissionsState(permissions) { permissionsMap ->
        val allGranted = permissionsMap.values.all { it }
        onPermissionsResult(allGranted)
    }

    when {
        // All permissions granted - show content
        permissionsState.allPermissionsGranted -> {
            content()
        }

        // Should show rationale (after first denial)
        permissionsState.shouldShowRationale -> {
            PermissionRationaleDialog(
                title = rationaleTitle,
                message = rationaleMessage,
                onDismiss = { /* User dismissed, do nothing */ },
                onConfirm = {
                    permissionsState.launchMultiplePermissionRequest()
                }
            )
        }

        // First time - request permissions
        !hasRequestedPermissions -> {
            LaunchedEffect(Unit) {
                hasRequestedPermissions = true
                permissionsState.launchMultiplePermissionRequest()
            }
        }

        hasRequestedPermissions && permissionsState.isPermanentlyDenied() -> {
            PermissionRationaleDialog(
                title = "Permission Required",
                message = "$rationaleMessage\n\nPlease grant the required permissions in app settings.",
                onDismiss = { /* User dismissed, do nothing */ },
                onConfirm = {
                    context.openAppSettings()
                },
                confirmButtonText = "Open Settings"
            )
        }
    }
}
