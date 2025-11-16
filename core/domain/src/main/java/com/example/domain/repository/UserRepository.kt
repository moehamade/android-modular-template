package com.example.domain.repository

import com.example.domain.model.Result
import com.example.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user-related operations.
 * This defines the contract for user data operations.
 * Implementations are in the data layer.
 */
interface UserRepository {

    /**
     * Get current logged-in user
     * @return Flow of User or null if not logged in
     */
    fun getCurrentUser(): Flow<User?>

    /**
     * Get user by ID
     * @param userId The user's unique identifier
     * @param forceRefresh Force fetch from remote, bypass cache
     * @return Result containing User
     */
    suspend fun getUserById(userId: String, forceRefresh: Boolean = false): Result<User>

    /**
     * Update current user profile
     * @return Result containing updated User
     */
    suspend fun updateUserProfile(
        username: String? = null,
        profileImageUrl: String? = null
    ): Result<User>

    /**
     * Update user's email
     * @return Result containing updated User
     */
    suspend fun updateEmail(newEmail: String): Result<User>

    /**
     * Update user's password
     */
    suspend fun updatePassword(
        currentPassword: String,
        newPassword: String
    ): Result<Unit>

    /**
     * Delete user account
     */
    suspend fun deleteAccount(): Result<Unit>

    /**
     * Search users by username
     * @return Result containing list of matching users
     */
    suspend fun searchUsers(query: String): Result<List<User>>

    /**
     * Save user to local database
     */
    suspend fun saveUser(user: User): Result<Unit>

    /**
     * Clear current user from local storage
     */
    suspend fun clearCurrentUser(): Result<Unit>

    /**
     * Sync user data from remote to local
     */
    suspend fun syncCurrentUser(): Result<User>
}

