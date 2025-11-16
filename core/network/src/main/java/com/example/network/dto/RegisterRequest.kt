package com.example.network.dto

import kotlinx.serialization.Serializable

/**
 * Registration request payload
 */
@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String
)

