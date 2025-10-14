package com.acksession.domain.usecase

import com.acksession.domain.model.Result
import com.acksession.domain.model.User
import com.acksession.domain.repository.AuthRepository
import com.acksession.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Use case for converting a guest account to a full account.
 * Requires email, password, and username.
 */
class ConvertGuestAccountUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        username: String
    ): Result<User> {
        // Step 1: Convert guest to full account
        val authResult = authRepository.convertGuestToFullAccount(email, password, username)

        return when (authResult) {
            is Result.Success -> {
                // Step 2: Sync updated user data
                userRepository.syncCurrentUser()
            }
            is Result.Error -> Result.Error(authResult.exception, authResult.message)
            is Result.Loading -> Result.Loading
        }
    }
}

