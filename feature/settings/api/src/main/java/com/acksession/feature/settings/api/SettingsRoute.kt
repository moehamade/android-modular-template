package com.acksession.feature.settings.api

import androidx.navigation3.runtime.NavKey
import com.acksession.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
sealed interface SettingsRoute : NavKey {
    @Serializable
    data object SettingsScreen : SettingsRoute
}

fun Navigator.navigateToSettings() {
    navigateTo(SettingsRoute.SettingsScreen)
}
