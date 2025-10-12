package com.acksession.feature.recording

import android.Manifest
import androidx.compose.runtime.Composable
import com.acksession.ui.components.PermissionGate

/**
 * Permission gate specifically for recording that requires both CAMERA and RECORD_AUDIO permissions.
 *
 * This is a convenience wrapper around the generic PermissionGate that pre-configures
 * the permissions needed for video recording with audio.
 *
 * Usage:
 * ```
 * RecordingPermissionGate(
 *     content = { /* Your recording UI */ },
 *     permissionDeniedContent = { onRequest, shouldOpenSettings ->
 *         if (shouldOpenSettings) {
 *             context.openAppSettings()
 *         } else {
 *             onRequest()
 *         }
 *     }
 * )
 * ```
 *
 * @param content The content to show when all permissions are granted
 * @param permissionDeniedContent The content to show when permissions are denied
 *        Parameters:
 *        - onEnablePermissions: Callback to re-request permissions or open settings
 *        - shouldOpenSettings: Boolean indicating if settings should be opened
 */
@Composable
fun RecordingPermissionGate(
    content: @Composable () -> Unit,
    permissionDeniedContent: @Composable (onEnablePermissions: () -> Unit, shouldOpenSettings: Boolean) -> Unit
) {
    PermissionGate(
        permissions = listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
        content = content,
        deniedContent = permissionDeniedContent
    )
}
