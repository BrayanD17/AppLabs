package com.labs.applabs.student

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.labs.applabs.R

class FirebaseMessagingService : FirebaseMessagingService() {
    //Receive a push message of Firebase Cloud Messaging
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.data["subject"]
            ?: remoteMessage.notification?.title
            ?: "Nuevo mensaje"

        val body = remoteMessage.data["message"]
            ?: remoteMessage.notification?.body
            ?: "Tienes una nueva notificación"

        showNotification(title, body)
    }

    //Created and show notification
    private fun showNotification(title: String, body: String) {
        val channelId = "default_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //Channel notification with priority high
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Canal de mensajes",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.notification_icon)
            .setAutoCancel(true)

        notificationManager.notify(0, notificationBuilder.build())
    }
}
