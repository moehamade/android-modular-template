package com.acksession.data.remote.interceptor

import com.acksession.data.local.datasource.AuthTokenManager
import com.acksession.data.mapper.toAuthTokens
import com.acksession.data.remote.api.AuthApiService
import com.acksession.data.remote.dto.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp Authenticator for automatically refreshing expired access tokens.
 * On 401 response, attempts to refresh the token and retry the request.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val authTokenManager: AuthTokenManager,
    private val authApiService: AuthApiService
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Avoid infinite retry loop - if this is already a retry, give up
        if (response.request.header("Authorization")?.contains("retry") == true) {
            Timber.e("Token refresh already attempted, failing request")
            return null
        }

        return runBlocking {
            try {
                val refreshToken = authTokenManager.getRefreshToken()

                if (refreshToken == null) {
                    Timber.e("No refresh token available, cannot refresh")
                    return@runBlocking null
                }

                // Attempt to refresh the token
                Timber.d("Attempting to refresh access token")
                val authResponse = authApiService.refreshToken(RefreshTokenRequest(refreshToken))
                val newTokens = authResponse.toAuthTokens()

                // Save new tokens
                authTokenManager.saveAuthTokens(newTokens)
                Timber.d("Access token refreshed successfully via authenticator")

                // Retry the request with new access token
                response.request.newBuilder()
                    .header("Authorization", "Bearer ${newTokens.accessToken} retry")
                    .build()
            } catch (e: Exception) {
                Timber.e(e, "Token refresh failed in authenticator")
                // Clear invalid tokens
                authTokenManager.clearAuthTokens()
                null
            }
        }
    }
}
