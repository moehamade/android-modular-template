package com.example.feature.settings

import androidx.lifecycle.ViewModel
import com.example.domain.config.BuildConfigProvider
import com.example.navigation.Navigator
import com.example.network.qualifier.ApiBaseUrl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * UI state for Settings screen (read-only build information).
 */
data class SettingsUiState(
    val currentApiUrl: String = "",
    val flavorEnvironment: String = "",
    val buildType: String = "",
    val appVersion: String = "",
    val canShowDeveloperInfo: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val navigator: Navigator,
    @param:ApiBaseUrl private val currentApiUrl: String,
    buildConfigProvider: BuildConfigProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            currentApiUrl = currentApiUrl,
            flavorEnvironment = buildConfigProvider.environment,
            buildType = buildConfigProvider.buildType,
            appVersion = buildConfigProvider.versionName,
            canShowDeveloperInfo = buildConfigProvider.isDebug
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun navigateBack() {
        navigator.navigateBack()
    }
}
