package com.acksession.zencastr

import android.app.Application
import com.acksession.analytics.AnalyticsTracker
import com.acksession.analytics.CrashlyticsTree
import com.acksession.common.di.qualifiers.ApplicationScopeIO
import com.acksession.datastore.preferences.TinkAuthStorage
import com.acksession.notifications.ZencastrNotificationManager
import com.acksession.remoteconfig.FeatureFlagManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Application class for Zencastr.
 *
 * Initializes:
 * - Timber logging (debug/production)
 * - Firebase services (Crashlytics, Analytics, Remote Config)
 * - Push notifications (FCM)
 * - Global exception handling
 *
 * @HiltAndroidApp triggers Hilt's code generation including a base class for
 * the application that serves as the application-level dependency container.
 */
@HiltAndroidApp
class ZencastrApplication : Application() {

    @Inject
    lateinit var authStorage: TinkAuthStorage

    @Inject
    lateinit var analytics: AnalyticsTracker

    @Inject
    lateinit var notificationManager: ZencastrNotificationManager

    @Inject
    lateinit var featureFlagManager: FeatureFlagManager

    @Inject
    @ApplicationScopeIO
    lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()

        initializeLogging()
        initializeFirebase()
        setupGlobalExceptionHandler()

        Timber.d("Zencastr initialized - Environment: ${BuildConfig.ENVIRONMENT}")
    }

    /**
     * Initialize logging based on build type.
     * - Debug: Log to Logcat via DebugTree
     * - Production: Log to Crashlytics via CrashlyticsTree
     */
    private fun initializeLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }
    }

    /**
     * Initialize Firebase services.
     * - Create notification channels
     * - Fetch remote config in background
     */
    private fun initializeFirebase() {
        // Create notification channels (required for Android O+)
        notificationManager.createNotificationChannels()

        // Fetch remote config in background (non-blocking)
        applicationScope.launch {
            featureFlagManager.fetchAndActivate()
        }

        // TODO: Set user ID for analytics after login
        // analytics.setUserId(userId)
    }

    /**
     * Setup global exception handler for uncaught exceptions.
     *
     * Behavior:
     * - Debug: Log and crash normally (for visibility during development)
     * - Production: Log to Crashlytics and crash (Firebase will capture)
     */
    private fun setupGlobalExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Timber.e(throwable, "Uncaught exception in thread: ${thread.name}")

            // Log to Crashlytics with context
            analytics.logException(throwable, "Uncaught exception: ${throwable.message}")
            analytics.setCustomKey("crash_thread", thread.name)
            analytics.setCustomKey("environment", BuildConfig.ENVIRONMENT)

            if (BuildConfig.DEBUG) {
                // In debug, let app crash normally for visibility
                defaultHandler?.uncaughtException(thread, throwable)
            } else {
                // In production, report to Crashlytics and crash
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        if (level >= TRIM_MEMORY_UI_HIDDEN) {
            // App UI is not shown - clear sensitive data from memory
            Timber.d("App UI hidden (level=$level) - clearing token cache")
            authStorage.clearMemoryCache()
        }
    }
}
