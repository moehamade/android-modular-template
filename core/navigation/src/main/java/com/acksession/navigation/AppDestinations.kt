package com.acksession.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Recording feature navigation routes.
 *
 * This sealed interface defines all navigation destinations within the Recording feature.
 * Kept in :core:navigation as it's the app's starting point.
 *
 * Note: In a larger app, you could move this to :feature:recording:api for consistency.
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
 */
fun Navigator.navigateToRecording() {
    navigateTo(RecordingRoute.Recording)
}

