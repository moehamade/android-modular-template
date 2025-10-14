package com.acksession.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for User from API responses.
 * This represents the JSON structure from the backend.
 */
@Serializable
data class UserDto(
    val id: String,
    val email: String? = null,
    val username: String,
    val profileImageUrl: String? = null,
    val isGuest: Boolean = false,
    val createdAt: Long,
    val lastLoginAt: Long? = null
)
