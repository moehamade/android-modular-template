package com.acksession.domain.repository

import com.acksession.domain.model.AuthTokens
import com.acksession.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations.
 * This defines the contract for authentication-related data operations.
 * Implementations are in the data layer.
 */
interface AuthRepository {

    /**
     * Login with email and password
     * @return Result containing AuthTokens on success
     */
    suspend fun login(email: String, password: String): Result<AuthTokens>

    /**
     * Register a new user account
     * @return Result containing AuthTokens on success
     */
    suspend fun register(
        email: String,
        password: String,
        username: String
    ): Result<AuthTokens>

    /**
     * Continue as guest (creates temporary guest session)
     * @return Result containing guest AuthTokens
     */
    suspend fun continueAsGuest(): Result<AuthTokens>

    /**
     * Convert guest account to full account
     * @return Result containing updated AuthTokens
     */
    suspend fun convertGuestToFullAccount(
        email: String,
        password: String,
        username: String
    ): Result<AuthTokens>

    /**
     * Refresh the access token using refresh token
     * @return Result containing new AuthTokens
     */
    suspend fun refreshAccessToken(): Result<AuthTokens>

    /**
     * Logout current user and clear tokens
     */
    suspend fun logout(): Result<Unit>

    /**
     * Get current auth tokens
     * @return Flow of AuthTokens or null if not authenticated
     */
    fun getAuthTokens(): Flow<AuthTokens?>

    /**
     * Check if user is currently authenticated
     */
    fun isAuthenticated(): Flow<Boolean>

    /**
     * Save auth tokens locally
     */
    suspend fun saveAuthTokens(tokens: AuthTokens): Result<Unit>

    /**
     * Clear all stored auth tokens
     */
    suspend fun clearAuthTokens(): Result<Unit>
}

