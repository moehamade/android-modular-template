package com.acksession.common.di.qualifiers

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.RUNTIME

/**
 * Qualifier for application-scoped CoroutineScope using Dispatchers.IO.
 *
 * Use for I/O-bound operations like DataStore writes, database operations,
 * file operations, or network calls that should persist across the app lifecycle.
 *
 * Usage:
 * ```
 * @Inject
 * constructor(
 *     @ApplicationScopeIO private val ioScope: CoroutineScope
 * )
 * ```
 */
@Qualifier
@Retention(RUNTIME)
annotation class ApplicationScopeIO
