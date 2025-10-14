package com.acksession.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a user in the local database.
 * This is the local cache layer for user data.
 *
 * @property id Unique identifier (primary key)
 * @property email User's email address (nullable for guest users)
 * @property username Display name/username
 * @property profileImageUrl URL to user's profile picture
 * @property isGuest Whether this is a guest user
 * @property createdAt Timestamp when user was created
 * @property lastLoginAt Timestamp of last login
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String?,
    val username: String,
    val profileImageUrl: String?,
    val isGuest: Boolean = false,
    val createdAt: Long,
    val lastLoginAt: Long? = null
)

