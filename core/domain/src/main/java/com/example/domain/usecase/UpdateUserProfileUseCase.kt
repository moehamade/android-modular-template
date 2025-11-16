package com.example.domain.usecase

import com.example.domain.model.Result
import com.example.domain.model.User
import com.example.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Use case for updating user profile.
 * Allows updating username and/or profile image.
 */
class UpdateUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        username: String? = null,
        profileImageUrl: String? = null
    ): Result<User> {
        return userRepository.updateUserProfile(username, profileImageUrl)
    }
}

