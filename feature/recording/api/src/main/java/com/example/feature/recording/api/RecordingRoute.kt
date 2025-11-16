package com.example.feature.recording.api

import androidx.navigation3.runtime.NavKey
import com.example.navigation.Navigator
import kotlinx.serialization.Serializable

/**
 * Recording feature navigation routes.
 *
 * This sealed interface defines all navigation destinations within the Recording feature.
 * Other features can depend on :feature:recording:api to navigate to recording screens
 * without depending on the recording implementation.
 *
 * All routes are marked with @Serializable for Navigation3 state persistence.
 */
@Serializable
sealed interface RecordingRoute : NavKey {
    /**
     * Main recording screen for capturing video.
     */
    @Serializable
    data object Recording : RecordingRoute
}

/**
 * Extension function for navigating to the recording screen.
 *
 * Example:
 * ```
 * navigator.navigateToRecording()
 * ```
 */
fun Navigator.navigateToRecording() {
    navigateTo(RecordingRoute.Recording)
}
