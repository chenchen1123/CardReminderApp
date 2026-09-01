package com.cardreminder.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.cardreminder.app.ui.screens.ConfigDashboardScreen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CardReminderAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppNavigation()
                }
            }
        }
    }
}

// ================= 主题配置 =================
@Composable
fun CardReminderAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF1E88E5),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFD1E4FF),
            onPrimaryContainer = Color(0xFF001D36),
            secondary = Color(0xFF26A69A),
            onSecondary = Color.White,
            background = Color(0xFFF8F9FA),
            surface = Color.White,
            error = Color(0xFFBA1A1A),
            outline = Color(0xFF74777F)
        ),
        content = content
    )
}

// ================= 数据实体与状态 =================
enum class CardCategory(val title: String, val icon: ImageVector) {
    ALL("全部", Icons.Default.AllInclusive),
    CREDIT_CARD("信用卡", Icons.Default.CreditCard),
    MEMBERSHIP("会员卡", Icons.Default.CardMembership),
    CERTIFICATE("证件", Icons.Default.Badge),
    OTHER("其他", Icons.Default.Category)
}

data class CardItem(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val category: CardCategory = CardCategory.CREDIT_CARD,
    val expireDate: String, // yyyy-MM-dd
    val reminderDaysBefore: Int = 30,
    val note: String = "",
    val isFavorite: Boolean = false,
    val cardColor: Long = 0xFF1E88E5
)

// ================= 底部导航栏定义 =================
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Cards : Screen("cards", "卡包", Icons.Default.CreditCard)
    object Timeline : Screen("history_timeline", "时间轴", Icons.Default.Timeline)
    object Batch : Screen("batch_management", "批量管理", Icons.Default.DynamicFeed)
    object Dashboard : Screen("config_dashboard", "仪表盘", Icons.Default.Dashboard)
    object Profile : Screen("profile", "我的", Icons.Default.Person)
}

// ================= 主应用入口与导航 =================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current

    // 卡片内存数据（支持增删查改）
    var cardList by remember {
        mutableStateOf(
            listOf(
                CardItem(1L, "招商银行信用卡", CardCategory.CREDIT_CARD, "2027-08-31", 30, "主刷卡，免年费条件每年6次"),
                CardItem(2L, "山姆会员卡", CardCategory.MEMBERSHIP, "2026-12-15", 15, "亲友卡绑在父母手机上"),
                CardItem(3L, "中国居民身份证", CardCategory.CERTIFICATE, "2030-05-20", 60, "记得提前3个月换领")
            )
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingCard by remember { mutableStateOf<CardItem?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val bottomNavItems = listOf(
        Screen.Cards,
        Screen.Timeline,
        Screen.Batch,
        Screen.Dashboard,
        Screen.Profile
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title, fontSize = 11.sp) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentRoute == Screen.Cards.route) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "添加卡片")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Cards.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. 卡包主页
            composable(Screen.Cards.route) {
                CardListScreen(
                    cards = cardList,
                    onCardClick = { card -> editingCard = card },
                    onDeleteCard = { card ->
                        cardList = cardList.filter { it.id != card.id }
                        scope.launch { snackbarHostState.showSnackbar("已删除卡片：${card.name}") }
                    }
                )
            }

            // 2. 时间轴页面
            composable(Screen.Timeline.route) {
                HistoryTimelineScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onCardClick = { cardId: Long ->
                        val card = cardList.find { it.id == cardId }
                        if (card != null) editingCard = card
                    }
                )
            }

            // 3. 批量管理页面
            composable(Screen.Batch.route) {
                BatchManagementScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onActionCompleted = { action: String, count: Int ->
                        scope.launch { snackbarHostState.showSnackbar("$action $count 张卡片") }
                    }
                )
            }

            // 4. 仪表盘页面
            composable(Screen.Dashboard.route) {
                ConfigDashboardScreen()
            }

            // 5. 个人中心页面
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateToSettings = {
                        scope.launch { snackbarHostState.showSnackbar("设置功能正在完善中") }
                    },
                    onExportData = { path: String ->
                        scope.launch { snackbarHostState.showSnackbar("数据已备份至：$path") }
                    },
                    onImportData = { path: String ->
                        scope.launch { snackbarHostState.showSnackbar("数据导入成功") }
                    }
                )
            }
        }
    }

    // 新增/编辑弹窗
    if (showAddDialog || editingCard != null) {
        CardEditDialog(
            initialCard = editingCard,
            onDismiss = {
                showAddDialog = false
                editingCard = null
            },
            onSave = { savedCard ->
                if (editingCard != null) {
                    cardList = cardList.map { if (it.id == savedCard.id) savedCard else it }
                    scope.launch { snackbarHostState.showSnackbar("已更新：${savedCard.name}") }
                } else {
                    cardList = listOf(savedCard) + cardList
                    scope.launch { snackbarHostState.showSnackbar("添加成功：${savedCard.name}") }
                }
                showAddDialog = false
                editingCard = null
            }
        )
    }
}

