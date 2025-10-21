package com.acksession.notifications

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.acksession.analytics.AnalyticsTracker
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ZencastrNotificationManagerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val analytics: AnalyticsTracker,
    private val firebaseMessaging: FirebaseMessaging
) : ZencastrNotificationManager {

    private val notificationManager = NotificationManagerCompat.from(context)

    override fun createNotificationChannels() {
        val channels = listOf(
            NotificationChannel(
                NotificationChannels.CHANNEL_ID_DEFAULT,
                NotificationChannels.CHANNEL_NAME_DEFAULT,
                NotificationManager.IMPORTANCE_DEFAULT
            ),
            NotificationChannel(
                NotificationChannels.CHANNEL_ID_RECORDING,
                NotificationChannels.CHANNEL_NAME_RECORDING,
                NotificationManager.IMPORTANCE_HIGH
            ),
            NotificationChannel(
                NotificationChannels.CHANNEL_ID_MESSAGES,
                NotificationChannels.CHANNEL_NAME_MESSAGES,
                NotificationManager.IMPORTANCE_HIGH
            )
        )

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        channels.forEach { manager.createNotificationChannel(it) }

        Timber.d("Notification channels created: ${channels.size}")
        analytics.logEvent("notification_channels_created", mapOf("count" to channels.size))
    }

    override fun showNotification(
        channelId: String,
        notificationId: Int,
        title: String,
        message: String,
        autoCancel: Boolean
    ) {
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: Replace with app icon
            .setAutoCancel(autoCancel)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        if (hasNotificationPermission(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions(
                    context.applicationContext as Activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    0
                )
                return
            }
            notificationManager.notify(notificationId, notification)
            Timber.d("Notification shown: $title")
        } else {
            Timber.w("Notification permission not granted")
        }
    }

    override suspend fun getFcmToken(): String? {
        return try {
            val token = firebaseMessaging.token.await()
            Timber.d("FCM token retrieved: ${token.take(10)}...")
            analytics.setCustomKey("fcm_token", token.take(10))
            token
        } catch (e: Exception) {
            Timber.e(e, "Failed to get FCM token")
            analytics.logException(e, "fcm_token_retrieval_failed")
            null
        }
    }

    override suspend fun subscribeToTopic(topic: String): Result<Unit> {
        return try {
            firebaseMessaging.subscribeToTopic(topic).await()
            Timber.d("Subscribed to topic: $topic")
            analytics.logEvent("fcm_topic_subscribed", mapOf("topic" to topic))
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to subscribe to topic: $topic")
            analytics.logException(e, "fcm_subscribe_failed_$topic")
            Result.failure(e)
        }
    }

    override suspend fun unsubscribeFromTopic(topic: String): Result<Unit> {
        return try {
            firebaseMessaging.unsubscribeFromTopic(topic).await()
            Timber.d("Unsubscribed from topic: $topic")
            analytics.logEvent("fcm_topic_unsubscribed", mapOf("topic" to topic))
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to unsubscribe from topic: $topic")
            analytics.logException(e, "fcm_unsubscribe_failed_$topic")
            Result.failure(e)
        }
    }

    override fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permission not required on Android 12 and below
        }
    }

    override fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
        Timber.d("Notification cancelled: $notificationId")
    }

    override fun cancelAllNotifications() {
        notificationManager.cancelAll()
        Timber.d("All notifications cancelled")
    }
}
