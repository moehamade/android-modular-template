package com.example.myapp

import android.app.Application
import com.example.analytics.AnalyticsTracker
import com.example.analytics.CrashlyticsTree
import com.example.common.di.qualifiers.ApplicationScopeIO
import com.example.datastore.preferences.TinkAuthStorage
import com.example.domain.config.BuildConfigProvider
import com.example.notifications.NotificationHandler
import com.example.remoteconfig.FeatureFlagManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Application class for MyApp.
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
class MainApplication : Application() {

    @Inject
    lateinit var authStorage: TinkAuthStorage

    @Inject
    lateinit var analytics: AnalyticsTracker

    @Inject
    lateinit var notificationHandler: NotificationHandler

    @Inject
    lateinit var featureFlagManager: FeatureFlagManager

    @Inject
    lateinit var buildConfigProvider: BuildConfigProvider

    @Inject
    @ApplicationScopeIO
    lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()

        initializeLogging()
        initializeFirebase()
        setupGlobalExceptionHandler()

        Timber.d("MyApp initialized - Environment: ${buildConfigProvider.environment}")
    }

    /**
     * Initialize logging based on build type.
     * - Debug: Log to Logcat via DebugTree
     * - Production: Log to Crashlytics via CrashlyticsTree
     */
    private fun initializeLogging() {
        if (buildConfigProvider.isDebug) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }
    }

    /**
     * Initialize Firebase services.
     * - Create notification channels
     * - Fetch remote config in background
     * - Disable data collection in debug builds
     */
    private fun initializeFirebase() {
        // Disable Firebase data collection in debug builds
        analytics.setAnalyticsCollectionEnabled(!buildConfigProvider.isDebug)

        // Create notification channels (required for Android O+)
        notificationHandler.createNotificationChannels()

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
     * - Debug builds: Crashes locally (Logcat only)
     * - Production builds: Logs to Crashlytics before crashing
     */
    private fun setupGlobalExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Timber.e(throwable, "Uncaught exception in thread: ${thread.name}")

            // Only report to Crashlytics in production builds
            if (!buildConfigProvider.isDebug) {
                analytics.logException(throwable, "Uncaught exception: ${throwable.message}")
                analytics.setCustomKey("crash_thread", thread.name)
                analytics.setCustomKey("environment", buildConfigProvider.environment)
            }

            // Crash the app (default behavior)
            defaultHandler?.uncaughtException(thread, throwable)
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
