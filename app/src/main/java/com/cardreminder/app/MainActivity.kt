package com.cardreminder.app

import android.os.Bundle
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Collections

// --- 数据模型 ---
data class CardModel(
    val id: String = java.util.UUID.randomUUID().toString(),
    var category: String,           // 银行卡, 电话卡, 邮箱, 账号, 其他
    var name: String,               // 卡名
    var cardNumber: String,         // 卡号
    var balance: String,            // 余额
    var expiryDate: String,         // 到期日 (YYYY-MM-DD)
    var advanceDays: String,        // 提前提醒天数
    var remindType: String,         // 提醒方式 (当天提醒 / 重复提醒)
    var repeatIntervalDays: String, // 周期间隔天数
    var isPinned: Boolean = false   // 是否置顶
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

    val cardList = remember {
        mutableStateListOf(
            CardModel(category = "银行卡", name = "招商银行", cardNumber = "6214 **** 8888", balance = "12500.00", expiryDate = "2026-12-31", advanceDays = "提前3天", remindType = "重复提醒", repeatIntervalDays = "30天", isPinned = true),
            CardModel(category = "电话卡", name = "中国移动", cardNumber = "138 **** 8888", balance = "50.00", expiryDate = "2027-05-20", advanceDays = "当天提醒", remindType = "当天提醒", repeatIntervalDays = "0天"),
            CardModel(category = "账号", name = "Bitget Account", cardNumber = "4514 **** 9910", balance = "500.00", expiryDate = "2026-09-15", advanceDays = "提前1天", remindType = "重复提醒", repeatIntervalDays = "7天"),
            CardModel(category = "其他", name = "身份证", cardNumber = "1101 **** 001X", balance = "0.00", expiryDate = "2030-01-01", advanceDays = "提前7天", remindType = "当天提醒", repeatIntervalDays = "0天")
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
                    selectedTab = 1
                })
                3 -> ProfileScreen()
            }
        }
    }
}

