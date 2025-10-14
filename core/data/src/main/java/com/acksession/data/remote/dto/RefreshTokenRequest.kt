package com.acksession.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Refresh token request payload
 */
@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

