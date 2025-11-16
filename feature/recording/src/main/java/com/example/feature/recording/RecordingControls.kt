package com.example.feature.recording

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.icons.AppIcons

/**
 * Recording controls UI component that displays:
 * - Recording status indicator (when recording)
 * - Switch camera button
 * - Record/Stop button
 * - Symmetrical layout with placeholder
 *
 * @param recordingState Current recording state
 * @param onSwitchCamera Callback when switch camera button is clicked
 * @param onStartRecording Callback when start recording button is clicked
 * @param onStopRecording Callback when stop recording button is clicked
 * @param modifier Modifier for the controls container
 */
@Composable
fun RecordingControls(
    recordingState: RecordingState,
    onSwitchCamera: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (recordingState is RecordingState.Recording) {
            Text(
                text = "● Recording...",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(
                onClick = onSwitchCamera,
                enabled = recordingState !is RecordingState.Recording
            ) {
                Icon(
                    imageVector = AppIcons.SwitchCamera,
                    contentDescription = "Switch Camera"
                )
            }

            RecordButton(
                isRecording = recordingState is RecordingState.Recording,
                onStartRecording = onStartRecording,
                onStopRecording = onStopRecording
            )

            Box(modifier = Modifier.padding(24.dp))
        }
    }
}

/**
 * Record button that toggles between record and stop states.
 *
 * @param isRecording Whether recording is currently active
 * @param onStartRecording Callback when record button is clicked
 * @param onStopRecording Callback when stop button is clicked
 */
@Composable
private fun RecordButton(
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit
) {
    Button(
        onClick = {
            if (isRecording) {
                onStopRecording()
            } else {
                onStartRecording()
            }
        }
    ) {
        Icon(
            imageVector = if (isRecording) AppIcons.Stop else AppIcons.Record,
            contentDescription = if (isRecording) "Stop Recording" else "Start Recording"
        )
        Text(
            text = if (isRecording) "Stop" else "Record",
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
