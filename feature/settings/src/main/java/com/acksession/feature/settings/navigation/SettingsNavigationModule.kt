package com.acksession.feature.settings.navigation

import com.acksession.feature.settings.SettingsScreen
import com.acksession.feature.settings.api.SettingsRoute
import com.acksession.navigation.EntryProviderInstaller
import com.acksession.navigation.Navigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

/**
 * Hilt module that provides the settings feature's navigation entry provider.
 *
 * This module uses @IntoSet to add the settings feature's navigation destinations
 * to the app's main navigation entry provider set. This allows the settings feature
 * to self-register its navigation destinations without the app module needing to
 * know about them directly.
 */
@Module
@InstallIn(ActivityRetainedComponent::class)
object SettingsNavigationModule {

    @IntoSet
    @Provides
    fun provideSettingsEntryProvider(
    ): EntryProviderInstaller = {
        entry<SettingsRoute.SettingsScreen> {
            SettingsScreen()
        }
    }
}
