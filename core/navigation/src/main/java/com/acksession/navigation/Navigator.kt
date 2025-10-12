package com.acksession.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

/**
 * Activity-scoped navigator that manages the navigation back stack.
 *
 * This class provides methods to navigate between destinations in a type-safe manner.
 * Each destination is represented as a route object (data class or object).
 */
@ActivityRetainedScoped
class Navigator @Inject constructor() {

    private val _backStack: SnapshotStateList<Any> = mutableStateListOf()

    /**
     * The current navigation back stack.
     * Exposed as a read-only list to prevent external modifications.
     */
    val backStack: SnapshotStateList<Any>
        get() = _backStack

    /**
     * Initialize the navigator with a start destination.
     * Should be called once when the activity is created.
     */
    fun initialize(startDestination: Any) {
        if (_backStack.isEmpty()) {
            _backStack.add(startDestination)
        }
    }

    /**
     * Navigate to a new destination by adding it to the back stack.
     */
    fun navigateTo(destination: Any) {
        _backStack.add(destination)
    }

    /**
     * Navigate back by removing the current destination from the back stack.
     * Returns true if navigation was successful, false if already at root.
     */
    fun navigateBack(): Boolean {
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
     */
    fun replaceCurrent(destination: Any) {
        if (_backStack.isNotEmpty()) {
            _backStack.removeLastOrNull()
        }
        _backStack.add(destination)
    }

    /**
     * Clear the entire back stack and navigate to a new root destination.
     * Useful for logout flows or resetting navigation state.
     */
    fun clearAndNavigateTo(destination: Any) {
        _backStack.clear()
        _backStack.add(destination)
    }
}
