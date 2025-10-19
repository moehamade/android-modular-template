package com.acksession.domain.model

import android.os.SystemClock

/**
 * Domain model for authentication tokens.
 *
 * Pure data model without infrastructure concerns (no HTTP, caching, or expiration logic).
 * Token expiration checking is handled by the network layer (AuthInterceptor) as a
 * network optimization, not as domain validation.
 *
 * Uses SystemClock.elapsedRealtime() instead of System.currentTimeMillis() to prevent
 * device time manipulation.
 *
 * @property accessToken JWT access token for authenticated API requests
 * @property refreshToken JWT refresh token for obtaining new access tokens
 * @property issuedAt Elapsed realtime when tokens were issued (milliseconds since boot)
 * @property accessTokenLifespanMs Access token validity period in milliseconds
 * @property refreshTokenLifespanMs Refresh token validity period in milliseconds
 */
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val issuedAt: Long = SystemClock.elapsedRealtime(), // Monotonic clock, immune to time changes
    val accessTokenLifespanMs: Long, // e.g., 15 minutes = 15 * 60 * 1000
    val refreshTokenLifespanMs: Long  // e.g., 7 days = 7 * 24 * 60 * 60 * 1000
) {
    companion object {
        /**
         * Create AuthTokens from server response with absolute expiration timestamps.
         *
         * Converts server's UTC timestamps to elapsed realtime + lifespan for storage.
         * This approach is immune to device time changes (clock adjustments, timezone changes).
         *
         * @param accessToken The JWT access token
         * @param refreshToken The JWT refresh token
         * @param accessTokenExpiresAtUtc Unix timestamp (ms) when access token expires
         * @param refreshTokenExpiresAtUtc Unix timestamp (ms) when refresh token expires
         */
        fun fromServerResponse(
            accessToken: String,
            refreshToken: String,
            accessTokenExpiresAtUtc: Long,
            refreshTokenExpiresAtUtc: Long
        ): AuthTokens {
            val now = System.currentTimeMillis()
            val elapsedNow = SystemClock.elapsedRealtime()

            // Calculate lifespan based on server expiration times
            val accessTokenLifespan = (accessTokenExpiresAtUtc - now).coerceAtLeast(0)
            val refreshTokenLifespan = (refreshTokenExpiresAtUtc - now).coerceAtLeast(0)

            return AuthTokens(
                accessToken = accessToken,
                refreshToken = refreshToken,
                issuedAt = elapsedNow,
                accessTokenLifespanMs = accessTokenLifespan,
                refreshTokenLifespanMs = refreshTokenLifespan
            )
        }
    }
}
