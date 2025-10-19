package com.acksession.network.interceptor

import com.acksession.datastore.preferences.TinkAuthStorage
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
        // Avoid infinite retry loop - if this is already a retry, give up
        if (response.request.header("Authorization")?.contains("retry") == true) {
            Timber.e("Token refresh already attempted, failing request")
            return null
        }

        try {
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
                .header("Authorization", "Bearer $newAccessToken retry")
                .build()
        } catch (e: Exception) {
            Timber.e(e, "Token refresh failed in authenticator")
            // Clear invalid tokens (non-blocking)
            encryptedAuthStorage.clearAuthTokens()
            return null
        }
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
