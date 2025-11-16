package com.example.domain.usecase

import com.example.domain.model.Result
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Use case for user logout.
 * Handles clearing auth tokens and user data.
 */
class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        // Clear auth tokens
        val authResult = authRepository.logout()

        // Clear user data
        userRepository.clearCurrentUser()

        return authResult
    }
}

