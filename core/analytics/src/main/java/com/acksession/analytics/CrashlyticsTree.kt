package com.acksession.analytics

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * A Timber tree that logs to Firebase Crashlytics.
 *
 * This tree:
 * - Logs ERROR and WARN priority messages to Crashlytics
 * - Records exceptions with full stack traces
 * - Adds breadcrumb logs for debugging crashes
 * - Does NOT log to Android Logcat (use DebugTree in debug builds)
 *
 * Usage in ZencastrApplication:
 * ```kotlin
 * if (BuildConfig.DEBUG) {
 *     Timber.plant(Timber.DebugTree())
 * } else {
 *     Timber.plant(CrashlyticsTree())
 * }
 * ```
 */
class CrashlyticsTree : Timber.Tree() {

    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // Only log WARN and ERROR to Crashlytics to reduce noise
        if (priority < Log.WARN) return

        // Add log message as breadcrumb
        val logMessage = tag?.let { "[$it] $message" } ?: message
        crashlytics.log(logMessage)

        // If there's an exception, record it
        t?.let { throwable ->
            crashlytics.recordException(throwable)
        }
    }
}
