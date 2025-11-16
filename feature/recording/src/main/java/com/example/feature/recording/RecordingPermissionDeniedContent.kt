package com.example.feature.recording

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.components.PermissionDeniedContent

/**
 * Recording-specific permission denied content.
 *
 * This is a convenience wrapper around the generic PermissionDeniedContent that pre-configures
 * the title and message for recording permissions (camera and microphone).
 *
 * Usage:
 * ```
 * RecordingPermissionDeniedContent(
 *     onEnablePermissions = {
 *         if (shouldOpenSettings) {
 *             context.openAppSettings()
 *         } else {
 *             requestPermissions()
 *         }
 *     }
 * )
 * ```
 *
 * @param onEnablePermissions Callback when the user clicks the enable permissions button
 * @param modifier Modifier for the container
 */
@Composable
fun RecordingPermissionDeniedContent(
    onEnablePermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    PermissionDeniedContent(
        title = "Camera and Microphone Required",
        message = "Camera and microphone access are essential for recording videos with audio. " +
                "Please grant the necessary permissions to continue using this feature.",
        buttonText = "Enable Permissions",
        onEnablePermissions = onEnablePermissions,
        modifier = modifier
    )
}
