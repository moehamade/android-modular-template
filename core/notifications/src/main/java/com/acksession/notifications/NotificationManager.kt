package com.acksession.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

/**
 * Interface for managing notifications and FCM tokens.
 *
 * Handles:
 * - Notification channel creation (Android O+)
 * - Showing notifications
 * - FCM token management
 * - Notification permission handling (Android 13+)
 */
interface ZencastrNotificationManager {

    /**
     * Create notification channels (required for Android O+).
     * Should be called early in app lifecycle.
     */
    fun createNotificationChannels()

    /**
     * Show a notification.
     *
     * @param channelId Channel ID for the notification
     * @param notificationId Unique ID for this notification
     * @param title Notification title
     * @param message Notification message
     * @param autoCancel Whether notification should auto-dismiss when tapped
     */
    fun showNotification(
        channelId: String,
        notificationId: Int,
        title: String,
        message: String,
        autoCancel: Boolean = true
    )

    /**
     * Get the current FCM token.
     * Returns null if token is not yet available.
     */
    suspend fun getFcmToken(): String?

    /**
     * Subscribe to a FCM topic.
     *
     * @param topic Topic name to subscribe to
     */
    suspend fun subscribeToTopic(topic: String): Result<Unit>

    /**
     * Unsubscribe from a FCM topic.
     *
     * @param topic Topic name to unsubscribe from
     */
    suspend fun unsubscribeFromTopic(topic: String): Result<Unit>

    /**
     * Check if notification permission is granted (Android 13+).
     * Always returns true on Android 12 and below.
     */
    fun hasNotificationPermission(context: Context): Boolean

    /**
     * Cancel a notification by ID.
     *
     * @param notificationId The ID of the notification to cancel
     */
    fun cancelNotification(notificationId: Int)

    /**
     * Cancel all notifications.
     */
    fun cancelAllNotifications()
}

/**
 * Notification channel IDs and names.
 * Add more channels as needed for different notification types.
 */
object NotificationChannels {
    const val CHANNEL_ID_DEFAULT = "default_channel"
    const val CHANNEL_ID_RECORDING = "recording_channel"
    const val CHANNEL_ID_MESSAGES = "messages_channel"

    const val CHANNEL_NAME_DEFAULT = "General Notifications"
    const val CHANNEL_NAME_RECORDING = "Recording Notifications"
    const val CHANNEL_NAME_MESSAGES = "Message Notifications"
}
