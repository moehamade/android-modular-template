package com.acksession.feature.profile.api

import androidx.navigation3.runtime.NavKey
import com.acksession.navigation.Navigator
import kotlinx.serialization.Serializable

/**
 * Profile feature navigation routes.
 *
 * This sealed interface defines all navigation destinations within the Profile feature.
 * Other features can depend on :feature:profile:api to navigate to profile screens
 * without depending on the profile implementation.
 *
 * All routes are marked with @Serializable for Navigation3 state persistence.
 */
@Serializable
sealed interface ProfileRoute : NavKey {

    /**
     * Main profile screen showing user information.
     *
     * @property userId The unique identifier for the user
     * @property name The display name of the user
     * @property role Optional role/title of the user
     */
    @Serializable
    data class Profile(
        val userId: String,
        val name: String,
        val role: String? = null
    ) : ProfileRoute

    /**
     * Profile dialog for displaying messages.
     * Used for testing dialog-style navigation.
     *
     * @property userId The user this dialog is associated with
     * @property message The message to display in the dialog
     */
    @Serializable
    data class ProfileDialog(
        val userId: String,
        val message: String
    ) : ProfileRoute
}

// Extension functions for convenient navigation

/**
 * Navigate to user profile screen.
 *
 * Example:
 * ```
 * navigator.navigateToProfile(
 *     userId = "123",
 *     name = "John Doe",
 *     role = "Admin"
 * )
 * ```
 */
fun Navigator.navigateToProfile(userId: String, name: String, role: String? = null) {
    navigateTo(ProfileRoute.Profile(userId, name, role))
}

/**
 * Open profile dialog with a message.
 *
 * Example:
 * ```
 * navigator.openProfileDialog(
 *     userId = "123",
 *     message = "Welcome!"
 * )
 * ```
 */
fun Navigator.openProfileDialog(userId: String, message: String) {
    navigateTo(ProfileRoute.ProfileDialog(userId, message))
}
