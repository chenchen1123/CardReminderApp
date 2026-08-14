package com.cardreminder.app

import android.Manifest
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.provider.CalendarContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*

enum class AppTheme(val displayName: String, val isDynamic: Boolean) {
    DEFAULT("默认明亮", false),
    DARK("深色夜间", false),
    OCEAN("静谧海洋", false),
    DYNAMIC_GRADIENT("动态炫彩 ✨", true),
    STAR_DEEP_SPACE("深空星海 🌌", false),
    STAR_AURORA("极光星穹 ✨", true),
    ANIME_SAKURA("落樱物语 🌸", true),
    ANIME_CYBER("赛博霓虹 ⚡", false)
}

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
    val intervalDays: Int = 30,
    val isPinned: Boolean = false,
    val pinTime: Long = 0L,
    val bgType: String = "COLOR",
    val bgValue: String = "0xFFFFFFFF",
    val cost: Double = 0.0,
    val costCycle: String = "每年", // 每年 / 每月 / 一次性
    val syncCalendar: Boolean = false,
    val historyLogs: List<String> = emptyList()
)

object CardStorage {
    private const val PREF_NAME = "card_reminder_prefs"
    private const val KEY_CARDS = "key_cards_json"
    private const val KEY_BACKUP_CARDS = "key_backup_cards_json"
    private const val KEY_BACKUP_TIME = "key_backup_time"
    private const val KEY_LAST_EXPORT_TIME = "key_last_export_time"
    private const val KEY_THEME = "key_app_theme"
    private const val KEY_BIOMETRIC_ENABLED = "key_biometric_enabled"

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
                put("intervalDays", card.intervalDays)
                put("isPinned", card.isPinned)
                put("pinTime", card.pinTime)
                put("bgType", card.bgType)
                put("bgValue", card.bgValue)
                put("cost", card.cost)
                put("costCycle", card.costCycle)
                put("syncCalendar", card.syncCalendar)
                val logArray = JSONArray()
                card.historyLogs.forEach { logArray.put(it) }
                put("historyLogs", logArray)
            }
            jsonArray.put(obj)
        }
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sp.edit().putString(KEY_CARDS, jsonArray.toString()).apply()
    }

    fun loadCards(context: Context): List<CardItem> {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonStr = sp.getString(KEY_CARDS, null) ?: return emptyList()
        return parseJsonToCards(jsonStr)
    }

    fun backupCurrentCards(context: Context, cards: List<CardItem>) {
        val jsonArray = JSONArray()
        cards.forEach { card ->
            val obj = JSONObject().apply {
                put("id", card.id)
                put("title", card.title)
                put("cardNumber", card.cardNumber)
                put("category", card.category)
                put("note", card.note)
                put("expiryDateMillis", card.expiryDateMillis)
                put("intervalDays", card.intervalDays)
                put("isPinned", card.isPinned)
                put("pinTime", card.pinTime)
                put("bgType", card.bgType)
                put("bgValue", card.bgValue)
                put("cost", card.cost)
                put("costCycle", card.costCycle)
                put("syncCalendar", card.syncCalendar)
                val logArray = JSONArray()
                card.historyLogs.forEach { logArray.put(it) }
                put("historyLogs", logArray)
            }
            jsonArray.put(obj)
        }
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        sp.edit()
            .putString(KEY_BACKUP_CARDS, jsonArray.toString())
            .putString(KEY_BACKUP_TIME, timeStr)
            .apply()
    }

    fun getBackupInfo(context: Context): Pair<List<CardItem>, String?> {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonStr = sp.getString(KEY_BACKUP_CARDS, null) ?: return Pair(emptyList(), null)
        val timeStr = sp.getString(KEY_BACKUP_TIME, null)
        return Pair(parseJsonToCards(jsonStr), timeStr)
    }

    fun saveLastExportTime(context: Context) {
        val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sp.edit().putString(KEY_LAST_EXPORT_TIME, timeStr).apply()
    }

    fun getLastExportTime(context: Context): String? {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sp.getString(KEY_LAST_EXPORT_TIME, null)
    }

    private fun parseJsonToCards(jsonStr: String): List<CardItem> {
        val list = mutableListOf<CardItem>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val logs = mutableListOf<String>()
                val logArray = obj.optJSONArray("historyLogs")
                if (logArray != null) {
                    for (j in 0 until logArray.length()) {
                        logs.add(logArray.getString(j))
                    }
                }
                list.add(
                    CardItem(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        title = obj.optString("title", ""),
                        cardNumber = obj.optString("cardNumber", ""),
                        category = obj.optString("category", "其他"),
                        note = obj.optString("note", ""),
                        expiryDateMillis = obj.optLong("expiryDateMillis", System.currentTimeMillis()),
                        intervalDays = obj.optInt("intervalDays", 30),
                        isPinned = obj.optBoolean("isPinned", false),
                        pinTime = obj.optLong("pinTime", 0L),
                        bgType = obj.optString("bgType", "COLOR"),
                        bgValue = obj.optString("bgValue", "0xFFFFFFFF"),
                        cost = obj.optDouble("cost", 0.0),
                        costCycle = obj.optString("costCycle", "每年"),
                        syncCalendar = obj.optBoolean("syncCalendar", false),
                        historyLogs = logs
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

    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sp.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun isBiometricEnabled(context: Context): Boolean {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sp.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }
}

// 日历事件同步工具
object CalendarSyncHelper {
    fun syncEventToCalendar(context: Context, card: CardItem) {
        if (!card.syncCalendar) return
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        try {
            val calId = getDefaultCalendarId(context) ?: return
            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, card.expiryDateMillis)
                put(CalendarContract.Events.DTEND, card.expiryDateMillis + 60 * 60 * 1000)
                put(CalendarContract.Events.TITLE, "【到期提醒】${card.title}")
                put(CalendarContract.Events.DESCRIPTION, "卡号: ${card.cardNumber}\n备注: ${card.note}")
                put(CalendarContract.Events.CALENDAR_ID, calId)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                put(CalendarContract.Events.HAS_ALARM, 1)
            }
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            if (uri != null) {
                val eventId = uri.lastPathSegment?.toLongOrNull()
                if (eventId != null) {
                    val reminderValues = ContentValues().apply {
                        put(CalendarContract.Reminders.EVENT_ID, eventId)
                        put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
                        put(CalendarContract.Reminders.MINUTES, 24 * 60) // 提前1天提醒
                    }
                    context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getDefaultCalendarId(context: Context): Long? {
        val projection = arrayOf(CalendarContract.Calendars._ID)
        val cursor = context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            null,
            null,
            null
        )
        return cursor?.use {
            if (it.moveToFirst()) it.getLong(0) else null
        }
    }
}

object ExcelExportImportHelper {
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun exportToCsv(context: Context, cards: List<CardItem>) {
        try {
            val exportDir = File(context.filesDir, "exports").apply { mkdirs() }
            val fileName = "卡片数据备份_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}.csv"
            val file = File(exportDir, fileName)
            val outputStream = FileOutputStream(file)
            
            outputStream.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            val writer = outputStream.bufferedWriter(Charsets.UTF_8)
            writer.write("名称,卡号,分类,到期日期,提醒间隔天数,维护费用,费用周期,备注\n")

            cards.forEach { card ->
                val dateStr = sdf.format(Date(card.expiryDateMillis))
                val line = "\"${card.title.replace("\"", "\"\"")}\"," +
                           "\"${card.cardNumber.replace("\"", "\"\"")}\"," +
                           "\"${card.category.replace("\"", "\"\"")}\"," +
                           "\"$dateStr\"," +
                           "${card.intervalDays}," +
                           "${card.cost}," +
                           "\"${card.costCycle}\"," +
                           "\"${card.note.replace("\"", "\"\"")}\"\n"
                writer.write(line)
            }
            writer.flush()
            writer.close()

            CardStorage.saveLastExportTime(context)

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "卡片数据备份")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "导出/分享卡片表格"))

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "导出失败: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun exportTemplate(context: Context) {
        try {
            val exportDir = File(context.filesDir, "exports").apply { mkdirs() }
            val file = File(exportDir, "卡片导入标准模板.csv")
            val outputStream = FileOutputStream(file)
            
            outputStream.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            val writer = outputStream.bufferedWriter(Charsets.UTF_8)
            writer.write("名称,卡号,分类,到期日期,提醒间隔天数,维护费用,费用周期,备注\n")
            writer.write("\"招商银行信用卡\",\"6225888899990000\",\"银行卡\",\"2026-12-31\",30,0.0,\"每年\",\"每月账单日5号\"\n")
            writer.write("\"香港手机卡\",\"+852 98765432\",\"电话卡\",\"2026-10-15\",180,6.0,\"每年\",\"需每半年发一条短信保号\"\n")
            writer.flush()
            writer.close()

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "卡片导入标准模板")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "下载/保存标准导入模板"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "生成模板失败: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun parseCsvFromUri(context: Context, uri: Uri): List<CardItem> {
        val result = mutableListOf<CardItem>()
        try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return emptyList()
            val charset = detectCharset(bytes)
            val reader = BufferedReader(InputStreamReader(ByteArrayInputStream(bytes), charset))
            var isFirstLine = true
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                val cleanLine = (line ?: "").removePrefix("\uFEFF")
                if (isFirstLine) {
                    isFirstLine = false
                    continue
                }
                val tokens = parseCsvLine(cleanLine)
                if (tokens.isNotEmpty() && tokens[0].isNotBlank()) {
                    val title = tokens.getOrNull(0) ?: ""
                    val cardNumber = tokens.getOrNull(1) ?: ""
                    val category = tokens.getOrNull(2) ?: "其他"
                    val dateStr = tokens.getOrNull(3) ?: ""
                    val interval = tokens.getOrNull(4)?.toIntOrNull() ?: 30
                    val cost = tokens.getOrNull(5)?.toDoubleOrNull() ?: 0.0
                    val costCycle = tokens.getOrNull(6) ?: "每年"
                    val note = tokens.getOrNull(7) ?: ""

                    val dateMillis = try {
                        sdf.parse(dateStr)?.time ?: System.currentTimeMillis()
                    } catch (e: Exception) {
                        System.currentTimeMillis()
                    }

                    result.add(
                        CardItem(
                            title = title,
                            cardNumber = cardNumber,
                            category = category,
                            expiryDateMillis = dateMillis,
                            intervalDays = interval,
                            cost = cost,
                            costCycle = costCycle,
                            note = note
                        )
                    )
                }
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    private fun detectCharset(bytes: ByteArray): Charset {
        if (bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()) {
            return Charsets.UTF_8
        }
        return try {
            val decoder = Charsets.UTF_8.newDecoder()
            decoder.decode(java.nio.ByteBuffer.wrap(bytes))
            Charsets.UTF_8
        } catch (e: Exception) {
            try {
                Charset.forName("GB18030")
            } catch (ex: Exception) {
                Charset.forName("GBK")
            }
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val list = mutableListOf<String>()
        var inQuotes = false
        val sb = StringBuilder()
        for (i in line.indices) {
            val c = line[i]
            if (c == '\"') {
                inQuotes = !inQuotes
            } else if (c == ',' && !inQuotes) {
                list.add(sb.toString().trim())
                sb.setLength(0)
            } else {
                sb.append(c)
            }
        }
        list.add(sb.toString().trim())
        return list
    }
}

