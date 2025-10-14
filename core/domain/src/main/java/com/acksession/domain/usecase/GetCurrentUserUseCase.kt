package com.acksession.domain.usecase

import com.acksession.domain.model.User
import com.acksession.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting the current logged-in user.
 * Returns a Flow that emits whenever user data changes.
 */
class GetCurrentUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<User?> {
        return userRepository.getCurrentUser()
    }
}

