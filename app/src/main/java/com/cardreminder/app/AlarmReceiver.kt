package com.cardreminder.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("EXTRA_TITLE") ?: "卡片到期提醒"
        val content = intent.getStringExtra("EXTRA_CONTENT") ?: "您有一张卡片即将到期，请及时处理！"
        val reminderId = intent.getIntExtra("EXTRA_REMINDER_ID", (System.currentTimeMillis() % 10000).toInt())

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "card_alarm_channel_v3"

        // 绑定系统默认闹钟铃声
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // 适配 Android 8.0+ 高优先级横幅响铃渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "卡片强提醒响铃", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "卡片到期强响铃提醒通知"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(alarmUri, null)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 构建强提醒横幅通知 (锁屏+响铃+悬浮)
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(alarmUri)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setAutoCancel(true)

        notificationManager.notify(reminderId, builder.build())
    }
}