// ================= 1. 首页 (HomeScreen) =================
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
                            items(cards.size) { index ->
                                val card = cards[index]
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                    modifier = Modifier
                                        .width(160.dp)
                                        .height(110.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(card.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            if (card.isPinned) {
                                                Icon(Icons.Default.PushPin, contentDescription = "置顶", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                            }
                                        }
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

// ================= 2. 列表页面 (ListScreen) =================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(cardList: MutableList<CardModel>) {
    val context = LocalContext.current
    var filterCategory by remember { mutableStateOf("全部") }
    var sortOrder by remember { mutableStateOf("DEFAULT") } // DEFAULT, ASC, DESC
    var showMenu by remember { mutableStateOf(false) }
    var editingCard by remember { mutableStateOf<CardModel?>(null) }

    val presetCategories = listOf("全部", "银行卡", "电话卡", "邮箱", "账号", "其他")

    // 计算排序与筛选列表（置顶项目优先）
    val displayedCards = cardList
        .filter { filterCategory == "全部" || it.category == filterCategory }
        .sortedWith(Comparator { c1, c2 ->
            if (c1.isPinned != c2.isPinned) {
                return@Comparator if (c1.isPinned) -1 else 1
            }
            when (sortOrder) {
                "ASC" -> c1.expiryDate.compareTo(c2.expiryDate)
                "DESC" -> c2.expiryDate.compareTo(c1.expiryDate)
                else -> 0
            }
        })

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("卡片列表", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.FilterList, contentDescription = "筛选排序")
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("默认排序") },
                        trailingIcon = { if (sortOrder == "DEFAULT") Icon(Icons.Default.Check, null) },
                        onClick = { sortOrder = "DEFAULT"; showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("按到期日期升序") },
                        trailingIcon = { if (sortOrder == "ASC") Icon(Icons.Default.Check, null) },
                        onClick = { sortOrder = "ASC"; showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("按到期日期降序") },
                        trailingIcon = { if (sortOrder == "DESC") Icon(Icons.Default.Check, null) },
                        onClick = { sortOrder = "DESC"; showMenu = false }
                    )
                    HorizontalDivider()
                    presetCategories.forEach { cat ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "分类: $cat",
                                    color = if (filterCategory == cat) MaterialTheme.colorScheme.primary else Color.Unspecified,
                                    fontWeight = if (filterCategory == cat) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            trailingIcon = {
                                if (filterCategory == cat) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            onClick = { filterCategory = cat; showMenu = false }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            itemsIndexed(displayedCards, key = { _, item -> item.id }) { index, card ->
                var offsetY by remember { mutableFloatStateOf(0f) }

                // 支持滑动弹窗编辑/删除
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { dismissValue ->
                        if (dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                            editingCard = card // 往右滑动触发编辑
                            false
                        } else if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                            cardList.remove(card) // 往左滑动触发删除
                            Toast.makeText(context, "已删除卡片", Toast.LENGTH_SHORT).show()
                            true
                        } else false
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        val isRightToLeft = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
                        val bgColor = if (isRightToLeft) Color(0xFFE53935) else Color(0xFF1E88E5)
                        val icon = if (isRightToLeft) Icons.Default.Delete else Icons.Default.Edit
                        val text = if (isRightToLeft) "删除" else "编辑"

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(bgColor, shape = RoundedCornerShape(12.dp))
                                .padding(horizontal = 20.dp),
                            contentAlignment = if (isRightToLeft) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(icon, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                ) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { translationY = offsetY }
                            .pointerInput(Unit) {
                                detectDragGesturesAfterLongPress(
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        offsetY += dragAmount.y
                                    },
                                    onDragEnd = {
                                        val targetIndex = (index + (offsetY / 200).toInt()).coerceIn(0, cardList.size - 1)
                                        if (targetIndex != index) {
                                            Collections.swap(cardList, index, targetIndex)
                                        }
                                        offsetY = 0f
                                    }
                                )
                            }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(card.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AssistChip(onClick = {}, label = { Text(card.category) })
                                    Spacer(modifier = Modifier.width(8.dp))
                                    // 置顶切换按钮
                                    IconButton(
                                        onClick = {
                                            card.isPinned = !card.isPinned
                                            // 重新触发组件刷新
                                            val idx = cardList.indexOf(card)
                                            if (idx != -1) cardList[idx] = card.copy(isPinned = card.isPinned)
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.PushPin,
                                            contentDescription = "置顶",
                                            tint = if (card.isPinned) MaterialTheme.colorScheme.primary else Color.Gray
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("卡号: ${card.cardNumber}", fontSize = 14.sp)
                            Text("到期日: ${card.expiryDate}", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                            Text("提醒配置: ${card.advanceDays} | ${card.remindType} (${card.repeatIntervalDays})", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }

    // 编辑卡片对话框
    editingCard?.let { card ->
        var name by remember { mutableStateOf(card.name) }
        var number by remember { mutableStateOf(card.cardNumber) }
        var expiry by remember { mutableStateOf(card.expiryDate) }

        AlertDialog(
            onDismissRequest = { editingCard = null },
            title = { Text("编辑卡片") },
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

// ================= 3. 新增页面 (AddScreen) =================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(onSave: (CardModel) -> Unit) {
    val context = LocalContext.current

    // 可选项数据定义
    val categoryOptions = listOf("银行卡", "电话卡", "邮箱", "账号", "其他")
    val advanceOptions = listOf("当天提醒", "提前1天", "提前3天", "提前7天", "提前30天")
    val remindTypeOptions = listOf("当天提醒", "重复提醒")
    val repeatIntervalOptions = listOf("0天 (单次)", "7天 (每周)", "30天 (每月)", "365天 (每年)")

    var category by remember { mutableStateOf(categoryOptions[0]) }
    var name by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var advanceDays by remember { mutableStateOf(advanceOptions[0]) }
    var remindType by remember { mutableStateOf(remindTypeOptions[0]) }
    var repeatIntervalDays by remember { mutableStateOf(repeatIntervalOptions[0]) }

    // 下拉框展开状态
    var expCategory by remember { mutableStateOf(false) }
    var expAdvance by remember { mutableStateOf(false) }
    var expRemindType by remember { mutableStateOf(false) }
    var expRepeatInterval by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("新增卡片", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        // 1. 分类下拉选择
        ExposedDropdownMenuBox(expanded = expCategory, onExpandedChange = { expCategory = !expCategory }) {
            OutlinedTextField(
                value = category, onValueChange = {}, readOnly = true,
                label = { Text("分类") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expCategory) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expCategory, onDismissRequest = { expCategory = false }) {
                categoryOptions.forEach { item ->
                    DropdownMenuItem(text = { Text(item) }, onClick = { category = item; expCategory = false })
                }
            }
        }

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("卡名") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = cardNumber, onValueChange = { cardNumber = it }, label = { Text("卡号") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = balance, onValueChange = { balance = it }, label = { Text("余额") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = expiryDate, onValueChange = { expiryDate = it }, label = { Text("到期日 (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())

        // 2. 提前提醒天数下拉框
        ExposedDropdownMenuBox(expanded = expAdvance, onExpandedChange = { expAdvance = !expAdvance }) {
            OutlinedTextField(
                value = advanceDays, onValueChange = {}, readOnly = true,
                label = { Text("提前提醒设置") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expAdvance) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expAdvance, onDismissRequest = { expAdvance = false }) {
                advanceOptions.forEach { item ->
                    DropdownMenuItem(text = { Text(item) }, onClick = { advanceDays = item; expAdvance = false })
                }
            }
        }

        // 3. 提醒方式下拉框
        ExposedDropdownMenuBox(expanded = expRemindType, onExpandedChange = { expRemindType = !expRemindType }) {
            OutlinedTextField(
                value = remindType, onValueChange = {}, readOnly = true,
                label = { Text("提醒方式") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expRemindType) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expRemindType, onDismissRequest = { expRemindType = false }) {
                remindTypeOptions.forEach { item ->
                    DropdownMenuItem(text = { Text(item) }, onClick = { remindType = item; expRemindType = false })
                }
            }
        }

        // 4. 提醒周期（间隔天数）下拉框
        ExposedDropdownMenuBox(expanded = expRepeatInterval, onExpandedChange = { expRepeatInterval = !expRepeatInterval }) {
            OutlinedTextField(
                value = repeatIntervalDays, onValueChange = {}, readOnly = true,
                label = { Text("提醒周期 (下个任务循环天数)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expRepeatInterval) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expRepeatInterval, onDismissRequest = { expRepeatInterval = false }) {
                repeatIntervalOptions.forEach { item ->
                    DropdownMenuItem(text = { Text(item) }, onClick = { repeatIntervalDays = item; expRepeatInterval = false })
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (name.isBlank()) {
                    Toast.makeText(context, "请补全卡名信息", Toast.LENGTH_SHORT).show()
                } else {
                    onSave(
                        CardModel(
                            category = category,
                            name = name,
                            cardNumber = cardNumber,
                            balance = balance.ifBlank { "0.00" },
                            expiryDate = expiryDate.ifBlank { "2026-12-31" },
                            advanceDays = advanceDays,
                            remindType = remindType,
                            repeatIntervalDays = repeatIntervalDays
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

// ================= 4. 我的页面 (ProfileScreen) =================
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
