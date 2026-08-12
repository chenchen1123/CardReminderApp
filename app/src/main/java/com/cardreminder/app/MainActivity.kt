package com.cardreminder.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- 数据模型 ---
data class CardModel(
    val id: String = java.util.UUID.randomUUID().toString(),
    var category: String,       // 分类
    var name: String,           // 卡名
    var cardNumber: String,     // 卡号
    var balance: String,        // 余额
    var expiryDate: String,     // 到期日
    var reminderCycle: String   // 提醒周期
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                MainContainerScreen()
            }
        }
    }
}

@Composable
fun MainContainerScreen() {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: 首页, 1: 列表, 2: 新增, 3: 我的

    // 全局数据源
    val cardList = remember {
        mutableStateListOf(
            CardModel(category = "银行卡", name = "招商银行", cardNumber = "6214 **** 8888", balance = "12500.00", expiryDate = "2026-12-31", reminderCycle = "到期前3天"),
            CardModel(category = "银行卡", name = "工商银行", cardNumber = "6222 **** 1234", balance = "3200.50", expiryDate = "2027-05-20", reminderCycle = "到期前7天"),
            CardModel(category = "电子卡", name = "Bitget Card", cardNumber = "4514 **** 9910", balance = "500.00", expiryDate = "2026-09-15", reminderCycle = "每周"),
            CardModel(category = "证件", name = "居民身份证", cardNumber = "1101 **** 001X", balance = "0.00", expiryDate = "2030-01-01", reminderCycle = "到期前30天")
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "首页") },
                    label = { Text("首页") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.List, contentDescription = "列表") },
                    label = { Text("列表") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = "新增") },
                    label = { Text("新增") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "我的") },
                    label = { Text("我的") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> HomeScreen(cardList)
                1 -> ListScreen(cardList)
                2 -> AddScreen(onSave = { newCard ->
                    cardList.add(newCard)
                    selectedTab = 1 // 保存后自动跳到列表页
                })
                3 -> ProfileScreen()
            }
        }
    }
}

// 1. 首页 (卡片按类型汇总 + 横向滑动)
@Composable
fun HomeScreen(cardList: List<CardModel>) {
    val groupedCards = cardList.groupBy { it.category }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("首页 - 分类概览", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (groupedCards.isEmpty()) {
            Text("暂无卡片数据，请前往新增页面添加", color = Color.Gray)
        } else {
            groupedCards.forEach { (category, cards) ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = category, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Badge { Text("${cards.size} 张") }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(cards) { card ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                    modifier = Modifier
                                        .width(160.dp)
                                        .height(100.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(card.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(card.cardNumber, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                                        Text("到期: ${card.expiryDate}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 2. 列表页 (筛选/排序/长按编辑)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListScreen(cardList: MutableList<CardModel>) {
    val context = LocalContext.current
    var filterCategory by remember { mutableStateOf("全部") }
    var sortByExpiry by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var editingCard by remember { mutableStateOf<CardModel?>(null) }

    val categories = listOf("全部") + cardList.map { it.category }.distinct()
    val displayedCards = cardList
        .filter { filterCategory == "全部" || it.category == filterCategory }
        .let { list -> if (sortByExpiry) list.sortedBy { it.expiryDate } else list }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("卡片列表", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.FilterList, contentDescription = "筛选")
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text(if (sortByExpiry) "恢复默认排序" else "按到期日期排序") },
                        onClick = { sortByExpiry = !sortByExpiry; showMenu = false }
                    )
                    HorizontalDivider()
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text("分类: $cat") },
                            onClick = { filterCategory = cat; showMenu = false }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(displayedCards, key = { it.id }) { card ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { Toast.makeText(context, "长按卡片可修改信息", Toast.LENGTH_SHORT).show() },
                            onLongClick = { editingCard = card }
                        )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(card.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            AssistChip(onClick = {}, label = { Text(card.category) })
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("卡号: ${card.cardNumber}", fontSize = 14.sp)
                        Text("到期日: ${card.expiryDate}", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                        Text("余额: ¥${card.balance}  |  提醒: ${card.reminderCycle}", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }

    editingCard?.let { card ->
        var name by remember { mutableStateOf(card.name) }
        var number by remember { mutableStateOf(card.cardNumber) }
        var expiry by remember { mutableStateOf(card.expiryDate) }

        AlertDialog(
            onDismissRequest = { editingCard = null },
            title = { Text("编辑卡片信息") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("卡名") })
                    OutlinedTextField(value = number, onValueChange = { number = it }, label = { Text("卡号") })
                    OutlinedTextField(value = expiry, onValueChange = { expiry = it }, label = { Text("到期日") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    card.name = name
                    card.cardNumber = number
                    card.expiryDate = expiry
                    editingCard = null
                    Toast.makeText(context, "更新成功", Toast.LENGTH_SHORT).show()
                }) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { editingCard = null }) { Text("取消") }
            }
        )
    }
}

// 3. 新增页面
@Composable
fun AddScreen(onSave: (CardModel) -> Unit) {
    val context = LocalContext.current
    var category by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var reminderCycle by remember { mutableStateOf("到期前3天") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("新增卡片", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("分类 (如: 银行卡/电子卡)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("卡名") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = cardNumber, onValueChange = { cardNumber = it }, label = { Text("卡号") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = balance, onValueChange = { balance = it }, label = { Text("余额") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = expiryDate, onValueChange = { expiryDate = it }, label = { Text("到期日 (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = reminderCycle, onValueChange = { reminderCycle = it }, label = { Text("提醒周期") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (name.isBlank() || category.isBlank()) {
                    Toast.makeText(context, "请填写分类和卡名", Toast.LENGTH_SHORT).show()
                } else {
                    onSave(
                        CardModel(
                            category = category,
                            name = name,
                            cardNumber = cardNumber,
                            balance = balance.ifBlank { "0.00" },
                            expiryDate = expiryDate.ifBlank { "2026-12-31" },
                            reminderCycle = reminderCycle
                        )
                    )
                    Toast.makeText(context, "卡片保存成功", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("保存卡片", fontSize = 16.sp)
        }
    }
}

// 4. 我的页面 (个人信息与导出)
@Composable
fun ProfileScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("个人中心", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("昵称", color = Color.Gray)
                    Text("Card Master", fontWeight = FontWeight.Bold)
                }
                HorizontalDivider()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("邮箱", color = Color.Gray)
                    Text("user@example.com")
                }
                HorizontalDivider()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("电话", color = Color.Gray)
                    Text("+86 138 0000 0000")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { Toast.makeText(context, "准备从 Excel 导入数据...", Toast.LENGTH_SHORT).show() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.FileDownload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("导入数据 (Excel)")
        }

        OutlinedButton(
            onClick = { Toast.makeText(context, "数据已成功导出为 Excel 文件！", Toast.LENGTH_SHORT).show() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.FileUpload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("导出数据 (Excel)")
        }
    }
}
