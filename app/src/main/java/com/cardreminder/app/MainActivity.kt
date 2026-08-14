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

// 多语言枚举
enum class AppLanguage(val code: String, val displayName: String) {
    ZH_CN("zh_CN", "简体中文"),
    ZH_TW("zh_TW", "繁體中文"),
    EN("en", "English"),
    JA("ja", "日本語"),
    PT("pt", "Português")
}

// 多语言字典提供者
object StringsProvider {
    fun get(key: String, lang: AppLanguage): String {
        return when (lang) {
            AppLanguage.ZH_TW -> when (key) {
                "nav_home" -> "首頁"
                "nav_list" -> "列表"
                "nav_add" -> "新增"
                "nav_mine" -> "我的"
                "title_home" -> "分類瀏覽"
                "title_list" -> "我的卡片"
                "title_add" -> "新增卡片"
                "title_edit" -> "編輯卡片"
                "title_mine" -> "個人中心"
                "title_batch" -> "批量卡片整理"
                "search_hint" -> "搜尋卡片名稱 / 卡號 / 備註..."
                "empty_data" -> "暫無卡片數據，點擊底部“新增”添加"
                "expired" -> "已過期"
                "days_left" -> "剩%d天"
                "expire_date" -> "到期"
                "remind_interval" -> "提醒間隔"
                "no_remind" -> "不提醒"
                "update" -> "更新"
                "confirm_update" -> "更新到期日確認"
                "confirm_update_desc" -> "將基於提醒間隔 (%d天)，順延更新到期日期為："
                "btn_confirm_update" -> "確認更新"
                "cancel" -> "取消"
                "card_name" -> "名稱 (如: 招商信用卡)"
                "card_number" -> "卡號 / 帳號 (選填)"
                "note" -> "備註信息 (選填)"
                "sync_calendar" -> "同時自動同步到手機系統行事曆"
                "category" -> "卡片分類:"
                "custom_category" -> "自訂分類"
                "custom_category_hint" -> "輸入自訂分類 (如: 會員卡)"
                "custom_bg" -> "自訂背景樣式:"
                "select_photo" -> "選擇相簿"
                "photo_selected" -> "已選相簿圖片"
                "expire_date_label" -> "到期日期:"
                "remind_interval_label" -> "提醒/續期間隔天數:"
                "custom_interval" -> "自訂"
                "custom_interval_hint" -> "輸入自訂間隔天數 (如: 60)"
                "save_card" -> "儲存卡片信息"
                "batch_manage" -> "卡片批量整理與管理"
                "batch_manage_desc" -> "多選、批量刪除、批量改分類及批量順延"
                "biometric_title" -> "應用生物識別鎖 (指紋/面容)"
                "biometric_desc" -> "開啟後啟動應用需進行身分驗證"
                "data_backup" -> "數據備份與表格導入導出"
                "export_csv" -> "導出表格"
                "import_csv" -> "導入表格"
                "download_template" -> "下載導入模板"
                "restore_backup" -> "恢復上次備份"
                "theme_setting" -> "軟體外觀主題設置"
                "lang_setting" -> "軟體語言設置 (Language)"
                "all" -> "全部"
                "bank_card" -> "銀行卡"
                "sim_card" -> "電話卡"
                "email" -> "郵箱"
                "account" -> "帳號"
                "other" -> "其他"
                "day" -> "天"
                else -> key
            }
            AppLanguage.EN -> when (key) {
                "nav_home" -> "Home"
                "nav_list" -> "Cards"
                "nav_add" -> "Add"
                "nav_mine" -> "Profile"
                "title_home" -> "Categories"
                "title_list" -> "My Cards"
                "title_add" -> "Add Card"
                "title_edit" -> "Edit Card"
                "title_mine" -> "Profile"
                "title_batch" -> "Batch Management"
                "search_hint" -> "Search name / card number / notes..."
                "empty_data" -> "No cards found. Tap 'Add' to create one."
                "expired" -> "Expired"
                "days_left" -> "%d days left"
                "expire_date" -> "Expiry"
                "remind_interval" -> "Interval"
                "no_remind" -> "None"
                "update" -> "Renew"
                "confirm_update" -> "Confirm Renewal"
                "confirm_update_desc" -> "Extend expiry date based on interval (%d days) to:"
                "btn_confirm_update" -> "Confirm"
                "cancel" -> "Cancel"
                "card_name" -> "Card Name (e.g. Visa Debit)"
                "card_number" -> "Card / Account Number (Optional)"
                "note" -> "Notes (Optional)"
                "sync_calendar" -> "Sync to device system calendar"
                "category" -> "Category:"
                "custom_category" -> "Custom"
                "custom_category_hint" -> "Enter custom category"
                "custom_bg" -> "Card Style:"
                "select_photo" -> "Gallery"
                "photo_selected" -> "Image Selected"
                "expire_date_label" -> "Expiry Date:"
                "remind_interval_label" -> "Renewal Interval Days:"
                "custom_interval" -> "Custom"
                "custom_interval_hint" -> "Enter custom days (e.g. 60)"
                "save_card" -> "Save Card Information"
                "batch_manage" -> "Batch Card Management"
                "batch_manage_desc" -> "Select, delete, change category and renew in batch"
                "biometric_title" -> "Biometric App Lock (Fingerprint/Face)"
                "biometric_desc" -> "Require authentication when opening app"
                "data_backup" -> "Data Backup & CSV Excel"
                "export_csv" -> "Export CSV"
                "import_csv" -> "Import CSV"
                "download_template" -> "Download Template"
                "restore_backup" -> "Restore Backup"
                "theme_setting" -> "App Theme & Appearance"
                "lang_setting" -> "Language Settings"
                "all" -> "All"
                "bank_card" -> "Bank Card"
                "sim_card" -> "SIM Card"
                "email" -> "Email"
                "account" -> "Account"
                "other" -> "Others"
                "day" -> "d"
                else -> key
            }
            AppLanguage.JA -> when (key) {
                "nav_home" -> "ホーム"
                "nav_list" -> "カード"
                "nav_add" -> "追加"
                "nav_mine" -> "マイ"
                "title_home" -> "カテゴリ一覧"
                "title_list" -> "マイカード"
                "title_add" -> "カード追加"
                "title_edit" -> "カード編集"
                "title_mine" -> "マイページ"
                "title_batch" -> "一括整理"
                "search_hint" -> "名前・番号・メモを検索..."
                "empty_data" -> "カードがありません。「追加」から作成してください"
                "expired" -> "期限切れ"
                "days_left" -> "残り%d日"
                "expire_date" -> "有効期限"
                "remind_interval" -> "更新間隔"
                "no_remind" -> "なし"
                "update" -> "更新"
                "confirm_update" -> "期限更新の確認"
                "confirm_update_desc" -> "更新間隔（%d日）に基づいて有効期限を延長します："
                "btn_confirm_update" -> "更新する"
                "cancel" -> "キャンセル"
                "card_name" -> "カード名 (例: 楽天カード)"
                "card_number" -> "カード番号 / 口座 (任意)"
                "note" -> "備考 (任意)"
                "sync_calendar" -> "スマホのカレンダーに自動同期する"
                "category" -> "カテゴリ:"
                "custom_category" -> "カスタム"
                "custom_category_hint" -> "カスタムカテゴリを入力"
                "custom_bg" -> "背景スタイル:"
                "select_photo" -> "写真選択"
                "photo_selected" -> "画像選択済み"
                "expire_date_label" -> "有効期限:"
                "remind_interval_label" -> "更新間隔日数:"
                "custom_interval" -> "カスタム"
                "custom_interval_hint" -> "日数を入力 (例: 60)"
                "save_card" -> "カード情報を保存"
                "batch_manage" -> "カード一括整理・管理"
                "batch_manage_desc" -> "複数選択、一括削除、カテゴリ変更、一括更新"
                "biometric_title" -> "生体認証ロック (指紋・顔認証)"
                "biometric_desc" -> "起動時に認証を要求します"
                "data_backup" -> "データバックアップとExcel入出力"
                "export_csv" -> "CSV出力"
                "import_csv" -> "CSV読込"
                "download_template" -> "テンプレート取得"
                "restore_backup" -> "バックアップ復元"
                "theme_setting" -> "外観テーマ設定"
                "lang_setting" -> "言語設定 (Language)"
                "all" -> "すべて"
                "bank_card" -> "銀行カード"
                "sim_card" -> "SIMカード"
                "email" -> "メール"
                "account" -> "アカウント"
                "other" -> "その他"
                "day" -> "日"
                else -> key
            }
            AppLanguage.PT -> when (key) {
                "nav_home" -> "Início"
                "nav_list" -> "Cartões"
                "nav_add" -> "Adicionar"
                "nav_mine" -> "Perfil"
                "title_home" -> "Categorias"
                "title_list" -> "Meus Cartões"
                "title_add" -> "Adicionar Cartão"
                "title_edit" -> "Editar Cartão"
                "title_mine" -> "Perfil"
                "title_batch" -> "Gerenciamento em Lote"
                "search_hint" -> "Buscar por nome / número / notas..."
                "empty_data" -> "Nenhum cartão. Toque em 'Adicionar' para criar."
                "expired" -> "Expirado"
                "days_left" -> "%d dias restantes"
                "expire_date" -> "Validade"
                "remind_interval" -> "Intervalo"
                "no_remind" -> "Nenhum"
                "update" -> "Renovar"
                "confirm_update" -> "Confirmar Renovação"
                "confirm_update_desc" -> "Estender a validade com base no intervalo (%d dias) para:"
                "btn_confirm_update" -> "Confirmar"
                "cancel" -> "Cancelar"
                "card_name" -> "Nome do Cartão"
                "card_number" -> "Número do Cartão / Conta (Opcional)"
                "note" -> "Notas (Opcional)"
                "sync_calendar" -> "Sincronizar com o calendário do sistema"
                "category" -> "Categoria:"
                "custom_category" -> "Personalizado"
                "custom_category_hint" -> "Insira a categoria personalizada"
                "custom_bg" -> "Estilo do Cartão:"
                "select_photo" -> "Galeria"
                "photo_selected" -> "Imagem Selecionada"
                "expire_date_label" -> "Data de Validade:"
                "remind_interval_label" -> "Dias de Intervalo de Renovação:"
                "custom_interval" -> "Personalizado"
                "custom_interval_hint" -> "Insira os dias (ex: 60)"
                "save_card" -> "Salvar Cartão"
                "batch_manage" -> "Gerenciamento em Lote"
                "batch_manage_desc" -> "Selecionar, excluir, alterar categoria e renovar"
                "biometric_title" -> "Bloqueio Biométrico (Digital/Face)"
                "biometric_desc" -> "Exigir autenticação ao abrir o aplicativo"
                "data_backup" -> "Backup de Dados e CSV Excel"
                "export_csv" -> "Exportar CSV"
                "import_csv" -> "Importar CSV"
                "download_template" -> "Baixar Modelo"
                "restore_backup" -> "Restaurar Backup"
                "theme_setting" -> "Tema e Aparência"
                "lang_setting" -> "Configurações de Idioma"
                "all" -> "Todos"
                "bank_card" -> "Cartão Bancário"
                "sim_card" -> "Cartão SIM"
                "email" -> "E-mail"
                "account" -> "Conta"
                "other" -> "Outros"
                "day" -> "d"
                else -> key
            }
            AppLanguage.ZH_CN -> when (key) {
                "nav_home" -> "首页"
                "nav_list" -> "列表"
                "nav_add" -> "新增"
                "nav_mine" -> "我的"
                "title_home" -> "分类浏览"
                "title_list" -> "我的卡片"
                "title_add" -> "新增卡片"
                "title_edit" -> "编辑卡片"
                "title_mine" -> "个人中心"
                "title_batch" -> "批量卡片整理"
                "search_hint" -> "搜索卡片名称 / 卡号 / 备注..."
                "empty_data" -> "暂无卡片数据，点击底部“新增”添加"
                "expired" -> "已过期"
                "days_left" -> "剩%d天"
                "expire_date" -> "到期"
                "remind_interval" -> "提醒间隔"
                "no_remind" -> "不提醒"
                "update" -> "更新"
                "confirm_update" -> "更新到期日确认"
                "confirm_update_desc" -> "将基于提醒间隔 (%d天)，顺延更新到期日期为："
                "btn_confirm_update" -> "确认更新"
                "cancel" -> "取消"
                "card_name" -> "名称 (如: 招商信用卡)"
                "card_number" -> "卡号 / 账号 (选填)"
                "note" -> "备注信息 (选填)"
                "sync_calendar" -> "同时自动同步到手机系统日历日程"
                "category" -> "卡片分类:"
                "custom_category" -> "自定义分类"
                "custom_category_hint" -> "输入自定义分类 (如: 会员卡/公积金)"
                "custom_bg" -> "自定义背景样式:"
                "select_photo" -> "选择相册"
                "photo_selected" -> "已选相册图片"
                "expire_date_label" -> "到期日期:"
                "remind_interval_label" -> "提醒/续期间隔天数:"
                "custom_interval" -> "自定义"
                "custom_interval_hint" -> "输入自定义间隔天数 (如: 60)"
                "save_card" -> "保存卡片信息"
                "batch_manage" -> "卡片批量整理与管理"
                "batch_manage_desc" -> "多选、批量删除、批量改分类及批量顺延"
                "biometric_title" -> "应用生物识别锁 (指纹/面容)"
                "biometric_desc" -> "开启后启动应用需进行身份验证"
                "data_backup" -> "数据备份与表格导入导出"
                "export_csv" -> "导出表格"
                "import_csv" -> "导入表格"
                "download_template" -> "下载导入模板"
                "restore_backup" -> "恢复上次备份"
                "theme_setting" -> "软件外观主题设置"
                "lang_setting" -> "软件语言设置 (Language)"
                "all" -> "全部"
                "bank_card" -> "银行卡"
                "sim_card" -> "电话卡"
                "email" -> "邮箱"
                "account" -> "账号"
                "other" -> "其他"
                "day" -> "天"
                else -> key
            }
        }
    }
}

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
    private const val KEY_LANGUAGE = "key_app_language"
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

    fun saveLanguage(context: Context, lang: AppLanguage) {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sp.edit().putString(KEY_LANGUAGE, lang.name).apply()
    }

    fun loadLanguage(context: Context): AppLanguage {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val name = sp.getString(KEY_LANGUAGE, AppLanguage.ZH_CN.name)
        return try { AppLanguage.valueOf(name!!) } catch (e: Exception) { AppLanguage.ZH_CN }
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
                        put(CalendarContract.Reminders.MINUTES, 24 * 60)
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
            writer.write("名称,卡号,分类,到期日期,提醒间隔天数,备注\n")

            cards.forEach { card ->
                val dateStr = sdf.format(Date(card.expiryDateMillis))
                val line = "\"${card.title.replace("\"", "\"\"")}\"," +
                           "\"${card.cardNumber.replace("\"", "\"\"")}\"," +
                           "\"${card.category.replace("\"", "\"\"")}\"," +
                           "\"$dateStr\"," +
                           "${card.intervalDays}," +
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
            writer.write("名称,卡号,分类,到期日期,提醒间隔天数,备注\n")
            writer.write("\"招商银行信用卡\",\"6225888899990000\",\"银行卡\",\"2026-12-31\",30,\"每月账单日5号\"\n")
            writer.write("\"香港手机卡\",\"+852 98765432\",\"电话卡\",\"2026-10-15\",180,\"需每半年发一条短信保号\"\n")
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
                    val note = tokens.getOrNull(5) ?: ""

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
    var currentLanguage by remember { mutableStateOf(CardStorage.loadLanguage(context)) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf(StringsProvider.get("all", currentLanguage)) }
    var currentSortOrder by remember { mutableStateOf(SortOrder.NONE) }
    var editingCard by remember { mutableStateOf<CardItem?>(null) }
    var deletingCard by remember { mutableStateOf<CardItem?>(null) }
    var pinDialogCard by remember { mutableStateOf<CardItem?>(null) }
    var operateConfirmCard by remember { mutableStateOf<CardItem?>(null) }
    var filterMenuExpanded by remember { mutableStateOf(false) }

    var isBatchManaging by remember { mutableStateOf(false) }
    var importPendingCards by remember { mutableStateOf<List<CardItem>?>(null) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }

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

    val allCategories = remember(cardList, currentLanguage) {
        val defaultList = listOf(
            StringsProvider.get("all", currentLanguage),
            StringsProvider.get("bank_card", currentLanguage),
            StringsProvider.get("sim_card", currentLanguage),
            StringsProvider.get("email", currentLanguage),
            StringsProvider.get("account", currentLanguage),
            StringsProvider.get("other", currentLanguage)
        )
        val customList = cardList.map { it.category }.distinct().filter { !defaultList.contains(it) }
        defaultList + customList
    }

    val now = System.currentTimeMillis()
    val displayList = remember(cardList, searchQuery, selectedCategoryFilter, currentSortOrder, currentLanguage) {
        val allStr = StringsProvider.get("all", currentLanguage)
        val filtered = cardList.filter {
            (selectedCategoryFilter == allStr || it.category == selectedCategoryFilter) &&
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
                            if (isBatchManaging) StringsProvider.get("title_batch", currentLanguage)
                            else when (selectedTab) {
                                0 -> StringsProvider.get("title_home", currentLanguage)
                                1 -> StringsProvider.get("title_list", currentLanguage)
                                2 -> if (editingCard == null) StringsProvider.get("title_add", currentLanguage) else StringsProvider.get("title_edit", currentLanguage)
                                else -> StringsProvider.get("title_mine", currentLanguage)
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
                            icon = { Icon(Icons.Default.Home, contentDescription = null) },
                            label = { Text(StringsProvider.get("nav_home", currentLanguage)) }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                            label = { Text(StringsProvider.get("nav_list", currentLanguage)) }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = {
                                editingCard = null
                                selectedTab = 2
                            },
                            icon = { Icon(Icons.Default.AddCircle, contentDescription = null) },
                            label = { Text(StringsProvider.get("nav_add", currentLanguage)) }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            icon = { Icon(Icons.Default.Person, contentDescription = null) },
                            label = { Text(StringsProvider.get("nav_mine", currentLanguage)) }
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
                        currentLanguage = currentLanguage,
                        onBack = { isBatchManaging = false },
                        onUpdateCards = { updatedList ->
                            CardStorage.backupCurrentCards(context, cardList)
                            cardList = updatedList
                            CardStorage.saveCards(context, updatedList)
                        }
                    )
                } else {
                    when (selectedTab) {
                        0 -> CategorizedHomeScreen(cardList = cardList, currentLanguage = currentLanguage, onEdit = { card ->
                            editingCard = card
                            selectedTab = 2
                        })
                        1 -> ListScreen(
                            displayList = displayList,
                            searchQuery = searchQuery,
                            currentLanguage = currentLanguage,
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
                            currentLanguage = currentLanguage,
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
                            currentLanguage = currentLanguage,
                            cardCount = cardList.size,
                            onThemeChanged = { newTheme ->
                                currentTheme = newTheme
                                CardStorage.saveTheme(context, newTheme)
                            },
                            onLanguageChanged = { newLang ->
                                currentLanguage = newLang
                                CardStorage.saveLanguage(context, newLang)
                                selectedCategoryFilter = StringsProvider.get("all", newLang)
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
            title = { Text(StringsProvider.get("confirm_update", currentLanguage), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${StringsProvider.get("card_name", currentLanguage).take(2)}: ${card.title}")
                    Text("${StringsProvider.get("expire_date", currentLanguage)}: $currentExpiryStr", color = Color.Gray, fontSize = 13.sp)
                    Text(
                        String.format(StringsProvider.get("confirm_update_desc", currentLanguage), interval),
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
                        Toast.makeText(context, "更新成功！", Toast.LENGTH_SHORT).show()
                        operateConfirmCard = null
                    }
                ) {
                    Text(StringsProvider.get("btn_confirm_update", currentLanguage))
                }
            },
            dismissButton = {
                TextButton(onClick = { operateConfirmCard = null }) {
                    Text(StringsProvider.get("cancel", currentLanguage))
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
    currentLanguage: AppLanguage,
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
                            Text("${StringsProvider.get("expire_date", currentLanguage)}: ${sdf.format(Date(card.expiryDateMillis))}", fontSize = 12.sp, color = Color.Gray)
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
fun CategorizedHomeScreen(cardList: List<CardItem>, currentLanguage: AppLanguage, onEdit: (CardItem) -> Unit) {
    val defaultCategories = listOf(
        StringsProvider.get("bank_card", currentLanguage),
        StringsProvider.get("sim_card", currentLanguage),
        StringsProvider.get("email", currentLanguage),
        StringsProvider.get("account", currentLanguage),
        StringsProvider.get("other", currentLanguage)
    )
    val customCategories = cardList.map { it.category }.distinct().filter { !defaultCategories.contains(it) }
    val categories = defaultCategories + customCategories
    val now = System.currentTimeMillis()

    if (cardList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(StringsProvider.get("empty_data", currentLanguage), color = Color.Gray)
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
                                HorizontalCardItem(card = card, currentLanguage = currentLanguage, onClick = { onEdit(card) })
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
fun HorizontalCardItem(card: CardItem, currentLanguage: AppLanguage, onClick: () -> Unit) {
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
                            Text(StringsProvider.get("expired", currentLanguage), color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                        }
                    } else if (diffDays <= 30) {
                        Surface(
                            color = if (diffDays <= 3) Color(0xFFE53935) else Color(0xFFFF9800),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(String.format(StringsProvider.get("days_left", currentLanguage), diffDays), color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (card.cardNumber.isNotBlank()) {
                    Text(card.cardNumber, fontSize = 12.sp, color = textColor.copy(alpha = 0.85f), maxLines = 1)
                }

                if (card.note.isNotBlank()) {
                    Text("备注: ${card.note}", fontSize = 11.sp, color = textColor.copy(alpha = 0.75f), maxLines = 1)
                }
                Text("${StringsProvider.get("expire_date", currentLanguage)}: ${sdf.format(Date(card.expiryDateMillis))}", fontSize = 11.sp, color = textColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ListScreen(
    displayList: List<CardItem>,
    searchQuery: String,
    currentLanguage: AppLanguage,
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
            placeholder = { Text(StringsProvider.get("search_hint", currentLanguage)) },
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
                    Text(StringsProvider.get("empty_data", currentLanguage), color = Color.Gray)
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
                            currentLanguage = currentLanguage,
                            onTogglePin = { onTogglePin(card) },
                            onLongClickPin = { onLongClickPin(card) },
                            onEdit = { onEdit(card) },
                            onOperated = { onOperated(card) },
                            onDelete = { onDeleteRequest(card) }
                        )
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(
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
    currentLanguage: AppLanguage,
    onTogglePin: () -> Unit,
    onLongClickPin: () -> Unit,
    onEdit: () -> Unit,
    onOperated: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var expandedNote by remember { mutableStateOf(false) }
    var isMasked by remember { mutableStateOf(true) }

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
                                    Text(StringsProvider.get("expired", currentLanguage), color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                                }
                            } else if (diffDays <= 30) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(color = if (diffDays <= 3) Color(0xFFE53935) else Color(0xFFFF9800), shape = RoundedCornerShape(4.dp)) {
                                    Text(String.format(StringsProvider.get("days_left", currentLanguage), diffDays), color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
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
                                    contentDescription = null,
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
                                    contentDescription = null,
                                    tint = iconActionColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    if (card.note.isNotBlank() || card.historyLogs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        androidx.compose.animation.AnimatedVisibility(visible = expandedNote) {
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
                            Text("${StringsProvider.get("expire_date", currentLanguage)}: ${sdf.format(Date(card.expiryDateMillis))}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
                            Text("${StringsProvider.get("remind_interval", currentLanguage)}: ${if(card.intervalDays > 0) "${card.intervalDays}${StringsProvider.get("day", currentLanguage)}" else StringsProvider.get("no_remind", currentLanguage)}", fontSize = 12.sp, color = textColor.copy(alpha = 0.82f))
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
                            Text(StringsProvider.get("update", currentLanguage), fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
    currentLanguage: AppLanguage,
    onSave: (CardItem) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val presetCategories = remember(currentLanguage) {
        listOf(
            StringsProvider.get("bank_card", currentLanguage),
            StringsProvider.get("sim_card", currentLanguage),
            StringsProvider.get("email", currentLanguage),
            StringsProvider.get("account", currentLanguage),
            StringsProvider.get("other", currentLanguage)
        )
    }
    val presetIntervals = remember { listOf(0, 3, 7, 15, 30) }

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
                label = { Text(StringsProvider.get("card_name", currentLanguage)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = cardNumber,
                onValueChange = { cardNumber = it },
                label = { Text(StringsProvider.get("card_number", currentLanguage)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(StringsProvider.get("note", currentLanguage)) },
                minLines = 2,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = syncCalendar, onCheckedChange = { syncCalendar = it })
                Spacer(modifier = Modifier.width(4.dp))
                Text(StringsProvider.get("sync_calendar", currentLanguage), fontSize = 13.sp)
            }

            Text(StringsProvider.get("category", currentLanguage), fontSize = 13.sp, color = Color.Gray)
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
                    label = { Text(StringsProvider.get("custom_category", currentLanguage)) }
                )
            }

            if (isCustomCategory) {
                OutlinedTextField(
                    value = customCategoryInput,
                    onValueChange = { customCategoryInput = it },
                    label = { Text(StringsProvider.get("custom_category_hint", currentLanguage)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Text(StringsProvider.get("custom_bg", currentLanguage), fontSize = 13.sp, color = Color.Gray)
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
                    Text(if (bgType == "URI") StringsProvider.get("photo_selected", currentLanguage) else StringsProvider.get("select_photo", currentLanguage), fontSize = 12.sp)
                }
            }

            Text(StringsProvider.get("expire_date_label", currentLanguage), fontSize = 13.sp, color = Color.Gray)
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

            Text(StringsProvider.get("remind_interval_label", currentLanguage), fontSize = 13.sp, color = Color.Gray)
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
                        label = { Text(if (days == 0) StringsProvider.get("no_remind", currentLanguage) else "${days}${StringsProvider.get("day", currentLanguage)}") }
                    )
                }
                FilterChip(
                    selected = isCustomInterval,
                    onClick = { isCustomInterval = true },
                    label = { Text(StringsProvider.get("custom_interval", currentLanguage)) }
                )
            }

            if (isCustomInterval) {
                OutlinedTextField(
                    value = customIntervalInput,
                    onValueChange = { customIntervalInput = it.filter { char -> char.isDigit() } },
                    label = { Text(StringsProvider.get("custom_interval_hint", currentLanguage)) },
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
                        syncCalendar = syncCalendar,
                        historyLogs = initialCard?.historyLogs ?: emptyList()
                    )
                    onSave(card)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(StringsProvider.get("save_card", currentLanguage), fontSize = 16.sp)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    currentTheme: AppTheme,
    currentLanguage: AppLanguage,
    cardCount: Int,
    onThemeChanged: (AppTheme) -> Unit,
    onLanguageChanged: (AppLanguage) -> Unit,
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
            Text("卡片提醒助手 v4.1", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text("已安全管理 $cardCount 张卡片", color = Color.Gray, fontSize = 13.sp)
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
                        Text(StringsProvider.get("batch_manage", currentLanguage), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(StringsProvider.get("batch_manage_desc", currentLanguage), fontSize = 12.sp, color = Color.Gray)
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
                    Text(StringsProvider.get("biometric_title", currentLanguage), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(StringsProvider.get("biometric_desc", currentLanguage), fontSize = 12.sp, color = Color.Gray)
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
                Text(StringsProvider.get("data_backup", currentLanguage), fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                        Text(StringsProvider.get("export_csv", currentLanguage))
                    }

                    OutlinedButton(
                        onClick = { importLauncher.launch("*/*") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(StringsProvider.get("import_csv", currentLanguage))
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
                        Text(StringsProvider.get("download_template", currentLanguage), fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onRestoreClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Outlined.History, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(StringsProvider.get("restore_backup", currentLanguage), fontSize = 12.sp)
                    }
                }
            }
        }

        // 多语言设置
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
                Text(StringsProvider.get("lang_setting", currentLanguage), fontWeight = FontWeight.Bold, fontSize = 16.sp)

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AppLanguage.values().forEach { lang ->
                        FilterChip(
                            selected = currentLanguage == lang,
                            onClick = { onLanguageChanged(lang) },
                            label = { Text(lang.displayName) },
                            leadingIcon = {
                                if (currentLanguage == lang) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        )
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
                Text(StringsProvider.get("theme_setting", currentLanguage), fontWeight = FontWeight.Bold, fontSize = 16.sp)

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
