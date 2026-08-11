package com.example.myapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val button = Button(this).apply {
            text = "设置 5 秒后卡片提醒"
            setOnClickListener {
                val triggerTime = System.currentTimeMillis() + 5000
                CardReminder.setReminder(
                    context = this@MainActivity,
                    reminderId = 1001,
                    triggerAtMillis = triggerTime,
                    title = "卡片复习提醒",
                    content = "您设定的卡片学习时间到了，请开始复习！"
                )
                Toast.makeText(this@MainActivity, "提醒已设置！5秒后弹出", Toast.LENGTH_SHORT).show()
            }
        }
        setContentView(button)

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
    }
}
