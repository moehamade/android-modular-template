package com.example.network.interceptor

import android.os.SystemClock
import com.example.datastore.preferences.TinkAuthStorage
import com.example.network.config.AuthConfig
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp interceptor that adds JWT access token to API requests.
 *
 * **Features:**
 * - Skips authentication for public endpoints (login, register, guest, refresh)
 * - Proactively checks token expiration before making requests
 * - Triggers 401 for expired tokens (allowing TokenAuthenticator to refresh)
 *
 * **Performance Optimization:**
 * Checking expiration client-side prevents sending known-expired tokens to the server,
 * reducing unnecessary 401 responses and subsequent refresh cycles.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val encryptedAuthStorage: TinkAuthStorage
) : Interceptor {

    companion object {
        private val PUBLIC_ENDPOINTS = setOf(
            "auth/login",
            "auth/register",
            "auth/guest",
            "auth/refresh"
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        // Skip authentication for public endpoints
        if (PUBLIC_ENDPOINTS.any { path.contains(it) }) {
            return chain.proceed(request)
        }

        val accessToken = encryptedAuthStorage.getAccessToken()

        if (accessToken == null) {
            Timber.w("No access token available for request: $path")
            return chain.proceed(request)
        }

        // Check if token is expired or expiring soon (proactive refresh)
        if (isTokenExpired()) {
            Timber.d("Access token expired or expiring soon, triggering 401 for refresh")
            // Return 401 to trigger TokenAuthenticator refresh flow
            return createUnauthorizedResponse(request)
        }

        val authenticatedRequest = request.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()

        return chain.proceed(authenticatedRequest)
    }

    /**
     * Check if the access token is expired or will expire soon.
     *
     * Uses cached metadata to avoid DataStore reads (fast synchronous check).
     * Applies buffer from [AuthConfig.TOKEN_EXPIRATION_BUFFER_MS] to trigger
     * proactive refresh before token actually expires.
     *
     * @return true if token is expired or will expire within buffer window
     */
    private fun isTokenExpired(): Boolean {
        val issuedAt = encryptedAuthStorage.getIssuedAt()
        val lifespan = encryptedAuthStorage.getAccessTokenLifespan()

        // If metadata is not available (cache not populated yet), assume not expired
        if (issuedAt == 0L || lifespan == 0L) {
            return false
        }

        val elapsedSinceIssued = SystemClock.elapsedRealtime() - issuedAt
        val expirationThreshold = lifespan - AuthConfig.TOKEN_EXPIRATION_BUFFER_MS

        return elapsedSinceIssued >= expirationThreshold
    }

    /**
     * Create a synthetic 401 Unauthorized response to trigger TokenAuthenticator.
     * This allows us to leverage the existing refresh logic instead of duplicating it.
     */
    private fun createUnauthorizedResponse(
        request: okhttp3.Request
    ): Response {
        return Response.Builder()
            .request(request)
            .protocol(okhttp3.Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized - Token expired (client-side check)")
            .body("".toResponseBody(null))
            .build()
    }
}
