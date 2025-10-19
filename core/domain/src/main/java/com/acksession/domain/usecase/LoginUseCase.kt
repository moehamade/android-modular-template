package com.acksession.domain.usecase

import com.acksession.common.di.qualifiers.Dispatcher
import com.acksession.common.di.qualifiers.ZencastrDispatchers
import com.acksession.domain.model.Result
import com.acksession.domain.model.User
import com.acksession.domain.repository.AuthRepository
import com.acksession.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case for user login.
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    @param:Dispatcher(ZencastrDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(email: String, password: String): Result<User> = withContext(ioDispatcher) {
        // Step 1: Authenticate and get tokens
        val authResult = authRepository.login(email, password)

        return@withContext when (authResult) {
            is Result.Success -> {
                // Step 2: Tokens are already saved in the repository
                // Step 3: Sync user data
                userRepository.syncCurrentUser()
            }
            is Result.Error -> Result.Error(authResult.exception, authResult.message)
            is Result.Loading -> Result.Loading
        }
    }
}
