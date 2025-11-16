package com.example.data.repository

import com.example.data.local.datasource.AuthTokenManager
import com.example.network.datasource.AuthRemoteDataSource
import com.example.data.mapper.toAuthTokens
import com.example.domain.model.AuthTokens
import com.example.domain.model.Result
import com.example.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of AuthRepository.
 * Coordinates between remote API and local token storage for authentication operations.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val authTokenManager: AuthTokenManager,
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthTokens> {
        return try {
            val response = authRemoteDataSource.login(email, password)
            val tokens = response.toAuthTokens()

            // Save tokens locally
            authTokenManager.saveAuthTokens(tokens)
            authTokenManager.saveCurrentUserId(response.user.id)

            Timber.d("Login successful for user: ${response.user.id}")
            Result.Success(tokens)
        } catch (e: Exception) {
            Timber.e(e, "Login failed")
            Result.Error(e, "Login failed: ${e.message}")
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        username: String,
    ): Result<AuthTokens> {
        return try {
            val response = authRemoteDataSource.register(email, password, username)
            val tokens = response.toAuthTokens()

            // Save tokens locally
            authTokenManager.saveAuthTokens(tokens)
            authTokenManager.saveCurrentUserId(response.user.id)

            Timber.d("Registration successful for user: ${response.user.id}")
            Result.Success(tokens)
        } catch (e: Exception) {
            Timber.e(e, "Registration failed")
            Result.Error(e, "Registration failed: ${e.message}")
        }
    }

    override suspend fun continueAsGuest(): Result<AuthTokens> {
        return try {
            val response = authRemoteDataSource.continueAsGuest()
            val tokens = response.toAuthTokens()

            // Save tokens locally
            authTokenManager.saveAuthTokens(tokens)
            authTokenManager.saveCurrentUserId(response.user.id)

            Timber.d("Guest session created for user: ${response.user.id}")
            Result.Success(tokens)
        } catch (e: Exception) {
            Timber.e(e, "Guest session creation failed")
            Result.Error(e, "Guest session failed: ${e.message}")
        }
    }

    override suspend fun convertGuestToFullAccount(
        email: String,
        password: String,
        username: String,
    ): Result<AuthTokens> {
        return try {
            val response = authRemoteDataSource.convertGuestToFullAccount(email, password, username)
            val tokens = response.toAuthTokens()

            // Update tokens locally
            authTokenManager.saveAuthTokens(tokens)
            authTokenManager.saveCurrentUserId(response.user.id)

            Timber.d("Guest account converted for user: ${response.user.id}")
            Result.Success(tokens)
        } catch (e: Exception) {
            Timber.e(e, "Guest account conversion failed")
            Result.Error(e, "Conversion failed: ${e.message}")
        }
    }

    override suspend fun refreshAccessToken(): Result<AuthTokens> {
        return try {
            val currentRefreshToken = authTokenManager.getRefreshToken()
                ?: return Result.Error(
                    IllegalStateException("No refresh token available"),
                    "No refresh token found",
                )

            val response = authRemoteDataSource.refreshToken(currentRefreshToken)
            val tokens = response.toAuthTokens()

            // Save new tokens
            authTokenManager.saveAuthTokens(tokens)

            Timber.d("Access token refreshed successfully")
            Result.Success(tokens)
        } catch (e: Exception) {
            Timber.e(e, "Token refresh failed")
            // Clear tokens on refresh failure (likely expired refresh token)
            authTokenManager.clearAuthTokens()
            Result.Error(e, "Token refresh failed: ${e.message}")
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            // Notify server about logout (best effort, don't fail on error)
            try {
                authRemoteDataSource.logout()
            } catch (e: Exception) {
                Timber.w(e, "Server logout notification failed (continuing with local logout)")
            }

            // Clear local tokens and user ID
            authTokenManager.clearAuthTokens()
            authTokenManager.clearCurrentUserId()

            Timber.d("Logout successful")
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Logout failed")
            Result.Error(e, "Logout failed: ${e.message}")
        }
    }

    override fun getAuthTokens(): Flow<AuthTokens?> {
        return authTokenManager.getAuthTokens()
    }

    override fun isAuthenticated(): Flow<Boolean> {
        return authTokenManager.isAuthenticated()
    }

    override suspend fun saveAuthTokens(tokens: AuthTokens): Result<Unit> {
        return try {
            authTokenManager.saveAuthTokens(tokens)
            Timber.d("Auth tokens saved successfully")
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to save auth tokens")
            Result.Error(e, "Failed to save tokens: ${e.message}")
        }
    }

    override suspend fun clearAuthTokens(): Result<Unit> {
        return try {
            authTokenManager.clearAuthTokens()
            authTokenManager.clearCurrentUserId()
            Timber.d("Auth tokens cleared successfully")
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear auth tokens")
            Result.Error(e, "Failed to clear tokens: ${e.message}")
        }
    }
}
