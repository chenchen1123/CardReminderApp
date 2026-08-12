package com.cardreminder.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

// 数据模型 (增加 note 备注字段)
data class CardItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val cardNumber: String = "",
    val category: String,
    val note: String = "", // 备注字段
    val expiryDateMillis: Long,
    val remindHour: Int = 9,
    val remindMinute: Int = 0,
    val advanceDays: Int = 0,
    val isRepeat: Boolean = false,
    val repeatDays: Int = 7,
    val isPinned: Boolean = false,
    val pinTime: Long = 0L
)

// SharedPreferences 数据持久化工具
object CardStorage {
    private const val PREF_NAME = "card_reminder_prefs"
    private const val KEY_CARDS = "key_cards_json"

    fun saveCards(context: Context, cards: List<CardItem>) {
        val jsonArray = JSONArray()
        cards.forEach { card ->
            val obj = JSONObject().apply {
                put("id", card.id)
                put("title", card.title)
                put("cardNumber", card.cardNumber)
                put("category", card.category)
                put("note", card.note)
                put("expiryDateMillis", card.expiryDateMillis)
                put("remindHour", card.remindHour)
                put("remindMinute", card.remindMinute)
                put("advanceDays", card.advanceDays)
                put("isRepeat", card.isRepeat)
                put("repeatDays", card.repeatDays)
                put("isPinned", card.isPinned)
                put("pinTime", card.pinTime)
            }
            jsonArray.put(obj)
        }
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sp.edit().putString(KEY_CARDS, jsonArray.toString()).apply()
    }

    fun loadCards(context: Context): List<CardItem> {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonStr = sp.getString(KEY_CARDS, null) ?: return emptyList()
        val list = mutableListOf<CardItem>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    CardItem(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        title = obj.optString("title", ""),
                        cardNumber = obj.optString("cardNumber", ""),
                        category = obj.optString("category", "其他"),
                        note = obj.optString("note", ""),
                        expiryDateMillis = obj.optLong("expiryDateMillis", System.currentTimeMillis()),
                        remindHour = obj.optInt("remindHour", 9),
                        remindMinute = obj.optInt("remindMinute", 0),
                        advanceDays = obj.optInt("advanceDays", 0),
                        isRepeat = obj.optBoolean("isRepeat", false),
                        repeatDays = obj.optInt("repeatDays", 7),
                        isPinned = obj.optBoolean("isPinned", false),
                        pinTime = obj.optLong("pinTime", 0L)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}

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
    var selectedTab by remember { mutableStateOf(0) } // 默认显示首页

    var cardList by remember { mutableStateOf(CardStorage.loadCards(context)) }

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
                            0 -> "分类浏览"
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
                    label = { Text("列表") }
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
                0 -> CategorizedHomeScreen(cardList, onEdit = { card ->
                    editingCard = card
                    showAddDialog = true
                })
                1 -> ListScreen(
                    displayList = displayList,
                    onTogglePin = { card ->
                        val newList = cardList.map {
                            if (it.id == card.id) it.copy(isPinned = !it.isPinned, pinTime = System.currentTimeMillis()) else it
                        }
                        cardList = newList
                        CardStorage.saveCards(context, newList)
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
                val updatedList = cardList.filter { it.id != newCard.id } + newCard
                cardList = updatedList
                CardStorage.saveCards(context, updatedList)

                try {
                    val reminderCalendar = Calendar.getInstance().apply {
                        timeInMillis = newCard.expiryDateMillis
                        add(Calendar.DAY_OF_MONTH, -newCard.advanceDays)
                        set(Calendar.HOUR_OF_DAY, newCard.remindHour)
                        set(Calendar.MINUTE, newCard.remindMinute)
                        set(Calendar.SECOND, 0)
                    }

                    var triggerMillis = reminderCalendar.timeInMillis
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
                    e.printStackTrace()
                }

                showAddDialog = false
                selectedTab = 1
                Toast.makeText(context, "卡片已永久保存！", Toast.LENGTH_SHORT).show()
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
                            val newList = cardList.filter { it.id != card.id }
                            cardList = newList
                            CardStorage.saveCards(context, newList)
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

// 1. 首页：按分类分组展示，每类横向滑动
@Composable
fun CategorizedHomeScreen(cardList: List<CardItem>, onEdit: (CardItem) -> Unit) {
    val categories = listOf("银行卡", "电话卡", "邮箱", "账号", "其他")

    if (cardList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("暂无卡片数据，点击底部“新增”添加", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(categories) { category ->
                val categoryCards = cardList.filter { it.category == category }
                if (categoryCards.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = category,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${categoryCards.size}张",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }

                        // 横向左右滑动列表
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(categoryCards) { card ->
                                HorizontalCardItem(card = card, onClick = { onEdit(card) })
                            }
                        }
                    }
                }
            }
        }
    }
}

// 首页横向滑动的单个卡片样式
@Composable
fun HorizontalCardItem(card: CardItem, onClick: () -> Unit) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    Card(
        modifier = Modifier
            .width(220.dp)
            .height(130.dp)
            .combinedClickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(card.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                if (card.cardNumber.isNotBlank()) {
                    Text(card.cardNumber, fontSize = 12.sp, color = Color.DarkGray, maxLines = 1)
                }
            }
            if (card.note.isNotBlank()) {
                Text("备注: ${card.note}", fontSize = 11.sp, color = Color.Gray, maxLines = 1)
            }
            Text("到期: ${sdf.format(Date(card.expiryDateMillis))}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}

// 2. 列表视图
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

// 列表单项卡片 (支持单击展开查看备注)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SwipeableCardItem(
    card: CardItem,
    onTogglePin: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expandedNote by remember { mutableStateOf(false) }

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
                .combinedClickable(onClick = { expandedNote = !expandedNote }), // 单击展开/折叠备注
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

                // 备注展示区域 (点击展开)
                if (card.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    AnimatedVisibility(visible = expandedNote) {
                        Surface(
                            color = Color(0xFFF0F4F8),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "备注: ${card.note}",
                                fontSize = 13.sp,
                                color = Color.DarkGray,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                    if (!expandedNote) {
                        Text("点击卡片查看备注...", fontSize = 11.sp, color = Color.LightGray)
                    }
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

// 支持新增备注多行输入的编辑框
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
    val minuteOptions = (0..59).toList()

    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val yearOptions = (currentYear..currentYear + 10).toList()
    val monthOptions = (1..12).toList()
    val dayOptions = (1..31).toList()

    var title by remember { mutableStateOf(initialCard?.title ?: "") }
    var cardNumber by remember { mutableStateOf(initialCard?.cardNumber ?: "") }
    var note by remember { mutableStateOf(initialCard?.note ?: "") } // 备注状态
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

                // 备注多行输入框
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注信息 (选填)") },
                    minLines = 2,
                    maxLines = 4,
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

                // 2. 年月日选择
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

                // 3. 响铃具体时间
                Text("精确闹钟响铃时间:", fontSize = 13.sp, color = Color.Gray)
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
                        note = note,
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
        Text("卡片提醒助手 v1.7", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("极简易用的卡片管理与到期提醒工具", color = Color.Gray, fontSize = 14.sp)
    }
}
