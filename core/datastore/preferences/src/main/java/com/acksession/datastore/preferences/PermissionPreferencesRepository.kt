package com.acksession.datastore.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing permission-related preferences using DataStore.
 *
 * This repository provides persistent storage for permission request state,
 * solving the issue where permission state is lost across app restarts when
 * using compose remember state.
 *
 * Use cases:
 * - Track whether a permission has been requested at least once
 * - Distinguish "first request" from "permanently denied after restart"
 * - Persist permission state across app restarts and configuration changes
 */
interface PermissionPreferencesRepository {
    /**
     * Returns a Flow indicating whether the given permission has been requested.
     *
     * This Flow emits true if the permission has been requested at least once,
     * false otherwise. The Flow is reactive and updates when the underlying
     * DataStore value changes.
     *
     * @param permission The Android permission string (e.g., android.permission.CAMERA)
     * @return Flow<Boolean> - true if permission has been requested, false otherwise
     */
    fun hasRequestedPermission(permission: String): Flow<Boolean>

    /**
     * Marks the given permission as requested.
     *
     * This should be called when the permission request dialog is shown to the user
     * for the first time. The state persists across app restarts.
     *
     * @param permission The Android permission string (e.g., android.permission.CAMERA)
     */
    suspend fun markPermissionRequested(permission: String)

    /**
     * Clears the request state for the given permission.
     *
     * Useful for testing or resetting permission flows. In production, permissions
     * should rarely need to be cleared.
     *
     * @param permission The Android permission string (e.g., android.permission.CAMERA)
     */
    suspend fun clearPermissionState(permission: String)
}

/**
 * Default implementation of PermissionPreferencesRepository using DataStore Preferences.
 *
 * This implementation is thread-safe and handles concurrent reads/writes automatically
 * thanks to DataStore's internal synchronization.
 *
 * @param dataStore The Preferences DataStore instance (injected via Hilt)
 */
@Singleton
class PermissionPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : PermissionPreferencesRepository {

    override fun hasRequestedPermission(permission: String): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[PreferencesKeys.permissionRequestKey(permission)] ?: false
        }
    }

    override suspend fun markPermissionRequested(permission: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.permissionRequestKey(permission)] = true
        }
    }

    override suspend fun clearPermissionState(permission: String) {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.permissionRequestKey(permission))
        }
    }
}
