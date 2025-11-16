package com.example.network.interceptor

import com.example.datastore.preferences.TinkAuthStorage
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp Authenticator for automatically refreshing expired access tokens.
 *
 * When a 401 Unauthorized response is received:
 * 1. Attempts to refresh the access token using the refresh token
 * 2. Updates stored tokens
 * 3. Retries the failed request with the new access token
 *
 * **Thread Safety:**
 * Uses `@Synchronized` to prevent concurrent token refresh attempts when multiple
 * requests fail with 401 simultaneously. Only one refresh operation proceeds,
 * others wait and reuse the refreshed token.
 *
 * **Infinite Loop Prevention:**
 * Tracks the authentication attempt chain via `response.priorResponse` to detect
 * if we're stuck in an infinite retry loop (e.g., server keeps rejecting the new
 * token). After 3 attempts, gives up and returns null. This does NOT mean we retry
 * token refresh 3 times - if refresh fails once, we stop immediately.
 *
 * **Why 3 attempts?**
 * - Attempt 1: Original request with expired token → 401
 * - Attempt 2: Retry with refreshed token → 401 (token still invalid?)
 * - Attempt 3: Second retry → 401 (definitely something wrong)
 * - Stop: Return null, let app handle re-authentication
 *
 * Note: This authenticator delegates the actual refresh logic to a callback
 * that should be provided by the data layer (to avoid circular dependencies).
 *
 * If refresh fails (e.g., refresh token expired), returns null to fail the request
 * and let the app handle re-authentication.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val encryptedAuthStorage: TinkAuthStorage,
    private val tokenRefreshCallback: TokenRefreshCallback
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Prevent infinite loops - if this request already went through auth chain 3+ times, give up
        if (responseCount(response) >= 3) {
            Timber.e("Token refresh stuck in loop (3+ auth attempts), failing request")
            return null
        }

        // Synchronize to prevent concurrent refresh attempts (only one thread refreshes)
        return refreshTokenAndRetry(response)
    }

    @Synchronized
    private fun refreshTokenAndRetry(response: Response): Request? {
        try {
            // Check if token was already refreshed by another thread
            val currentToken = encryptedAuthStorage.getAccessToken()
            val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")

            if (currentToken != null && currentToken != requestToken) {
                // Token was already refreshed by another concurrent request
                Timber.d("Token already refreshed by another request, reusing it")
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            val refreshToken = encryptedAuthStorage.getRefreshToken()

            if (refreshToken == null) {
                Timber.e("No refresh token available, cannot refresh")
                return null
            }

            // Attempt to refresh the token via callback
            Timber.d("Attempting to refresh access token")
            val newAccessToken = tokenRefreshCallback.refreshTokenSync(refreshToken)

            if (newAccessToken == null) {
                Timber.e("Token refresh returned null")
                encryptedAuthStorage.clearAuthTokens()
                return null
            }

            Timber.d("Access token refreshed successfully via authenticator")

            // Retry the request with new access token
            return response.request.newBuilder()
                .header("Authorization", "Bearer $newAccessToken")
                .build()
        } catch (e: Exception) {
            Timber.e(e, "Token refresh failed in authenticator")
            // Clear invalid tokens (non-blocking)
            encryptedAuthStorage.clearAuthTokens()
            return null
        }
    }

    /**
     * Count the number of times this request has been retried.
     * Uses the response chain to detect retry loops.
     */
    private fun responseCount(response: Response): Int {
        var count = 1
        var currentResponse = response.priorResponse
        while (currentResponse != null) {
            count++
            currentResponse = currentResponse.priorResponse
        }
        return count
    }
}

/**
 * Callback interface for token refresh.
 *
 * This allows the data layer to provide the refresh logic without creating
 * a circular dependency between core:network and core:data.
 *
 * Implementation should be provided by core:data module.
 */
interface TokenRefreshCallback {
    /**
     * Refresh the access token using the refresh token (synchronous for OkHttp).
     *
     * @param refreshToken The current refresh token
     * @return New access token, or null if refresh failed
     */
    fun refreshTokenSync(refreshToken: String): String?
}
