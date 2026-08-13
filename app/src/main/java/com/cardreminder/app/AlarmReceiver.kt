package com.cardreminder.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        const val TAG = "ALARM_DEBUG"
        const val CHANNEL_ID = "card_reminder_channel_v2"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "AlarmReceiver 收到闹钟广播")

        val title = intent.getStringExtra("EXTRA_TITLE") ?: "卡片提醒"
        val content = intent.getStringExtra("EXTRA_CONTENT") ?: "您有一条待处理的卡片到期提醒！"
        val reminderId = intent.getIntExtra("EXTRA_REMINDER_ID", 0)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "卡片强响铃提醒通知",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "用于卡片到期高优先响铃提醒"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(alarmSound, null)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_reminder) // 务必替换为你自己项目内的mipmap图标
            .setContentTitle(title)
            .setContentText(content)
            .setSound(alarmSound)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(reminderId, notification)
    }
}
