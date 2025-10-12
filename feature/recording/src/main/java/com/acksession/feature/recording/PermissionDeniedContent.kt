package com.acksession.feature.recording

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.acksession.ui.theme.ZencastrTheme

/**
 * Enhanced permission denied content that displays a clear message to the user
 * when camera or microphone permissions are not granted.
 *
 * Features:
 * - Warning icon for visual emphasis
 * - Clear title and message
 * - Centered layout with proper spacing
 * - Material Design styling
 *
 * @param onEnablePermissions Callback when the user clicks the enable permissions button
 * @param modifier Modifier for the container
 */
@Composable
fun PermissionDeniedContent(
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
            contentDescription = "Permission required",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = "Permissions Required",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Message
        Text(
            text = "Camera and microphone access are required to record videos with audio. Please grant the necessary permissions to continue.",
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
            Text("Enable Permissions")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PermissionDeniedContentPreview() {
    ZencastrTheme {
        PermissionDeniedContent(onEnablePermissions = {})
    }
}