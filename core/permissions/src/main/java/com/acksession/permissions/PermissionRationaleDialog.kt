package com.acksession.permissions

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * A dialog that explains why permissions are needed (rationale).
 *
 * @param title Dialog title
 * @param message Dialog message explaining why the permissions are needed
 * @param onDismiss Callback when the user dismisses the dialog
 * @param onConfirm Callback when the user confirms and wants to grant permissions
 * @param dismissButtonText Text for the dismiss button
 * @param confirmButtonText Text for the confirm button
 */
@Composable
fun PermissionRationaleDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    dismissButtonText: String = "Cancel",
    confirmButtonText: String = "Grant Permission"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title)
        },
        text = {
            Text(text = message)
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = dismissButtonText)
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = confirmButtonText)
            }
        }
    )
}
