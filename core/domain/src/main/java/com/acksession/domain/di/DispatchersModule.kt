package com.acksession.domain.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Qualifiers for different dispatcher types.
 * Using custom dispatchers makes testing easier and provides better control over threading.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UnconfinedDispatcher

/**
 * Qualifiers for application-level coroutine scopes.
 * These scopes live for the entire application lifetime and survive component cancellations.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScopeIO

/**
 * Hilt module providing coroutine infrastructure:
 * - Dispatchers: Thread pools for different work types (IO, Main, Default, Unconfined)
 * - Application Scopes: Long-lived coroutine scopes that survive component cancellations
 *
 * Usage in ViewModels/UseCases:
 * ```
 * // For dispatchers:
 * @Inject constructor(
 *     @IoDispatcher private val ioDispatcher: CoroutineDispatcher
 * )
 *
 * // For application scopes:
 * @Inject constructor(
 *     @ApplicationScopeIO private val appScope: CoroutineScope
 * )
 * ```
 */
@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {

    /**
     * IO Dispatcher for disk and network operations.
     * Optimized for blocking IO tasks.
     */
    @Provides
    @Singleton
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    /**
     * Main Dispatcher for UI updates.
     * Use this when you need to update UI elements.
     */
    @Provides
    @Singleton
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    /**
     * Default Dispatcher for CPU-intensive work.
     * Use this for heavy computations, JSON parsing, etc.
     */
    @Provides
    @Singleton
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    /**
     * Unconfined Dispatcher for tests or special cases.
     * Generally avoid using this in production code.
     */
    @Provides
    @Singleton
    @UnconfinedDispatcher
    fun provideUnconfinedDispatcher(): CoroutineDispatcher = Dispatchers.Unconfined

    /**
     * Application-level coroutine scope for I/O operations.
     *
     * Uses SupervisorJob so one failed child doesn't cancel others.
     * Use for operations that involve I/O and should continue even if
     * the originating scope (ViewModel, Activity) is cancelled:
     * - Token refresh (network)
     * - Cache persistence (disk)
     * - Analytics events (network)
     * - Background sync (network/disk)
     *
     * Example:
     * ```
     * applicationScope.launch {
     *     // This continues even if the calling ViewModel is cleared
     *     analyticsService.logEvent(event)
     * }
     * ```
     */
    @Provides
    @Singleton
    @ApplicationScopeIO
    fun provideApplicationScopeIO(
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): CoroutineScope {
        return CoroutineScope(SupervisorJob() + ioDispatcher)
    }

    /**
     * Application-level coroutine scope for CPU-intensive operations.
     *
     * Uses SupervisorJob so one failed child doesn't cancel others.
     * Use for operations that are CPU-bound and should continue even if
     * the originating scope is cancelled:
     * - Large data processing
     * - Encryption/decryption
     * - Complex calculations
     * - Heavy JSON parsing
     *
     * Example:
     * ```
     * applicationScope.launch {
     *     // This continues even if the calling ViewModel is cleared
     *     val processed = processLargeDataset(data)
     *     saveToCache(processed)
     * }
     * ```
     */
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(
        @DefaultDispatcher defaultDispatcher: CoroutineDispatcher
    ): CoroutineScope {
        return CoroutineScope(SupervisorJob() + defaultDispatcher)
    }
}