enum class SortOrder { NONE, EXPIRY_ASC, EXPIRY_DESC }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkAndRequestPermissions(this)

        setContent {
            MainTabContainer()
        }
    }

    private fun checkAndRequestPermissions(context: Context) {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissions.add(Manifest.permission.READ_CALENDAR)
        permissions.add(Manifest.permission.WRITE_CALENDAR)

        val neededPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        if (neededPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, neededPermissions.toTypedArray(), 101)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabContainer() {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    var cardList by remember { mutableStateOf(CardStorage.loadCards(context)) }
    var currentTheme by remember { mutableStateOf(CardStorage.loadTheme(context)) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("全部") }
    var currentSortOrder by remember { mutableStateOf(SortOrder.NONE) }
    var editingCard by remember { mutableStateOf<CardItem?>(null) }
    var deletingCard by remember { mutableStateOf<CardItem?>(null) }
    var pinDialogCard by remember { mutableStateOf<CardItem?>(null) }
    var operateConfirmCard by remember { mutableStateOf<CardItem?>(null) }
    var filterMenuExpanded by remember { mutableStateOf(false) }

    var isBatchManaging by remember { mutableStateOf(false) }
    var importPendingCards by remember { mutableStateOf<List<CardItem>?>(null) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }

    // 生物识别应用锁检查
    var isUnlocked by remember { mutableStateOf(!CardStorage.isBiometricEnabled(context)) }

    LaunchedEffect(Unit) {
        if (CardStorage.isBiometricEnabled(context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val biometricPrompt = BiometricPrompt.Builder(context)
                .setTitle("身份验证")
                .setSubtitle("请验证指纹或面容进入卡片管理")
                .setNegativeButton("取消", context.mainExecutor) { _, _ -> }
                .build()

            biometricPrompt.authenticate(
                CancellationSignal(),
                context.mainExecutor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                        super.onAuthenticationSucceeded(result)
                        isUnlocked = true
                    }
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                        super.onAuthenticationError(errorCode, errString)
                    }
                }
            )
        }
    }

    if (!isUnlocked) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(Icons.Outlined.Lock, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                Text("应用已锁定，请验证身份", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Button(onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val prompt = BiometricPrompt.Builder(context)
                            .setTitle("身份验证")
                            .setNegativeButton("取消", context.mainExecutor) { _, _ -> }
                            .build()
                        prompt.authenticate(CancellationSignal(), context.mainExecutor, object : BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                                isUnlocked = true
                            }
                        })
                    }
                }) {
                    Text("点击验证指纹/面容")
                }
            }
        }
        return
    }

    val allCategories = remember(cardList) {
        val defaultList = listOf("全部", "银行卡", "电话卡", "邮箱", "账号", "其他")
        val customList = cardList.map { it.category }.distinct().filter { !defaultList.contains(it) }
        defaultList + customList
    }

    val now = System.currentTimeMillis()
    val displayList = remember(cardList, searchQuery, selectedCategoryFilter, currentSortOrder) {
        val filtered = cardList.filter {
            (selectedCategoryFilter == "全部" || it.category == selectedCategoryFilter) &&
            (searchQuery.isBlank() || it.title.contains(searchQuery, true) || it.cardNumber.contains(searchQuery, true) || it.note.contains(searchQuery, true))
        }
        filtered.sortedWith { a, b ->
            if (a.isPinned != b.isPinned) {
                if (a.isPinned) -1 else 1
            } else if (a.isPinned && b.isPinned) {
                b.pinTime.compareTo(a.pinTime)
            } else {
                val aUrgent = (a.expiryDateMillis - now) <= 30L * 24 * 60 * 60 * 1000L
                val bUrgent = (b.expiryDateMillis - now) <= 30L * 24 * 60 * 60 * 1000L
                if (aUrgent != bUrgent) {
                    if (aUrgent) -1 else 1
                } else {
                    when (currentSortOrder) {
                        SortOrder.EXPIRY_ASC -> a.expiryDateMillis.compareTo(b.expiryDateMillis)
                        SortOrder.EXPIRY_DESC -> b.expiryDateMillis.compareTo(a.expiryDateMillis)
                        SortOrder.NONE -> a.expiryDateMillis.compareTo(b.expiryDateMillis)
                    }
                }
            }
        }
    }

    val dynamicBrush = if (currentTheme.isDynamic) {
        val infiniteTransition = rememberInfiniteTransition(label = "theme_gradient")
        val animatedOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1200f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 10000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "offset"
        )
        when (currentTheme) {
            AppTheme.STAR_AURORA -> Brush.linearGradient(
                colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364), Color(0xFF00E5FF)),
                start = Offset(animatedOffset, 0f),
                end = Offset(0f, animatedOffset)
            )
            AppTheme.ANIME_SAKURA -> Brush.linearGradient(
                colors = listOf(Color(0xFFFFF0F5), Color(0xFFFFE4E1), Color(0xFFFFD1DC), Color(0xFFFFF5F7)),
                start = Offset(animatedOffset, 0f),
                end = Offset(0f, animatedOffset)
            )
            else -> Brush.linearGradient(
                colors = listOf(Color(0xFFE0F7FA), Color(0xFFE8EAF6), Color(0xFFF3E5F5), Color(0xFFE1F5FE)),
                start = Offset(animatedOffset, 0f),
                end = Offset(0f, animatedOffset)
            )
        }
    } else null

    val colorScheme = when (currentTheme) {
        AppTheme.DARK -> darkColorScheme(background = Color(0xFF121212), surface = Color(0xFF1E1E1E))
        AppTheme.OCEAN -> lightColorScheme(primary = Color(0xFF006699), background = Color(0xFFEBF3F5), surface = Color.White)
        AppTheme.STAR_DEEP_SPACE -> darkColorScheme(primary = Color(0xFF7C4DFF), secondary = Color(0xFF00E5FF), background = Color(0xFF0B0F19), surface = Color(0xFF161B2A))
        AppTheme.STAR_AURORA -> darkColorScheme(primary = Color(0xFF00E5FF), secondary = Color(0xFF69F0AE), background = Color(0xFF0F2027), surface = Color(0xFF1B2E37))
        AppTheme.ANIME_SAKURA -> lightColorScheme(primary = Color(0xFFFF4081), secondary = Color(0xFFFF80AB), background = Color(0xFFFFF5F7), surface = Color.White)
        AppTheme.ANIME_CYBER -> darkColorScheme(primary = Color(0xFFFFEA00), secondary = Color(0xFF00E5FF), background = Color(0xFF101010), surface = Color(0xFF1D1D1D))
        else -> lightColorScheme(primary = Color(0xFF1E88E5), secondary = Color(0xFF26A69A), background = Color(0xFFF5F7FA), surface = Color.White)
    }

    MaterialTheme(colorScheme = colorScheme) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (currentTheme == AppTheme.DARK || currentTheme == AppTheme.STAR_DEEP_SPACE || currentTheme == AppTheme.ANIME_CYBER) Color(0xFF1E1E1E).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.85f)
                    ),
                    title = {
                        Text(
                            if (isBatchManaging) "批量卡片整理"
                            else when (selectedTab) {
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
                        if (!isBatchManaging && (selectedTab == 1 || selectedTab == 0)) {
                            IconButton(onClick = { filterMenuExpanded = true }) {
                                Icon(Icons.Default.FilterList, contentDescription = "筛选排序")
                            }

                            DropdownMenu(
                                expanded = filterMenuExpanded,
                                onDismissRequest = { filterMenuExpanded = false }
                            ) {
                                Text(" 分类筛选", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp, color = Color.Gray)
                                allCategories.forEach { cat ->
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
                if (!isBatchManaging) {
                    NavigationBar(
                        containerColor = if (currentTheme == AppTheme.DARK || currentTheme == AppTheme.STAR_DEEP_SPACE || currentTheme == AppTheme.ANIME_CYBER) Color(0xFF1E1E1E) else Color.White,
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
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (dynamicBrush != null) Modifier.background(dynamicBrush)
                        else Modifier.background(MaterialTheme.colorScheme.background)
                    )
                    .padding(padding)
            ) {
                if (isBatchManaging) {
                    BatchManagementScreen(
                        cardList = cardList,
                        onBack = { isBatchManaging = false },
                        onUpdateCards = { updatedList ->
                            CardStorage.backupCurrentCards(context, cardList)
                            cardList = updatedList
                            CardStorage.saveCards(context, updatedList)
                        }
                    )
                } else {
                    when (selectedTab) {
                        0 -> CategorizedHomeScreen(cardList = cardList, onEdit = { card ->
                            editingCard = card
                            selectedTab = 2
                        })
                        1 -> ListScreen(
                            displayList = displayList,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            onTogglePin = { card ->
                                val newList = cardList.map {
                                    if (it.id == card.id) it.copy(isPinned = !it.isPinned, pinTime = System.currentTimeMillis()) else it
                                }
                                cardList = newList
                                CardStorage.saveCards(context, newList)
                            },
                            onLongClickPin = { card ->
                                pinDialogCard = card
                            },
                            onEdit = { card ->
                                editingCard = card
                                selectedTab = 2
                            },
                            onOperated = { card ->
                                operateConfirmCard = card
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
                                CalendarSyncHelper.syncEventToCalendar(context, newCard)

                                editingCard = null
                                selectedTab = 1
                                Toast.makeText(context, "卡片已保存！", Toast.LENGTH_SHORT).show()
                            }
                        )
                        3 -> ProfileScreen(
                            currentTheme = currentTheme,
                            cardList = cardList,
                            onThemeChanged = { newTheme ->
                                currentTheme = newTheme
                                CardStorage.saveTheme(context, newTheme)
                            },
                            onBatchManageClick = {
                                isBatchManaging = true
                            },
                            onExportClick = {
                                if (cardList.isEmpty()) {
                                    Toast.makeText(context, "当前暂无卡片数据可导出", Toast.LENGTH_SHORT).show()
                                } else {
                                    ExcelExportImportHelper.exportToCsv(context, cardList)
                                }
                            },
                            onExportTemplateClick = {
                                ExcelExportImportHelper.exportTemplate(context)
                            },
                            onImportFileParsed = { importedCards ->
                                if (importedCards.isEmpty()) {
                                    Toast.makeText(context, "未能从表格中解析出有效卡片数据", Toast.LENGTH_SHORT).show()
                                } else {
                                    importPendingCards = importedCards
                                }
                            },
                            onRestoreClick = {
                                showRestoreConfirmDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (importPendingCards != null) {
        val incomingCards = importPendingCards!!
        AlertDialog(
            onDismissRequest = { importPendingCards = null },
            title = { Text("选择导入模式", fontWeight = FontWeight.Bold) },
            text = {
                Text("成功解析出 ${incomingCards.size} 张卡片数据。\n请选择如何更新现有卡片？（系统将自动备份当前数据以便恢复）")
            },
            confirmButton = {
                Button(
                    onClick = {
                        CardStorage.backupCurrentCards(context, cardList)
                        val merged = cardList.toMutableList()
                        incomingCards.forEach { inc ->
                            val existsIndex = merged.indexOfFirst { it.title == inc.title && it.cardNumber == inc.cardNumber }
                            if (existsIndex >= 0) {
                                merged[existsIndex] = inc
                            } else {
                                merged.add(inc)
                            }
                        }
                        cardList = merged
                        CardStorage.saveCards(context, merged)
                        Toast.makeText(context, "合并导入完成！已备份旧数据", Toast.LENGTH_SHORT).show()
                        importPendingCards = null
                    }
                ) {
                    Text("合并追加")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        CardStorage.backupCurrentCards(context, cardList)
                        cardList = incomingCards
                        CardStorage.saveCards(context, incomingCards)
                        Toast.makeText(context, "已清空并覆盖导入！已备份旧数据", Toast.LENGTH_SHORT).show()
                        importPendingCards = null
                    }
                ) {
                    Text("清空覆盖")
                }
            }
        )
    }

    if (showRestoreConfirmDialog) {
        val (backupCards, backupTime) = CardStorage.getBackupInfo(context)
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = false },
            title = { Text("恢复历史备份数据", fontWeight = FontWeight.Bold) },
            text = {
                if (backupCards.isEmpty() || backupTime == null) {
                    Text("暂无历史导入备份记录。")
                } else {
                    Text("找到于【$backupTime】自动保存的备份，包含 ${backupCards.size} 张卡片。\n确定要回滚恢复吗？当前数据将被替换。")
                }
            },
            confirmButton = {
                if (backupCards.isNotEmpty()) {
                    Button(
                        onClick = {
                            cardList = backupCards
                            CardStorage.saveCards(context, backupCards)
                            Toast.makeText(context, "数据已成功恢复！", Toast.LENGTH_SHORT).show()
                            showRestoreConfirmDialog = false
                        }
                    ) {
                        Text("确认恢复")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (operateConfirmCard != null) {
        val card = operateConfirmCard!!
        val interval = card.intervalDays
        
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentExpiryStr = sdf.format(Date(card.expiryDateMillis))
        
        val nextCalendar = Calendar.getInstance().apply {
            timeInMillis = card.expiryDateMillis
            add(Calendar.DAY_OF_MONTH, if (interval > 0) interval else 30)
        }
        val nextExpiryStr = sdf.format(nextCalendar.time)

        AlertDialog(
            onDismissRequest = { operateConfirmCard = null },
            title = { Text("更新到期日确认", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("卡片：${card.title}")
                    Text("当前到期日期：$currentExpiryStr", color = Color.Gray, fontSize = 13.sp)
                    Text(
                        "将基于提醒间隔 (${interval}天)，顺延更新到期日期为：",
                        fontSize = 13.sp
                    )
                    Text(
                        nextExpiryStr,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val logTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                        val newLog = "$logTime 顺延 ${if (interval > 0) interval else 30} 天至 $nextExpiryStr"
                        val updatedList = cardList.map { item ->
                            if (item.id == card.id) {
                                val updatedCard = item.copy(
                                    expiryDateMillis = nextCalendar.timeInMillis,
                                    historyLogs = listOf(newLog) + item.historyLogs
                                )
                                CalendarSyncHelper.syncEventToCalendar(context, updatedCard)
                                updatedCard
                            } else {
                                item
                            }
                        }
                        cardList = updatedList
                        CardStorage.saveCards(context, updatedList)
                        Toast.makeText(context, "更新成功！新到期日：$nextExpiryStr", Toast.LENGTH_SHORT).show()
                        operateConfirmCard = null
                    }
                ) {
                    Text("确认更新")
                }
            },
            dismissButton = {
                TextButton(onClick = { operateConfirmCard = null }) {
                    Text("取消")
                }
            }
        )
    }

    if (pinDialogCard != null) {
        val isPinned = pinDialogCard?.isPinned == true
        AlertDialog(
            onDismissRequest = { pinDialogCard = null },
            title = { Text("卡片置顶设置", fontWeight = FontWeight.Bold) },
            text = { Text("是否将卡片“${pinDialogCard?.title}”${if (isPinned) "取消置顶" else "设为置顶"}？") },
            confirmButton = {
                Button(
                    onClick = {
                        pinDialogCard?.let { card ->
                            val newList = cardList.map {
                                if (it.id == card.id) it.copy(isPinned = !it.isPinned, pinTime = System.currentTimeMillis()) else it
                            }
                            cardList = newList
                            CardStorage.saveCards(context, newList)
                            Toast.makeText(context, if (!isPinned) "已置顶" else "已取消置顶", Toast.LENGTH_SHORT).show()
                        }
                        pinDialogCard = null
                    }
                ) {
                    Text(if (isPinned) "取消置顶" else "确认置顶")
                }
            },
            dismissButton = {
                TextButton(onClick = { pinDialogCard = null }) {
                    Text("取消")
                }
            }
        )
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BatchManagementScreen(
    cardList: List<CardItem>,
    onBack: () -> Unit,
    onUpdateCards: (List<CardItem>) -> Unit
) {
    BackHandler { onBack() }

    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showClearAllConfirm by remember { mutableStateOf(false) }
    var showBatchCategoryDialog by remember { mutableStateOf(false) }
    var showBatchExtendDialog by remember { mutableStateOf(false) }

    val isAllSelected = cardList.isNotEmpty() && selectedIds.size == cardList.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "已勾选: ${selectedIds.size} / ${cardList.size} 张",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = {
                        selectedIds = if (isAllSelected) emptySet() else cardList.map { it.id }.toSet()
                    },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(if (isAllSelected) "取消全选" else "全选", fontSize = 12.sp)
                }

                Button(
                    onClick = onBack,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("完成返回", fontSize = 12.sp)
                }
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = { showDeleteConfirm = true },
                enabled = selectedIds.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("删除所选", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = { showBatchCategoryDialog = true },
                enabled = selectedIds.isNotEmpty(),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("修改分类", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = { showBatchExtendDialog = true },
                enabled = selectedIds.isNotEmpty(),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Update, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("顺延到期日", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = { showClearAllConfirm = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("清空全部", fontSize = 12.sp)
            }
        }

        HorizontalDivider()

        if (cardList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("当前无任何卡片", color = Color.Gray)
            }
        } else {
            val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cardList, key = { it.id }) { card ->
                    val isChecked = selectedIds.contains(card.id)
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedIds = if (isChecked) selectedIds - card.id else selectedIds + card.id
                            },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        selectedIds = if (checked) selectedIds + card.id else selectedIds - card.id
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text(card.category, fontSize = 11.sp) }
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(card.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                    if (card.cardNumber.isNotBlank()) {
                                        Text(card.cardNumber, fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                            }
                            Text("到期: ${sdf.format(Date(card.expiryDateMillis))}", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认批量删除", fontWeight = FontWeight.Bold) },
            text = { Text("确定要删除选中的 ${selectedIds.size} 张卡片吗？此操作不可逆（已自动备份）。") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        val remaining = cardList.filter { !selectedIds.contains(it.id) }
                        onUpdateCards(remaining)
                        selectedIds = emptySet()
                        showDeleteConfirm = false
                    }
                ) {
                    Text("确认删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("取消") }
            }
        )
    }

    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            title = { Text("危险：确认清空全部卡片？", fontWeight = FontWeight.Bold) },
            text = { Text("将彻底清空当前所有卡片数据！系统已为您自动快照备份，随时可在“我的”页面恢复。") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        onUpdateCards(emptyList())
                        selectedIds = emptySet()
                        showClearAllConfirm = false
                    }
                ) {
                    Text("清空全部")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirm = false }) { Text("取消") }
            }
        )
    }

    if (showBatchCategoryDialog) {
        val categories = listOf("银行卡", "电话卡", "邮箱", "账号", "其他")
        var targetCat by remember { mutableStateOf(categories[0]) }
        var isCustomCat by remember { mutableStateOf(false) }
        var customCatInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showBatchCategoryDialog = false },
            title = { Text("批量修改分类", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("将选中的 ${selectedIds.size} 张卡片统一更改分类为：")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = !isCustomCat && targetCat == cat,
                                onClick = {
                                    isCustomCat = false
                                    targetCat = cat
                                },
                                label = { Text(cat) }
                            )
                        }
                        FilterChip(
                            selected = isCustomCat,
                            onClick = { isCustomCat = true },
                            label = { Text("自定义") }
                        )
                    }
                    if (isCustomCat) {
                        OutlinedTextField(
                            value = customCatInput,
                            onValueChange = { customCatInput = it },
                            label = { Text("输入新分类名") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalCat = if (isCustomCat && customCatInput.isNotBlank()) customCatInput.trim() else targetCat
                        val updated = cardList.map {
                            if (selectedIds.contains(it.id)) it.copy(category = finalCat) else it
                        }
                        onUpdateCards(updated)
                        showBatchCategoryDialog = false
                    }
                ) {
                    Text("确认修改")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchCategoryDialog = false }) { Text("取消") }
            }
        )
    }

    if (showBatchExtendDialog) {
        AlertDialog(
            onDismissRequest = { showBatchExtendDialog = false },
            title = { Text("批量顺延到期日", fontWeight = FontWeight.Bold) },
            text = { Text("将根据选中 ${selectedIds.size} 张卡片各自设定的【提醒/续期间隔天数】，统一向后顺延计算新的到期日。") },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = cardList.map { card ->
                            if (selectedIds.contains(card.id)) {
                                val nextCal = Calendar.getInstance().apply {
                                    timeInMillis = card.expiryDateMillis
                                    add(Calendar.DAY_OF_MONTH, if (card.intervalDays > 0) card.intervalDays else 30)
                                }
                                card.copy(expiryDateMillis = nextCal.timeInMillis)
                            } else {
                                card
                            }
                        }
                        onUpdateCards(updated)
                        showBatchExtendDialog = false
                    }
                ) {
                    Text("确认顺延")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchExtendDialog = false }) { Text("取消") }
            }
        )
    }
}

@Composable
fun CategorizedHomeScreen(cardList: List<CardItem>, onEdit: (CardItem) -> Unit) {
    val defaultCategories = listOf("银行卡", "电话卡", "邮箱", "账号", "其他")
    val customCategories = cardList.map { it.category }.distinct().filter { !defaultCategories.contains(it) }
    val categories = defaultCategories + customCategories
    val now = System.currentTimeMillis()

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
                    val urgentCount = categoryCards.count {
                        val diffDays = (it.expiryDateMillis - now) / (1000 * 60 * 60 * 24)
                        diffDays <= 30
                    }

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
                                text = if (urgentCount > 0) "共 ${categoryCards.size} 张 · ${urgentCount}张需处理" else "共 ${categoryCards.size} 张",
                                fontSize = 12.sp,
                                color = if (urgentCount > 0) Color(0xFFFF9800) else Color.Gray,
                                fontWeight = if (urgentCount > 0) FontWeight.Bold else FontWeight.Normal
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
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val now = System.currentTimeMillis()
    val diffDays = (card.expiryDateMillis - now) / (1000 * 60 * 60 * 24)

    Card(
        modifier = Modifier
            .width(220.dp)
            .height(135.dp)
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

            val isDarkBg = card.bgType == "URI" || parseColorHex(card.bgValue).luminance() < 0.5f
            val textColor = if (isDarkBg) Color.White else Color.Black

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        card.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        color = textColor,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (diffDays < 0) {
                        Surface(
                            color = Color(0xFFE53935),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("已过期", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                        }
                    } else if (diffDays <= 30) {
                        Surface(
                            color = if (diffDays <= 3) Color(0xFFE53935) else Color(0xFFFF9800),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("剩${diffDays}天", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (card.cardNumber.isNotBlank()) {
                    Text(card.cardNumber, fontSize = 12.sp, color = textColor.copy(alpha = 0.85f), maxLines = 1)
                }

                if (card.note.isNotBlank()) {
                    Text("备注: ${card.note}", fontSize = 11.sp, color = textColor.copy(alpha = 0.75f), maxLines = 1)
                }
                Text("到期: ${sdf.format(Date(card.expiryDateMillis))}", fontSize = 11.sp, color = textColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ListScreen(
    displayList: List<CardItem>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onTogglePin: (CardItem) -> Unit,
    onLongClickPin: (CardItem) -> Unit,
    onEdit: (CardItem) -> Unit,
    onOperated: (CardItem) -> Unit,
    onDeleteRequest: (CardItem) -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("搜索卡片名称 / 卡号 / 备注...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Box(modifier = Modifier.fillMaxSize()) {
            if (displayList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("未找到相关卡片数据", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(displayList, key = { _, item -> item.id }) { _, card ->
                        SwipeableCardItem(
                            card = card,
                            onTogglePin = { onTogglePin(card) },
                            onLongClickPin = { onLongClickPin(card) },
                            onEdit = { onEdit(card) },
                            onOperated = { onOperated(card) },
                            onDelete = { onDeleteRequest(card) }
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = showScrollToTop,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 20.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "回到顶部")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SwipeableCardItem(
    card: CardItem,
    onTogglePin: () -> Unit,
    onLongClickPin: () -> Unit,
    onEdit: () -> Unit,
    onOperated: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var expandedNote by remember { mutableStateOf(false) }
    var isMasked by remember { mutableStateOf(true) } // 卡号脱敏状态

    val now = System.currentTimeMillis()
    val diffDays = (card.expiryDateMillis - now) / (1000 * 60 * 60 * 24)

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
                .combinedClickable(
                    onClick = { expandedNote = !expandedNote },
                    onLongClick = onLongClickPin
                ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                val isDarkBg = if (card.bgType == "URI") true else parseColorHex(card.bgValue).luminance() < 0.5f

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

                val textColor = if (isDarkBg) Color.White else Color.Black
                val iconActionColor = if (isDarkBg) Color.White.copy(alpha = 0.85f) else Color.Black.copy(alpha = 0.65f)
                val iconPinnedActiveColor = if (isDarkBg) Color(0xFFFFD54F) else MaterialTheme.colorScheme.primary

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
                            
                            if (diffDays < 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(color = Color(0xFFE53935), shape = RoundedCornerShape(4.dp)) {
                                    Text("已过期", color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                                }
                            } else if (diffDays <= 30) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(color = if (diffDays <= 3) Color(0xFFE53935) else Color(0xFFFF9800), shape = RoundedCornerShape(4.dp)) {
                                    Text("剩${diffDays}天", color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Row {
                            IconButton(onClick = onTogglePin) {
                                Icon(
                                    imageVector = if (card.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                    contentDescription = "置顶",
                                    tint = if (card.isPinned) iconPinnedActiveColor else iconActionColor
                                )
                            }
                            IconButton(onClick = onDelete) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "删除", tint = iconActionColor)
                            }
                        }
                    }

                    if (card.cardNumber.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        val displayedCardNum = if (isMasked && card.cardNumber.length > 6) {
                            val start = card.cardNumber.take(4)
                            val end = card.cardNumber.takeLast(4)
                            "$start **** $end"
                        } else card.cardNumber

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("卡号: $displayedCardNum", fontSize = 14.sp, color = textColor.copy(alpha = 0.88f), fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { isMasked = !isMasked },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (isMasked) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                    contentDescription = "显隐卡号",
                                    tint = iconActionColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(card.cardNumber))
                                    Toast.makeText(context, "卡号已复制", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentCopy,
                                    contentDescription = "复制卡号",
                                    tint = iconActionColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    if (card.cost > 0.0) {
                        Text("维护费用: ¥${card.cost} / ${card.costCycle}", fontSize = 12.sp, color = textColor.copy(alpha = 0.8f))
                    }

                    if (card.note.isNotBlank() || card.historyLogs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        AnimatedVisibility(visible = expandedNote) {
                            Surface(
                                color = if (isDarkBg) Color.Black.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (card.note.isNotBlank()) {
                                        Text("备注: ${card.note}", fontSize = 13.sp, color = if (isDarkBg) Color.White else Color.Black)
                                    }
                                    if (card.historyLogs.isNotEmpty()) {
                                        Text("📜 维护历史记录:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        card.historyLogs.take(5).forEach { log ->
                                            Text("• $log", fontSize = 11.sp, color = if (isDarkBg) Color.LightGray else Color.DarkGray)
                                        }
                                    }
                                }
                            }
                        }
                        if (!expandedNote) {
                            Text("点击查看备注与历史日志 (长按置顶)...", fontSize = 11.sp, color = textColor.copy(alpha = 0.7f))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("到期: ${sdf.format(Date(card.expiryDateMillis))}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
                            Text("提醒间隔: ${if(card.intervalDays > 0) "${card.intervalDays}天" else "不提醒"}", fontSize = 12.sp, color = textColor.copy(alpha = 0.82f))
                        }

                        val updateBtnContainerColor = if (isDarkBg) Color.White else Color(0xFF1E88E5)
                        val updateBtnContentColor = if (isDarkBg) Color.Black else Color.White

                        Button(
                            onClick = onOperated,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = updateBtnContainerColor,
                                contentColor = updateBtnContentColor
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Outlined.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("更新", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
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
    val focusManager = LocalFocusManager.current

    val presetCategories = remember { listOf("银行卡", "电话卡", "邮箱", "账号", "其他") }
    val presetIntervals = remember { listOf(0, 3, 7, 15, 30) }
    val costCycles = remember { listOf("每年", "每月", "一次性") }

    val presetColors = remember {
        listOf(
            "0xFFFFFFFF" to "白",
            "0xFFE3F2FD" to "蓝",
            "0xFF1E88E5" to "深蓝",
            "0xFF26A69A" to "绿",
            "0xFFFF7043" to "橙"
        )
    }

    var title by remember { mutableStateOf(initialCard?.title ?: "") }
    var cardNumber by remember { mutableStateOf(initialCard?.cardNumber ?: "") }
    var note by remember { mutableStateOf(initialCard?.note ?: "") }
    
    var costInput by remember { mutableStateOf(if ((initialCard?.cost ?: 0.0) > 0) initialCard!!.cost.toString() else "") }
    var costCycle by remember { mutableStateOf(initialCard?.costCycle ?: "每年") }
    var syncCalendar by remember { mutableStateOf(initialCard?.syncCalendar ?: false) }

    var selectedCategory by remember { mutableStateOf(initialCard?.category ?: presetCategories[0]) }
    var isCustomCategory by remember { mutableStateOf(!presetCategories.contains(initialCard?.category ?: presetCategories[0])) }
    var customCategoryInput by remember { mutableStateOf(if (isCustomCategory) (initialCard?.category ?: "") else "") }

    var bgType by remember { mutableStateOf(initialCard?.bgType ?: "COLOR") }
    var bgValue by remember { mutableStateOf(initialCard?.bgValue ?: "0xFFFFFFFF") }

    val calendar = remember {
        Calendar.getInstance().apply {
            timeInMillis = initialCard?.expiryDateMillis ?: (System.currentTimeMillis() + 86400000L * 30)
        }
    }

    var selectedYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH) + 1) }
    var selectedDay by remember { mutableIntStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }

    var selectedInterval by remember { mutableIntStateOf(initialCard?.intervalDays ?: 30) }
    var isCustomInterval by remember { mutableStateOf(!presetIntervals.contains(initialCard?.intervalDays ?: 30)) }
    var customIntervalInput by remember { mutableStateOf(if (isCustomInterval) (initialCard?.intervalDays ?: 30).toString() else "") }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {
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

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = costInput,
                    onValueChange = { costInput = it },
                    label = { Text("维护费用 (选填)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text("周期", fontSize = 12.sp, color = Color.Gray)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        costCycles.forEach { cycle ->
                            FilterChip(
                                selected = costCycle == cycle,
                                onClick = { costCycle = cycle },
                                label = { Text(cycle, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("备注信息 (选填)") },
                minLines = 2,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = syncCalendar, onCheckedChange = { syncCalendar = it })
                Spacer(modifier = Modifier.width(4.dp))
                Text("同时自动同步到手机系统日历日程", fontSize = 13.sp)
            }

            Text("卡片分类:", fontSize = 13.sp, color = Color.Gray)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                presetCategories.forEach { cat ->
                    FilterChip(
                        selected = !isCustomCategory && selectedCategory == cat,
                        onClick = {
                            isCustomCategory = false
                            selectedCategory = cat
                        },
                        label = { Text(cat) }
                    )
                }
                FilterChip(
                    selected = isCustomCategory,
                    onClick = { isCustomCategory = true },
                    label = { Text("自定义分类") }
                )
            }

            if (isCustomCategory) {
                OutlinedTextField(
                    value = customCategoryInput,
                    onValueChange = { customCategoryInput = it },
                    label = { Text("输入自定义分类 (如: 会员卡/公积金)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
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

            Text("提醒/续期间隔天数:", fontSize = 13.sp, color = Color.Gray)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presetIntervals.forEach { days ->
                    FilterChip(
                        selected = !isCustomInterval && selectedInterval == days,
                        onClick = {
                            isCustomInterval = false
                            selectedInterval = days
                        },
                        label = { Text(if (days == 0) "不提醒" else "${days}天") }
                    )
                }
                FilterChip(
                    selected = isCustomInterval,
                    onClick = { isCustomInterval = true },
                    label = { Text("自定义") }
                )
            }

            if (isCustomInterval) {
                OutlinedTextField(
                    value = customIntervalInput,
                    onValueChange = { customIntervalInput = it.filter { char -> char.isDigit() } },
                    label = { Text("输入自定义间隔天数 (如: 60)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (title.isBlank()) return@Button

                    val saveCalendar = Calendar.getInstance().apply {
                        set(Calendar.YEAR, selectedYear)
                        set(Calendar.MONTH, selectedMonth - 1)
                        set(Calendar.DAY_OF_MONTH, selectedDay)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                    }

                    val finalCategory = if (isCustomCategory && customCategoryInput.isNotBlank()) customCategoryInput.trim() else selectedCategory

                    val finalInterval = if (isCustomInterval) {
                        customIntervalInput.toIntOrNull() ?: 30
                    } else {
                        selectedInterval
                    }

                    val card = CardItem(
                        id = initialCard?.id ?: UUID.randomUUID().toString(),
                        title = title,
                        cardNumber = cardNumber,
                        category = finalCategory,
                        note = note,
                        expiryDateMillis = saveCalendar.timeInMillis,
                        intervalDays = finalInterval,
                        isPinned = initialCard?.isPinned ?: false,
                        pinTime = initialCard?.pinTime ?: 0L,
                        bgType = bgType,
                        bgValue = bgValue,
                        cost = costInput.toDoubleOrNull() ?: 0.0,
                        costCycle = costCycle,
                        syncCalendar = syncCalendar,
                        historyLogs = initialCard?.historyLogs ?: emptyList()
                    )
                    onSave(card)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存卡片信息", fontSize = 16.sp)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    currentTheme: AppTheme,
    cardList: List<CardItem>,
    onThemeChanged: (AppTheme) -> Unit,
    onBatchManageClick: () -> Unit,
    onExportClick: () -> Unit,
    onExportTemplateClick: () -> Unit,
    onImportFileParsed: (List<CardItem>) -> Unit,
    onRestoreClick: () -> Unit
) {
    val context = LocalContext.current
    val lastExportTime = remember { CardStorage.getLastExportTime(context) }
    var biometricEnabled by remember { mutableStateOf(CardStorage.isBiometricEnabled(context)) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val parsedCards = ExcelExportImportHelper.parseCsvFromUri(context, it)
            onImportFileParsed(parsedCards)
        }
    }

    // 统计年度总支出与月度均摊支出
    val (annualTotal, monthlyAvg) = remember(cardList) {
        var total = 0.0
        cardList.forEach {
            when (it.costCycle) {
                "每年" -> total += it.cost
                "每月" -> total += it.cost * 12
                "一次性" -> total += it.cost
            }
        }
        Pair(total, total / 12.0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        
        val iconResId = remember(context) {
            context.resources.getIdentifier("app_icon", "drawable", context.packageName)
        }
        if (iconResId != 0) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = "应用卡片封面",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
        } else {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(90.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("卡片提醒助手 v4.0", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text("已安全管理 ${cardList.size} 张卡片", color = Color.Gray, fontSize = 13.sp)
        }

        // 维护费用支出统计看板
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("📊 维护与续费支出看板", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("年均维护支出预估", fontSize = 12.sp, color = Color.Gray)
                        Text(String.format("¥ %.2f", annualTotal), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("月均均摊支出", fontSize = 12.sp, color = Color.Gray)
                        Text(String.format("¥ %.2f", monthlyAvg), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF26A69A))
                    }
                }
            }
        }

        // 批量管理入口
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBatchManageClick() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Checklist,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("卡片批量整理与管理", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("多选、批量删除、批量改分类及批量顺延", fontSize = 12.sp, color = Color.Gray)
                    }
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
            }
        }

        // 安全与隐私设置
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("应用生物识别锁 (指纹/面容)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("开启后启动应用需进行身份验证", fontSize = 12.sp, color = Color.Gray)
                }
                Switch(
                    checked = biometricEnabled,
                    onCheckedChange = {
                        biometricEnabled = it
                        CardStorage.setBiometricEnabled(context, it)
                    }
                )
            }
        }

        // 表格导入导出与备份
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
                Text("数据备份与表格导入导出", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    if (lastExportTime != null) "上次导出备份时间: $lastExportTime" else "支持与 Excel (.csv) 格式交互，建议定期备份",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onExportClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("导出表格")
                    }

                    OutlinedButton(
                        onClick = { importLauncher.launch("*/*") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("导入表格")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onExportTemplateClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Outlined.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("下载导入模板", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onRestoreClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Outlined.History, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("恢复上次备份", fontSize = 12.sp)
                    }
                }
            }
        }

        // 外观主题
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

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AppTheme.values().forEach { theme ->
                        FilterChip(
                            selected = currentTheme == theme,
                            onClick = { onThemeChanged(theme) },
                            label = { 
                                Text(theme.displayName) 
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
