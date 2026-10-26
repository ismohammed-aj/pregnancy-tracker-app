package com.pfe.mamacareplus.notification

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.pfe.mamacareplus.R
import com.pfe.mamacareplus.ui.home.HomeActivity

class MedicationReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getIntExtra(EXTRA_REMINDER_ID, 0)
        val medicationName = intent.getStringExtra(EXTRA_MEDICATION_NAME).orEmpty()
        if (reminderId == 0 || medicationName.isBlank()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        LocalNotificationScheduler.ensureChannel(context)
        val openAppIntent = PendingIntent.getActivity(
            context,
            reminderId,
            Intent(context, HomeActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, LocalNotificationScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_medicine_24)
            .setContentTitle("Rappel de médicament")
            .setContentText("Il est temps de prendre $medicationName")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Il est temps de prendre $medicationName"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(reminderId, notification)
    }

    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_MEDICATION_NAME = "medication_name"
    }
}
