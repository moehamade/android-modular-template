package com.acksession.common.di

import com.acksession.common.di.qualifiers.ApplicationScope
import com.acksession.common.di.qualifiers.ApplicationScopeIO
import com.acksession.common.di.qualifiers.Dispatcher
import com.acksession.common.di.qualifiers.ZencastrDispatchers
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * Hilt module providing application-scoped CoroutineScopes.
 *
 * **Available Scopes:**
 * - `@ApplicationScope` - General-purpose scope using Dispatchers.Default
 * - `@ApplicationScopeIO` - I/O-bound scope using Dispatchers.IO
 *
 * Both scopes use SupervisorJob to ensure a failure in one child doesn't
 * cancel sibling coroutines.
 */
@Module
@InstallIn(SingletonComponent::class)
object CoroutineScopesModule {

    /**
     * Application-scoped CoroutineScope using Dispatchers.Default.
     *
     * Use for:
     * - General background work
     * - CPU-bound operations that persist across the app lifecycle
     * - Non-I/O tasks
     */
    @Provides
    @Singleton
    @ApplicationScope
    fun providesApplicationScope(
        @Dispatcher(ZencastrDispatchers.Default) defaultDispatcher: CoroutineDispatcher
    ): CoroutineScope = CoroutineScope(SupervisorJob() + defaultDispatcher)

    /**
     * Application-scoped CoroutineScope using Dispatchers.IO.
     *
     * Use for:
     * - DataStore writes (TinkAuthStorage)
     * - Database operations
     * - File operations
     * - Token refresh (network)
     * - Cache persistence (disk)
     * - Analytics events (network)
     * - Any I/O that needs to persist across app lifecycle
     */
    @Provides
    @Singleton
    @ApplicationScopeIO
    fun providesApplicationScopeIO(
        @Dispatcher(ZencastrDispatchers.IO) ioDispatcher: CoroutineDispatcher
    ): CoroutineScope = CoroutineScope(SupervisorJob() + ioDispatcher)
}
