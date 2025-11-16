package com.example.feature.profile

import androidx.lifecycle.ViewModel
import com.example.feature.profile.api.ProfileRoute
import com.example.navigation.Navigator
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel(assistedFactory = ProfileScreenViewModel.Factory::class)
class ProfileScreenViewModel @AssistedInject constructor(
    private val navigator: Navigator,
    @Assisted private val route: ProfileRoute.Profile
) : ViewModel() {
    private val _profileState = MutableStateFlow(
        ProfileScreenState(
            userId = route.userId,
            name = route.name,
            role = route.role
        )
    )
    val profileState = _profileState.asStateFlow()


    fun navigateBack() = navigator.navigateBack()


    @AssistedFactory
    interface Factory {
        fun create(route: ProfileRoute.Profile): ProfileScreenViewModel
    }
}


data class ProfileScreenState(
    val userId: String,
    val name: String,
    val role: String?
)
