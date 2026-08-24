package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.util.ReminderNotificationManager
import java.util.Calendar

class JournalReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Re-schedule reminder after reboot if configured
            ReminderNotificationManager.rescheduleFromPreferences(context)
            return
        }

        // Show Reminder Notification
        showNotification(context)

        // Schedule next day's alarm
        ReminderNotificationManager.rescheduleFromPreferences(context)
    }

    private fun showNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "akalabya_daily_journal_reminder"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Journal Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily reminders to reflect and write in your Akalabya journal"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            ReminderNotificationManager.REMINDER_NOTIFICATION_ID,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val motivationalQuotes = listOf(
            "How was your day? Take a quiet moment to record your thoughts.",
            "A few words today become timeless memories tomorrow.",
            "Clear your mind. What made you pause or smile today?",
            "Your daily reflection is waiting. Pen down today's page.",
            "Take 3 minutes to unwind and reflect on what mattered today."
        )
        val selectedQuote = motivationalQuotes.random()

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setContentTitle("Daily Journal Reflection")
            .setContentText(selectedQuote)
            .setStyle(NotificationCompat.BigTextStyle().bigText(selectedQuote))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(ReminderNotificationManager.REMINDER_NOTIFICATION_ID, notification)
    }
}
