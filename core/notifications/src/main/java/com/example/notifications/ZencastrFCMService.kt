package com.example.notifications

import com.example.analytics.AnalyticsTracker
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * Firebase Cloud Messaging service.
 *
 * Handles:
 * - New FCM token generation
 * - Receiving push notifications
 * - Notification routing
 *
 * Note: This service runs in a separate process and must be registered in AndroidManifest.
 */
@AndroidEntryPoint
class ZencastrFCMService : FirebaseMessagingService() {

    @Inject
    lateinit var analytics: AnalyticsTracker

    @Inject
    lateinit var notificationHandler: NotificationHandler

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("New FCM token: ${token.take(10)}...")

        analytics.setCustomKey("fcm_token", token.take(10))
        analytics.logEvent("fcm_token_refreshed", null)

        // TODO: Send token to backend server
        // Example: uploadTokenToServer(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Timber.d("FCM message received from: ${message.from}")
        analytics.logEvent("fcm_message_received", mapOf(
            "from" to (message.from ?: "unknown"),
            "has_notification" to (message.notification != null),
            "has_data" to message.data.isNotEmpty()
        ))

        // Handle notification payload
        message.notification?.let { notification ->
            val title = notification.title ?: "MyApp"
            val body = notification.body ?: ""

            notificationHandler.showNotification(
                channelId = NotificationChannels.CHANNEL_ID_DEFAULT,
                notificationId = System.currentTimeMillis().toInt(),
                title = title,
                message = body
            )
        }

        // Handle data payload
        if (message.data.isNotEmpty()) {
            handleDataPayload(message.data)
        }
    }

    private fun handleDataPayload(data: Map<String, String>) {
        Timber.d("FCM data payload: $data")

        // TODO: Route notification based on data payload
        // Example routing:
        // when (data["type"]) {
        //     "recording_started" -> handleRecordingNotification(data)
        //     "message" -> handleMessageNotification(data)
        //     else -> handleGenericNotification(data)
        // }
    }
}
