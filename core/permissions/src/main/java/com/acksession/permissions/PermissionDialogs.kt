package com.acksession.permissions

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Internal composable that manages permission-related dialogs.
 * Shows either rationale or settings dialog based on permission state.
 *
 * @param showRationale Whether to show the rationale dialog
 * @param showSettings Whether to show the settings dialog
 * @param onDismissRationale Callback when rationale dialog is dismissed
 * @param onConfirmRationale Callback when user confirms rationale dialog
 * @param onDismissSettings Callback when settings dialog is dismissed
 * @param onConfirmSettings Callback when user confirms settings dialog (opens app settings)
 * @param rationaleTitle Title for the rationale dialog
 * @param rationaleMessage Message explaining why the permissions are needed
 */
@Composable
internal fun PermissionDialogs(
    showRationale: Boolean,
    showSettings: Boolean,
    onDismissRationale: () -> Unit,
    onConfirmRationale: () -> Unit,
    onDismissSettings: () -> Unit,
    onConfirmSettings: () -> Unit,
    rationaleTitle: String,
    rationaleMessage: String
) {
    val context = LocalContext.current

    // Show rationale dialog when needed (after first denial)
    if (showRationale) {
        PermissionRationaleDialog(
            title = rationaleTitle,
            message = rationaleMessage,
            onDismiss = onDismissRationale,
            onConfirm = onConfirmRationale
        )
    }

    // Show settings dialog when permanently denied (after second denial)
    if (showSettings) {
        PermissionRationaleDialog(
            title = "Permission Required",
            message = "$rationaleMessage\n\nPlease grant the required permissions in app settings.",
            onDismiss = onDismissSettings,
            onConfirm = {
                onConfirmSettings()
                context.openAppSettings()
            },
            confirmButtonText = "Open Settings"
        )
    }
}
