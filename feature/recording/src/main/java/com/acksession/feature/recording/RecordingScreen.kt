package com.acksession.feature.recording

import android.widget.Toast
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.acksession.ui.utils.openAppSettings

/**
 * Recording screen that shows camera preview and allows video recording.
 *
 * This screen uses a permission gate to ensure camera and microphone permissions
 * are granted before displaying the recording interface.
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

    // Camera controller - created once and reused
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.VIDEO_CAPTURE)
        }
    }

    // Handle recording state changes (toasts and snackbars)
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        RecordingPermissionGate(
            content = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Camera preview
                    CameraPreview(
                        cameraController = cameraController,
                        lifecycleOwner = lifecycleOwner,
                        cameraSelector = cameraSelector
                    )

                    // Recording controls overlay
                    RecordingControls(
                        recordingState = recordingState,
                        onSwitchCamera = { viewModel.switchCamera() },
                        onStartRecording = {
                            viewModel.startRecordingWithController(cameraController, context)
                        },
                        onStopRecording = { viewModel.stopRecording() },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            },
            permissionDeniedContent = { onEnablePermissions, shouldOpenSettings ->
                // Permission denied UI shown when permissions are not granted
                PermissionDeniedContent(
                    onEnablePermissions = {
                        if (shouldOpenSettings) {
                            // Permanently denied - open settings
                            context.openAppSettings()
                        } else {
                            // Can request again - trigger permission request
                            onEnablePermissions()
                        }
                    }
                )
            }
        )
    }
}
