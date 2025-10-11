package com.acksession.datastore.preferences.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.acksession.datastore.preferences.PermissionPreferencesRepository
import com.acksession.datastore.preferences.PermissionPreferencesRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Extension property to create/access the app's Preferences DataStore.
 *
 * This uses the Kotlin property delegate pattern provided by DataStore to ensure
 * a single instance of the DataStore is created and reused throughout the app lifecycle.
 *
 * The DataStore file is named "app_preferences" and is stored in the app's data directory.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

/**
 * Hilt module providing DataStore and related dependencies.
 *
 * This module is installed in the SingletonComponent, ensuring that DataStore
 * instances are application-scoped singletons shared across all components.
 */
@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    /**
     * Provides the Preferences DataStore singleton.
     *
     * The DataStore is created using the application context to ensure it survives
     * the entire app lifecycle and is properly scoped.
     *
     * @param context Application context (injected by Hilt)
     * @return DataStore<Preferences> singleton instance
     */
    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return context.dataStore
    }
}

/**
 * Hilt module for binding repository interfaces to their implementations.
 *
 * This module uses @Binds instead of @Provides for better performance
 * (no method body needed, Hilt generates the binding code).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PreferencesRepositoryModule {

    /**
     * Binds the PermissionPreferencesRepository interface to its implementation.
     *
     * This allows components to depend on the interface while Hilt provides
     * the concrete implementation automatically.
     *
     * @param impl The concrete implementation (injected by Hilt)
     * @return The interface bound to the implementation
     */
    @Binds
    @Singleton
    abstract fun bindPermissionPreferencesRepository(
        impl: PermissionPreferencesRepositoryImpl
    ): PermissionPreferencesRepository
}
