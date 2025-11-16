package com.example.network.datasource

import com.example.network.api.AuthApiService
import com.example.network.dto.AuthResponse
import com.example.network.dto.ConvertGuestRequest
import com.example.network.dto.LoginRequest
import com.example.network.dto.RefreshTokenRequest
import com.example.network.dto.RegisterRequest
import javax.inject.Inject

/**
 * Remote data source for authentication API calls.
 */
class AuthRemoteDataSource @Inject constructor(
    private val authApiService: AuthApiService
) {
    /**
     * Login with email and password
     */
    suspend fun login(email: String, password: String): AuthResponse {
        return authApiService.login(LoginRequest(email, password))
    }

    /**
     * Register a new user account
     */
    suspend fun register(email: String, password: String, username: String): AuthResponse {
        return authApiService.register(RegisterRequest(email, password, username))
    }

    /**
     * Continue as guest
     */
    suspend fun continueAsGuest(): AuthResponse {
        return authApiService.continueAsGuest()
    }

    /**
     * Convert guest account to full account
     */
    suspend fun convertGuestToFullAccount(
        email: String,
        password: String,
        username: String
    ): AuthResponse {
        return authApiService.convertGuestToFullAccount(
            ConvertGuestRequest(email, password, username)
        )
    }

    /**
     * Refresh access token using refresh token
     */
    suspend fun refreshToken(refreshToken: String): AuthResponse {
        return authApiService.refreshToken(RefreshTokenRequest(refreshToken))
    }

    /**
     * Logout (notify server)
     */
    suspend fun logout() {
        authApiService.logout()
    }
}
