package com.acksession.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.acksession.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for user operations.
 * Provides methods to interact with the users table in Room database.
 */
@Dao
interface UserDao {

    /**
     * Get user by ID
     */
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    /**
     * Get user by ID as Flow (observes changes)
     */
    @Query("SELECT * FROM users WHERE id = :userId")
    fun observeUserById(userId: String): Flow<UserEntity?>

    /**
     * Get all users (for caching search results, etc.)
     */
    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>

    /**
     * Search users by username
     */
    @Query("SELECT * FROM users WHERE username LIKE '%' || :query || '%' AND isGuest = 0")
    suspend fun searchUsersByUsername(query: String): List<UserEntity>

    /**
     * Insert or replace user
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    /**
     * Insert multiple users
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    /**
     * Update user
     */
    @Update
    suspend fun updateUser(user: UserEntity)

    /**
     * Delete user by ID
     */
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)

    /**
     * Delete all users
     */
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}

