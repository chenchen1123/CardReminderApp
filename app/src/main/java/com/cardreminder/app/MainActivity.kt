package com.cardreminder.app

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
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

enum class AppLanguage(val code: String, val displayName: String) {
    ZH_CN("zh_CN", "简体中文"),
    ZH_TW("zh_TW", "繁體中文"),
    EN("en", "English"),
    JA("ja", "日本語"),
    PT("pt", "Português")
}

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
                "title_history" -> "操作記錄與回滾"
                "search_hint" -> "搜尋卡片名稱 / 卡號 / 備註..."
                "empty_data" -> "暫無卡片數據，點擊底部“新增”添加"
                "empty_search" -> "未找到相關卡片數據"
                "empty_history" -> "暫無任何操作記錄"
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
                "confirm" -> "確認"
                "back" -> "返回"
                "card_name" -> "名稱 (如: 招商信用卡)"
                "card_number" -> "卡號 / 帳號 (選填)"
                "note" -> "備註信息 (選填)"
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
                "history_timeline" -> "📜 最近操作記錄 (最近100條)"
                "history_desc" -> "支援查看每一次改動並一鍵恢復歷史狀態"
                "history_total_cards" -> "對應卡片總數: %d 張"
                "history_record_count" -> "操作改動記錄 (%d/100)"
                "history_confirm_title" -> "確認恢復至此狀態？"
                "history_confirm_desc" -> "將卡片列表回滾至【%s】操作時的狀態（共 %d 張卡片）。"
                "btn_restore_this" -> "恢復至此狀態"
                "batch_manage" -> "卡片批量整理與管理"
                "batch_manage_desc" -> "多選、批量刪除、批量改分類及批量順延"
                "batch_selected_count" -> "已勾選: %d / %d 張"
                "batch_select_all" -> "全選"
                "batch_unselect_all" -> "取消全選"
                "batch_done_back" -> "完成返回"
                "batch_delete_selected" -> "刪除所選"
                "batch_change_category" -> "修改分類"
                "batch_extend_date" -> "順延到期日"
                "batch_clear_all" -> "清空全部"
                "batch_delete_confirm_title" -> "確認批量刪除"
                "batch_delete_confirm_desc" -> "確定要刪除選中的 %d 張卡片嗎？此操作不可逆（已自動備份）。"
                "batch_clear_confirm_title" -> "危險：確認清空全部卡片？"
                "batch_clear_confirm_desc" -> "將徹底清空當前所有卡片數據！系統已為您自動快照備份，隨時可在“我的”頁面恢復。"
                "batch_category_confirm_title" -> "批量修改分類"
                "batch_category_confirm_desc" -> "將選中的 %d 張卡片統一更改分類為："
                "batch_extend_confirm_title" -> "批量順延到期日"
                "batch_extend_confirm_desc" -> "將根據選中 %d 張卡片各自設定的【提醒/續期間隔天數】，統向後順延計算新的到期日。"
                "biometric_title" -> "應用生物識別鎖 (指紋/面容)"
                "biometric_desc" -> "開啟後啟動應用需進行身分驗證"
                "data_backup" -> "數據備份與表格導入導出"
                "export_csv" -> "導出表格"
                "import_csv" -> "導入表格"
                "download_template" -> "下載導入模板"
                "restore_backup" -> "恢復上次備份"
                "theme_setting" -> "軟體外觀主題設置"
                "lang_setting" -> "軟體語言設置 (Language)"
                "theme_default" -> "預設明亮"
                "theme_dark" -> "深色夜間"
                "theme_ocean" -> "靜謐海洋"
                "theme_dynamic" -> "動態炫彩 ✨"
                "theme_star_space" -> "深空星海 🌌"
                "theme_star_aurora" -> "極光星穹 ✨"
                "theme_anime_sakura" -> "落櫻物語 🌸"
                "theme_anime_cyber" -> "賽博霓虹 ⚡"
                "all" -> "全部"
                "bank_card" -> "銀行卡"
                "sim_card" -> "電話卡"
                "email" -> "郵箱"
                "account" -> "帳號"
                "other" -> "其他"
                "day" -> "天"
                "toast_saved" -> "卡片已儲存！"
                "toast_updated" -> "更新成功！新到期日：%s"
                "toast_copied" -> "卡號已複製"
                "toast_deleted" -> "已刪除"
                "toast_restored" -> "數據已成功恢復至該歷史狀態！"
                "pin_title" -> "卡片置頂設置"
                "pin_desc" -> "是否將卡片“%s”%s？"
                "pin_action" -> "設為置頂"
                "unpin_action" -> "取消置頂"
                "delete_title" -> "確認刪除"
                "delete_desc" -> "確定要刪除卡片“%s”嗎？"
                "sort_asc" -> "按到期日期升序"
                "sort_desc" -> "按到期日期降序"
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
                "title_history" -> "Operation Logs & Rollback"
                "search_hint" -> "Search name / card number / notes..."
                "empty_data" -> "No cards found. Tap 'Add' to create one."
                "empty_search" -> "No matching cards found"
                "empty_history" -> "No operation logs yet."
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
                "confirm" -> "Confirm"
                "back" -> "Back"
                "card_name" -> "Card Name (e.g. Visa Debit)"
                "card_number" -> "Card / Account Number (Optional)"
                "note" -> "Notes (Optional)"
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
                "history_timeline" -> "📜 Recent Operations (Last 100)"
                "history_desc" -> "Review every change and rollback state anytime"
                "history_total_cards" -> "Total cards: %d"
                "history_record_count" -> "Operation History (%d/100)"
                "history_confirm_title" -> "Restore this state?"
                "history_confirm_desc" -> "Rollback card list to the state at [%s] (%d cards total)."
                "btn_restore_this" -> "Restore State"
                "batch_manage" -> "Batch Card Management"
                "batch_manage_desc" -> "Select, delete, change category and renew in batch"
                "batch_selected_count" -> "Selected: %d / %d"
                "batch_select_all" -> "Select All"
                "batch_unselect_all" -> "Deselect All"
                "batch_done_back" -> "Done"
                "batch_delete_selected" -> "Delete Selected"
                "batch_change_category" -> "Change Category"
                "batch_extend_date" -> "Renew Expiry"
                "batch_clear_all" -> "Clear All"
                "batch_delete_confirm_title" -> "Confirm Batch Deletion"
                "batch_delete_confirm_desc" -> "Are you sure you want to delete %d selected cards?"
                "batch_clear_confirm_title" -> "Warning: Clear All Cards?"
                "batch_clear_confirm_desc" -> "This will clear all cards. An automatic backup has been created."
                "batch_category_confirm_title" -> "Batch Change Category"
                "batch_category_confirm_desc" -> "Change category of %d selected cards to:"
                "batch_extend_confirm_title" -> "Batch Renewal"
                "batch_extend_confirm_desc" -> "Extend expiry dates for %d selected cards according to their intervals."
                "biometric_title" -> "Biometric App Lock (Fingerprint/Face)"
                "biometric_desc" -> "Require authentication when opening app"
                "data_backup" -> "Data Backup & CSV Excel"
                "export_csv" -> "Export CSV"
                "import_csv" -> "Import CSV"
                "download_template" -> "Download Template"
                "restore_backup" -> "Restore Backup"
                "theme_setting" -> "App Theme & Appearance"
                "lang_setting" -> "Language Settings"
                "theme_default" -> "Default Light"
                "theme_dark" -> "Dark Mode"
                "theme_ocean" -> "Ocean Blue"
                "theme_dynamic" -> "Dynamic Flow ✨"
                "theme_star_space" -> "Deep Space 🌌"
                "theme_star_aurora" -> "Cosmic Aurora ✨"
                "theme_anime_sakura" -> "Sakura Blossom 🌸"
                "theme_anime_cyber" -> "Cyber Neon ⚡"
                "all" -> "All"
                "bank_card" -> "Bank Card"
                "sim_card" -> "SIM Card"
                "email" -> "Email"
                "account" -> "Account"
                "other" -> "Others"
                "day" -> "d"
                "toast_saved" -> "Card saved successfully!"
                "toast_updated" -> "Updated! New expiry date: %s"
                "toast_copied" -> "Card number copied"
                "toast_deleted" -> "Card deleted"
                "toast_restored" -> "Data restored to this historical state!"
                "pin_title" -> "Pin Card"
                "pin_desc" -> "Do you want to %s card '%s'?"
                "pin_action" -> "pin"
                "unpin_action" -> "unpin"
                "delete_title" -> "Confirm Deletion"
                "delete_desc" -> "Are you sure you want to delete '%s'?"
                "sort_asc" -> "Expiry Date: Ascending"
                "sort_desc" -> "Expiry Date: Descending"
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
                "title_history" -> "操作履歴と復元"
                "search_hint" -> "名前・番号・メモを検索..."
                "empty_data" -> "カードがありません。「追加」から作成してください"
                "empty_search" -> "該当するカードがありません"
                "empty_history" -> "操作履歴がありません"
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
                "confirm" -> "確認"
                "back" -> "戻る"
                "card_name" -> "カード名 (例: 楽天カード)"
                "card_number" -> "カード番号 / 口座 (任意)"
                "note" -> "備考 (任意)"
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
                "history_timeline" -> "📜 最近の操作履歴 (最新100件)"
                "history_desc" -> "すべての変更を確認し、過去の状態に復元できます"
                "history_total_cards" -> "対象カード数: %d枚"
                "history_record_count" -> "操作履歴 (%d/100)"
                "history_confirm_title" -> "この状態に復元しますか？"
                "history_confirm_desc" -> "【%s】の操作時の状態（合計%d枚）に復元します。"
                "btn_restore_this" -> "この状態に復元"
                "batch_manage" -> "カード一括整理・管理"
                "batch_manage_desc" -> "複数選択、一括削除、カテゴリ変更、一括更新"
                "batch_selected_count" -> "選択中: %d / %d枚"
                "batch_select_all" -> "全選択"
                "batch_unselect_all" -> "選択解除"
                "batch_done_back" -> "完了"
                "batch_delete_selected" -> "選択削除"
                "batch_change_category" -> "カテゴリ変更"
                "batch_extend_date" -> "期限一括更新"
                "batch_clear_all" -> "全削除"
                "batch_delete_confirm_title" -> "一括削除の確認"
                "batch_delete_confirm_desc" -> "選択した%d枚のカードを削除しますか？"
                "batch_clear_confirm_title" -> "警告：すべてのカードを削除しますか？"
                "batch_clear_confirm_desc" -> "すべてのカードを削除します。自動バックアップからいつでも復元できます。"
                "batch_category_confirm_title" -> "カテゴリ一括変更"
                "batch_category_confirm_desc" -> "選択した%d枚のカードのカテゴリを変更："
                "batch_extend_confirm_title" -> "期限の一括更新"
                "batch_extend_confirm_desc" -> "選択した%d枚のカードの有効期限を設定間隔に応じて延長します。"
                "biometric_title" -> "生体認証ロック (指紋・顔認証)"
                "biometric_desc" -> "起動時に認証を要求します"
                "data_backup" -> "データバックアップとExcel入出力"
                "export_csv" -> "CSV出力"
                "import_csv" -> "CSV読込"
                "download_template" -> "テンプレート取得"
                "restore_backup" -> "バックアップ復元"
                "theme_setting" -> "外観テーマ設定"
                "lang_setting" -> "言語設定 (Language)"
                "theme_default" -> "デフォルト・ライト"
                "theme_dark" -> "ダークモード"
                "theme_ocean" -> "オーシャン・ブルー"
                "theme_dynamic" -> "グラデーション ✨"
                "theme_star_space" -> "コズミック・スター 🌌"
                "theme_star_aurora" -> "オーロラ・スカイ ✨"
                "theme_anime_sakura" -> "サクラ・ブロッサム 🌸"
                "theme_anime_cyber" -> "サイバー・ネオン ⚡"
                "all" -> "すべて"
                "bank_card" -> "銀行カード"
                "sim_card" -> "SIMカード"
                "email" -> "メール"
                "account" -> "アカウント"
                "other" -> "その他"
                "day" -> "日"
                "toast_saved" -> "保存しました！"
                "toast_updated" -> "更新完了！新しい有効期限: %s"
                "toast_copied" -> "カード番号をコピーしました"
                "toast_deleted" -> "削除しました"
                "toast_restored" -> "この状態に正常に復元しました！"
                "pin_title" -> "ピン留め設定"
                "pin_desc" -> "カード「%s」を%sしますか？"
                "pin_action" -> "ピン留め"
                "unpin_action" -> "ピン留め解除"
                "delete_title" -> "削除の確認"
                "delete_desc" -> "「%s」を削除してもよろしいですか？"
                "sort_asc" -> "有効期限が近い順"
                "sort_desc" -> "有効期限が遠い順"
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
                "title_history" -> "Histórico de Operações"
                "search_hint" -> "Buscar por nome / número / notas..."
                "empty_data" -> "Nenhum cartão. Toque em 'Adicionar' para criar."
                "empty_search" -> "Nenhum cartão correspondente encontrado"
                "empty_history" -> "Nenhum histórico de operação ainda."
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
                "confirm" -> "Confirmar"
                "back" -> "Voltar"
                "card_name" -> "Nome do Cartão"
                "card_number" -> "Número do Cartão / Conta (Opcional)"
                "note" -> "Notas (Opcional)"
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
                "history_timeline" -> "📜 Histórico de Operações (Últimas 100)"
                "history_desc" -> "Veja todas as alterações e restaure qualquer estado"
                "history_total_cards" -> "Total de cartões: %d"
                "history_record_count" -> "Histórico (%d/100)"
                "history_confirm_title" -> "Restaurar este estado?"
                "history_confirm_desc" -> "Reverter lista de cartões para o estado em [%s] (%d cartões no total)."
                "btn_restore_this" -> "Restaurar este estado"
                "batch_manage" -> "Gerenciamento em Lote"
                "batch_manage_desc" -> "Selecionar, excluir, alterar categoria e renovar"
                "batch_selected_count" -> "Selecionados: %d / %d"
                "batch_select_all" -> "Selecionar Tudo"
                "batch_unselect_all" -> "Desmarcar Tudo"
                "batch_done_back" -> "Concluído"
                "batch_delete_selected" -> "Excluir Selecionados"
                "batch_change_category" -> "Alterar Categoria"
                "batch_extend_date" -> "Renovar Validade"
                "batch_clear_all" -> "Limpar Tudo"
                "batch_delete_confirm_title" -> "Confirmar Exclusão em Lote"
                "batch_delete_confirm_desc" -> "Tem certeza de que deseja excluir %d cartões selecionados?"
                "batch_clear_confirm_title" -> "Aviso: Limpar Todos os Cartões?"
                "batch_clear_confirm_desc" -> "Isso limpará todos os cartões. Um backup automático foi criado."
                "batch_category_confirm_title" -> "Alterar Categoria em Lote"
                "batch_category_confirm_desc" -> "Alterar categoria de %d cartões selecionados para:"
                "batch_extend_confirm_title" -> "Renovação em Lote"
                "batch_extend_confirm_desc" -> "Estender as datas de validade de %d cartões de acordo com seus intervalos."
                "biometric_title" -> "Bloqueio Biométrico (Digital/Face)"
                "biometric_desc" -> "Exigir autenticação ao abrir o aplicativo"
                "data_backup" -> "Backup de Dados e CSV Excel"
                "export_csv" -> "Exportar CSV"
                "import_csv" -> "Importar CSV"
                "download_template" -> "Baixar Modelo"
                "restore_backup" -> "Restaurar Backup"
                "theme_setting" -> "Tema e Aparência"
                "lang_setting" -> "Configurações de Idioma"
                "theme_default" -> "Claro Padrão"
                "theme_dark" -> "Modo Escuro"
                "theme_ocean" -> "Azul Oceano"
                "theme_dynamic" -> "Gradiente Dinâmico ✨"
                "theme_star_space" -> "Espaço Profundo 🌌"
                "theme_star_aurora" -> "Aurora Cósmica ✨"
                "theme_anime_sakura" -> "Flor de Cerejeira 🌸"
                "theme_anime_cyber" -> "Neon Cibernético ⚡"
                "all" -> "Todos"
                "bank_card" -> "Cartão Bancário"
                "sim_card" -> "Cartão SIM"
                "email" -> "E-mail"
                "account" -> "Conta"
                "other" -> "Outros"
                "day" -> "d"
                "toast_saved" -> "Cartão salvo com sucesso!"
                "toast_updated" -> "Renovado! Nova validade: %s"
                "toast_copied" -> "Número copiado"
                "toast_deleted" -> "Cartão excluído"
                "toast_restored" -> "Dados restaurados para este estado!"
                "pin_title" -> "Fixar Cartão"
                "pin_desc" -> "Deseja %s o cartão '%s'?"
                "pin_action" -> "fixar"
                "unpin_action" -> "desafixar"
                "delete_title" -> "Confirmar Exclusão"
                "delete_desc" -> "Tem certeza de que deseja excluir '%s'?"
                "sort_asc" -> "Validade: Crescente"
                "sort_desc" -> "Validade: Decrescente"
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
                "title_history" -> "操作记录与回滚"
                "search_hint" -> "搜索卡片名称 / 卡号 / 备注..."
                "empty_data" -> "暂无卡片数据，点击底部“新增”添加"
                "empty_search" -> "未找到相关卡片数据"
                "empty_history" -> "暂无任何操作记录"
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
                "confirm" -> "确认"
                "back" -> "返回"
                "card_name" -> "名称 (如: 招商信用卡)"
                "card_number" -> "卡号 / 账号 (选填)"
                "note" -> "备注信息 (选填)"
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
                "history_timeline" -> "📜 最近操作记录 (最近100条)"
                "history_desc" -> "支持查看每一次改动并一键恢复历史状态"
                "history_total_cards" -> "对应卡片总数: %d 张"
                "history_record_count" -> "操作改动记录 (%d/100)"
                "history_confirm_title" -> "确认恢复至此状态？"
                "history_confirm_desc" -> "将卡片列表回滚至【%s】操作时的状态（共 %d 张卡片）。"
                "btn_restore_this" -> "恢复至此状态"
                "batch_manage" -> "卡片批量整理与管理"
                "batch_manage_desc" -> "多选、批量删除、批量改分类及批量顺延"
                "batch_selected_count" -> "已勾选: %d / %d 张"
                "batch_select_all" -> "全选"
                "batch_unselect_all" -> "取消全选"
                "batch_done_back" -> "完成返回"
                "batch_delete_selected" -> "删除所选"
                "batch_change_category" -> "修改分类"
                "batch_extend_date" -> "顺延到期日"
                "batch_clear_all" -> "清空全部"
                "batch_delete_confirm_title" -> "确认批量删除"
                "batch_delete_confirm_desc" -> "确定要删除选中的 %d 张卡片吗？此操作不可逆（已自动备份）。"
                "batch_clear_confirm_title" -> "危险：确认清空全部卡片？"
                "batch_clear_confirm_desc" -> "将彻底清空当前所有卡片数据！系统已为您自动快照备份，随时可在“我的”页面恢复。"
                "batch_category_confirm_title" -> "批量修改分类"
                "batch_category_confirm_desc" -> "将选中的 %d 张卡片统一更改分类为："
                "batch_extend_confirm_title" -> "批量顺延到期日"
                "batch_extend_confirm_desc" -> "将根据选中 %d 张卡片各自设定的【提醒/续期间隔天数】，统一向后顺延计算新的到期日。"
                "biometric_title" -> "应用生物识别锁 (指纹/面容)"
                "biometric_desc" -> "开启后启动应用需进行身份验证"
                "data_backup" -> "数据备份与表格导入导出"
                "export_csv" -> "导出表格"
                "import_csv" -> "导入表格"
                "download_template" -> "下载导入模板"
                "restore_backup" -> "恢复上次备份"
                "theme_setting" -> "软件外观主题设置"
                "lang_setting" -> "软件语言设置 (Language)"
                "theme_default" -> "默认明亮"
                "theme_dark" -> "深色夜间"
                "theme_ocean" -> "静谧海洋"
                "theme_dynamic" -> "动态炫彩 ✨"
                "theme_star_space" -> "深空星海 🌌"
                "theme_star_aurora" -> "极光星穹 ✨"
                "theme_anime_sakura" -> "落樱物语 🌸"
                "theme_anime_cyber" -> "赛博霓虹 ⚡"
                "all" -> "全部"
                "bank_card" -> "银行卡"
                "sim_card" -> "电话卡"
                "email" -> "邮箱"
                "account" -> "账号"
                "other" -> "其他"
                "day" -> "天"
                "toast_saved" -> "卡片已保存！"
                "toast_updated" -> "更新成功！新到期日：%s"
                "toast_copied" -> "卡号已复制"
                "toast_deleted" -> "已被删除"
                "toast_restored" -> "数据已成功恢复至该历史状态！"
                "pin_title" -> "卡片置顶设置"
                "pin_desc" -> "是否将卡片“%s”%s？"
                "pin_action" -> "设为置顶"
                "unpin_action" -> "取消置顶"
                "delete_title" -> "确认删除"
                "delete_desc" -> "确定要删除卡片“%s”吗？"
                "sort_asc" -> "按到期日期升序"
                "sort_desc" -> "按到期日期降序"
                else -> key
            }
        }
    }
}

