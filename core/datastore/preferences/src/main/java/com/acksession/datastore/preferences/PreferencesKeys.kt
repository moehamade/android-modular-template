package com.acksession.datastore.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey

/**
 * Centralized DataStore preference keys for the application.
 *
 * This object defines all preference keys used across the app to avoid
 * key conflicts and provide a single source of truth.
 */
object PreferencesKeys {
    /**
     * Prefix for permission request state keys.
     * Used to track whether a permission has been requested at least once.
     */
    private const val PERMISSION_REQUEST_PREFIX = "permission_requested_"

    /**
     * Creates a preference key for tracking if a permission has been requested.
     *
     * @param permission The permission string (e.g., android.permission.CAMERA)
     * @return DataStore boolean preference key
     *
     * Example:
     * ```
     * val cameraKey = permissionRequestKey("android.permission.CAMERA")
     * // Key: "permission_requested_android.permission.CAMERA"
     * ```
     */
    fun permissionRequestKey(permission: String) =
        booleanPreferencesKey("$PERMISSION_REQUEST_PREFIX$permission")
}
