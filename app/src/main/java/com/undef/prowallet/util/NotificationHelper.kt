package com.undef.prowallet.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.undef.prowallet.R

object NotificationHelper {

    private const val CHANNEL_BUDGET = "budget_alerts"
    private const val CHANNEL_REMINDERS = "spending_reminders"
    private const val BUDGET_NOTIFICATION_ID = 1001
    private const val REMINDER_NOTIFICATION_ID = 1002

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_BUDGET,
                context.getString(R.string.notification_channel_budget_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_budget_description)
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_REMINDERS,
                context.getString(R.string.notification_channel_reminder_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.notification_channel_reminder_description)
            }
        )
    }

    private fun hasPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    fun showBudgetAlert(context: Context, title: String, message: String) {
        if (!hasPermission(context)) return
        ensureChannels(context)
        val notification = NotificationCompat.Builder(context, CHANNEL_BUDGET)
            .setSmallIcon(R.drawable.ic_app_logo_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(BUDGET_NOTIFICATION_ID, notification)
    }

    fun showWeeklyReminder(context: Context, weekSpend: Double, monthSpend: Double, budget: Double) {
        if (!hasPermission(context)) return
        ensureChannels(context)
        val title = context.getString(R.string.notif_weekly_reminder_title)
        val message = context.getString(
            R.string.notif_weekly_reminder_body,
            "%.0f".format(weekSpend),
            "%.0f".format(monthSpend),
            "%.0f".format(budget)
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_app_logo_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(REMINDER_NOTIFICATION_ID, notification)
    }
}
