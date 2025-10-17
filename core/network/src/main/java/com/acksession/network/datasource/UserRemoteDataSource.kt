package com.acksession.network.datasource

import com.acksession.network.api.UserApiService
import com.acksession.network.dto.UpdateEmailRequest
import com.acksession.network.dto.UpdatePasswordRequest
import com.acksession.network.dto.UpdateProfileRequest
import com.acksession.network.dto.UserDto
import javax.inject.Inject

/**
 * Remote data source for user operations.
 * Encapsulates all API calls related to users.
 */
class UserRemoteDataSource @Inject constructor(
    private val userApiService: UserApiService
) {
    /**
     * Get current logged-in user from API
     */
    suspend fun getCurrentUser(): UserDto {
        return userApiService.getCurrentUser()
    }

    /**
     * Get user by ID from API
     */
    suspend fun getUserById(userId: String): UserDto {
        return userApiService.getUserById(userId)
    }

    /**
     * Search users by query
     */
    suspend fun searchUsers(query: String): List<UserDto> {
        return userApiService.searchUsers(query)
    }

    /**
     * Update user profile
     */
    suspend fun updateProfile(username: String?, profileImageUrl: String?): UserDto {
        return userApiService.updateProfile(
            UpdateProfileRequest(username, profileImageUrl)
        )
    }

    /**
     * Update user email
     */
    suspend fun updateEmail(newEmail: String): UserDto {
        return userApiService.updateEmail(UpdateEmailRequest(newEmail))
    }

    /**
     * Update user password
     */
    suspend fun updatePassword(currentPassword: String, newPassword: String) {
        userApiService.updatePassword(
            UpdatePasswordRequest(currentPassword, newPassword)
        )
    }

    /**
     * Delete user account
     */
    suspend fun deleteAccount() {
        userApiService.deleteAccount()
    }
}

