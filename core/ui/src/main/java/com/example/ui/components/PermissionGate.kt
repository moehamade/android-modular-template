package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/**
 * Generic permission gate that handles requesting and checking multiple permissions.
 *
 * Usage:
 * ``` kotlin
 * PermissionGate(
 *     permissions = listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
 *     content = { /* Your main UI */ },
 *     deniedContent = { onRequest, shouldOpenSettings -> /* Handle denial */ }
 * )
 * ```
 *
 * @param permissions List of Android permissions to request
 * @param content Content shown when all permissions are granted
 * @param deniedContent Content shown when permissions are denied
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionGate(
    permissions: List<String>,
    content: @Composable () -> Unit,
    deniedContent: @Composable (onRequest: () -> Unit, shouldOpenSettings: Boolean) -> Unit,
) {
    var showDeniedContent by remember { mutableStateOf(false) }

    val permissionState = rememberMultiplePermissionsState(
        permissions = permissions,
    ) { permissionsResult ->
        val allGranted = permissionsResult.values.all { it }
        showDeniedContent = !allGranted
    }

    // Request permissions on first composition and react to state changes
    LaunchedEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) {
            showDeniedContent = false
        } else {
            permissionState.launchMultiplePermissionRequest()
        }
    }

    if (showDeniedContent) {
        deniedContent(
            {
                if (permissionState.shouldShowRationale) {
                    // User denied but can be asked again
                    permissionState.launchMultiplePermissionRequest()
                }
                // If !shouldShowRationale, do nothing - caller should open settings instead
            },
            !permissionState.shouldShowRationale,
        )
    } else {
        content()
    }
}
