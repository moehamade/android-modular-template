package com.acksession.feature.recording

import android.widget.Toast
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.acksession.feature.profile.api.navigateToProfile
import com.acksession.navigation.Navigator
import com.acksession.ui.utils.openAppSettings

/**
 * Recording screen with camera preview and video recording.
 * Uses permission gate to ensure camera and microphone permissions are granted.
 */
@Composable
fun RecordingScreen(
    navigator: Navigator,
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            // Test navigation button - navigate to Profile with parameters
            FloatingActionButton(
                onClick = {
                    navigator.navigateToProfile(
                        userId = "USER_123",
                        name = "Test User",
                        role = "Video Creator"
                    )
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Navigate to Profile"
                )
            }
        }
    ) { paddingValues ->
        RecordingPermissionGate(
            content = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .consumeWindowInsets(paddingValues)
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
                RecordingPermissionDeniedContent(
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
