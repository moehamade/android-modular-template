package com.example.feature.recording

import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner

/**
 * Camera preview composable that displays the camera feed using CameraX.
 *
 * This component:
 * - Displays the camera preview using AndroidView
 * - Binds the camera controller to the lifecycle
 * - Automatically updates when camera selector changes (front/back camera)
 *
 * @param cameraController The LifecycleCameraController managing the camera
 * @param lifecycleOwner The lifecycle owner to bind the camera to
 * @param cameraSelector The camera selector (front or back camera)
 * @param modifier Modifier for the preview container
 */
@Composable
fun CameraPreview(
    cameraController: LifecycleCameraController,
    lifecycleOwner: LifecycleOwner,
    cameraSelector: CameraSelector,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(cameraSelector) {
        cameraController.cameraSelector = cameraSelector
    }

    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                controller = cameraController
                cameraController.bindToLifecycle(lifecycleOwner)
            }
        },
        modifier = modifier.fillMaxSize()
    )
}
