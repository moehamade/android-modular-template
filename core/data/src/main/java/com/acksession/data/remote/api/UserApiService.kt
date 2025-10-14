package com.acksession.data.remote.api

import com.acksession.data.remote.dto.ApiResponse
import com.acksession.data.remote.dto.UpdateEmailRequest
import com.acksession.data.remote.dto.UpdatePasswordRequest
import com.acksession.data.remote.dto.UpdateProfileRequest
import com.acksession.data.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API service for user endpoints.
 * Define your actual backend endpoints here.
 */
interface UserApiService {

    @GET("users/me")
    suspend fun getCurrentUser(): UserDto

    @GET("users/{userId}")
    suspend fun getUserById(@Path("userId") userId: String): UserDto

    @GET("users/search")
    suspend fun searchUsers(@Query("q") query: String): List<UserDto>

    @PUT("users/me/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): UserDto

    @PUT("users/me/email")
    suspend fun updateEmail(@Body request: UpdateEmailRequest): UserDto

    @PUT("users/me/password")
    suspend fun updatePassword(@Body request: UpdatePasswordRequest): ApiResponse<Unit>

    @DELETE("users/me")
    suspend fun deleteAccount(): ApiResponse<Unit>
}

