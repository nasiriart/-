package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.GameCatalog
import com.example.domain.GameEngine
import com.example.domain.LevelMilestone
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AlertRed
import com.example.ui.theme.BadgeGold
import com.example.ui.theme.BadgeGoldSoft
import com.example.ui.theme.IceBlueSurface
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyElevated
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PoliceBlue
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyAccent
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateInk
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenSoft
import com.example.ui.viewmodel.GameUiState
import com.example.util.PersianFormat

@Composable
fun StatsScreen(
    state: GameUiState,
    onResetCareer: () -> Unit
) {
    val profile = state.profile
    val totalMissions = profile.missionsSucceeded + profile.missionsFailed
    val winRate = if (totalMissions == 0) 0 else ((profile.missionsSucceeded * 100) / totalMissions)
    val safetyScore = GameEngine.citySafetyScore(profile, state.officers.size, state.vehicles.size)

    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("شروع مجدد بازی", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    "آیا مطمئن هستید که می‌خواهید تمام پیشرفت‌های کلانتری، نیروها، خودروها و آمار را پاک کرده و از سطح ۱ شروع کنید؟",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        onResetCareer()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed, contentColor = PureWhite),
                    modifier = Modifier.testTag("confirm_reset_button")
                ) {
                    Text("بله، شروع مجدد")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(IceBlueSurface)
            .testTag("stats_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Player Rank Hero Banner
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(NavyDeep, NavyPrimary, NavyElevated)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(62.dp)
                                .clip(CircleShape)
                                .background(BadgeGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EmojiEvents,
                                contentDescription = "رتبه بازیکن",
                                tint = NavyDeep,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "رتبه فعلی فرمانده",
                                style = MaterialTheme.typography.labelMedium,
                                color = SkyAccent
                            )
                            Text(
                                text = GameCatalog.rankTitleForLevel(profile.level),
                                style = MaterialTheme.typography.headlineSmall,
                                color = PureWhite,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "سطح ${PersianFormat.number(profile.level)} از ۲۰ • شاخص امنیت شهر ${PersianFormat.percent(safetyScore)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = PureWhite.copy(alpha = 0.82f)
                            )
                        }
                    }
                }
            }
        }

        // Core 4 Stats Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        label = "مأموریت‌های موفق",
                        value = PersianFormat.number(profile.missionsSucceeded),
                        sub = "نرخ موفقیت: ${PersianFormat.percent(winRate)}",
                        icon = Icons.Filled.Verified,
                        accent = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "درآمد کل",
                        value = PersianFormat.money(profile.totalEarnings),
                        sub = "روز ${PersianFormat.number(profile.dayNumber)} خدمتی",
                        icon = Icons.Filled.MonetizationOn,
                        accent = BadgeGold,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        label = "مجرمان دستگیرشده",
                        value = "${PersianFormat.number(profile.criminalsArrested)} نفر",
                        sub = "تحویل زندان: ${PersianFormat.number(profile.inmatesTransferred)} • فراری: ${PersianFormat.number(profile.criminalsEscaped)}",
                        icon = Icons.Filled.Gavel,
                        accent = PoliceBlue,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "نیروها و خودروها",
                        value = "${PersianFormat.number(state.officers.size)} / ${PersianFormat.number(state.vehicles.size)}",
                        sub = GameEngine.stationStatusLabel(profile),
                        icon = Icons.Filled.Shield,
                        accent = NavyPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 20-Level Progression Roadmap
        item {
            SectionHeader(
                title = "مسیر پیشرفت ۲۰ سطح رئیس پلیس",
                subtitle = "با کسب تجربه (XP)، مأموریت‌ها، نیروها و خودروهای جدید باز می‌شوند"
            )
        }

        items(GameCatalog.milestones, key = { it.level }) { milestone ->
            MilestoneRow(
                milestone = milestone,
                currentLevel = profile.level
            )
        }

        // Reset Game Button
        item {
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("reset_game_button"),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.65f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed)
            ) {
                Icon(
                    imageVector = Icons.Filled.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "شروع مجدد بازی از سطح ۱", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    sub: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, SlateBorder.copy(alpha = 0.55f)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = SlateMuted)
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = NavyDeep,
                fontWeight = FontWeight.Bold
            )
            Text(text = sub, style = MaterialTheme.typography.labelSmall, color = SlateMuted)
        }
    }
}

@Composable
private fun MilestoneRow(
    milestone: LevelMilestone,
    currentLevel: Int
) {
    val unlocked = currentLevel >= milestone.level
    val isCurrent = currentLevel == milestone.level

    Surface(
        shape = RoundedCornerShape(15.dp),
        color = when {
            isCurrent -> BadgeGoldSoft.copy(alpha = 0.6f)
            unlocked -> PureWhite
            else -> PureWhite.copy(alpha = 0.65f)
        },
        border = BorderStroke(
            width = if (isCurrent) 1.5.dp else 1.dp,
            color = when {
                isCurrent -> BadgeGold
                unlocked -> SuccessGreen.copy(alpha = 0.45f)
                else -> SlateBorder.copy(alpha = 0.45f)
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCurrent -> BadgeGold
                            unlocked -> SuccessGreenSoft
                            else -> IceBlueSurface
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (unlocked) {
                    Text(
                        text = PersianFormat.number(milestone.level),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isCurrent) NavyDeep else SuccessGreen,
                        fontWeight = FontWeight.ExtraBold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = SlateMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "سطح ${PersianFormat.number(milestone.level)} • ${milestone.rankTitleFa}",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (unlocked) NavyDeep else SlateMuted
                )
                Text(
                    text = milestone.unlockDescriptionFa,
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateMuted
                )
            }

            if (unlocked) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "باز شده",
                    tint = if (isCurrent) BadgeGold else SuccessGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
