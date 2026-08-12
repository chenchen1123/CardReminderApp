import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 定义暗色调主题颜色
private val DarkBg = Color(0xFF0D0D0D)
private val GroupCardBg = Color(0xFF1C1C1E)
private val InnerCardBg = Color(0xFF2C2C2E)
private val PrimaryText = Color(0xFFFFFFFF)
private val SecondaryText = Color(0xFF8E8E93)
private val BadgeBg = Color(0xFF3A3A3C)

@Composable
fun ConfigDashboardScreen() {
    Scaffold(
        containerColor = DarkBg,
        bottomBar = { CustomBottomNavigationBar() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            HeaderSection()
            Spacer(modifier = Modifier.height(20.dp))

            // 分组 1: 身份与证件
            CategoryGroupCard(
                title = "身份与证件",
                count = 1,
                icon = Icons.Default.Badge,
                iconTint = Color(0xFF5E5CE6)
            ) {
                SingleCardItem(title = "护照", subtitle = null)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 分组 2: 网络环境
            CategoryGroupCard(
                title = "网络环境",
                count = 2,
                icon = Icons.Default.Language,
                iconTint = Color(0xFF64D2FF)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        InfoCardItem(title = "纯净 IP", tag = "德国", detail = "95% 极纯净")
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        AppCardItem(title = "Shadowrocket", subtitle = "代理客户端", icon = Icons.Default.RocketLaunch)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 分组 3: 支付 (卡片横向滑动)
            CategoryGroupCard(
                title = "支付",
                count = 3,
                icon = Icons.Default.CreditCard,
                iconTint = Color(0xFF30D158)
            ) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BankCardItem("Bitget Wallet Card", Color(0xFF0052FF))
                    BankCardItem("Plasma One Card", Color(0xFF1E293B))
                    BankCardItem("Kast Card", Color(0xFF27272A))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// 顶栏标题
@Composable
private fun HeaderSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("我的海外配置", color = PrimaryText, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("11 项配置", color = SecondaryText, fontSize = 14.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = {}, modifier = Modifier.clip(CircleShape).background(BadgeBg)) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = PrimaryText)
            }
            IconButton(onClick = {}, modifier = Modifier.clip(CircleShape).background(BadgeBg)) {
                Icon(Icons.Default.GridView, contentDescription = null, tint = PrimaryText)
            }
            IconButton(onClick = {}, modifier = Modifier.clip(CircleShape).background(BadgeBg)) {
                Icon(Icons.Default.IosShare, contentDescription = null, tint = PrimaryText)
            }
        }
    }
}

// 通用分组容器外壳
@Composable
private fun CategoryGroupCard(
    title: String,
    count: Int,
    icon: ImageVector,
    iconTint: Color,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GroupCardBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 分组 Head
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, color = PrimaryText, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(BadgeBg)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(count.toString(), color = SecondaryText, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SecondaryText)
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

// 内部样式卡片 1: 软件/应用卡片
@Composable
private fun AppCardItem(title: String, subtitle: String, icon: ImageVector) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = InnerCardBg),
        modifier = Modifier.fillMaxWidth().height(100.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFFBF5AF2), modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, color = PrimaryText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = SecondaryText, fontSize = 12.sp)
            }
        }
    }
}

// 内部样式卡片 2: 信息卡片（纯净 IP）
@Composable
private fun InfoCardItem(title: String, tag: String, detail: String) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = InnerCardBg),
        modifier = Modifier.fillMaxWidth().height(100.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, color = PrimaryText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(tag, color = SecondaryText, fontSize = 12.sp)
            }
            Text(detail, color = Color(0xFF30D158), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// 内部样式卡片 3: 银行卡片
@Composable
private fun BankCardItem(title: String, cardColor: Color) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        modifier = Modifier.width(140.dp).height(85.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(10.dp), contentAlignment = Alignment.BottomStart) {
            Text(title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// 内部样式卡片 4: 基础卡片（护照等）
@Composable
private fun SingleCardItem(title: String, subtitle: String?) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = InnerCardBg),
        modifier = Modifier.width(110.dp).height(100.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.BottomStart) {
            Text(title, color = PrimaryText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// 底部导航栏
@Composable
private fun CustomBottomNavigationBar() {
    NavigationBar(containerColor = Color(0xFF161618)) {
        NavigationBarItem(
            selected = false, onClick = {},
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("首页") }
        )
        NavigationBarItem(
            selected = true, onClick = {},
            icon = { Icon(Icons.Default.GridView, contentDescription = null) },
            label = { Text("配置") }
        )
        NavigationBarItem(
            selected = false, onClick = {},
            icon = { Icon(Icons.Default.Explore, contentDescription = null) },
            label = { Text("市场") }
        )
        NavigationBarItem(
            selected = false, onClick = {},
            icon = { Icon(Icons.Default.SimCard, contentDescription = null) },
            label = { Text("eSIM") }
        )
        NavigationBarItem(
            selected = false, onClick = {},
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text("我的") }
        )
    }
}
