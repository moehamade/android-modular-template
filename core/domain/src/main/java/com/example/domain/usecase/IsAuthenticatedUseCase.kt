package com.example.domain.usecase

import com.example.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for checking if user is authenticated.
 * Returns a Flow that emits true if authenticated, false otherwise.
 */
class IsAuthenticatedUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<Boolean> {
        return authRepository.isAuthenticated()
    }
}

