package com.acksession.feature.profile.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Navigation destination for the Profile feature.
 *
 * This data class represents a profile screen with parameters.
 * It demonstrates how Navigation3 handles type-safe navigation with arguments.
 *
 * @Serializable enables automatic serialization/deserialization of navigation parameters
 * when the route is saved/restored (e.g., process death, deep links).
 * NavKey marks this as a valid navigation destination.
 *
 * @property userId The unique identifier for the user
 * @property name The display name of the user
 * @property role Optional role/title of the user
 */
@Serializable
data class ProfileDestination(
    val userId: String,
    val name: String,
    val role: String? = null
) : NavKey
