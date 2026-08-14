package com.cardreminder.app

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.widget.Toast
import java.util.*

object CardReminder {

    /**
     * 兼容性优化的系统闹钟调起逻辑
     */
    fun setSystemAlarm(
        context: Context,
        expiryDateMillis: Long,
        remindHour: Int,
        remindMinute: Int,
        title: String,
        advanceDays: Int = 0
    ) {
        val reminderCalendar = Calendar.getInstance().apply {
            timeInMillis = expiryDateMillis
            add(Calendar.DAY_OF_MONTH, -advanceDays)
            set(Calendar.HOUR_OF_DAY, remindHour)
            set(Calendar.MINUTE, remindMinute)
            set(Calendar.SECOND, 0)
        }

        val now = Calendar.getInstance()
        if (reminderCalendar.before(now)) {
            reminderCalendar.timeInMillis = now.timeInMillis + 60000L
        }

        val hour = reminderCalendar.get(Calendar.HOUR_OF_DAY)
        val minute = reminderCalendar.get(Calendar.MINUTE)

        // 优先使用标准 SET_ALARM 意图
        val intentSetAlarm = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, "【到期提醒】$title")
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // 备用：跳转到系统闹钟列表/打开闹钟界面
        val intentShowAlarms = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intentSetAlarm)
            Toast.makeText(context, "正在为您调起系统闹钟，请确认生成！", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            try {
                context.startActivity(intentShowAlarms)
                Toast.makeText(context, "已打开系统闹钟，请手动确认提醒时间！", Toast.LENGTH_LONG).show()
            } catch (ex: Exception) {
                ex.printStackTrace()
                Toast.makeText(context, "未能唤醒系统闹钟，请检查系统权限", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
