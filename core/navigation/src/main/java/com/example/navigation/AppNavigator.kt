package com.example.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

/** Defines the contract for a type-safe navigation controller.
*
* Implementations of this interface are responsible for managing the navigation back stack
* and handling navigation actions. This allows feature modules to remain decoupled
* from the concrete navigation implementation.
*/
interface Navigator {
    /** The current navigation back stack, exposed as a read-only list. */
    val backStack: SnapshotStateList<NavKey>

    /** Initializes the navigator with a start destination. */
    fun initialize(startDestination: NavKey)

    /** Navigates to a new destination. */
    fun navigateTo(destination: NavKey)

    /** Attempts to navigate back, returning true on success. */
    fun navigateBack(): Boolean

    /** Replaces the current destination with a new one. */
    fun replaceCurrent(destination: NavKey)

    /** Clears the back stack and navigates to a new root. */
    fun clearAndNavigateTo(destination: NavKey)
}




/**
 * Activity-scoped navigator that manages the navigation back stack.
 * Features define their own route sealed interfaces and provide extension functions for type-safe navigation.
 */
@ActivityRetainedScoped
internal class AppNavigator @Inject constructor() : Navigator {

    private val _backStack: SnapshotStateList<NavKey> = mutableStateListOf()

    /**
     * The current navigation back stack.
     * Exposed as a read-only list to prevent external modifications.
     */
    override val backStack: SnapshotStateList<NavKey>
        get() = _backStack

    /**
     * Initialize the navigator with a start destination.
     * Should be called once when the activity is created.
     *
     * @param startDestination The initial navigation destination (must be a NavKey)
     */
    override fun initialize(startDestination: NavKey) {
        if (_backStack.isEmpty()) {
            _backStack.add(startDestination)
        }
    }

    /**
     * Navigate to a new destination by adding it to the back stack.
     *
     * This method is public but features should provide typed extension functions
     * for better discoverability and documentation.
     *
     * @param destination The navigation destination (must be a NavKey)
     */
    override fun navigateTo(destination: NavKey) {
        _backStack.add(destination)
    }

    /**
     * Navigate back by removing the current destination from the back stack.
     * Returns true if navigation was successful, false if already at root.
     */
    override fun navigateBack(): Boolean {
        return if (_backStack.size > 1) {
            _backStack.removeLastOrNull()
            true
        } else {
            false
        }
    }

    /**
     * Replace the current destination with a new one.
     * Useful for login flows or replacing screens without adding to back stack.
     *
     * @param destination The new destination to replace the current one
     */
    override fun replaceCurrent(destination: NavKey) {
        if (_backStack.isNotEmpty()) {
            _backStack.removeLastOrNull()
        }
        _backStack.add(destination)
    }

    /**
     * Clear the entire back stack and navigate to a new root destination.
     * Useful for logout flows or resetting navigation state.
     *
     * @param destination The new root destination
     */
    override fun clearAndNavigateTo(destination: NavKey) {
        _backStack.clear()
        _backStack.add(destination)
    }
}

/**
 * Hilt module for providing navigation-related dependencies.
 *
 * This module is installed in the [ActivityRetainedComponent], meaning that the provided
 * [Navigator] instance will be scoped to the lifecycle of the activity, surviving
 * configuration changes.
 *
 * It uses `@Binds` to provide the concrete `AppNavigator` implementation whenever
 * the `Navigator` interface is requested, promoting dependency inversion.
 */
@Suppress("unused")
@Module
@InstallIn(ActivityRetainedComponent::class)
internal abstract class NavigationModule {

    @Binds
    abstract fun bindNavigator(
        appNavigator: AppNavigator
    ): Navigator
}
