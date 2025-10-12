package com.acksession.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.acksession.ui.R
import com.acksession.ui.theme.ZencastrTheme

/**
 * Generic permission denied content that displays a clear message to the user
 * when required permissions are not granted.
 *
 * Features:
 * - Warning icon for visual emphasis
 * - Customizable title and message
 * - Centered layout with proper spacing
 * - Material Design styling
 * - Full-screen blocking layout for required permissions
 *
 * Usage:
 * ```
 * PermissionDeniedContent(
 *     title = "Camera Access Required",
 *     message = "We need camera access to take photos. Please grant permission to continue.",
 *     buttonText = "Grant Permission",
 *     onEnablePermissions = { /* Request permissions or open settings */ }
 * )
 * ```
 *
 * @param title The title text to display
 * @param message The explanation message about why permissions are needed
 * @param buttonText The text for the action button (default: "Enable Permissions")
 * @param onEnablePermissions Callback when the user clicks the enable permissions button
 * @param modifier Modifier for the container
 */
@Composable
fun PermissionDeniedContent(
    title: String,
    message: String,
    buttonText: String = stringResource(R.string.enable_permissions),
    onEnablePermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Warning icon
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = stringResource(R.string.permission_required),
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Message
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Enable permissions button
        Button(
            onClick = onEnablePermissions,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(buttonText)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PermissionDeniedContentPreview() {
    ZencastrTheme {
        PermissionDeniedContent(
            title = stringResource(R.string.permissions_required),
            message = stringResource(R.string.camera_and_microphone_access_are_required_to_record_videos_with_audio_please_grant_the_necessary_permissions_to_continue),
            onEnablePermissions = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PermissionDeniedContentShortPreview() {
    ZencastrTheme {
        PermissionDeniedContent(
            title = stringResource(R.string.location_required),
            message = stringResource(R.string.we_need_location_access_to_show_nearby_users),
            buttonText = stringResource(R.string.grant_location),
            onEnablePermissions = {}
        )
    }
}
