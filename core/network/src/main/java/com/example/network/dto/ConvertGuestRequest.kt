package com.example.network.dto

import kotlinx.serialization.Serializable

/**
 * Convert guest account request payload
 */
@Serializable
data class ConvertGuestRequest(
    val email: String,
    val password: String,
    val username: String
)

