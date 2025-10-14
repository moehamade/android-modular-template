package com.acksession.domain.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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
 * Hilt module providing custom coroutine dispatchers.
 *
 * Usage in ViewModels/UseCases:
 * ```
 * @Inject constructor(
 *     @IoDispatcher private val ioDispatcher: CoroutineDispatcher
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
}

