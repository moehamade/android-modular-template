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
 * Extension to check if permissions are permanently denied.
 *
 * A permission is considered permanently denied when:
 * - It is not granted AND
 * - The system should NOT show rationale (user denied twice)
 */
@OptIn(ExperimentalPermissionsApi::class)
internal fun MultiplePermissionsState.isPermanentlyDenied(): Boolean {
    return permissions.any { permission ->
        !permission.status.isGranted && !permission.status.shouldShowRationale
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
