package com.acksession.zencastr

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Application class for Zencastr.
 *
 * @HiltAndroidApp triggers Hilt's code generation including a base class for
 * the application that serves as the application-level dependency container.
 */
@HiltAndroidApp
class ZencastrApplication : Application() {

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
}

