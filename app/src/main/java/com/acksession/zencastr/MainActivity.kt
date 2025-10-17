package com.acksession.zencastr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.acksession.navigation.EntryProviderInstaller
import com.acksession.navigation.Navigator
import com.acksession.navigation.RecordingRoute
import com.acksession.ui.theme.ZencastrTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Main activity that hosts the Navigation3 NavDisplay.
 * Features self-register their navigation destinations using Hilt's @IntoSet annotation.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var entryProviderInstallers: Set<@JvmSuppressWildcards EntryProviderInstaller>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize navigator with the recording screen as start destination
        navigator.initialize(RecordingRoute.Recording)

        setContent {
            ZencastrTheme {
                Scaffold { paddingValues ->
                    NavDisplay(
                        backStack = navigator.backStack,
                        modifier = Modifier.consumeWindowInsets(paddingValues),
                        onBack = {
                            // Handle back navigation
                            if (!navigator.navigateBack()) {
                                // If can't go back, finish activity
                                finish()
                            }
                        },
                        // Entry decorators provide additional functionality:
                        // - SavedState support for configuration changes
                        // - ViewModel scoping per navigation entry
                        entryDecorators = listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator()
                        ),
                        entryProvider = entryProvider {
                            // Install all feature-provided navigation entries
                            entryProviderInstallers.forEach { installer ->
                                installer()
                            }
                        }
                    )
                }
            }
        }
    }
}
