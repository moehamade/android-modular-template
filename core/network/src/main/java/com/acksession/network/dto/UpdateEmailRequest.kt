package com.acksession.network.dto

import kotlinx.serialization.Serializable

/**
 * Update email request payload
 */
@Serializable
data class UpdateEmailRequest(
    val newEmail: String
)

