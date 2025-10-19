package com.acksession.zencastr

import android.app.Application
import android.content.ComponentCallbacks2
import com.acksession.datastore.preferences.TinkAuthStorage
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

/**
 * Application class for Zencastr.
 *
 * @HiltAndroidApp triggers Hilt's code generation including a base class for
 * the application that serves as the application-level dependency container.
 */
@HiltAndroidApp
class ZencastrApplication : Application() {

    @Inject
    lateinit var authStorage: TinkAuthStorage

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            // TODO: Add Crashlytics tree or custom production logging
            // Timber.plant(CrashlyticsTree())
        }

        Timber.d("ZencastrApplication initialized")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        if (level >= TRIM_MEMORY_UI_HIDDEN) {
            // App ui is not shown - clear sensitive data from memory
            Timber.d("App UI is not shown (level=$level) - clearing token cache")
            authStorage.clearMemoryCache()
        }
    }
}
