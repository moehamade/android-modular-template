package com.acksession.network.config

/**
 * Authentication configuration for network layer.
 *
 * Contains constants and settings related to token management and HTTP authentication.
 */
object AuthConfig {
    /**
     * Buffer time before actual token expiration to trigger proactive refresh.
     *
     * When a request is about to be sent, AuthInterceptor checks if the access token
     * will expire within this buffer window. If so, it triggers a synthetic 401 to
     * force TokenAuthenticator to refresh the token before the actual request is sent.
     *
     * **Why this matters:**
     * - Prevents sending requests with tokens that expire mid-flight
     * - Reduces unnecessary 401 responses from server
     * - Improves user experience (fewer failed requests)
     *
     * **Value:** 5 minutes (300,000 milliseconds)
     * - Typical access token lifespan: 15 minutes
     * - With 5-minute buffer: refresh happens at 10-minute mark
     * - Leaves 5 minutes for refresh operation to complete
     */
    const val TOKEN_EXPIRATION_BUFFER_MS = 5 * 60 * 1000L // 5 minutes
}
