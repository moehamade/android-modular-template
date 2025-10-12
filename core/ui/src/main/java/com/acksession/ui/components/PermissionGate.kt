package com.acksession.ui.components

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
 * This composable:
 * - Automatically requests permissions on first composition
 * - Shows denied content when permissions are not granted
 * - Shows main content when all permissions are granted
 * - Provides `shouldOpenSettings` flag for permanently denied permissions
 *
 * Usage:
 * ```
 * PermissionGate(
 *     permissions = listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
 *     content = { /* Your main UI */ },
 *     deniedContent = { onRequest, shouldOpenSettings ->
 *         if (shouldOpenSettings) {
 *             // Show "Open Settings" button
 *         } else {
 *             // Show "Grant Permission" button that calls onRequest()
 *         }
 *     }
 * )
 * ```
 *
 * @param permissions List of Android permissions to request
 * @param content Content to show when all permissions are granted
 * @param deniedContent Content to show when permissions are denied
 *        Parameters:
 *        - onRequest: Callback to re-request permissions (only works if shouldShowRationale = true)
 *        - shouldOpenSettings: Boolean indicating if app settings should be opened (permanent denial)
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionGate(
    permissions: List<String>,
    content: @Composable () -> Unit,
    deniedContent: @Composable (onRequest: () -> Unit, shouldOpenSettings: Boolean) -> Unit
) {
    var showDeniedContent by remember { mutableStateOf(false) }

    val permissionState = rememberMultiplePermissionsState(
        permissions = permissions
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

    // Show content based on permission state
    if (showDeniedContent) {
        deniedContent(
            {
                if (permissionState.shouldShowRationale) {
                    // User denied but can be asked again
                    permissionState.launchMultiplePermissionRequest()
                }
                // If !shouldShowRationale, do nothing - caller should open settings instead
            },
            !permissionState.shouldShowRationale
        )
    } else {
        content()
    }
}
