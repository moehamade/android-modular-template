package com.acksession.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase implementation of [AnalyticsTracker].
 *
 * Integrates with Firebase Analytics and Crashlytics for event tracking
 * and crash reporting.
 *
 * Note: Firebase Analytics automatically collects certain events like:
 * - first_open, session_start, user_engagement, etc.
 * See: https://support.google.com/firebase/answer/9234069
 */
@Singleton
class FirebaseAnalyticsTracker @Inject constructor(
    private val analytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics
) : AnalyticsTracker {

    override fun logEvent(eventName: String, params: Map<String, Any>?) {
        val bundle = params?.toBundle()
        analytics.logEvent(eventName, bundle)
        Timber.d("Analytics event: $eventName, params: $params")
    }

    override fun logScreenView(screenName: String, screenClass: String?) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            screenClass?.let { param(FirebaseAnalytics.Param.SCREEN_CLASS, it) }
        }
        Timber.d("Screen view: $screenName (class: $screenClass)")
    }

    override fun setUserId(userId: String?) {
        analytics.setUserId(userId)
        crashlytics.setUserId(userId ?: "")
        Timber.d("User ID set: ${userId?.take(4)}***")
    }

    override fun setUserProperty(name: String, value: String?) {
        analytics.setUserProperty(name, value)
        Timber.d("User property: $name = $value")
    }

    override fun logException(throwable: Throwable, message: String?) {
        message?.let { crashlytics.log(it) }
        crashlytics.recordException(throwable)
        Timber.w(throwable, "Non-fatal exception: $message")
    }

    override fun setCustomKey(key: String, value: Any) {
        when (value) {
            is String -> crashlytics.setCustomKey(key, value)
            is Boolean -> crashlytics.setCustomKey(key, value)
            is Int -> crashlytics.setCustomKey(key, value)
            is Long -> crashlytics.setCustomKey(key, value)
            is Float -> crashlytics.setCustomKey(key, value)
            is Double -> crashlytics.setCustomKey(key, value)
            else -> crashlytics.setCustomKey(key, value.toString())
        }
    }

    override fun setAnalyticsCollectionEnabled(enabled: Boolean) {
        analytics.setAnalyticsCollectionEnabled(enabled)
        Timber.d("Analytics collection ${if (enabled) "enabled" else "disabled"}")
    }

    /**
     * Convert a Map to Bundle for Firebase Analytics.
     */
    private fun Map<String, Any>.toBundle(): Bundle {
        return Bundle().apply {
            this@toBundle.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putBoolean(key, value)
                    is Float -> putFloat(key, value)
                    else -> putString(key, value.toString())
                }
            }
        }
    }
}
