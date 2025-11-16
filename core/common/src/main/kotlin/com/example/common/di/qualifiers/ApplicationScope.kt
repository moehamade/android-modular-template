package com.example.common.di.qualifiers

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.RUNTIME

/**
 * Qualifier for application-scoped CoroutineScope using Dispatchers.Default.
 *
 * Use for general-purpose background work that is not I/O bound.
 *
 * Usage:
 * ```
 * @Inject
 * constructor(
 *     @ApplicationScope private val appScope: CoroutineScope
 * )
 * ```
 */
@Qualifier
@Retention(RUNTIME)
annotation class ApplicationScope
