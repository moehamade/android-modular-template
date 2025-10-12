package com.acksession.feature.recording

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.video.AudioConfig
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel for the recording feature.
 * Manages recording state and video file handling.
 */
@HiltViewModel
class RecordingViewModel @Inject constructor(
    private val appContext: Application
) : AndroidViewModel(appContext) {

    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private val _cameraSelector = MutableStateFlow(CameraSelector.DEFAULT_BACK_CAMERA)
    val cameraSelector: StateFlow<CameraSelector> = _cameraSelector.asStateFlow()

    private var activeRecording: Recording? = null

    /**
     * Toggle between front and back camera
     */
    fun switchCamera() {
        _cameraSelector.value = if (_cameraSelector.value == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
    }

    /**
     * Start video recording
     */
    fun startRecording(recording: Recording) {
        activeRecording = recording
        _recordingState.value = RecordingState.Recording
    }

    /**
     * Start recording with camera controller.
     * This method handles the camera controller interaction and permission-guarded recording start.
     *
     * @param cameraController The camera controller to use for recording
     * @param context Context for executor
     * @suppress MissingPermission Permissions are checked before calling this method
     */
    @SuppressLint("MissingPermission")
    fun startRecordingWithController(
        cameraController: LifecycleCameraController,
        context: Context
    ) {
        val outputOptions = getOutputOptions()
        val recording = cameraController.startRecording(
            outputOptions,
            AudioConfig.create(true),
            ContextCompat.getMainExecutor(context)
        ) { event ->
            handleRecordingEvent(event)
        }
        startRecording(recording)
    }

    /**
     * Stop video recording
     */
    fun stopRecording() {
        activeRecording?.stop()
        activeRecording = null
        _recordingState.value = RecordingState.Idle
    }

    /**
     * Handle recording events
     */
    fun handleRecordingEvent(event: VideoRecordEvent) {
        when (event) {
            is VideoRecordEvent.Start -> {
                _recordingState.value = RecordingState.Recording
            }

            is VideoRecordEvent.Finalize -> {
                if (event.hasError()) {
                    _recordingState.value = RecordingState.Error(
                        event.cause?.message ?: "Unknown error occurred"
                    )
                } else {
                    _recordingState.value = RecordingState.Saved(event.outputResults.outputUri)
                }
            }

            is VideoRecordEvent.Status -> {
                // Update duration if needed
                val durationMs = event.recordingStats.recordedDurationNanos / 1_000_000
                if (_recordingState.value is RecordingState.Recording) {
                    _recordingState.value = RecordingState.Recording
                }
            }

            else -> {
                // Handle other events if needed
            }
        }
    }

    /**
     * Reset state to idle
     */
    fun resetState() {
        _recordingState.value = RecordingState.Idle
    }

    /**
     * Generate output options for saving video to internal storage
     */
    fun getOutputOptions(): FileOutputOptions {

        // Create a file in the app's internal storage (no WRITE_EXTERNAL_STORAGE permission needed)
        val fileName = "video_${
            SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.US
            ).format(System.currentTimeMillis())
        }.mp4"
        val file = File(appContext.filesDir, fileName)

        return FileOutputOptions.Builder(file).build()
    }

    override fun onCleared() {
        super.onCleared()
        activeRecording?.stop()
    }
}

/**
 * Sealed class representing the state of video recording
 */
sealed class RecordingState {
    data object Idle : RecordingState()
    data object Recording : RecordingState()
    data class Saved(val uri: Uri) : RecordingState()
    data class Error(val message: String) : RecordingState()
}
