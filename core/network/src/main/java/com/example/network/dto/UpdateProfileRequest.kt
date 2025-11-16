package com.example.network.dto

import kotlinx.serialization.Serializable

/**
 * Update user profile request payload
 */
@Serializable
data class UpdateProfileRequest(
    val username: String? = null,
    val profileImageUrl: String? = null
)

