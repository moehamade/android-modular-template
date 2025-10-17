package com.acksession.data.mapper

import com.acksession.network.dto.AuthResponse
import com.acksession.domain.model.AuthTokens

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
