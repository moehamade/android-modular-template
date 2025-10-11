package com.acksession.permissions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale

/**
 * A composable that handles runtime permissions with rationale support.
 *
 * @param permissions List of permissions to request
 * @param rationaleTitle Title for the rationale dialog
 * @param rationaleMessage Message explaining why the permissions are needed
 * @param onPermissionsResult Callback invoked when all permissions are granted or denied
 * @param content Content to display when permissions are granted
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionHandler(
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
        // Should show rationale - show dialog
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
        // First time - request permissions (must check BEFORE permanently denied)
        !hasRequestedPermissions -> {
            LaunchedEffect(Unit) {
                hasRequestedPermissions = true
                permissionsState.launchMultiplePermissionRequest()
            }
        }
        // Permissions permanently denied - show settings dialog
        permissionsState.isPermanentlyDenied() -> {
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

/**
 * Extension to check if permissions are permanently denied
 */
@OptIn(ExperimentalPermissionsApi::class)
private fun MultiplePermissionsState.isPermanentlyDenied(): Boolean {
    return permissions.any { permission ->
        !permission.status.isGranted && !permission.status.shouldShowRationale
    }
}

/**
 * Extension to open app settings
 */
private fun Context.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    startActivity(intent)
}
