package com.acksession.network.api

import com.acksession.network.dto.AuthResponse
import com.acksession.network.dto.ConvertGuestRequest
import com.acksession.network.dto.LoginRequest
import com.acksession.network.dto.RefreshTokenRequest
import com.acksession.network.dto.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit API service for authentication endpoints.
 * Define your actual backend endpoints here.
 */
interface AuthApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth/guest")
    suspend fun continueAsGuest(): AuthResponse

    @POST("auth/convert-guest")
    suspend fun convertGuestToFullAccount(@Body request: ConvertGuestRequest): AuthResponse

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): AuthResponse

    @POST("auth/logout")
    suspend fun logout()
}

