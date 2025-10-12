package com.acksession.feature.recording

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/**
 * Permission gate specifically for recording that requires both CAMERA and RECORD_AUDIO permissions.
 *
 * This composable handles:
 * - Requesting permissions on first composition
 * - Showing denied content when permissions are not granted
 * - Showing main content when all permissions are granted
 * - Re-requesting permissions when state changes
 *
 * @param content The content to show when all permissions are granted
 * @param permissionDeniedContent The content to show when permissions are denied.
 *        Receives:
 *        - onEnablePermissions: callback to request permissions again (only works if shouldShowRationale)
 *        - shouldOpenSettings: boolean indicating if settings should be opened (permanently denied)
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RecordingPermissionGate(
    content: @Composable () -> Unit,
    permissionDeniedContent: @Composable (onEnablePermissions: () -> Unit, shouldOpenSettings: Boolean) -> Unit
) {
    var showDeniedContent by remember { mutableStateOf(false) }

    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
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
        permissionDeniedContent(
            {
                if (permissionState.shouldShowRationale) {
                    // User denied but can be asked again
                    permissionState.launchMultiplePermissionRequest()
                }
                // If !shouldShowRationale, do nothing here - caller should check and open settings
            },
            !permissionState.shouldShowRationale
        )
    } else {
        content()
    }
}
