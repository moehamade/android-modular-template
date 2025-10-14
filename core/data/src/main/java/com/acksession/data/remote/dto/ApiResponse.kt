package com.acksession.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Generic API response wrapper
 */
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val errorCode: String? = null
)

