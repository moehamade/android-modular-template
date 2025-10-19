package com.acksession.network.interceptor

import com.acksession.datastore.preferences.TinkAuthStorage
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp interceptor that adds JWT access token to API requests.
 * Skips authentication for public endpoints (login, register, guest, refresh).
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

        if (PUBLIC_ENDPOINTS.any { path.contains(it) }) {
            return chain.proceed(request)
        }

        val accessToken = encryptedAuthStorage.getAccessToken()

        if (accessToken == null) {
            Timber.w("No access token available for request: $path")
            return chain.proceed(request)
        }

        val authenticatedRequest = request.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}
