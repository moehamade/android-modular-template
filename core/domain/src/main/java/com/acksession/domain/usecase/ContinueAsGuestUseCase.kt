package com.acksession.domain.usecase

import com.acksession.domain.model.Result
import com.acksession.domain.model.User
import com.acksession.domain.repository.AuthRepository
import com.acksession.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Use case for continuing as a guest user.
 * Creates a temporary guest session.
 */
class ContinueAsGuestUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<User> {
        // Step 1: Create guest session
        val authResult = authRepository.continueAsGuest()

        return when (authResult) {
            is Result.Success -> {
                // Step 2: Sync guest user data
                userRepository.syncCurrentUser()
            }
            is Result.Error -> Result.Error(authResult.exception, authResult.message)
            is Result.Loading -> Result.Loading
        }
    }
}

