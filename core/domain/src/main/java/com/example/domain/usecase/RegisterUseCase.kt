package com.example.domain.usecase

import com.example.common.di.qualifiers.Dispatcher
import com.example.common.di.qualifiers.AppDispatchers
import com.example.domain.model.Result
import com.example.domain.model.User
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case for user registration.
 */
class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    @param:Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        username: String
    ): Result<User> = withContext(ioDispatcher) {
        // Step 1: Register and get tokens
        val authResult = authRepository.register(email, password, username)

        return@withContext when (authResult) {
            is Result.Success -> {
                // Step 2: Sync user data from server
                userRepository.syncCurrentUser()
            }
            is Result.Error -> Result.Error(authResult.exception, authResult.message)
            is Result.Loading -> Result.Loading
        }
    }
}
