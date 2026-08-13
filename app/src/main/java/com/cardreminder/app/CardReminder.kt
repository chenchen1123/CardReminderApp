package com.cardreminder.app

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.widget.Toast
import java.util.*

object CardReminder {

    /**
     * 调用 Android 原生系统闹钟 App 生成对应的强响铃闹钟
     */
    fun setSystemAlarm(
        context: Context,
        expiryDateMillis: Long,
        remindHour: Int,
        remindMinute: Int,
        title: String,
        advanceDays: Int = 0
    ) {
        try {
            // 计算具体的提醒日期 (扣除提前提醒的天数)
            val reminderCalendar = Calendar.getInstance().apply {
                timeInMillis = expiryDateMillis
                add(Calendar.DAY_OF_MONTH, -advanceDays)
                set(Calendar.HOUR_OF_DAY, remindHour)
                set(Calendar.MINUTE, remindMinute)
                set(Calendar.SECOND, 0)
            }

            // 如果设置的到期提醒时间比当前早，自动顺延到明天或提示
            val now = Calendar.getInstance()
            if (reminderCalendar.before(now)) {
                reminderCalendar.timeInMillis = now.timeInMillis + 60000L // 默认设为 1 分钟后
            }

            val hour = reminderCalendar.get(Calendar.HOUR_OF_DAY)
            val minute = reminderCalendar.get(Calendar.MINUTE)

            // 调起系统闹钟 Intent
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, "【卡片到期提醒】$title")
                putExtra(AlarmClock.EXTRA_SKIP_UI, false) // 设为 false 让用户在系统闹钟里看到并确认
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
            Toast.makeText(context, "正在为您调起系统闹钟，请确认生成！", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "未找到系统闹钟应用，请检查设置！", Toast.LENGTH_SHORT).show()
        }
    }
}
