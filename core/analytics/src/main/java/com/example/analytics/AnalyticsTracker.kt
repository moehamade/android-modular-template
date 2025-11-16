package com.example.analytics

/**
 * Analytics tracking interface for logging events and user properties.
 *
 * Implementations should handle:
 * - Event tracking (user actions, screen views, etc.)
 * - User property tracking (user ID, preferences, etc.)
 * - Crash reporting
 *
 * Example usage:
 * ```kotlin
 * analyticsTracker.logEvent("login_success", mapOf("method" to "email"))
 * analyticsTracker.setUserId("user123")
 * analyticsTracker.logScreenView("HomeScreen")
 * ```
 */
interface AnalyticsTracker {

    /**
     * Log a custom event with optional parameters.
     *
     * @param eventName Name of the event (e.g., "login_success", "video_started")
     * @param params Optional parameters for the event
     */
    fun logEvent(eventName: String, params: Map<String, Any>? = null)

    /**
     * Log a screen view event.
     *
     * @param screenName Name of the screen (e.g., "HomeScreen", "ProfileScreen")
     * @param screenClass Optional screen class name
     */
    fun logScreenView(screenName: String, screenClass: String? = null)

    /**
     * Set the user ID for analytics.
     *
     * @param userId Unique user identifier (null to clear)
     */
    fun setUserId(userId: String?)

    /**
     * Set a user property.
     *
     * @param name Property name (e.g., "user_type", "subscription_tier")
     * @param value Property value
     */
    fun setUserProperty(name: String, value: String?)

    /**
     * Log a non-fatal exception to crash reporting.
     *
     * @param throwable The exception to log
     * @param message Optional message describing the context
     */
    fun logException(throwable: Throwable, message: String? = null)

    /**
     * Add a custom key-value pair to the crash report.
     * Useful for adding context to crash reports.
     *
     * @param key Key name
     * @param value Value (supports String, Boolean, Int, Long, Float, Double)
     */
    fun setCustomKey(key: String, value: Any)

    /**
     * Enable/disable analytics collection.
     * Useful for respecting user privacy preferences.
     *
     * @param enabled Whether to enable analytics collection
     */
    fun setAnalyticsCollectionEnabled(enabled: Boolean)
}
