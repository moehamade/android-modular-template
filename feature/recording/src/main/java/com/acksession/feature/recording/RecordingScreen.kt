package com.acksession.feature.recording

import android.Manifest
import android.annotation.SuppressLint
import android.widget.Toast
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.camera.view.video.AudioConfig
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.acksession.permissions.PermissionHandler
import com.acksession.ui.icons.ZencastrIcons

/**
 * Recording screen that shows camera preview and allows video recording.
 */
@Composable
fun RecordingScreen(
    viewModel: RecordingViewModel = hiltViewModel<RecordingViewModel>()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val recordingState by viewModel.recordingState.collectAsState()
    val cameraSelector by viewModel.cameraSelector.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle recording state changes
    LaunchedEffect(recordingState) {
        when (val state = recordingState) {
            is RecordingState.Saved -> {
                Toast.makeText(
                    context,
                    "Video saved: ${state.uri}",
                    Toast.LENGTH_LONG
                ).show()
                viewModel.resetState()
            }

            is RecordingState.Error -> {
                snackbarHostState.showSnackbar(
                    message = "Error: ${state.message}"
                )
                viewModel.resetState()
            }

            else -> { /* No action needed */
            }
        }
    }

    PermissionHandler(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ),
        rationaleTitle = "Camera & Microphone Required",
        rationaleMessage = "This app needs access to your camera and microphone to record videos."
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Camera Preview
                val cameraController = remember {
                    LifecycleCameraController(context).apply {
                        setEnabledUseCases(CameraController.VIDEO_CAPTURE)
                    }
                }

                // Update camera selector when it changes
                LaunchedEffect(cameraSelector) {
                    cameraController.cameraSelector = cameraSelector
                }

                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            controller = cameraController
                            cameraController.bindToLifecycle(lifecycleOwner)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Controls overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Recording status indicator
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
                        // Switch camera button
                        FilledTonalIconButton(
                            onClick = {
                                viewModel.switchCamera()
                            },
                            enabled = recordingState !is RecordingState.Recording
                        ) {
                            Icon(
                                imageVector = ZencastrIcons.SwitchCamera,
                                contentDescription = "Switch Camera"
                            )
                        }

                        // Record/Stop button
                        RecordButton(
                            isRecording = recordingState is RecordingState.Recording,
                            onStartRecording = {
                                if (ActivityCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                                ) {
                                    Toast.makeText(
                                        context,
                                        "Audio permission not granted",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@RecordButton
                                }

                                val outputOptions = viewModel.getOutputOptions()
                                val recording = cameraController.startRecording(
                                    outputOptions,
                                    AudioConfig.create(true),
                                    ContextCompat.getMainExecutor(context)
                                ) { event ->
                                    viewModel.handleRecordingEvent(event)
                                }
                                viewModel.startRecording(recording)
                            },
                            onStopRecording = {
                                viewModel.stopRecording()
                            }
                        )

                        // Placeholder for symmetry
                        Box(modifier = Modifier.padding(24.dp))
                    }
                }
            }
        }
    }
}

/**
 * Record button that toggles between record and stop states
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
            imageVector = if (isRecording) ZencastrIcons.Stop else ZencastrIcons.Record,
            contentDescription = if (isRecording) "Stop Recording" else "Start Recording"
        )
        Text(
            text = if (isRecording) "Stop" else "Record",
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
