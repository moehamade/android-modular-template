package com.example.feature.recording.navigation

import com.example.feature.recording.RecordingScreen
import com.example.feature.recording.api.RecordingRoute
import com.example.navigation.EntryProviderInstaller
import com.example.navigation.Navigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

/**
 * Hilt module that provides the recording feature's navigation entry provider.
 *
 * This module uses @IntoSet to add the recording feature's navigation destinations
 * to the app's main navigation entry provider set. This allows the recording feature
 * to self-register its navigation destinations without the app module needing to
 * know about them directly.
 */
@Module
@InstallIn(ActivityRetainedComponent::class)
object RecordingNavigationModule {

    @IntoSet
    @Provides
    fun provideRecordingEntryProvider(
        navigator: Navigator
    ): EntryProviderInstaller = {
        // Register the Recording destination
        entry<RecordingRoute.Recording> {
            RecordingScreen(
                navigator = navigator
            )
        }
    }
}

