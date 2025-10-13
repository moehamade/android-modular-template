package com.acksession.feature.profile.navigation

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.acksession.feature.profile.ProfileScreen
import com.acksession.feature.profile.ProfileScreenViewModel
import com.acksession.feature.profile.api.ProfileRoute
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
 * This module registers all ProfileRoute destinations:
 * - ProfileRoute.Profile: Main profile screen
 * - ProfileRoute.ProfileDialog: Dialog for testing dialog navigation
 */
@Module
@InstallIn(ActivityRetainedComponent::class)
object ProfileNavigationModule {

    @IntoSet
    @Provides
    fun provideProfileEntryProvider(
        navigator: Navigator
    ): EntryProviderInstaller = {

        entry<ProfileRoute.Profile> { route ->
            val profileScreenViewModel: ProfileScreenViewModel = hiltViewModel(
                creationCallback = { factory: ProfileScreenViewModel.Factory ->
                    factory.create(route)
                }
            )
            ProfileScreen(
                profileScreenViewModel = profileScreenViewModel,
            )
        }

        entry<ProfileRoute.ProfileDialog> { route ->
            ProfileDialogContent(
                userId = route.userId,
                message = route.message,
                navigator = navigator
            )
        }
    }
}

/**
 * Simple dialog composable for testing dialog-style navigation.
 */
@Composable
private fun ProfileDialogContent(
    userId: String,
    message: String,
    navigator: Navigator
) {
    AlertDialog(
        onDismissRequest = { navigator.navigateBack() },
        title = { Text("Profile Dialog") },
        text = {
            Text("User: $userId\nMessage: $message")
        },
        confirmButton = {
            TextButton(onClick = { navigator.navigateBack() }) {
                Text("Close")
            }
        }
    )
}

