package com.acksession.data.local.datasource

import com.acksession.data.local.dao.UserDao
import com.acksession.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Local data source for user operations.
 * Encapsulates all local database operations for users.
 * This follows the single responsibility principle - only handles local storage.
 */
class UserLocalDataSource @Inject constructor(
    private val userDao: UserDao
) {
    /**
     * Get user by ID from local database
     */
    suspend fun getUserById(userId: String): UserEntity? {
        return userDao.getUserById(userId)
    }

    /**
     * Observe user by ID (returns Flow for reactive updates)
     */
    fun observeUserById(userId: String): Flow<UserEntity?> {
        return userDao.observeUserById(userId)
    }

    /**
     * Get all cached users
     */
    suspend fun getAllUsers(): List<UserEntity> {
        return userDao.getAllUsers()
    }

    /**
     * Search users by username in local cache
     */
    suspend fun searchUsers(query: String): List<UserEntity> {
        return userDao.searchUsersByUsername(query)
    }

    /**
     * Save user to local database
     */
    suspend fun saveUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    /**
     * Save multiple users to local database
     */
    suspend fun saveUsers(users: List<UserEntity>) {
        userDao.insertUsers(users)
    }

    /**
     * Update existing user
     */
    suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }

    /**
     * Delete user from local database
     */
    suspend fun deleteUser(userId: String) {
        userDao.deleteUserById(userId)
    }

    /**
     * Clear all users from local database
     */
    suspend fun clearAllUsers() {
        userDao.deleteAllUsers()
    }
}

