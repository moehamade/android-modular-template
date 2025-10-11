package com.acksession.permissions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale

/**
 * Extension to check if any permissions are currently revoked (not granted).
 *
 * This is more reliable than tracking state manually because it reflects the actual
 * system permission state and works correctly across app restarts.
 *
 * @return true if any permissions are in the revokedPermissions list
 */
@OptIn(ExperimentalPermissionsApi::class)
internal fun MultiplePermissionsState.hasRevokedPermissions(): Boolean {
    return revokedPermissions.isNotEmpty()
}

/**
 * Extension to check if permissions are in a "permanently denied" state.
 *
 * **IMPORTANT:** This function returns true in TWO scenarios:
 * 1. **Never requested:** Permission hasn't been requested yet (shouldShowRationale = false on first launch)
 * 2. **Permanently denied:** User denied twice or clicked "Don't ask again"
 *
 * **Callers MUST check hasRequestedPermissions FIRST** to distinguish these cases:
 * ```kotlin
 * // CORRECT usage:
 * if (hasRequestedPermissions && isPermanentlyDenied()) {
 *     // Permission was actually permanently denied by user
 *     showSettingsDialog()
 * }
 *
 * // WRONG usage:
 * if (isPermanentlyDenied()) {
 *     // ❌ Will be true on first launch! Don't do this.
 * }
 * ```
 *
 * The Android permission system doesn't provide a direct way to distinguish between
 * "never requested" and "permanently denied" - both have shouldShowRationale = false.
 * We must track request history ourselves (via DataStore) to make this distinction.
 *
 * @return true if permission is revoked AND shouldShowRationale = false
 *         (could be never-requested OR permanently-denied - caller must distinguish)
 */
@OptIn(ExperimentalPermissionsApi::class)
internal fun MultiplePermissionsState.isPermanentlyDenied(): Boolean {
    return revokedPermissions.any { permission ->
        !permission.status.shouldShowRationale
    }
}

/**
 * Extension to open the app's settings page where users can manually grant permissions.
 */
internal fun Context.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    startActivity(intent)
}
