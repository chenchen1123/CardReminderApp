package com.cardreminder.app

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

// 软件主题枚举
enum class AppTheme(val displayName: String, val isDynamic: Boolean) {
    DEFAULT("默认明亮", false),
    DARK("深色夜间", false),
    OCEAN("静谧海洋", false),
    DYNAMIC_GRADIENT("动态炫彩", true)
}

// 安全解析 Hex 颜色
fun parseColorHex(hexString: String): Color {
    return try {
        val cleanHex = hexString.removePrefix("0x").removePrefix("#").trim()
        val longVal = cleanHex.toLong(16)
        if (cleanHex.length <= 6) {
            Color(0xFF000000L or longVal)
        } else {
            Color(longVal)
        }
    } catch (e: Exception) {
        Color.White
    }
}

// 将相册图片安全的复制保存到应用私有目录，彻底解决 Uri 重启失效问题
fun saveImageToInternalStorage(context: Context, uri: Uri): String {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return uri.toString()
        val fileName = "card_bg_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
        val file = File(context.filesDir, fileName)
        FileOutputStream(file).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        uri.toString()
    }
}

data class CardItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val cardNumber: String = "",
    val category: String,
    val note: String = "",
    val expiryDateMillis: Long,
    val remindHour: Int = 9,
    val remindMinute: Int = 0,
    val advanceDays: Int = 0,
    val isRepeat: Boolean = false,
    val repeatDays: Int = 7,
    val isPinned: Boolean = false,
    val pinTime: Long = 0L,
    val bgType: String = "COLOR",
    val bgValue: String = "0xFFFFFFFF"
)

object CardStorage {
    private const val PREF_NAME = "card_reminder_prefs"
    private const val KEY_CARDS = "key_cards_json"
    private const val KEY_THEME = "key_app_theme"

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
                put("bgType", card.bgType)
                put("bgValue", card.bgValue)
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
                        pinTime = obj.optLong("pinTime", 0L),
                        bgType = obj.optString("bgType", "COLOR"),
                        bgValue = obj.optString("bgValue", "0xFFFFFFFF")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveTheme(context: Context, theme: AppTheme) {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sp.edit().putString(KEY_THEME, theme.name).apply()
    }

    fun loadTheme(context: Context): AppTheme {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val name = sp.getString(KEY_THEME, AppTheme.DEFAULT.name)
        return try { AppTheme.valueOf(name!!) } catch (e: Exception) { AppTheme.DEFAULT }
    }
}

