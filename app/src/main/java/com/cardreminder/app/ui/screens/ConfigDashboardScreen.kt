package com.cardreminder.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 色彩定义
private val DarkBg = Color(0xFF0D0D0D)
private val GroupCardBg = Color(0xFF1C1C1E)
private val InnerCardBg = Color(0xFF2C2C2E)
private val PrimaryText = Color(0xFFFFFFFF)
private val SecondaryText = Color(0xFF8E8E93)
private val BadgeBg = Color(0xFF3A3A3C)

// 数据模型
data class ReminderItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val category: String, // 身份与证件, 网络环境, 支付, 账号, AI服务
    val delaySeconds: Int = 5
)

@Composable
fun ConfigDashboardScreen() {
    val context = LocalContext.current

    // 动态存储已添加的提醒事项列表
    val reminderList = remember {
        mutableStateListOf(
            ReminderItem(title = "护照", category = "身份与证件", delaySeconds = 0),
            ReminderItem(title = "纯净 IP", category = "网络环境", delaySeconds = 10),
            ReminderItem(title = "Shadowrocket", category = "网络环境", delaySeconds = 0),
            ReminderItem(title = "Bitget Card", category = "支付", delaySeconds = 5)
        )
    }

    // 控制弹窗状态
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBg,
        bottomBar = { CustomBottomNavigationBar() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            HeaderSection(onAddClick = { showAddDialog = true })
            Spacer(modifier = Modifier.height(20.dp))

            // 1. 身份与证件
            val identityItems = reminderList.filter { it.category == "身份与证件" }
            CategoryGroupCard(title = "身份与证件", count = identityItems.size, icon = Icons.Default.Badge, iconTint = Color(0xFF5E5CE6)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    identityItems.forEach { item ->
                        SingleCardItem(title = item.title) {
                            Toast.makeText(context, "点击了：${item.title}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. 网络环境
            val networkItems = reminderList.filter { it.category == "网络环境" }
            CategoryGroupCard(title = "网络环境", count = networkItems.size, icon = Icons.Default.Language, iconTint = Color(0xFF64D2FF)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    networkItems.forEach { item ->
                        Box(modifier = Modifier.weight(1f)) {
                            InfoCardItem(title = item.title, detail = if (item.delaySeconds > 0) "${item.delaySeconds}秒后提醒" else "未设置提醒") {
                                Toast.makeText(context, "开始处理：${item.title}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. 支付
            val payItems = reminderList.filter { it.category == "支付" }
            CategoryGroupCard(title = "支付", count = payItems.size, icon = Icons.Default.CreditCard, iconTint = Color(0xFF30D158)) {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    payItems.forEach { item ->
                        BankCardItem(title = item.title, cardColor = Color(0xFF0052FF)) {
                            Toast.makeText(context, "触发提醒: ${item.title} (${item.delaySeconds}s)", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // 点击右上角 + 弹出的添加卡片对话框
        if (showAddDialog) {
            AddReminderDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { title, category, seconds ->
                    reminderList.add(ReminderItem(title = title, category = category, delaySeconds = seconds))
                    showAddDialog = false
                    Toast.makeText(context, "添加成功！", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
private fun HeaderSection(onAddClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("我的海外配置", color = PrimaryText, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("点击加号可添加提醒", color = SecondaryText, fontSize = 14.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // 右上角的加号按钮，点击触发添加弹窗
            IconButton(onClick = onAddClick, modifier = Modifier.clip(CircleShape).background(BadgeBg)) {
                Icon(Icons.Default.Add, contentDescription = "添加", tint = PrimaryText)
            }
        }
    }
}

@Composable
private fun CategoryGroupCard(
    title: String,
    count: Int,
    icon: ImageVector,
    iconTint: Color,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GroupCardBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, color = PrimaryText, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier.clip(CircleShape).background(BadgeBg).padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(count.toString(), color = SecondaryText, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun SingleCardItem(title: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = InnerCardBg),
        modifier = Modifier.width(110.dp).height(90.dp).clickable { onClick() }
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.BottomStart) {
            Text(title, color = PrimaryText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun InfoCardItem(title: String, detail: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = InnerCardBg),
        modifier = Modifier.fillMaxWidth().height(90.dp).clickable { onClick() }
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(title, color = PrimaryText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(detail, color = Color(0xFF30D158), fontSize = 12.sp)
        }
    }
}

@Composable
private fun BankCardItem(title: String, cardColor: Color, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        modifier = Modifier.width(140.dp).height(85.dp).clickable { onClick() }
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(10.dp), contentAlignment = Alignment.BottomStart) {
            Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// “添加卡片/提醒” 弹窗界面
@Composable
private fun AddReminderDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, seconds: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("支付") }
    var delaySeconds by remember { mutableStateOf("5") }

    val categories = listOf("身份与证件", "网络环境", "支付")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加新提醒卡片") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("卡片名称") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = delaySeconds,
                    onValueChange = { delaySeconds = it },
                    label = { Text("提醒延迟时间（秒）") },
                    singleLine = true
                )
                Text("选择分类：", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, selectedCategory, delaySeconds.toIntOrNull() ?: 0)
                    }
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
private fun CustomBottomNavigationBar() {
    NavigationBar(containerColor = Color(0xFF161618)) {
        NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.Home, null) }, label = { Text("首页") })
        NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Default.GridView, null) }, label = { Text("配置") })
        NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.Explore, null) }, label = { Text("市场") })
        NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.Person, null) }, label = { Text("我的") })
    }
}
