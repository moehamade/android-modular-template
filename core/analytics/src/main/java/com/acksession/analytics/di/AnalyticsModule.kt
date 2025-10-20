package com.acksession.analytics.di

import com.acksession.analytics.AnalyticsTracker
import com.acksession.analytics.FirebaseAnalyticsTracker
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.crashlytics
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for analytics dependencies.
 *
 * Provides:
 * - FirebaseAnalytics instance
 * - FirebaseCrashlytics instance
 * - AnalyticsTracker implementation
 */
@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {

    @Provides
    @Singleton
    fun provideFirebaseAnalytics(
    ): FirebaseAnalytics {
        return Firebase.analytics
    }

    @Provides
    @Singleton
    fun provideFirebaseCrashlytics(): FirebaseCrashlytics {
        return Firebase.crashlytics
    }
}

@Module
@InstallIn(SingletonComponent::class)
interface AnalyticsBindingModule {

    @Binds
    @Singleton
    fun bindAnalyticsTracker(
        impl: FirebaseAnalyticsTracker
    ): AnalyticsTracker
}
