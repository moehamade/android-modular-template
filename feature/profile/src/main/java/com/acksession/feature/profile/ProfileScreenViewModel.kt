package com.acksession.feature.profile

import androidx.lifecycle.ViewModel
import com.acksession.feature.profile.api.ProfileRoute
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel(assistedFactory = ProfileScreenViewModel.Factory::class)
class ProfileScreenViewModel @AssistedInject constructor(
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