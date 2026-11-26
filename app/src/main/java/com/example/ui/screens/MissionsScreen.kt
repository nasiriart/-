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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.MissionEntity
import com.example.data.model.CrimeType
import com.example.data.model.MissionStatus
import com.example.data.model.TacticalAlertOption
import com.example.ui.components.DifficultyStars
import com.example.ui.components.SectionHeader
import com.example.ui.components.crimeIcon
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedSoft
import com.example.ui.theme.BadgeGold
import com.example.ui.theme.BadgeGoldSoft
import com.example.ui.theme.IceBlueSurface
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PoliceBlue
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateInk
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenSoft
import com.example.ui.theme.WarningOrange
import com.example.ui.theme.WarningOrangeSoft
import com.example.ui.viewmodel.GameUiState
import com.example.util.PersianFormat

@Composable
fun MissionsScreen(
    state: GameUiState,
    onOpenDispatch: (Int) -> Unit,
    onClaimMission: (Int) -> Unit,
    onScanNewCrime: () -> Unit,
    onTacticalDecision: (Int, TacticalAlertOption) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IceBlueSurface)
            .testTag("missions_screen")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MissionTabChip(
                selected = selectedTab == 0,
                label = "جدید (${PersianFormat.number(state.availableMissions.size)})",
                onClick = { selectedTab = 0 },
                modifier = Modifier
                    .weight(1f)
                    .testTag("tab_available_missions")
            )
            MissionTabChip(
                selected = selectedTab == 1,
                label = "در حال اجرا (${PersianFormat.number(state.activeMissions.size)})",
                onClick = { selectedTab = 1 },
                modifier = Modifier
                    .weight(1.1f)
                    .testTag("tab_active_missions")
            )
            MissionTabChip(
                selected = selectedTab == 2,
                label = "سوابق (${PersianFormat.number(state.completedMissions.size)})",
                onClick = { selectedTab = 2 },
                modifier = Modifier
                    .weight(0.95f)
                    .testTag("tab_history_missions")
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    item(key = "avail_header") {
                        SectionHeader(
                            title = "گزارش‌های جرائم شهری",
                            subtitle = "پیش از پایان مهلت، نیرو اعزام کنید تا مجرم فرار نکند!",
                            trailing = {
                                OutlinedButton(
                                    onClick = onScanNewCrime,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, PoliceBlue),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("missions_scan_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Radar,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("گزارش جدید", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        )
                    }

                    if (state.availableMissions.isEmpty()) {
                        item(key = "avail_empty") {
                            EmptyMissionState(
                                title = "در حال حاضر شهر آرام است!",
                                body = "می‌توانید برای دریافت گزارش جرم جدید، دکمه «گزارش جدید» را لمس کنید.",
                                actionText = "جستجوی گزارش جدید",
                                onAction = onScanNewCrime
                            )
                        }
                    } else {
                        items(state.availableMissions, key = { "avail_${it.id}" }) { mission ->
                            AvailableMissionCard(
                                mission = mission,
                                nowMillis = state.nowMillis,
                                onDispatchClick = { onOpenDispatch(mission.id) }
                            )
                        }
                    }
                }

                1 -> {
                    item(key = "active_header") {
                        SectionHeader(
                            title = "مأموریت‌های در حال انجام",
                            subtitle = "مراقب پیام‌های اضطراری بی‌سیم باشید تا دزد فرار نکند!"
                        )
                    }
                    if (state.activeMissions.isEmpty()) {
                        item(key = "active_empty") {
                            EmptyMissionState(
                                title = "هیچ مأموریتی در حال اجرا نیست",
                                body = "از تب «جدید» یکی از گزارش‌های شهر را انتخاب کرده و مأموران را اعزام کنید.",
                                actionText = "مشاهده گزارش‌های جدید",
                                onAction = { selectedTab = 0 }
                            )
                        }
                    } else {
                        items(state.activeMissions, key = { "active_${it.id}" }) { mission ->
                            ActiveMissionCompactCard(
                                mission = mission,
                                officers = state.officers,
                                vehicles = state.vehicles,
                                playerMoney = state.profile.money,
                                nowMillis = state.nowMillis,
                                onClaimClick = { onClaimMission(mission.id) },
                                onTacticalDecision = { option -> onTacticalDecision(mission.id, option) }
                            )
                        }
                    }
                }

                else -> {
                    item(key = "hist_header") {
                        SectionHeader(
                            title = "سوابق مأموریت‌ها و گزارش فرارها",
                            subtitle = "جزئیات پرونده‌های موفق، ناموفق و دلایل فرار مجرمان"
                        )
                    }
                    if (state.completedMissions.isEmpty()) {
                        item(key = "hist_empty") {
                            EmptyMissionState(
                                title = "هنوز پرونده‌ای تکمیل نشده است",
                                body = "پس از پایان اولین مأموریت، نتیجه آن در این بخش ثبت می‌شود."
                            )
                        }
                    } else {
                        items(state.completedMissions, key = { "done_${it.id}" }) { mission ->
                            CompletedMissionCard(mission = mission)
                        }
                    }
                }
            }

            item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(10.dp)) }
        }
    }
}

@Composable
private fun MissionTabChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1
                )
            }
        },
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = NavyPrimary,
            selectedLabelColor = PureWhite,
            containerColor = PureWhite,
            labelColor = SlateInk
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = SlateBorder,
            selectedBorderColor = NavyPrimary
        )
    )
}

