package com.acksession.network.dto

import kotlinx.serialization.Serializable

/**
 * Login request payload
 */
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

