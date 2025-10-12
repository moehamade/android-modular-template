package com.acksession.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * App-level navigation destinations.
 *
 * These are root-level destinations that can be used across the app.
 * Feature modules can reference these destinations without creating circular dependencies.
 *
 * @Serializable enables automatic serialization/deserialization of navigation parameters.
 * NavKey marks these as valid navigation destinations.
 */
object AppDestinations {
    /**
     * Recording destination - the main screen for video recording.
     */
    @Serializable
    data object Recording : NavKey
}
