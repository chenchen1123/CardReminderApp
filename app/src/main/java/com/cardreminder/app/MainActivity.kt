package com.cardreminder.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cardreminder.app.ui.screens.ConfigDashboardScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Android 13+ 动态权限申请
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }

        setContent {
            ConfigDashboardScreen(
                onSetReminderClicked = { title, content, delaySeconds ->
                    val triggerTime = System.currentTimeMillis() + (delaySeconds * 1000)
                    CardReminder.setReminder(
                        context = this,
                        reminderId = (1000..9999).random(),
                        triggerAtMillis = triggerTime,
                        title = title,
                        content = content
                    )
                }
            )
        }
    }
}
