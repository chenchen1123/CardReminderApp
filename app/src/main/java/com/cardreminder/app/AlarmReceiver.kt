package com.cardreminder.app

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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

        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "卡片强提醒响铃", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "卡片到期强响铃提醒通知"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(alarmUri, null)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val contentIntent = PendingIntent.getActivity(
            context,
            reminderId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(alarmUri)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)

        notificationManager.notify(reminderId, builder.build())
    }
}

object NotificationScheduler {
    fun scheduleCardReminders(context: Context, cardTitle: String, cardId: String, expiryDateMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val now = System.currentTimeMillis()

        val triggerOffsets = listOf(
            3 * 86400000L to "还有 3 天即将在到期，请注意及时处理！",
            1 * 86400000L to "将在明天到期，请尽快处理！",
            0L to "今天已经到期，请及时处理！"
        )

        triggerOffsets.forEachIndexed { index, (offset, msg) ->
            val triggerTime = expiryDateMillis - offset
            if (triggerTime > now) {
                val intent = Intent(context, AlarmReceiver::class.java).apply {
                    putExtra("EXTRA_TITLE", "【卡片到期提醒】$cardTitle")
                    putExtra("EXTRA_CONTENT", msg)
                    putExtra("EXTRA_REMINDER_ID", (cardId.hashCode() + index))
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    cardId.hashCode() + index,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    } else {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    }
                } catch (_: Exception) {
                    try {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    } catch (_: Exception) {}
                }
            }
        }
    }
}
