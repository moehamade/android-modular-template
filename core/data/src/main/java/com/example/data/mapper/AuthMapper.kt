package com.example.data.mapper

import com.example.network.dto.AuthResponse
import com.example.domain.model.AuthTokens

/**
 * Mapper functions for authentication tokens.
 */

/**
 * Convert API AuthResponse to domain AuthTokens model.
 */
fun AuthResponse.toAuthTokens(): AuthTokens {
    return AuthTokens.fromServerResponse(
        accessToken = accessToken,
        refreshToken = refreshToken,
        accessTokenExpiresAtUtc = accessTokenExpiresAt,
        refreshTokenExpiresAtUtc = refreshTokenExpiresAt
    )
}
