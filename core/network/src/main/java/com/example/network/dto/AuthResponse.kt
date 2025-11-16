package com.example.network.dto

import kotlinx.serialization.Serializable

/**
 * Response from authentication endpoints.
 * Contains both the JWT tokens and user information.
 */
@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresAt: Long,
    val refreshTokenExpiresAt: Long,
    val user: UserDto
)

