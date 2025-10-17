package com.acksession.network.dto

import kotlinx.serialization.Serializable

/**
 * Update password request payload
 */
@Serializable
data class UpdatePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

