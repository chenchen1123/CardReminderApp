package com.cardreminder.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

// 数据模型 (增加提醒小时、分钟字段)
data class CardItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val cardNumber: String = "",
    val category: String,
    val expiryDateMillis: Long,
    val remindHour: Int = 9,    // 默认上午 9 点提醒
    val remindMinute: Int = 0,  // 默认 0 分
    val advanceDays: Int = 0,
    val isRepeat: Boolean = false,
    val repeatDays: Int = 7,
    val isPinned: Boolean = false,
    val pinTime: Long = 0L
)

enum class SortOrder { NONE, EXPIRY_ASC, EXPIRY_DESC }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF1E88E5),
                    secondary = Color(0xFF26A69A),
                    background = Color(0xFFF5F7FA),
                    surface = Color.White
                )
            ) {
                MainTabContainer()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabContainer() {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(1) }

    var cardList by remember {
        mutableStateOf(
            listOf(
                CardItem("1", "招商银行信用卡", "6222 **** **** 8888", "银行卡", System.currentTimeMillis() + 86400000L * 15, 9, 0, 3, true, 30, true, System.currentTimeMillis()),
                CardItem("2", "移动手机卡到期", "138 **** 9999", "电话卡", System.currentTimeMillis() + 86400000L * 5, 10, 30, 1, false, 0, false)
            )
        )
    }

    var selectedCategoryFilter by remember { mutableStateOf("全部") }
    var currentSortOrder by remember { mutableStateOf(SortOrder.NONE) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCard by remember { mutableStateOf<CardItem?>(null) }
    var deletingCard by remember { mutableStateOf<CardItem?>(null) }
    var filterMenuExpanded by remember { mutableStateOf(false) }

    val categoryOptions = listOf("全部", "银行卡", "电话卡", "邮箱", "账号", "其他")

    val displayList = remember(cardList, selectedCategoryFilter, currentSortOrder) {
        cardList.filter { selectedCategoryFilter == "全部" || it.category == selectedCategoryFilter }
            .sortedWith { a, b ->
                if (a.isPinned != b.isPinned) {
                    if (a.isPinned) -1 else 1
                } else if (a.isPinned && b.isPinned) {
                    b.pinTime.compareTo(a.pinTime)
                } else {
                    when (currentSortOrder) {
                        SortOrder.EXPIRY_ASC -> a.expiryDateMillis.compareTo(b.expiryDateMillis)
                        SortOrder.EXPIRY_DESC -> b.expiryDateMillis.compareTo(a.expiryDateMillis)
                        SortOrder.NONE -> 0
                    }
                }
            }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (selectedTab) {
                            0 -> "首页概览"
                            1 -> "我的卡片"
                            2 -> "添加卡片"
                            else -> "个人中心"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                actions = {
                    if (selectedTab == 1 || selectedTab == 0) {
                        IconButton(onClick = { filterMenuExpanded = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "筛选排序")
                        }

                        DropdownMenu(
                            expanded = filterMenuExpanded,
                            onDismissRequest = { filterMenuExpanded = false }
                        ) {
                            Text(" 分类筛选", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp, color = Color.Gray)
                            categoryOptions.forEach { cat ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            cat,
                                            color = if (selectedCategoryFilter == cat) MaterialTheme.colorScheme.primary else Color.Unspecified,
                                            fontWeight = if (selectedCategoryFilter == cat) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    leadingIcon = {
                                        if (selectedCategoryFilter == cat) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    },
                                    onClick = {
                                        selectedCategoryFilter = cat
                                        filterMenuExpanded = false
                                    }
                                )
                            }

                            HorizontalDivider()
                            Text(" 到期时间排序", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp, color = Color.Gray)

                            DropdownMenuItem(
                                text = { Text("按到期日期升序", color = if (currentSortOrder == SortOrder.EXPIRY_ASC) MaterialTheme.colorScheme.primary else Color.Unspecified) },
                                leadingIcon = { if (currentSortOrder == SortOrder.EXPIRY_ASC) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = { currentSortOrder = SortOrder.EXPIRY_ASC; filterMenuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("按到期日期降序", color = if (currentSortOrder == SortOrder.EXPIRY_DESC) MaterialTheme.colorScheme.primary else Color.Unspecified) },
                                leadingIcon = { if (currentSortOrder == SortOrder.EXPIRY_DESC) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = { currentSortOrder = SortOrder.EXPIRY_DESC; filterMenuExpanded = false }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "首页") },
                    label = { Text("首页") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.CreditCard, contentDescription = "列表") },
                    label = { Text("卡片") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        editingCard = null
                        showAddDialog = true
                    },
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
                1 -> ListScreen(
                    displayList = displayList,
                    onTogglePin = { card ->
                        cardList = cardList.map {
                            if (it.id == card.id) it.copy(isPinned = !it.isPinned, pinTime = System.currentTimeMillis()) else it
                        }
                    },
                    onEdit = { card ->
                        editingCard = card
                        showAddDialog = true
                    },
                    onDeleteRequest = { card ->
                        deletingCard = card
                    }
                )
                3 -> ProfileScreen()
            }
        }
    }

    if (showAddDialog) {
        CardEditDialog(
            initialCard = editingCard,
            onDismiss = { showAddDialog = false },
            onSave = { newCard ->
                // 1. 本地更新卡片列表
                val updatedList = cardList.filter { it.id != newCard.id } + newCard
                cardList = updatedList

                // 2. 计算精准闹钟提醒时间 (到期日 - 提前天数，设定小时和分钟)
                try {
                    val reminderCalendar = Calendar.getInstance().apply {
                        timeInMillis = newCard.expiryDateMillis
                        add(Calendar.DAY_OF_MONTH, -newCard.advanceDays)
                        set(Calendar.HOUR_OF_DAY, newCard.remindHour)
                        set(Calendar.MINUTE, newCard.remindMinute)
                        set(Calendar.SECOND, 0)
                    }

                    var triggerMillis = reminderCalendar.timeInMillis
                    // 如果计算出的时间早于当前系统时间，设为 5 秒后直接触发作为体验
                    if (triggerMillis <= System.currentTimeMillis()) {
                        triggerMillis = System.currentTimeMillis() + 5000L
                    }

                    CardReminder.setReminder(
                        context = context,
                        reminderId = newCard.id.hashCode(),
                        triggerAtMillis = triggerMillis,
                        title = "[${newCard.category}] ${newCard.title}",
                        content = "您的卡片到期提醒到了！"
                    )
                } catch (e: Exception) {
                    // 安全容错，保证界面保存不受闹钟服务故障影响
                }

                showAddDialog = false
                selectedTab = 1
                Toast.makeText(context, "卡片已成功保存！", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (deletingCard != null) {
        AlertDialog(
            onDismissRequest = { deletingCard = null },
            title = { Text("确认删除", fontWeight = FontWeight.Bold) },
            text = { Text("确定要删除卡片“${deletingCard?.title}”吗？此操作无法撤销。") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        deletingCard?.let { card ->
                            cardList = cardList.filter { it.id != card.id }
                            try {
                                CardReminder.cancelReminder(context, card.id.hashCode())
                            } catch (e: Exception) { }
                            Toast.makeText(context, "已被删除", Toast.LENGTH_SHORT).show()
                        }
                        deletingCard = null
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingCard = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun HomeScreen(cardList: List<CardItem>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("卡片状态统计", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("总卡片数量", color = Color.DarkGray)
                    Text("${cardList.size} 张", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("置顶卡片", color = Color.DarkGray)
                    Text("${cardList.count { it.isPinned }} 张", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ListScreen(
    displayList: List<CardItem>,
    onTogglePin: (CardItem) -> Unit,
    onEdit: (CardItem) -> Unit,
    onDeleteRequest: (CardItem) -> Unit
) {
    if (displayList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("暂无卡片数据，点击底部“新增”添加", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(displayList, key = { _, item -> item.id }) { _, card ->
                SwipeableCardItem(
                    card = card,
                    onTogglePin = { onTogglePin(card) },
                    onEdit = { onEdit(card) },
                    onDelete = { onDeleteRequest(card) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SwipeableCardItem(
    card: CardItem,
    onTogglePin: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd) {
                onEdit()
                false
            } else if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                false
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50)
                SwipeToDismissBoxValue.EndToStart -> Color(0xFFE53935)
                else -> Color.Transparent
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, shape = RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                    Icon(Icons.Default.Edit, contentDescription = "编辑", tint = Color.White)
                } else if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                    Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color.White)
                }
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = { onEdit() }),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (card.isPinned) Color(0xFFE3F2FD) else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(card.category, fontSize = 12.sp) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(card.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    Row {
                        IconButton(onClick = onTogglePin) {
                            Icon(
                                imageVector = if (card.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                contentDescription = "置顶",
                                tint = if (card.isPinned) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "删除", tint = Color.Gray)
                        }
                    }
                }

                if (card.cardNumber.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("卡号: ${card.cardNumber}", fontSize = 14.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(8.dp))
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val timeStr = String.format("%02d:%02d", card.remindHour, card.remindMinute)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("到期: ${sdf.format(Date(card.expiryDateMillis))}", fontSize = 13.sp, color = Color.Gray)
                    Text(
                        "提醒时间: $timeStr (提前${card.advanceDays}天)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// 可滑动的自定义对话框 (含年月日及自定义提醒具体时间)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardEditDialog(
    initialCard: CardItem?,
    onDismiss: () -> Unit,
    onSave: (CardItem) -> Unit
) {
    val categories = listOf("银行卡", "电话卡", "邮箱", "账号", "其他")
    val advanceDaysOptions = listOf(0, 1, 2, 3, 7, 15, 30)
    val repeatDaysOptions = listOf(1, 3, 7, 14, 30, 90, 180, 365)

    val hourOptions = (0..23).toList()
    val minuteOptions = listOf(0, 15, 30, 45)

    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val yearOptions = (currentYear..currentYear + 10).toList()
    val monthOptions = (1..12).toList()
    val dayOptions = (1..31).toList()

    var title by remember { mutableStateOf(initialCard?.title ?: "") }
    var cardNumber by remember { mutableStateOf(initialCard?.cardNumber ?: "") }
    var selectedCategory by remember { mutableStateOf(initialCard?.category ?: categories[0]) }

    val initialCalendar = Calendar.getInstance().apply {
        timeInMillis = initialCard?.expiryDateMillis ?: (System.currentTimeMillis() + 86400000L * 30)
    }
    var selectedYear by remember { mutableStateOf(initialCalendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(initialCalendar.get(Calendar.MONTH) + 1) }
    var selectedDay by remember { mutableStateOf(initialCalendar.get(Calendar.DAY_OF_MONTH)) }

    var remindHour by remember { mutableStateOf(initialCard?.remindHour ?: 9) }
    var remindMinute by remember { mutableStateOf(initialCard?.remindMinute ?: 0) }

    var advanceDays by remember { mutableStateOf(initialCard?.advanceDays ?: 0) }
    var isRepeat by remember { mutableStateOf(initialCard?.isRepeat ?: false) }
    var repeatDays by remember { mutableStateOf(initialCard?.repeatDays ?: 7) }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var yearExpanded by remember { mutableStateOf(false) }
    var monthExpanded by remember { mutableStateOf(false) }
    var dayExpanded by remember { mutableStateOf(false) }
    var hourExpanded by remember { mutableStateOf(false) }
    var minuteExpanded by remember { mutableStateOf(false) }
    var advanceDropdownExpanded by remember { mutableStateOf(false) }
    var repeatDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialCard == null) "新增卡片" else "编辑卡片", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("名称 (如: 招商信用卡)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { cardNumber = it },
                    label = { Text("卡号 / 账号 (选填)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 1. 分类选择
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("卡片分类") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // 2. 年月日自由选择
                Text("到期日期设定:", fontSize = 13.sp, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = yearExpanded,
                        onExpandedChange = { yearExpanded = !yearExpanded },
                        modifier = Modifier.weight(1.2f)
                    ) {
                        OutlinedTextField(
                            value = "${selectedYear}年",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = yearExpanded,
                            onDismissRequest = { yearExpanded = false }
                        ) {
                            yearOptions.forEach { y -> DropdownMenuItem(text = { Text("${y}年") }, onClick = { selectedYear = y; yearExpanded = false }) }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = monthExpanded,
                        onExpandedChange = { monthExpanded = !monthExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = "${selectedMonth}月",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = monthExpanded,
                            onDismissRequest = { monthExpanded = false }
                        ) {
                            monthOptions.forEach { m -> DropdownMenuItem(text = { Text("${m}月") }, onClick = { selectedMonth = m; monthExpanded = false }) }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = dayExpanded,
                        onExpandedChange = { dayExpanded = !dayExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = "${selectedDay}日",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = dayExpanded,
                            onDismissRequest = { dayExpanded = false }
                        ) {
                            dayOptions.forEach { d -> DropdownMenuItem(text = { Text("${d}日") }, onClick = { selectedDay = d; dayExpanded = false }) }
                        }
                    }
                }

                // 3. 自定义闹钟提醒时间 (时、分)
                Text("自定义闹钟响铃时间:", fontSize = 13.sp, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = hourExpanded,
                        onExpandedChange = { hourExpanded = !hourExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = String.format("%02d 时", remindHour),
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = hourExpanded,
                            onDismissRequest = { hourExpanded = false }
                        ) {
                            hourOptions.forEach { h -> DropdownMenuItem(text = { Text(String.format("%02d 时", h)) }, onClick = { remindHour = h; hourExpanded = false }) }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = minuteExpanded,
                        onExpandedChange = { minuteExpanded = !minuteExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = String.format("%02d 分", remindMinute),
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = minuteExpanded,
                            onDismissRequest = { minuteExpanded = false }
                        ) {
                            minuteOptions.forEach { m -> DropdownMenuItem(text = { Text(String.format("%02d 分", m)) }, onClick = { remindMinute = m; minuteExpanded = false }) }
                        }
                    }
                }

                // 4. 提前提醒
                ExposedDropdownMenuBox(
                    expanded = advanceDropdownExpanded,
                    onExpandedChange = { advanceDropdownExpanded = !advanceDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = if (advanceDays == 0) "当天提醒" else "提前 $advanceDays 天",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("提前提醒") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = advanceDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = advanceDropdownExpanded,
                        onDismissRequest = { advanceDropdownExpanded = false }
                    ) {
                        advanceDaysOptions.forEach { days ->
                            DropdownMenuItem(
                                text = { Text(if (days == 0) "当天提醒" else "提前 $days 天") },
                                onClick = {
                                    advanceDays = days
                                    advanceDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // 5. 提醒模式
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("提醒模式:", fontSize = 14.sp)
                    FilterChip(
                        selected = !isRepeat,
                        onClick = { isRepeat = false },
                        label = { Text("单次") }
                    )
                    FilterChip(
                        selected = isRepeat,
                        onClick = { isRepeat = true },
                        label = { Text("周期重复") }
                    )
                }

                if (isRepeat) {
                    ExposedDropdownMenuBox(
                        expanded = repeatDropdownExpanded,
                        onExpandedChange = { repeatDropdownExpanded = !repeatDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = "每 $repeatDays 天",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("重复周期") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = repeatDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = repeatDropdownExpanded,
                            onDismissRequest = { repeatDropdownExpanded = false }
                        ) {
                            repeatDaysOptions.forEach { days ->
                                DropdownMenuItem(
                                    text = { Text("每 $days 天") },
                                    onClick = {
                                        repeatDays = days
                                        repeatDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) return@Button

                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.YEAR, selectedYear)
                        set(Calendar.MONTH, selectedMonth - 1)
                        set(Calendar.DAY_OF_MONTH, selectedDay)
                        set(Calendar.HOUR_OF_DAY, remindHour)
                        set(Calendar.MINUTE, remindMinute)
                        set(Calendar.SECOND, 0)
                    }

                    val card = CardItem(
                        id = initialCard?.id ?: UUID.randomUUID().toString(),
                        title = title,
                        cardNumber = cardNumber,
                        category = selectedCategory,
                        expiryDateMillis = calendar.timeInMillis,
                        remindHour = remindHour,
                        remindMinute = remindMinute,
                        advanceDays = advanceDays,
                        isRepeat = isRepeat,
                        repeatDays = repeatDays,
                        isPinned = initialCard?.isPinned ?: false,
                        pinTime = initialCard?.pinTime ?: 0L
                    )
                    onSave(card)
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
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text("卡片提醒助手 v1.5", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("极简易用的卡片管理与到期提醒工具", color = Color.Gray, fontSize = 14.sp)
    }
}
