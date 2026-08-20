package com.example.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.example.data.models.AlertCategory
import com.example.data.models.AlertPriority
import com.example.data.models.ProactiveFarmAlert

/**
 * Firebase Cloud Messaging (FCM) Architecture Integration.
 * Manages token registration, payload extraction, and Android system notification channel routing.
 */
class FarmSathiMessagingService(private val context: Context) {

    companion object {
        const val CHANNEL_CRITICAL = "farmsathi_critical_alerts"
        const val CHANNEL_ADVISORY = "farmsathi_farm_advisories"
        private const val TAG = "FarmSathiFCM"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val criticalChannel = NotificationChannel(
                CHANNEL_CRITICAL,
                "FarmSathi Critical Push Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent field, weather, and disease outbreak alerts"
                enableVibration(true)
            }

            val advisoryChannel = NotificationChannel(
                CHANNEL_ADVISORY,
                "FarmSathi Farm Advisories",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Market, irrigation, and agronomic recommendations"
            }

            notificationManager.createNotificationChannel(criticalChannel)
            notificationManager.createNotificationChannel(advisoryChannel)
        }
    }

    fun fetchFcmRegistrationToken(onTokenReceived: (String) -> Unit) {
        // FCM token simulation representing registration with backend server
        val mockFcmToken = "fcm_token_farmsathi_device_node_8832a_90"
        Log.d(TAG, "Registered FCM Device Token: $mockFcmToken")
        onTokenReceived(mockFcmToken)
    }

    fun parseFcmPayload(dataPayload: Map<String, String>): ProactiveFarmAlert? {
        return try {
            val id = dataPayload["id"] ?: "fcm_${System.currentTimeMillis()}"
            val title = dataPayload["title"] ?: return null
            val description = dataPayload["description"] ?: return null
            val categoryStr = dataPayload["category"] ?: "CRITICAL"
            val priorityStr = dataPayload["priority"] ?: "HIGH"
            val timestamp = dataPayload["timestamp"] ?: "Just now"
            val recommendedAction = dataPayload["recommendedAction"] ?: "Open FarmSathi App"
            val relatedField = dataPayload["relatedField"]
            val deepLinkRoute = dataPayload["deepLinkRoute"] ?: "notifications"

            val category = AlertCategory.values().find { it.name == categoryStr } ?: AlertCategory.CRITICAL
            val priority = AlertPriority.values().find { it.name == priorityStr } ?: AlertPriority.HIGH

            ProactiveFarmAlert(
                id = id,
                title = title,
                description = description,
                category = category,
                priority = priority,
                timestamp = timestamp,
                recommendedAction = recommendedAction,
                relatedField = relatedField,
                deepLinkRoute = deepLinkRoute,
                fcmMessageId = dataPayload["fcm_message_id"] ?: "msg_${System.currentTimeMillis()}"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing FCM payload: ${e.message}")
            null
        }
    }
}
