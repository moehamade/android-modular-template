package com.acksession.common.di

import com.acksession.common.di.qualifiers.Dispatcher
import com.acksession.common.di.qualifiers.ZencastrDispatchers
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

/**
 * Hilt module providing CoroutineDispatchers for dependency injection.
 *
 * **Available Dispatchers:**
 * - `@Dispatcher(ZencastrDispatchers.Default)` - For CPU-bound work
 * - `@Dispatcher(ZencastrDispatchers.IO)` - For I/O operations
 * - `@Dispatcher(ZencastrDispatchers.Main)` - For UI updates
 * - `@Dispatcher(ZencastrDispatchers.Unconfined)` - For tests/special cases
 */
@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {

    /**
     * Provides Dispatchers.Default for CPU-bound work.
     *
     * Use for:
     * - Data transformations
     * - Calculations
     * - Heavy computational tasks
     * - JSON parsing
     * - Encryption/decryption
     */
    @Provides
    @Singleton
    @Dispatcher(ZencastrDispatchers.Default)
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    /**
     * Provides Dispatchers.IO for I/O operations.
     *
     * Use for:
     * - Network calls
     * - Database operations
     * - File I/O
     * - DataStore reads/writes
     */
    @Provides
    @Singleton
    @Dispatcher(ZencastrDispatchers.IO)
    fun providesIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    /**
     * Provides Dispatchers.Main for UI updates.
     *
     * Use for:
     * - Updating Compose state
     * - UI interactions
     * - Navigation
     */
    @Provides
    @Singleton
    @Dispatcher(ZencastrDispatchers.Main)
    fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    /**
     * Provides Dispatchers.Unconfined for special cases.
     *
     * Generally avoid in production - useful for tests.
     */
    @Provides
    @Singleton
    @Dispatcher(ZencastrDispatchers.Unconfined)
    fun providesUnconfinedDispatcher(): CoroutineDispatcher = Dispatchers.Unconfined
}
