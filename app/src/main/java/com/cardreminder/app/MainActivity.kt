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

// 数据结构定义
data class CardItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: String, // 银行卡, 电话卡, 邮箱, 账号, 其他
    val expiryDateMillis: Long,
    val advanceDays: Int = 0, // 提前提醒天数
    val isRepeat: Boolean = false, // 当天提醒 vs 重复提醒
    val repeatDays: Int = 7, // 提醒周期(天)
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
            MaterialTheme {
                CardReminderAppScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardReminderAppScreen() {
    val context = LocalContext.current
    val categoryOptions = remember { listOf("全部", "银行卡", "电话卡", "邮箱", "账号", "其他") }
    
    // 状态定义
    var cardList by remember {
        mutableStateOf(
            listOf(
                CardItem("1", "招商银行信用卡", "银行卡", System.currentTimeMillis() + 86400000L * 15, 3, true, 30, true, System.currentTimeMillis()),
                CardItem("2", "移动手机卡到期", "电话卡", System.currentTimeMillis() + 86400000L * 5, 1, false, 0, false)
            )
        )
    }
    
    var selectedCategoryFilter by remember { mutableStateOf("全部") }
    var currentSortOrder by remember { mutableStateOf(SortOrder.NONE) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCard by remember { mutableStateOf<CardItem?>(null) }
    var filterMenuExpanded by remember { mutableStateOf(false) }

    // 过滤与排序计算
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
        topBar = {
            TopAppBar(
                title = { Text("卡片提醒助手", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { filterMenuExpanded = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "筛选排序")
                    }
                    
                    // 顶部筛选与排序下拉菜单
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
                            text = { 
                                Text(
                                    "按到期日期升序",
                                    color = if (currentSortOrder == SortOrder.EXPIRY_ASC) MaterialTheme.colorScheme.primary else Color.Unspecified
                                ) 
                            },
                            leadingIcon = {
                                if (currentSortOrder == SortOrder.EXPIRY_ASC) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            onClick = {
                                currentSortOrder = SortOrder.EXPIRY_ASC
                                filterMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    "按到期日期降序",
                                    color = if (currentSortOrder == SortOrder.EXPIRY_DESC) MaterialTheme.colorScheme.primary else Color.Unspecified
                                ) 
                            },
                            leadingIcon = {
                                if (currentSortOrder == SortOrder.EXPIRY_DESC) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            onClick = {
                                currentSortOrder = SortOrder.EXPIRY_DESC
                                filterMenuExpanded = false
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                editingCard = null
                showAddDialog = true 
            }) {
                Icon(Icons.Default.Add, contentDescription = "新增提醒")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(displayList, key = { _, item -> item.id }) { index, card ->
                SwipeableCardItem(
                    card = card,
                    onTogglePin = {
                        cardList = cardList.map {
                            if (it.id == card.id) it.copy(isPinned = !it.isPinned, pinTime = System.currentTimeMillis()) else it
                        }
                    },
                    onEdit = {
                        editingCard = card
                        showAddDialog = true
                    },
                    onDelete = {
                        cardList = cardList.filter { it.id != card.id }
                        CardReminder.cancelReminder(context, card.id.hashCode())
                        Toast.makeText(context, "已删除卡片", Toast.LENGTH_SHORT).show()
                    },
                    onMoveUp = {
                        if (index > 0) {
                            val mutable = cardList.toMutableList()
                            val fromIndex = mutable.indexOf(displayList[index])
                            val toIndex = mutable.indexOf(displayList[index - 1])
                            if (fromIndex != -1 && toIndex != -1) {
                                Collections.swap(mutable, fromIndex, toIndex)
                                cardList = mutable
                            }
                        }
                    },
                    onMoveDown = {
                        if (index < displayList.size - 1) {
                            val mutable = cardList.toMutableList()
                            val fromIndex = mutable.indexOf(displayList[index])
                            val toIndex = mutable.indexOf(displayList[index + 1])
                            if (fromIndex != -1 && toIndex != -1) {
                                Collections.swap(mutable, fromIndex, toIndex)
                                cardList = mutable
                            }
                        }
                    }
                )
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

                // 计算提醒触发时间戳
                val triggerTime = newCard.expiryDateMillis - (newCard.advanceDays * 86400000L)
                CardReminder.setReminder(
                    context = context,
                    reminderId = newCard.id.hashCode(),
                    triggerAtMillis = if (triggerTime > System.currentTimeMillis()) triggerTime else System.currentTimeMillis() + 5000L,
                    title = "[${newCard.category}] ${newCard.title}",
                    content = "您的卡片到期提醒到了！"
                )

                showAddDialog = false
                Toast.makeText(context, "卡片已保存并更新提醒", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

// 可划动/拖动交互卡片项
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SwipeableCardItem(
    card: CardItem,
    onTogglePin: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd) {
                onEdit()
                false
            } else if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50) // 左滑编辑 (绿)
                SwipeToDismissBoxValue.EndToStart -> Color(0xFFE53935) // 右滑删除 (红)
                else -> Color.Transparent
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑", tint = Color.White)
                        Text(" 编辑", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("删除 ", color = Color.White, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color.White)
                    }
                }
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { onEdit() },
                    onLongClick = { onMoveUp() } // 长按触发排序调整
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (card.isPinned) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AssistChip(
                            onClick = { },
                            label = { Text(card.category, fontSize = 12.sp) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(card.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    Text("到期日期: ${sdf.format(Date(card.expiryDateMillis))}", fontSize = 14.sp, color = Color.Gray)
                    Text(
                        "提醒: 提前 ${card.advanceDays} 天 | ${if (card.isRepeat) "重复周期 ${card.repeatDays}天" else "当天提醒"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 置顶按钮
                    IconButton(onClick = onTogglePin) {
                        Icon(
                            imageVector = if (card.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "置顶",
                            tint = if (card.isPinned) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                    // 排序微调提示按钮
                    IconButton(onClick = onMoveDown) {
                        Icon(Icons.Default.DragHandle, contentDescription = "长按拖拽", tint = Color.LightGray)
                    }
                }
            }
        }
    }
}

// 新增 / 编辑 对话框
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

    var title by remember { mutableStateOf(initialCard?.title ?: "") }
    var selectedCategory by remember { mutableStateOf(initialCard?.category ?: categories[0]) }
    var advanceDays by remember { mutableStateOf(initialCard?.advanceDays ?: 0) }
    var isRepeat by remember { mutableStateOf(initialCard?.isRepeat ?: false) }
    var repeatDays by remember { mutableStateOf(initialCard?.repeatDays ?: 7) }
    var expiryMillis by remember { mutableStateOf(initialCard?.expiryDateMillis ?: (System.currentTimeMillis() + 86400000L * 7)) }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var advanceDropdownExpanded by remember { mutableStateOf(false) }
    var repeatDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialCard == null) "新增卡片提醒" else "编辑卡片提醒") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("卡片名称/说明") },
                    modifier = Modifier.fillMaxWidth()
                )

                // 1. 分类下拉选择
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

                // 2. 提前提醒天数下拉
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

                // 3. 提醒模式与周期下拉
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("提醒模式:")
                    FilterChip(
                        selected = !isRepeat,
                        onClick = { isRepeat = false },
                        label = { Text("当天提醒") }
                    )
                    FilterChip(
                        selected = isRepeat,
                        onClick = { isRepeat = true },
                        label = { Text("重复提醒") }
                    )
                }

                if (isRepeat) {
                    ExposedDropdownMenuBox(
                        expanded = repeatDropdownExpanded,
                        onExpandedChange = { repeatDropdownExpanded = !repeatDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = "每 $repeatDays 天提醒一次",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("提醒周期") },
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
                    val card = CardItem(
                        id = initialCard?.id ?: UUID.randomUUID().toString(),
                        title = title,
                        category = selectedCategory,
                        expiryDateMillis = expiryMillis,
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
