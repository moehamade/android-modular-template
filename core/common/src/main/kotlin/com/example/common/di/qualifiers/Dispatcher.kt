package com.example.common.di.qualifiers

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.RUNTIME

/**
 * Qualifier for injecting specific CoroutineDispatchers.
 *
 * Usage:
 * ```
 * @Inject
 * constructor(
 *     @Dispatcher(ZencastrDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
 * )
 * ```
 */
@Qualifier
@Retention(RUNTIME)
annotation class Dispatcher(val dispatcher: ZencastrDispatchers)

/**
 * Enum defining available dispatchers in the MyApp app.
 *
 * - **Default**: For CPU-bound work (e.g., data transformations, calculations)
 * - **IO**: For I/O operations (e.g., network calls, database, file operations)
 * - **Main**: For UI updates (e.g., updating Compose state)
 * - **Unconfined**: For special cases and tests (avoid in production)
 */
enum class ZencastrDispatchers {
    Default,
    IO,
    Main,
    Unconfined
}