enum class AppTheme(val key: String, val isDynamic: Boolean) {
    DEFAULT("theme_default", false),
    DARK("theme_dark", false),
    OCEAN("theme_ocean", false),
    DYNAMIC_GRADIENT("theme_dynamic", true),
    STAR_DEEP_SPACE("theme_star_space", false),
    STAR_AURORA("theme_star_aurora", true),
    ANIME_SAKURA("theme_anime_sakura", true),
    ANIME_CYBER("theme_anime_cyber", false)
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
    val bgValue: String = "0xFFFFFFFF"
)

data class OperationHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val description: String,
    val snapshotCards: List<CardItem>
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
    private const val KEY_HISTORY_STACK = "key_history_stack_json"

    fun saveCards(context: Context, cards: List<CardItem>, recordAction: String? = null) {
        if (recordAction != null) {
            pushHistory(context, recordAction, cards)
        }

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

    private fun pushHistory(context: Context, action: String, currentCards: List<CardItem>) {
        val historyList = loadHistory(context).toMutableList()
        historyList.add(0, OperationHistoryItem(
            description = action,
            snapshotCards = currentCards
        ))
        val trimmedList = historyList.take(100)
        saveHistoryList(context, trimmedList)
    }

    fun loadHistory(context: Context): List<OperationHistoryItem> {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonStr = sp.getString(KEY_HISTORY_STACK, null) ?: return emptyList()
        val list = mutableListOf<OperationHistoryItem>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.optString("id", UUID.randomUUID().toString())
                val timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                val description = obj.optString("description", "")
                val cardsJson = obj.optString("snapshotCards", "[]")
                list.add(OperationHistoryItem(
                    id = id,
                    timestamp = timestamp,
                    description = description,
                    snapshotCards = parseJsonToCards(cardsJson)
                ))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun saveHistoryList(context: Context, list: List<OperationHistoryItem>) {
        val jsonArray = JSONArray()
        list.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("timestamp", item.timestamp)
                put("description", item.description)
                val cardsArray = JSONArray()
                item.snapshotCards.forEach { card ->
                    cardsArray.put(JSONObject().apply {
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
                    })
                }
                put("snapshotCards", cardsArray.toString())
            }
            jsonArray.put(obj)
        }
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sp.edit().putString(KEY_HISTORY_STACK, jsonArray.toString()).apply()
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

    fun saveLanguage(context: Context, lang: AppLanguage) {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sp.edit().putString(KEY_LANGUAGE, lang.name).apply()
    }

    // 根据操作系统语言自动识别默认语言
    fun loadLanguage(context: Context): AppLanguage {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val saved = sp.getString(KEY_LANGUAGE, null)
        if (saved != null) {
            return try { AppLanguage.valueOf(saved) } catch (e: Exception) { detectSystemDefaultLanguage() }
        }
        return detectSystemDefaultLanguage()
    }

    private fun detectSystemDefaultLanguage(): AppLanguage {
        val locale = Locale.getDefault()
        val lang = locale.language.lowercase(Locale.ROOT)
        val script = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) locale.script.lowercase(Locale.ROOT) else ""
        val country = locale.country.uppercase(Locale.ROOT)

        return when {
            lang == "zh" -> {
                if (script.contains("hant") || country == "TW" || country == "HK" || country == "MO") {
                    AppLanguage.ZH_TW
                } else {
                    AppLanguage.ZH_CN
                }
            }
            lang == "ja" -> AppLanguage.JA
            lang == "pt" -> AppLanguage.PT
            lang == "en" -> AppLanguage.EN
            else -> AppLanguage.EN
        }
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
    var isViewingHistory by remember { mutableStateOf(false) }
    var importPendingCards by remember { mutableStateOf<List<CardItem>?>(null) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }

    var isUnlocked by remember { mutableStateOf(!CardStorage.isBiometricEnabled(context)) }

    LaunchedEffect(Unit) {
        if (CardStorage.isBiometricEnabled(context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val biometricPrompt = BiometricPrompt.Builder(context)
                .setTitle("身份验证")
                .setSubtitle("请验证指纹或面容进入卡片管理")
                .setNegativeButton(StringsProvider.get("cancel", currentLanguage), context.mainExecutor) { _, _ -> }
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
                Text(StringsProvider.get("biometric_title", currentLanguage), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Button(onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val prompt = BiometricPrompt.Builder(context)
                            .setTitle("身份验证")
                            .setNegativeButton(StringsProvider.get("cancel", currentLanguage), context.mainExecutor) { _, _ -> }
                            .build()
                        prompt.authenticate(CancellationSignal(), context.mainExecutor, object : BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                                isUnlocked = true
                            }
                        })
                    }
                }) {
                    Text(StringsProvider.get("confirm", currentLanguage))
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
                            else if (isViewingHistory) StringsProvider.get("title_history", currentLanguage)
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
                        if (!isBatchManaging && !isViewingHistory && (selectedTab == 1 || selectedTab == 0)) {
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
                                    text = { Text(StringsProvider.get("sort_asc", currentLanguage), color = if (currentSortOrder == SortOrder.EXPIRY_ASC) MaterialTheme.colorScheme.primary else Color.Unspecified) },
                                    leadingIcon = { if (currentSortOrder == SortOrder.EXPIRY_ASC) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary) },
                                    onClick = { currentSortOrder = SortOrder.EXPIRY_ASC; filterMenuExpanded = false }
                                )
                                DropdownMenuItem(
                                    text = { Text(StringsProvider.get("sort_desc", currentLanguage), color = if (currentSortOrder == SortOrder.EXPIRY_DESC) MaterialTheme.colorScheme.primary else Color.Unspecified) },
                                    leadingIcon = { if (currentSortOrder == SortOrder.EXPIRY_DESC) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary) },
                                    onClick = { currentSortOrder = SortOrder.EXPIRY_DESC; filterMenuExpanded = false }
                                )
                            }
                        }
                    }
                )
            },
            bottomBar = {
                if (!isBatchManaging && !isViewingHistory) {
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
                if (isViewingHistory) {
                    HistoryTimelineScreen(
                        currentLanguage = currentLanguage,
                        onBack = { isViewingHistory = false },
                        onRollbackState = { rollbackCards ->
                            cardList = rollbackCards
                            CardStorage.saveCards(context, rollbackCards, "回滚恢复至历史状态")
                            Toast.makeText(context, StringsProvider.get("toast_restored", currentLanguage), Toast.LENGTH_SHORT).show()
                            isViewingHistory = false
                        }
                    )
                } else if (isBatchManaging) {
                    BatchManagementScreen(
                        cardList = cardList,
                        currentLanguage = currentLanguage,
                        onBack = { isBatchManaging = false },
                        onUpdateCards = { updatedList, actionName ->
                            cardList = updatedList
                            CardStorage.saveCards(context, updatedList, actionName)
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
                                val isPinning = !card.isPinned
                                val action = if (isPinning) "置顶卡片 [${card.title}]" else "取消置顶 [${card.title}]"
                                val newList = cardList.map {
                                    if (it.id == card.id) it.copy(isPinned = isPinning, pinTime = System.currentTimeMillis()) else it
                                }
                                cardList = newList
                                CardStorage.saveCards(context, newList, action)
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
                                val isEdit = (editingCard != null)
                                val action = if (isEdit) "修改编辑卡片 [${newCard.title}]" else "新增卡片 [${newCard.title}]"
                                val updatedList = cardList.filter { it.id != newCard.id } + newCard
                                cardList = updatedList
                                CardStorage.saveCards(context, updatedList, action)

                                editingCard = null
                                selectedTab = 1
                                Toast.makeText(context, StringsProvider.get("toast_saved", currentLanguage), Toast.LENGTH_SHORT).show()
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
                            onHistoryTimelineClick = {
                                isViewingHistory = true
                            },
                            onExportClick = {
                                if (cardList.isEmpty()) {
                                    Toast.makeText(context, StringsProvider.get("empty_data", currentLanguage), Toast.LENGTH_SHORT).show()
                                } else {
                                    ExcelExportImportHelper.exportToCsv(context, cardList)
                                }
                            },
                            onExportTemplateClick = {
                                ExcelExportImportHelper.exportTemplate(context)
                            },
                            onImportFileParsed = { importedCards ->
                                if (importedCards.isEmpty()) {
                                    Toast.makeText(context, StringsProvider.get("empty_data", currentLanguage), Toast.LENGTH_SHORT).show()
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
                        CardStorage.saveCards(context, merged, "合并导入 ${incomingCards.size} 张卡片")
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
                        CardStorage.saveCards(context, incomingCards, "清空覆盖导入 ${incomingCards.size} 张卡片")
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
                            CardStorage.saveCards(context, backupCards, "从上次备份回滚数据")
                            Toast.makeText(context, StringsProvider.get("toast_restored", currentLanguage), Toast.LENGTH_SHORT).show()
                            showRestoreConfirmDialog = false
                        }
                    ) {
                        Text(StringsProvider.get("confirm", currentLanguage))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmDialog = false }) {
                    Text(StringsProvider.get("cancel", currentLanguage))
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
                        val actionDesc = "更新卡片 [${card.title}] 顺延至 $nextExpiryStr"
                        val updatedList = cardList.map { item ->
                            if (item.id == card.id) {
                                item.copy(expiryDateMillis = nextCalendar.timeInMillis)
                            } else {
                                item
                            }
                        }
                        cardList = updatedList
                        CardStorage.saveCards(context, updatedList, actionDesc)
                        Toast.makeText(context, String.format(StringsProvider.get("toast_updated", currentLanguage), nextExpiryStr), Toast.LENGTH_SHORT).show()
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
        val pinActionText = if (!isPinned) StringsProvider.get("pin_action", currentLanguage) else StringsProvider.get("unpin_action", currentLanguage)

        AlertDialog(
            onDismissRequest = { pinDialogCard = null },
            title = { Text(StringsProvider.get("pin_title", currentLanguage), fontWeight = FontWeight.Bold) },
            text = { Text(String.format(StringsProvider.get("pin_desc", currentLanguage), pinDialogCard?.title, pinActionText)) },
            confirmButton = {
                Button(
                    onClick = {
                        pinDialogCard?.let { card ->
                            val actionDesc = if (!isPinned) "置顶卡片 [${card.title}]" else "取消置顶 [${card.title}]"
                            val newList = cardList.map {
                                if (it.id == card.id) it.copy(isPinned = !it.isPinned, pinTime = System.currentTimeMillis()) else it
                            }
                            cardList = newList
                            CardStorage.saveCards(context, newList, actionDesc)
                        }
                        pinDialogCard = null
                    }
                ) {
                    Text(pinActionText)
                }
            },
            dismissButton = {
                TextButton(onClick = { pinDialogCard = null }) {
                    Text(StringsProvider.get("cancel", currentLanguage))
                }
            }
        )
    }

    if (deletingCard != null) {
        AlertDialog(
            onDismissRequest = { deletingCard = null },
            title = { Text(StringsProvider.get("delete_title", currentLanguage), fontWeight = FontWeight.Bold) },
            text = { Text(String.format(StringsProvider.get("delete_desc", currentLanguage), deletingCard?.title)) },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        deletingCard?.let { card ->
                            val newList = cardList.filter { it.id != card.id }
                            cardList = newList
                            CardStorage.saveCards(context, newList, "删除卡片 [${card.title}]")
                            Toast.makeText(context, StringsProvider.get("toast_deleted", currentLanguage), Toast.LENGTH_SHORT).show()
                        }
                        deletingCard = null
                    }
                ) {
                    Text(StringsProvider.get("confirm", currentLanguage))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingCard = null }) {
                    Text(StringsProvider.get("cancel", currentLanguage))
                }
            }
        )
    }
}

