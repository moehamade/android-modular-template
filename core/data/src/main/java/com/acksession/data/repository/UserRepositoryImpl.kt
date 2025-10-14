package com.acksession.data.repository

import com.acksession.data.local.datasource.AuthTokenManager
import com.acksession.data.local.datasource.UserLocalDataSource
import com.acksession.data.mapper.toDomain
import com.acksession.data.mapper.toEntity
import com.acksession.data.remote.datasource.UserRemoteDataSource
import com.acksession.domain.model.Result
import com.acksession.domain.model.User
import com.acksession.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of UserRepository.
 * Follows offline-first approach with local cache (Room) and remote API sync.
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userRemoteDataSource: UserRemoteDataSource,
    private val userLocalDataSource: UserLocalDataSource,
    private val authTokenManager: AuthTokenManager
) : UserRepository {

    override fun getCurrentUser(): Flow<User?> {
        return authTokenManager.getCurrentUserId().map { userId ->
            if (userId != null) {
                userLocalDataSource.getUserById(userId)?.toDomain()
            } else {
                null
            }
        }
    }

    override suspend fun getUserById(userId: String, forceRefresh: Boolean): Result<User> {
        return try {
            // If not forcing refresh, try to get from cache first
            if (!forceRefresh) {
                val cachedUser = userLocalDataSource.getUserById(userId)
                if (cachedUser != null) {
                    Timber.d("User $userId found in cache")
                    return Result.Success(cachedUser.toDomain())
                }
            }

            // Fetch from network
            val userDto = userRemoteDataSource.getUserById(userId)

            // Cache the result
            userLocalDataSource.saveUser(userDto.toEntity())

            Timber.d("User $userId fetched from network")
            Result.Success(userDto.toDomain())
        } catch (e: Exception) {
            Timber.e(e, "Failed to get user $userId")

            // Try to return cached version on network error
            val cachedUser = userLocalDataSource.getUserById(userId)
            if (cachedUser != null) {
                Timber.d("Returning cached user $userId after network error")
                Result.Success(cachedUser.toDomain())
            } else {
                Result.Error(e, "Failed to get user: ${e.message}")
            }
        }
    }

    override suspend fun updateUserProfile(
        username: String?,
        profileImageUrl: String?
    ): Result<User> {
        return try {
            val updatedUserDto = userRemoteDataSource.updateProfile(username, profileImageUrl)

            // Update local cache
            userLocalDataSource.saveUser(updatedUserDto.toEntity())

            Timber.d("User profile updated successfully")
            Result.Success(updatedUserDto.toDomain())
        } catch (e: Exception) {
            Timber.e(e, "Failed to update user profile")
            Result.Error(e, "Failed to update profile: ${e.message}")
        }
    }

    override suspend fun updateEmail(newEmail: String): Result<User> {
        return try {
            val updatedUserDto = userRemoteDataSource.updateEmail(newEmail)

            // Update local cache
            userLocalDataSource.saveUser(updatedUserDto.toEntity())

            Timber.d("User email updated successfully")
            Result.Success(updatedUserDto.toDomain())
        } catch (e: Exception) {
            Timber.e(e, "Failed to update user email")
            Result.Error(e, "Failed to update email: ${e.message}")
        }
    }

    override suspend fun updatePassword(
        currentPassword: String,
        newPassword: String
    ): Result<Unit> {
        return try {
            userRemoteDataSource.updatePassword(currentPassword, newPassword)

            Timber.d("User password updated successfully")
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to update user password")
            Result.Error(e, "Failed to update password: ${e.message}")
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            userRemoteDataSource.deleteAccount()

            // Clear local data
            val userId = authTokenManager.getCurrentUserId().firstOrNull()
            if (userId != null) {
                userLocalDataSource.deleteUser(userId)
            }
            authTokenManager.clearAuthTokens()
            authTokenManager.clearCurrentUserId()

            Timber.d("User account deleted successfully")
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete user account")
            Result.Error(e, "Failed to delete account: ${e.message}")
        }
    }

    override suspend fun searchUsers(query: String): Result<List<User>> {
        return try {
            val userDtos = userRemoteDataSource.searchUsers(query)

            // Cache search results
            userLocalDataSource.saveUsers(userDtos.map { it.toEntity() })

            Timber.d("Found ${userDtos.size} users matching '$query'")
            Result.Success(userDtos.map { it.toDomain() })
        } catch (e: Exception) {
            Timber.e(e, "Failed to search users")

            // Fallback to cached search on network error
            val cachedUsers = userLocalDataSource.searchUsers(query)
            if (cachedUsers.isNotEmpty()) {
                Timber.d("Returning ${cachedUsers.size} cached users for '$query'")
                Result.Success(cachedUsers.map { it.toDomain() })
            } else {
                Result.Error(e, "Failed to search users: ${e.message}")
            }
        }
    }

    override suspend fun saveUser(user: User): Result<Unit> {
        return try {
            userLocalDataSource.saveUser(user.toEntity())
            Timber.d("User ${user.id} saved to local cache")
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to save user locally")
            Result.Error(e, "Failed to save user: ${e.message}")
        }
    }

    override suspend fun clearCurrentUser(): Result<Unit> {
        return try {
            val userId = authTokenManager.getCurrentUserId().firstOrNull()
            if (userId != null) {
                userLocalDataSource.deleteUser(userId)
                Timber.d("Current user $userId cleared from cache")
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear current user")
            Result.Error(e, "Failed to clear user: ${e.message}")
        }
    }

    override suspend fun syncCurrentUser(): Result<User> {
        return try {
            val userDto = userRemoteDataSource.getCurrentUser()

            // Save to local cache
            userLocalDataSource.saveUser(userDto.toEntity())

            // Update current user ID if needed
            val currentUserId = authTokenManager.getCurrentUserId().firstOrNull()
            if (currentUserId != userDto.id) {
                authTokenManager.saveCurrentUserId(userDto.id)
            }

            Timber.d("Current user synced successfully: ${userDto.id}")
            Result.Success(userDto.toDomain())
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync current user")
            Result.Error(e, "Failed to sync user: ${e.message}")
        }
    }
}
