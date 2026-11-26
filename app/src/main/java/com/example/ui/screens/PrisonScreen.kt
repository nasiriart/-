package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.InmateEntity
import com.example.data.local.MissionEntity
import com.example.data.model.InmateStatus
import com.example.data.model.MissionStatus
import com.example.data.model.PrisonUpgradeType
import com.example.domain.GameEngine
import com.example.ui.components.DifficultyStars
import com.example.ui.components.SectionHeader
import com.example.ui.components.escapeRiskColor
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedSoft
import com.example.ui.theme.BadgeGold
import com.example.ui.theme.BadgeGoldSoft
import com.example.ui.theme.IceBlueSurface
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PoliceBlue
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyAccent
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateInk
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenBright
import com.example.ui.theme.SuccessGreenSoft
import com.example.ui.theme.WarningOrange
import com.example.ui.theme.WarningOrangeSoft
import com.example.ui.viewmodel.GameUiState
import com.example.util.PersianFormat

@Composable
fun PrisonScreen(
    state: GameUiState,
    onInterrogateInmate: (Int) -> Unit,
    onTransferInmate: (Int) -> Unit,
    onQuellBreakout: (Int) -> Unit,
    onInspectCellblock: () -> Unit,
    onPursueEscapedMission: (MissionEntity) -> Unit,
    onUpgradePrison: (PrisonUpgradeType) -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val profile = state.profile
    val maxCells = GameEngine.maxPrisonCapacity(profile)
    val occupiedCells = state.activeInmates.size
    val securityRating = GameEngine.prisonSecurityRating(profile)
    val occupancyRatio = (occupiedCells.toFloat() / maxCells.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(IceBlueSurface)
            .testTag("prison_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Illustrated Prison HQ Hero Card
        item(key = "prison_hero") {
            Surface(
                shape = RoundedCornerShape(22.dp),
                shadowElevation = 6.dp,
                color = NavyDeep,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(195.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_prison_hq_1795703002683),
                        contentDescription = "بازداشتگاه و زندان مرکزی شهر",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        NavyDeep.copy(alpha = 0.25f),
                                        NavyDeep.copy(alpha = 0.88f)
                                    )
                                )
                            )
                    )

                    // Top Status Badge
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (state.breakoutAlertsCount > 0) AlertRed else NavyPrimary.copy(alpha = 0.85f),
                        border = BorderStroke(
                            1.dp,
                            if (state.breakoutAlertsCount > 0) BadgeGold else SkyAccent.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = if (state.breakoutAlertsCount > 0) Icons.Filled.GppBad else Icons.Filled.Security,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = if (state.breakoutAlertsCount > 0) {
                                    "آژیر قرمز: ${PersianFormat.number(state.breakoutAlertsCount)} تلاش فرار!"
                                } else {
                                    "امنیت بند: ${PersianFormat.percent(securityRating)}"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = PureWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Bottom Info & Cell Occupancy Meter
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "بازداشتگاه و زندان مرکزی",
                            style = MaterialTheme.typography.headlineSmall,
                            color = PureWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ظرفیت اشغال‌شده: ${PersianFormat.number(occupiedCells)} از ${PersianFormat.number(maxCells)} سلول",
                                style = MaterialTheme.typography.labelMedium,
                                color = SkyAccent
                            )
                            Text(
                                text = "تحویل دادگاه: ${PersianFormat.number(profile.inmatesTransferred)} نفر",
                                style = MaterialTheme.typography.labelMedium,
                                color = BadgeGold
                            )
                        }
                        LinearProgressIndicator(
                            progress = { occupancyRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(50)),
                            color = if (occupiedCells >= maxCells) AlertRed else SkyAccent,
                            trackColor = PureWhite.copy(alpha = 0.22f),
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }
        }

        // Sub-Tab Switcher
        item(key = "prison_tabs") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PrisonTabButton(
                    label = "بند زندانیان (${PersianFormat.number(occupiedCells)})",
                    selected = selectedTab == 0,
                    badgeAlert = state.breakoutAlertsCount > 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("prison_tab_inmates")
                )
                PrisonTabButton(
                    label = "توسعه زندان",
                    selected = selectedTab == 1,
                    badgeAlert = false,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("prison_tab_upgrades")
                )
            }
        }

        // Live Escaped Prisoners Loose in City — Direct Chase & Dispatch Section!
        if (state.prisonBreakoutPursuits.isNotEmpty()) {
            item(key = "escaped_pursuit_header") {
                SectionHeader(
                    title = "زندانیان فراری تحت تعقیب در شهر!",
                    subtitle = "این زندانیان از بند گریخته‌اند؛ همین حالا تیم گشت و خودروی سریع برای تعقیب اعزام کنید"
                )
            }
            items(state.prisonBreakoutPursuits, key = { "prison_pursuit_${it.id}" }) { pursuit ->
                val isInProgress = pursuit.status == MissionStatus.IN_PROGRESS.name
                val remainingSec = if (!isInProgress && pursuit.expiresAtMillis > 0L) {
                    ((pursuit.expiresAtMillis - state.nowMillis).coerceAtLeast(0L) + 999L) / 1000L
                } else {
                    0L
                }
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = AlertRedSoft.copy(alpha = 0.78f),
                    border = BorderStroke(1.6.dp, AlertRed),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prison_pursuit_card_${pursuit.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AlertRed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DirectionsRun,
                                        contentDescription = "فراری از زندان",
                                        tint = PureWhite,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = pursuit.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = NavyDeep,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "منطقه رؤیت: ${pursuit.district} • جایزه ${PersianFormat.money(pursuit.rewardMoney)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AlertRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            if (!isInProgress && remainingSec > 0L) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = PureWhite
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Timer,
                                            contentDescription = null,
                                            tint = AlertRed,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = PersianFormat.duration(remainingSec),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AlertRed,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = pursuit.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = NavyDeep
                        )

                        Button(
                            onClick = { onPursueEscapedMission(pursuit) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isInProgress) BadgeGold else AlertRed,
                                contentColor = if (isInProgress) NavyDeep else PureWhite
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("pursue_escaped_${pursuit.id}")
                        ) {
                            Icon(
                                imageVector = if (isInProgress) Icons.Filled.Schedule else Icons.Filled.Send,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isInProgress) {
                                    "مشاهده وضعیت تعقیب در مأموریت‌ها"
                                } else {
                                    "تعقیب فوری و اعزام نیرو به ${pursuit.district}"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        if (selectedTab == 0) {
            // Active Detained Inmates Section
            if (state.activeInmates.isEmpty()) {
                item(key = "prison_empty") {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = PureWhite,
                        border = BorderStroke(1.dp, SlateBorder.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(NavyPrimary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = "سلول‌های خالی",
                                    tint = NavyPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Text(
                                text = "تمام سلول‌های بازداشتگاه خالی است",
                                style = MaterialTheme.typography.titleMedium,
                                color = NavyDeep,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "با انجام موفق مأموریت‌ها و دستگیری مجرمان، متهمان به این بخش منتقل می‌شوند تا بازجویی و تحویل دادگاه شوند.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateMuted
                            )
                        }
                    }
                }
            } else {
                item(key = "inmates_header") {
                    SectionHeader(
                        title = "زندانیان تحت نظر در سلول‌ها",
                        subtitle = "بازجویی، بازرسی امنیتی بند و مهار تلاش‌های فرار",
                        trailing = {
                            OutlinedButton(
                                onClick = onInspectCellblock,
                                shape = RoundedCornerShape(11.dp),
                                border = BorderStroke(1.2.dp, NavyPrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("prison_inspect_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Policy,
                                    contentDescription = "بازرسی بند",
                                    tint = NavyPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "بازرسی بند",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = NavyPrimary
                                )
                            }
                        }
                    )
                }
                items(state.activeInmates, key = { "inmate_${it.id}" }) { inmate ->
                    InmateCellCard(
                        inmate = inmate,
                        escapeRiskPercent = GameEngine.inmateEscapeRiskPercent(inmate, profile),
                        interrogationBonus = GameEngine.interrogationRewardMoney(inmate, profile),
                        nowMillis = state.nowMillis,
                        onInterrogate = { onInterrogateInmate(inmate.id) },
                        onTransfer = { onTransferInmate(inmate.id) },
                        onQuellBreakout = { onQuellBreakout(inmate.id) }
                    )
                }
            }

            // Recent Prison Log (Transferred or Escaped)
            if (state.inmateHistory.isNotEmpty()) {
                item(key = "history_header") {
                    SectionHeader(
                        title = "گزارش پرونده‌های اخیر زندان",
                        subtitle = "سوابق تحویل به دادگاه و هشدارهای امنیتی بند"
                    )
                }
                items(state.inmateHistory.take(8), key = { "inmate_hist_${it.id}" }) { record ->
                    val escaped = record.status == InmateStatus.ESCAPED_PRISON.name
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (escaped) AlertRedSoft.copy(alpha = 0.55f) else PureWhite,
                        border = BorderStroke(
                            1.dp,
                            if (escaped) AlertRed.copy(alpha = 0.45f) else SlateBorder.copy(alpha = 0.55f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (escaped) Icons.Filled.ReportProblem else Icons.Filled.Verified,
                                contentDescription = null,
                                tint = if (escaped) AlertRed else SuccessGreen,
                                modifier = Modifier.size(22.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${record.name} «${record.nickname}» • ${record.crimeTitle}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = NavyDeep
                                )
                                Text(
                                    text = if (escaped && record.breakoutReason.isNotBlank()) {
                                        record.breakoutReason
                                    } else {
                                        "تحویل موفق به دادگاه • پاداش ${PersianFormat.money(record.bailReward)}"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (escaped) AlertRed else SlateMuted
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Prison Upgrades Tab
            item(key = "upgrades_header") {
                SectionHeader(
                    title = "ارتقای امکانات و امنیت زندان",
                    subtitle = "سلول‌های بیشتر بسازید، فرار از زندان را مهار کنید و درآمد پرونده‌سازی را بالا ببرید"
                )
            }
            items(PrisonUpgradeType.entries, key = { "prison_upg_${it.name}" }) { type ->
                val currentLevel = GameEngine.prisonUpgradeLevel(profile, type)
                val isMax = currentLevel >= type.maxLevel
                val cost = GameEngine.prisonUpgradeCost(type, currentLevel)
                val canAfford = profile.money >= cost

                val icon: ImageVector = when (type) {
                    PrisonUpgradeType.CELL_BLOCKS -> Icons.Filled.GridView
                    PrisonUpgradeType.PRISON_SECURITY -> Icons.Filled.Shield
                    PrisonUpgradeType.INTERROGATION_WING -> Icons.Filled.ManageSearch
                }
                val accent: Color = when (type) {
                    PrisonUpgradeType.CELL_BLOCKS -> PoliceBlue
                    PrisonUpgradeType.PRISON_SECURITY -> SuccessGreen
                    PrisonUpgradeType.INTERROGATION_WING -> BadgeGold
                }
                val currentMetricFa = when (type) {
                    PrisonUpgradeType.CELL_BLOCKS -> "ظرفیت فعلی: ${PersianFormat.number(maxCells)} سلول"
                    PrisonUpgradeType.PRISON_SECURITY -> "ضریب امنیت فعلی: ${PersianFormat.percent(securityRating)}"
                    PrisonUpgradeType.INTERROGATION_WING -> "افزایش پاداش فعلی: +${PersianFormat.percent((currentLevel - 1) * 15)}"
                }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = PureWhite,
                    border = BorderStroke(1.dp, SlateBorder.copy(alpha = 0.6f)),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prison_upgrade_${type.name.lowercase()}")
                ) {
                    Column(
                        modifier = Modifier.padding(15.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(accent.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = type.titleFa,
                                    tint = accent,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = type.titleFa,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = NavyDeep,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "سطح ${PersianFormat.number(currentLevel)} از ${PersianFormat.number(type.maxLevel)} • $currentMetricFa",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = accent
                                )
                            }
                        }

                        Text(
                            text = type.effectDescFa,
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateMuted
                        )

                        LinearProgressIndicator(
                            progress = { currentLevel.toFloat() / type.maxLevel.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(50)),
                            color = accent,
                            trackColor = SlateBorder.copy(alpha = 0.35f),
                            strokeCap = StrokeCap.Round
                        )

                        Button(
                            onClick = { onUpgradePrison(type) },
                            enabled = !isMax && canAfford,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NavyPrimary,
                                contentColor = PureWhite
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("prison_upgrade_btn_${type.name.lowercase()}")
                        ) {
                            if (isMax) {
                                Text("حداکثر سطح تکمیل شد", style = MaterialTheme.typography.labelLarge)
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.ArrowUpward,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ارتقا به سطح ${PersianFormat.number(currentLevel + 1)} (${PersianFormat.money(cost)})",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        }

        item(key = "prison_bottom_spacer") {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PrisonTabButton(
    label: String,
    selected: Boolean,
    badgeAlert: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) NavyPrimary else PureWhite,
        border = BorderStroke(
            width = if (badgeAlert) 1.5.dp else 1.dp,
            color = when {
                badgeAlert -> AlertRed
                selected -> NavyPrimary
                else -> SlateBorder
            }
        ),
        modifier = modifier.height(48.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (badgeAlert) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(AlertRed)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) PureWhite else SlateInk,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun InmateCellCard(
    inmate: InmateEntity,
    escapeRiskPercent: Int,
    interrogationBonus: Long,
    nowMillis: Long,
    onInterrogate: () -> Unit,
    onTransfer: () -> Unit,
    onQuellBreakout: () -> Unit
) {
    val isBreakout = inmate.status == InmateStatus.BREAKOUT_ALERT.name
    val totalMs = (inmate.releaseAtMillis - inmate.detainedAtMillis).coerceAtLeast(1000L)
    val remainingMs = (inmate.releaseAtMillis - nowMillis).coerceAtLeast(0L)
    val progress = (1f - remainingMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)
    val isReadyForTransfer = !isBreakout && remainingMs == 0L
    val breakoutRemainingSec = ((inmate.breakoutDeadlineMillis - nowMillis).coerceAtLeast(0L) + 999L) / 1000L

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = when {
            isBreakout -> AlertRedSoft.copy(alpha = 0.72f)
            isReadyForTransfer -> SuccessGreenSoft.copy(alpha = 0.52f)
            else -> PureWhite
        },
        border = BorderStroke(
            width = if (isBreakout || isReadyForTransfer) 1.6.dp else 1.dp,
            color = when {
                isBreakout -> AlertRed
                isReadyForTransfer -> SuccessGreen
                else -> SlateBorder.copy(alpha = 0.6f)
            }
        ),
        shadowElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("inmate_card_${inmate.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_inmate_avatar_1795703002796),
                    contentDescription = inmate.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(
                            width = 2.dp,
                            color = if (isBreakout) AlertRed else NavyPrimary,
                            shape = RoundedCornerShape(14.dp)
                        )
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = inmate.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = NavyDeep,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = WarningOrangeSoft
                        ) {
                            Text(
                                text = "«${inmate.nickname}»",
                                style = MaterialTheme.typography.labelSmall,
                                color = WarningOrange,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = "${inmate.crimeTitle} • ${inmate.district}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateMuted
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DifficultyStars(difficulty = inmate.dangerLevel)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MonetizationOn,
                                contentDescription = null,
                                tint = BadgeGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = PersianFormat.money(inmate.bailReward),
                                style = MaterialTheme.typography.labelSmall,
                                color = NavyPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "فرار ${PersianFormat.percent(escapeRiskPercent)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = escapeRiskColor(escapeRiskPercent),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (isBreakout) {
                // Emergency Prison Breakout Alert Strip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PureWhite,
                    border = BorderStroke(1.dp, AlertRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.GppBad,
                                    contentDescription = "تلاش فرار از زندان",
                                    tint = AlertRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "هشدار فرار از سلول!",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = AlertRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "فرصت مهار: ${PersianFormat.duration(breakoutRemainingSec)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = AlertRed,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        if (inmate.breakoutReason.isNotBlank()) {
                            Text(
                                text = inmate.breakoutReason,
                                style = MaterialTheme.typography.bodySmall,
                                color = NavyDeep
                            )
                        }
                        Button(
                            onClick = onQuellBreakout,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AlertRed,
                                contentColor = PureWhite
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("quell_breakout_${inmate.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Shield,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("مهار فوری فرار و قفل سلول", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            } else {
                // Sentence / Case Processing Progress Bar
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isReadyForTransfer) "پرونده آماده تحویل به دادگاه" else "تکمیل پرونده و نگهداری در بند",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isReadyForTransfer) SuccessGreen else SlateMuted
                        )
                        if (!isReadyForTransfer) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Schedule,
                                    contentDescription = null,
                                    tint = NavyPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = PersianFormat.duration((remainingMs + 999L) / 1000L),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NavyPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(50)),
                        color = if (isReadyForTransfer) SuccessGreenBright else PoliceBlue,
                        trackColor = SlateBorder.copy(alpha = 0.4f),
                        strokeCap = StrokeCap.Round
                    )
                }

                // Action Row: Interrogate + Transfer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onInterrogate,
                        enabled = !inmate.interrogated,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.2.dp,
                            if (inmate.interrogated) SlateBorder else PoliceBlue
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PoliceBlue
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("interrogate_inmate_${inmate.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ManageSearch,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (inmate.interrogated) {
                                "بازجویی شد"
                            } else {
                                "بازجویی (+${PersianFormat.number(interrogationBonus)})"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1
                        )
                    }

                    Button(
                        onClick = onTransfer,
                        enabled = isReadyForTransfer,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SuccessGreen,
                            contentColor = PureWhite
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1.1f)
                            .height(48.dp)
                            .testTag("transfer_inmate_${inmate.id}")
                    ) {
                        Icon(
                            imageVector = if (isReadyForTransfer) Icons.Filled.Gavel else Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isReadyForTransfer) "تحویل (+${PersianFormat.number(inmate.bailReward)})" else "در انتظار تکمیل",
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