@Composable
fun HistoryTimelineScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit,
    onRollbackState: (List<CardItem>) -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val historyList = remember { CardStorage.loadHistory(context) }
    var rollbackConfirmItem by remember { mutableStateOf<OperationHistoryItem?>(null) }
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

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
                String.format(StringsProvider.get("history_record_count", currentLanguage), historyList.size),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Button(onClick = onBack, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                Text(StringsProvider.get("back", currentLanguage))
            }
        }

        HorizontalDivider()

        if (historyList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(StringsProvider.get("empty_history", currentLanguage), color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(historyList, key = { it.id }) { item ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.description, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(sdf.format(Date(item.timestamp)), fontSize = 11.sp, color = Color.Gray)
                                Text(String.format(StringsProvider.get("history_total_cards", currentLanguage), item.snapshotCards.size), fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            OutlinedButton(
                                onClick = { rollbackConfirmItem = item },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(StringsProvider.get("btn_restore_this", currentLanguage), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (rollbackConfirmItem != null) {
        val item = rollbackConfirmItem!!
        AlertDialog(
            onDismissRequest = { rollbackConfirmItem = null },
            title = { Text(StringsProvider.get("history_confirm_title", currentLanguage), fontWeight = FontWeight.Bold) },
            text = { Text(String.format(StringsProvider.get("history_confirm_desc", currentLanguage), sdf.format(Date(item.timestamp)), item.snapshotCards.size)) },
            confirmButton = {
                Button(
                    onClick = {
                        onRollbackState(item.snapshotCards)
                        rollbackConfirmItem = null
                    }
                ) {
                    Text(StringsProvider.get("confirm", currentLanguage))
                }
            },
            dismissButton = {
                TextButton(onClick = { rollbackConfirmItem = null }) {
                    Text(StringsProvider.get("cancel", currentLanguage))
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
    onUpdateCards: (List<CardItem>, String) -> Unit
) {
    BackHandler { onBack() }

    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showClearAllConfirm by remember { mutableStateOf(false) }
    var showBatchCategoryDialog by remember { mutableStateOf(false) }
    var showBatchExtendDialog by remember { mutableStateOf(false) }

    val isAllSelected = cardList.isNotEmpty() && selectedIds.size == cardList.size

    val presetCategories = remember(currentLanguage) {
        listOf(
            StringsProvider.get("bank_card", currentLanguage),
            StringsProvider.get("sim_card", currentLanguage),
            StringsProvider.get("email", currentLanguage),
            StringsProvider.get("account", currentLanguage),
            StringsProvider.get("other", currentLanguage)
        )
    }

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
                String.format(StringsProvider.get("batch_selected_count", currentLanguage), selectedIds.size, cardList.size),
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
                    Text(if (isAllSelected) StringsProvider.get("batch_unselect_all", currentLanguage) else StringsProvider.get("batch_select_all", currentLanguage), fontSize = 12.sp)
                }

                Button(
                    onClick = onBack,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(StringsProvider.get("batch_done_back", currentLanguage), fontSize = 12.sp)
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
                Text(StringsProvider.get("batch_delete_selected", currentLanguage), fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = { showBatchCategoryDialog = true },
                enabled = selectedIds.isNotEmpty(),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(StringsProvider.get("batch_change_category", currentLanguage), fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = { showBatchExtendDialog = true },
                enabled = selectedIds.isNotEmpty(),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Update, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(StringsProvider.get("batch_extend_date", currentLanguage), fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = { showClearAllConfirm = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(StringsProvider.get("batch_clear_all", currentLanguage), fontSize = 12.sp)
            }
        }

        HorizontalDivider()

        if (cardList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(StringsProvider.get("empty_data", currentLanguage), color = Color.Gray)
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
            title = { Text(StringsProvider.get("batch_delete_confirm_title", currentLanguage), fontWeight = FontWeight.Bold) },
            text = { Text(String.format(StringsProvider.get("batch_delete_confirm_desc", currentLanguage), selectedIds.size)) },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        val remaining = cardList.filter { !selectedIds.contains(it.id) }
                        onUpdateCards(remaining, "批量删除 ${selectedIds.size} 张卡片")
                        selectedIds = emptySet()
                        showDeleteConfirm = false
                    }
                ) {
                    Text(StringsProvider.get("confirm", currentLanguage))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text(StringsProvider.get("cancel", currentLanguage)) }
            }
        )
    }

    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            title = { Text(StringsProvider.get("batch_clear_confirm_title", currentLanguage), fontWeight = FontWeight.Bold) },
            text = { Text(StringsProvider.get("batch_clear_confirm_desc", currentLanguage)) },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        onUpdateCards(emptyList(), "清空全部卡片数据")
                        selectedIds = emptySet()
                        showClearAllConfirm = false
                    }
                ) {
                    Text(StringsProvider.get("batch_clear_all", currentLanguage))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirm = false }) { Text(StringsProvider.get("cancel", currentLanguage)) }
            }
        )
    }

    if (showBatchCategoryDialog) {
        var targetCat by remember { mutableStateOf(presetCategories[0]) }
        var isCustomCat by remember { mutableStateOf(false) }
        var customCatInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showBatchCategoryDialog = false },
            title = { Text(StringsProvider.get("batch_category_confirm_title", currentLanguage), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(String.format(StringsProvider.get("batch_category_confirm_desc", currentLanguage), selectedIds.size))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        presetCategories.forEach { cat ->
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
                            label = { Text(StringsProvider.get("custom_category", currentLanguage)) }
                        )
                    }
                    if (isCustomCat) {
                        OutlinedTextField(
                            value = customCatInput,
                            onValueChange = { customCatInput = it },
                            label = { Text(StringsProvider.get("custom_category_hint", currentLanguage)) },
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
                        onUpdateCards(updated, "批量修改 ${selectedIds.size} 张卡片分类为 [$finalCat]")
                        showBatchCategoryDialog = false
                    }
                ) {
                    Text(StringsProvider.get("confirm", currentLanguage))
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchCategoryDialog = false }) { Text(StringsProvider.get("cancel", currentLanguage)) }
            }
        )
    }

    if (showBatchExtendDialog) {
        AlertDialog(
            onDismissRequest = { showBatchExtendDialog = false },
            title = { Text(StringsProvider.get("batch_extend_confirm_title", currentLanguage), fontWeight = FontWeight.Bold) },
            text = { Text(String.format(StringsProvider.get("batch_extend_confirm_desc", currentLanguage), selectedIds.size)) },
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
                        onUpdateCards(updated, "批量顺延 ${selectedIds.size} 张卡片到期日")
                        showBatchExtendDialog = false
                    }
                ) {
                    Text(StringsProvider.get("confirm", currentLanguage))
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchExtendDialog = false }) { Text(StringsProvider.get("cancel", currentLanguage)) }
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
                    Text(StringsProvider.get("empty_search", currentLanguage), color = Color.Gray)
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
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
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
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                } else if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
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
                                    contentDescription = null,
                                    tint = if (card.isPinned) iconPinnedActiveColor else iconActionColor
                                )
                            }
                            IconButton(onClick = onDelete) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = iconActionColor)
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
                                    Toast.makeText(context, StringsProvider.get("toast_copied", currentLanguage), Toast.LENGTH_SHORT).show()
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

                    if (card.note.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        androidx.compose.animation.AnimatedVisibility(visible = expandedNote) {
                            Surface(
                                color = if (isDarkBg) Color.Black.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("备注: ${card.note}", fontSize = 13.sp, color = if (isDarkBg) Color.White else Color.Black)
                                }
                            }
                        }
                        if (!expandedNote) {
                            Text("点击查看备注 (长按置顶)...", fontSize = 11.sp, color = textColor.copy(alpha = 0.7f))
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
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
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
                        bgValue = bgValue
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
    onHistoryTimelineClick: () -> Unit,
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
            Text("卡片提醒助手 v4.4", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text("已安全管理 $cardCount 张卡片", color = Color.Gray, fontSize = 13.sp)
        }

        // 操作历史时光机入口
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onHistoryTimelineClick() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(StringsProvider.get("history_timeline", currentLanguage), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(StringsProvider.get("history_desc", currentLanguage), fontSize = 12.sp, color = Color.Gray)
                    }
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
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

        // 外观主题设置
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
                                Text(StringsProvider.get(theme.key, currentLanguage)) 
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