@Composable
private fun AvailableMissionCard(
    mission: MissionEntity,
    nowMillis: Long,
    onDispatchClick: () -> Unit
) {
    val crimeType = CrimeType.fromName(mission.crimeType)
    val escapeRemainingSec = if (mission.expiresAtMillis > 0L) {
        ((mission.expiresAtMillis - nowMillis).coerceAtLeast(0L) + 999L) / 1000L
    } else {
        0L
    }
    val isUrgent = escapeRemainingSec in 1..30

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = PureWhite,
        border = BorderStroke(
            width = if (mission.isFugitivePursuit || isUrgent) 1.5.dp else 1.dp,
            color = when {
                mission.isFugitivePursuit -> WarningOrange
                isUrgent -> AlertRed
                else -> SlateBorder.copy(alpha = 0.55f)
            }
        ),
        shadowElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mission_card_${mission.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Fugitive / Escape Countdown Badges
            if (mission.isFugitivePursuit || escapeRemainingSec > 0L) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (mission.isFugitivePursuit) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BadgeGoldSoft,
                            border = BorderStroke(1.dp, BadgeGold)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DirectionsRun,
                                    contentDescription = null,
                                    tint = WarningOrange,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "تحت تعقیب • جایزه +۴۵٪",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NavyDeep,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (escapeRemainingSec > 0L) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isUrgent) AlertRedSoft else WarningOrangeSoft
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Timer,
                                    contentDescription = "مهلت قبل از فرار",
                                    tint = if (isUrgent) AlertRed else WarningOrange,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "فرار تا ${PersianFormat.duration(escapeRemainingSec)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isUrgent) AlertRed else WarningOrange,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(11.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (mission.isFugitivePursuit) AlertRedSoft else PoliceBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (mission.isFugitivePursuit) Icons.Filled.DirectionsRun else crimeIcon(crimeType),
                            contentDescription = mission.title,
                            tint = if (mission.isFugitivePursuit) AlertRed else PoliceBlue,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                    Column {
                        Text(
                            text = mission.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = NavyDeep,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${mission.district} • تخصص: ${crimeType.preferredSpecialty.titleFa}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateMuted
                        )
                    }
                }
                DifficultyStars(difficulty = mission.difficulty)
            }

            Text(
                text = mission.description,
                style = MaterialTheme.typography.bodyMedium,
                color = SlateMuted
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(IceBlueSurface)
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetricPill(
                    icon = Icons.Filled.MonetizationOn,
                    text = PersianFormat.money(mission.rewardMoney),
                    tint = BadgeGold
                )
                MetricPill(
                    icon = Icons.Filled.Groups,
                    text = "${PersianFormat.number(mission.requiredOfficers)} مأمور",
                    tint = PoliceBlue
                )
                MetricPill(
                    icon = Icons.Filled.Schedule,
                    text = PersianFormat.duration(mission.durationSeconds.toLong()),
                    tint = NavyPrimary
                )
            }

            Button(
                onClick = onDispatchClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("dispatch_button_${mission.id}"),
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (mission.isFugitivePursuit) AlertRed else PoliceBlue,
                    contentColor = PureWhite
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "انتخاب و اعزام نیرو (+${PersianFormat.number(mission.rewardXp)} تجربه)",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun MetricPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    tint: androidx.compose.ui.graphics.Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = SlateInk,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CompletedMissionCard(mission: MissionEntity) {
    val status = MissionStatus.fromName(mission.status)
    val isSuccess = status == MissionStatus.COMPLETED_SUCCESS
    val isEscaped = status == MissionStatus.ESCAPED

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = PureWhite,
        border = BorderStroke(
            1.dp,
            when {
                isSuccess -> SuccessGreen.copy(alpha = 0.4f)
                isEscaped -> AlertRed.copy(alpha = 0.55f)
                else -> WarningOrange.copy(alpha = 0.45f)
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when {
                            isSuccess -> SuccessGreenSoft
                            isEscaped -> AlertRedSoft
                            else -> WarningOrangeSoft
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        isSuccess -> Icons.Filled.Verified
                        isEscaped -> Icons.Filled.DirectionsRun
                        else -> Icons.Filled.Warning
                    },
                    contentDescription = null,
                    tint = when {
                        isSuccess -> SuccessGreen
                        isEscaped -> AlertRed
                        else -> WarningOrange
                    },
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${mission.title} • ${mission.district}",
                        style = MaterialTheme.typography.titleSmall,
                        color = NavyDeep,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            isSuccess -> SuccessGreenSoft
                            isEscaped -> AlertRedSoft
                            else -> WarningOrangeSoft
                        }
                    ) {
                        Text(
                            text = when {
                                isSuccess -> "موفق"
                                isEscaped -> "فرار مجرم"
                                else -> "ناموفق"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = when {
                                isSuccess -> SuccessGreen
                                isEscaped -> AlertRed
                                else -> WarningOrange
                            },
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Text(
                    text = if (isEscaped && mission.escapeReason.isNotBlank()) {
                        "دلیل فرار: ${mission.escapeReason}"
                    } else {
                        mission.resultMessage.ifBlank {
                            if (isSuccess) "عملیات با موفقیت ثبت شد" else "عملیات ناتمام ماند"
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isEscaped) AlertRed else SlateMuted
                )
            }
        }
    }
}

@Composable
private fun EmptyMissionState(
    title: String,
    body: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, SlateBorder.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = PoliceBlue,
                modifier = Modifier.size(44.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = NavyDeep
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = SlateMuted
            )
            if (actionText != null && onAction != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PoliceBlue, contentColor = PureWhite)
                ) {
                    Text(text = actionText, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
