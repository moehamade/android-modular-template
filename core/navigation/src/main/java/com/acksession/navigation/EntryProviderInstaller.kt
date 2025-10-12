package com.acksession.navigation

import androidx.navigation3.runtime.EntryProviderScope

/**
 * Type alias for entry provider installers used in modular navigation.
 *
 * Each feature module provides an implementation that installs its navigation entries
 * into the main app's entry provider. This allows features to self-register their
 * navigation destinations without the app module needing to know about them.
 *
 * Example usage in a feature module:
 * ```
 * @Module
 * @InstallIn(ActivityRetainedComponent::class)
 * object FeatureNavigationModule {
 *     @IntoSet
 *     @Provides
 *     fun provideEntryProvider(navigator: Navigator): EntryProviderInstaller = {
 *         entry<MyRoute> { key ->
 *             MyScreen(navigator = navigator)
 *         }
 *     }
 * }
 * ```
 */
typealias EntryProviderInstaller = EntryProviderScope<Any>.() -> Unit
