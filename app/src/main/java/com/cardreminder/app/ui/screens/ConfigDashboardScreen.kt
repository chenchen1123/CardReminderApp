package com.cardreminder.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigDashboardScreen(
    onSetReminderClicked: (title: String, content: String, delaySeconds: Long) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("卡片复习提醒") }
    var content by remember { mutableStateOf("您设定的卡片学习时间到了！") }
    var delayText by remember { mutableStateOf("5") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("卡片提醒配置中心") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("提醒标题") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("提醒内容") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = delayText,
                onValueChange = { delayText = it },
                label = { Text("延迟触发时间 (秒)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val seconds = delayText.toLongOrNull() ?: 5L
                    onSetReminderClicked(title, content, seconds)
                    Toast.makeText(context, "提醒设置成功！${seconds}秒后弹出", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("启动定时提醒")
            }
        }
    }
}
