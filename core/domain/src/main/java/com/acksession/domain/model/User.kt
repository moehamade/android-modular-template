package com.acksession.domain.model

/**
 * Domain model representing a user in the application.
 * This is a pure Kotlin data class with no Android dependencies.
 *
 * @property id Unique identifier for the user
 * @property email User's email address (nullable for guest users)
 * @property username Display name/username
 * @property profileImageUrl URL to user's profile picture
 * @property isGuest Whether this is a guest user (not fully authenticated)
 * @property createdAt Timestamp when user was created
 * @property lastLoginAt Timestamp of last login
 */
data class User(
    val id: String,
    val email: String?,
    val username: String,
    val profileImageUrl: String?,
    val isGuest: Boolean = false,
    val createdAt: Long,
    val lastLoginAt: Long? = null
)