// ================= 卡包列表视图 =================
@Composable
fun CardListScreen(
    cards: List<CardItem>,
    onCardClick: (CardItem) -> Unit,
    onDeleteCard: (CardItem) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(CardCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredCards = remember(cards, selectedCategory, searchQuery) {
        cards.filter { card ->
            val matchCategory = selectedCategory == CardCategory.ALL || card.category == selectedCategory
            val matchQuery = card.name.contains(searchQuery, ignoreCase = true) || card.note.contains(searchQuery, ignoreCase = true)
            matchCategory && matchQuery
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 顶部搜索栏
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("搜索卡片或备注...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "清除")
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // 分类筛选 Tab
        ScrollableTabRow(
            selectedTabIndex = CardCategory.values().indexOf(selectedCategory),
            edgePadding = 16.dp,
            divider = {}
        ) {
            CardCategory.values().forEach { category ->
                Tab(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    text = { Text(category.title) },
                    icon = { Icon(category.icon, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredCards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CreditCardOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "暂无卡片数据，点击下方 + 开始添加",
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredCards, key = { it.id }) { card ->
                    CardListItemRow(
                        card = card,
                        onClick = { onCardClick(card) },
                        onDelete = { onDeleteCard(card) }
                    )
                }
            }
        }
    }
}

@Composable
fun CardListItemRow(
    card: CardItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(card.cardColor)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(card.category.icon, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = card.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = card.category.title,
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 12.sp
                        )
                    }
                }

                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "到期时间: ${card.expireDate}",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "提前 ${card.reminderDaysBefore} 天提醒",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            if (card.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = card.note,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除卡片 \"${card.name}\" 吗？此操作无法撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }
}

// ================= 卡片编辑/添加弹窗 =================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardEditDialog(
    initialCard: CardItem?,
    onDismiss: () -> Unit,
    onSave: (CardItem) -> Unit
) {
    var name by remember { mutableStateOf(initialCard?.name ?: "") }
    var category by remember { mutableStateOf(initialCard?.category ?: CardCategory.CREDIT_CARD) }
    var expireDate by remember { mutableStateOf(initialCard?.expireDate ?: "2027-12-31") }
    var reminderDays by remember { mutableStateOf((initialCard?.reminderDaysBefore ?: 30).toString()) }
    var note by remember { mutableStateOf(initialCard?.note ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialCard == null) "添加新卡片" else "编辑卡片") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("卡片名称 *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("卡片分类", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CardCategory.values().filter { it != CardCategory.ALL }.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.title, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = expireDate,
                    onValueChange = { expireDate = it },
                    label = { Text("到期日期 (格式: YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = reminderDays,
                    onValueChange = { reminderDays = it.filter { char -> char.isDigit() } },
                    label = { Text("提前提醒天数") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注信息") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val card = initialCard?.copy(
                            name = name,
                            category = category,
                            expireDate = expireDate,
                            reminderDaysBefore = reminderDays.toIntOrNull() ?: 30,
                            note = note
                        ) ?: CardItem(
                            name = name,
                            category = category,
                            expireDate = expireDate,
                            reminderDaysBefore = reminderDays.toIntOrNull() ?: 30,
                            note = note
                        )
                        onSave(card)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

// ================= 内置缺失组件（解决编译报错） =================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryTimelineScreen(
    onNavigateBack: () -> Unit = {},
    onCardClick: (Long) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("历史时间轴") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Timeline,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "历史时间轴与动态记录",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "卡片操作记录与生命周期时间线将在后续版本展示",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchManagementScreen(
    onNavigateBack: () -> Unit = {},
    onActionCompleted: (String, Int) -> Unit = { _, _ -> }
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("批量管理") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.DynamicFeed,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "批量管理与多选操作",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "支持批量导出、分类标记与统一调整提醒时间",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit = {},
    onExportData: (String) -> Unit = {},
    onImportData: (String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("个人中心 / 设置") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "CardReminder 用户",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "本地卡片到期提醒助手",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Text("数据与备份", fontWeight = FontWeight.Bold, fontSize = 14.sp)

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExportData("/Download/CardReminder_Backup.json") }
            ) {
                ListItem(
                    headlineContent = { Text("导出卡片数据") },
                    supportingContent = { Text("将卡包数据备份至本地存储") },
                    leadingContent = { Icon(Icons.Default.FileDownload, contentDescription = null) }
                )
            }

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onImportData("/Download/CardReminder_Backup.json") }
            ) {
                ListItem(
                    headlineContent = { Text("导入卡片数据") },
                    supportingContent = { Text("从本地备份恢复卡片信息") },
                    leadingContent = { Icon(Icons.Default.FileUpload, contentDescription = null) }
                )
            }

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToSettings() }
            ) {
                ListItem(
                    headlineContent = { Text("通知与权限设置") },
                    supportingContent = { Text("检查精确闹钟与系统提醒通知权限") },
                    leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null) }
                )
            }
        }
    }
}