enum class SortOrder { NONE, EXPIRY_ASC, EXPIRY_DESC }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions(this)

        setContent {
            MainTabContainer()
        }
    }

    private fun checkAndRequestPermissions(context: Context) {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val neededPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        if (neededPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, neededPermissions.toTypedArray(), 101)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabContainer() {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }

    var cardList by remember { mutableStateOf(CardStorage.loadCards(context)) }
    var currentTheme by remember { mutableStateOf(CardStorage.loadTheme(context)) }

    var selectedCategoryFilter by remember { mutableStateOf("全部") }
    var currentSortOrder by remember { mutableStateOf(SortOrder.NONE) }
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

    // 动态主题渐变逻辑
    val infiniteTransition = rememberInfiniteTransition(label = "theme_gradient")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    val dynamicBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFE0F7FA),
            Color(0xFFE8EAF6),
            Color(0xFFF3E5F5),
            Color(0xFFE1F5FE)
        ),
        start = Offset(animatedOffset, 0f),
        end = Offset(0f, animatedOffset)
    )

    // 根据选择的主题配置全局配色
    val colorScheme = when (currentTheme) {
        AppTheme.DARK -> darkColorScheme(
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E)
        )
        AppTheme.OCEAN -> lightColorScheme(
            primary = Color(0xFF006699),
            background = Color(0xFFEBF3F5),
            surface = Color.White
        )
        else -> lightColorScheme(
            primary = Color(0xFF1E88E5),
            secondary = Color(0xFF26A69A),
            background = Color(0xFFF5F7FA),
            surface = Color.White
        )
    }

    MaterialTheme(colorScheme = colorScheme) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (currentTheme == AppTheme.DARK) Color(0xFF1E1E1E) else Color.White.copy(alpha = 0.85f)
                    ),
                    title = {
                        Text(
                            when (selectedTab) {
                                0 -> "分类浏览"
                                1 -> "我的卡片"
                                2 -> if (editingCard == null) "新增卡片" else "编辑卡片"
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
                NavigationBar(
                    containerColor = if (currentTheme == AppTheme.DARK) Color(0xFF1E1E1E) else Color.White,
                    tonalElevation = 8.dp
                ) {
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
                            selectedTab = 2
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (currentTheme.isDynamic) Modifier.background(dynamicBrush)
                        else Modifier.background(MaterialTheme.colorScheme.background)
                    )
                    .padding(padding)
            ) {
                when (selectedTab) {
                    0 -> CategorizedHomeScreen(cardList, onEdit = { card ->
                        editingCard = card
                        selectedTab = 2
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
                            selectedTab = 2
                        },
                        onDeleteRequest = { card ->
                            deletingCard = card
                        }
                    )
                    2 -> EditCardScreen(
                        initialCard = editingCard,
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

                            editingCard = null
                            selectedTab = 1
                            Toast.makeText(context, "保存成功！", Toast.LENGTH_SHORT).show()
                        }
                    )
                    3 -> ProfileScreen(
                        currentTheme = currentTheme,
                        onThemeChanged = { newTheme ->
                            currentTheme = newTheme
                            CardStorage.saveTheme(context, newTheme)
                        }
                    )
                }
            }
        }
    }

    if (deletingCard != null) {
        AlertDialog(
            onDismissRequest = { deletingCard = null },
            title = { Text("确认删除", fontWeight = FontWeight.Bold) },
            text = { Text("确定要删除卡片“${deletingCard?.title}”吗？") },
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalCardItem(card: CardItem, onClick: () -> Unit) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    Card(
        modifier = Modifier
            .width(220.dp)
            .height(130.dp)
            .combinedClickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (card.bgType == "URI") {
                AsyncImage(
                    model = card.bgValue,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(parseColorHex(card.bgValue))
                )
            }

            val textColor = if (card.bgType == "URI" || card.bgValue.contains("0xFF1E88E5") || card.bgValue.contains("0xFF26A69A")) Color.White else Color.Black

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(card.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, color = textColor)
                    if (card.cardNumber.isNotBlank()) {
                        Text(card.cardNumber, fontSize = 12.sp, color = textColor.copy(alpha = 0.8f), maxLines = 1)
                    }
                }
                if (card.note.isNotBlank()) {
                    Text("备注: ${card.note}", fontSize = 11.sp, color = textColor.copy(alpha = 0.7f), maxLines = 1)
                }
                Text("到期: ${sdf.format(Date(card.expiryDateMillis))}", fontSize = 11.sp, color = textColor, fontWeight = FontWeight.Bold)
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
    onEdit: (CardItem) -> Unit,
    onDelete: (CardItem) -> Unit
) {
    var expandedNote by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd) {
                onEdit(card)
                false
            } else if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete(card)
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
                .combinedClickable(onClick = { expandedNote = !expandedNote }),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                if (card.bgType == "URI") {
                    AsyncImage(
                        model = card.bgValue,
                        contentDescription = null,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.35f)))
                } else {
                    Box(modifier = Modifier.matchParentSize().background(parseColorHex(card.bgValue)))
                }

                val textColor = if (card.bgType == "URI" || card.bgValue.contains("0xFF1E88E5") || card.bgValue.contains("0xFF26A69A")) Color.White else Color.Black

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
                            Text(card.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
                        }

                        Row {
                            IconButton(onClick = onTogglePin) {
                                Icon(
                                    imageVector = if (card.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                    contentDescription = "置顶",
                                    tint = if (card.isPinned) MaterialTheme.colorScheme.primary else textColor.copy(alpha = 0.6f)
                                )
                            }
                            IconButton(onClick = { onDelete(card) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "删除", tint = textColor.copy(alpha = 0.6f))
                            }
                        }
                    }

                    if (card.cardNumber.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("卡号: ${card.cardNumber}", fontSize = 14.sp, color = textColor.copy(alpha = 0.85f), fontWeight = FontWeight.Medium)
                    }

                    if (card.note.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        AnimatedVisibility(visible = expandedNote) {
                            Surface(
                                color = Color.White.copy(alpha = 0.85f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "备注: ${card.note}",
                                    fontSize = 13.sp,
                                    color = Color.Black,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                        if (!expandedNote) {
                            Text("点击卡片查看备注...", fontSize = 11.sp, color = textColor.copy(alpha = 0.6f))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val timeStr = String.format("%02d:%02d", card.remindHour, card.remindMinute)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("到期: ${sdf.format(Date(card.expiryDateMillis))}", fontSize = 13.sp, color = textColor.copy(alpha = 0.8f))
                        Text(
                            "提醒时间: $timeStr (提前${card.advanceDays}天)",
                            fontSize = 12.sp,
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditCardScreen(
    initialCard: CardItem?,
    onSave: (CardItem) -> Unit
) {
    val context = LocalContext.current
    val categories = listOf("银行卡", "电话卡", "邮箱", "账号", "其他")
    val advanceDaysOptions = listOf(0, 1, 2, 3, 7, 15, 30)

    val presetColors = listOf(
        "0xFFFFFFFF" to "白",
        "0xFFE3F2FD" to "蓝",
        "0xFF1E88E5" to "深蓝",
        "0xFF26A69A" to "绿",
        "0xFFFF7043" to "橙"
    )

    var title by remember { mutableStateOf(initialCard?.title ?: "") }
    var cardNumber by remember { mutableStateOf(initialCard?.cardNumber ?: "") }
    var note by remember { mutableStateOf(initialCard?.note ?: "") }
    var selectedCategory by remember { mutableStateOf(initialCard?.category ?: categories[0]) }

    var bgType by remember { mutableStateOf(initialCard?.bgType ?: "COLOR") }
    var bgValue by remember { mutableStateOf(initialCard?.bgValue ?: "0xFFFFFFFF") }

    val calendar = remember {
        Calendar.getInstance().apply {
            timeInMillis = initialCard?.expiryDateMillis ?: (System.currentTimeMillis() + 86400000L * 30)
        }
    }

    var selectedYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH) + 1) }
    var selectedDay by remember { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }

    var remindHour by remember { mutableStateOf(initialCard?.remindHour ?: 9) }
    var remindMinute by remember { mutableStateOf(initialCard?.remindMinute ?: 0) }

    var advanceDays by remember { mutableStateOf(initialCard?.advanceDays ?: 0) }
    var isRepeat by remember { mutableStateOf(initialCard?.isRepeat ?: false) }
    var repeatDays by remember { mutableStateOf(initialCard?.repeatDays ?: 7) }

    // 选择相册图片并即时保存到内部私有目录，确保永久生效
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val savedPath = saveImageToInternalStorage(context, it)
            bgType = "URI"
            bgValue = savedPath
        }
    }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                selectedYear = year
                selectedMonth = month + 1
                selectedDay = dayOfMonth
            },
            selectedYear,
            selectedMonth - 1,
            selectedDay
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
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

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("备注信息 (选填)") },
            minLines = 2,
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Text("卡片分类:", fontSize = 13.sp, color = Color.Gray)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            categories.forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat) }
                )
            }
        }

        Text("自定义背景样式:", fontSize = 13.sp, color = Color.Gray)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            presetColors.forEach { (hex, _) ->
                val colorVal = parseColorHex(hex)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colorVal)
                        .border(
                            width = if (bgType == "COLOR" && bgValue == hex) 2.dp else 1.dp,
                            color = if (bgType == "COLOR" && bgValue == hex) MaterialTheme.colorScheme.primary else Color.LightGray,
                            shape = CircleShape
                        )
                        .clickable {
                            bgType = "COLOR"
                            bgValue = hex
                        }
                )
            }

            OutlinedButton(
                onClick = { photoLauncher.launch("image/*") },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (bgType == "URI") "已选相册图片" else "选择相册", fontSize = 12.sp)
            }
        }

        Text("到期日期:", fontSize = 13.sp, color = Color.Gray)
        OutlinedCard(
            onClick = { datePickerDialog.show() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedYear} 年 ${selectedMonth} 月 ${selectedDay} 日",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(Icons.Default.CalendarToday, contentDescription = "选择日期")
            }
        }

        Text("响铃时刻: ${String.format("%02d:%02d", remindHour, remindMinute)}", fontSize = 13.sp, color = Color.Gray)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("时: ", fontSize = 12.sp)
                IconButton(onClick = { if (remindHour > 0) remindHour-- }) { Icon(Icons.Default.Remove, null) }
                Text("$remindHour", fontWeight = FontWeight.Bold)
                IconButton(onClick = { if (remindHour < 23) remindHour++ }) { Icon(Icons.Default.Add, null) }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("分: ", fontSize = 12.sp)
                IconButton(onClick = { if (remindMinute >= 5) remindMinute -= 5 else remindMinute = 0 }) { Icon(Icons.Default.Remove, null) }
                Text("$remindMinute", fontWeight = FontWeight.Bold)
                IconButton(onClick = { if (remindMinute <= 54) remindMinute += 5 else remindMinute = 59 }) { Icon(Icons.Default.Add, null) }
            }
        }

        Text("提前提醒:", fontSize = 13.sp, color = Color.Gray)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            advanceDaysOptions.forEach { days ->
                FilterChip(
                    selected = advanceDays == days,
                    onClick = { advanceDays = days },
                    label = { Text(if (days == 0) "当天" else "${days}天") }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                if (title.isBlank()) return@Button

                val saveCalendar = Calendar.getInstance().apply {
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
                    expiryDateMillis = saveCalendar.timeInMillis,
                    remindHour = remindHour,
                    remindMinute = remindMinute,
                    advanceDays = advanceDays,
                    isRepeat = isRepeat,
                    repeatDays = repeatDays,
                    isPinned = initialCard?.isPinned ?: false,
                    pinTime = initialCard?.pinTime ?: 0L,
                    bgType = bgType,
                    bgValue = bgValue
                )
                onSave(card)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("保存卡片信息", fontSize = 16.sp)
        }
    }
}

// “我的”模块：集成卡片头像与全软件多主题选择
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    currentTheme: AppTheme,
    onThemeChanged: (AppTheme) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        
        Image(
            painter = painterResource(id = R.drawable.app_icon),
            contentDescription = "应用卡片封面",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("卡片提醒助手 v2.4", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text("极简易用的卡片管理与到期提醒工具", color = Color.Gray, fontSize = 14.sp)
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("软件外观主题设置", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("选择属于您的背景风格 (含流畅动态渐变)", fontSize = 12.sp, color = Color.Gray)

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AppTheme.values().forEach { theme ->
                        FilterChip(
                            selected = currentTheme == theme,
                            onClick = { onThemeChanged(theme) },
                            label = { 
                                Text(
                                    if (theme.isDynamic) "${theme.displayName} ✨" else theme.displayName 
                                ) 
                            },
                            leadingIcon = {
                                if (currentTheme == theme) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
