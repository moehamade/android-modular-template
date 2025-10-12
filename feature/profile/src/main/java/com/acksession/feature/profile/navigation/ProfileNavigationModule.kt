package com.acksession.feature.profile.navigation

import com.acksession.feature.profile.ProfileScreen
import com.acksession.navigation.EntryProviderInstaller
import com.acksession.navigation.Navigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

/**
 * Hilt module that provides the profile feature's navigation entry provider.
 *
 * This module demonstrates modular navigation with parameters:
 * - The ProfileDestination data class contains navigation parameters
 * - The entry provider extracts parameters and passes them to ProfileScreen
 * - The feature self-registers without app module knowing about it
 */
@Module
@InstallIn(ActivityRetainedComponent::class)
object ProfileNavigationModule {

    @IntoSet
    @Provides
    fun provideProfileEntryProvider(
        navigator: Navigator
    ): EntryProviderInstaller = {
        // Register the Profile destination with parameters
        entry<ProfileDestination> { destination ->
            ProfileScreen(
                userId = destination.userId,
                name = destination.name,
                role = destination.role,
                navigator = navigator
            )
        }
    }
}
