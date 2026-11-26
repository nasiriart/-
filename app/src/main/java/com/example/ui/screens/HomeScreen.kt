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
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.MissionEntity
import com.example.data.local.OfficerEntity
import com.example.data.local.VehicleEntity
import com.example.data.model.ChiefTrait
import com.example.data.model.MissionStatus
import com.example.data.model.OfficerSpecialty
import com.example.data.model.TacticalAlertOption
import com.example.domain.GameEngine
import com.example.ui.components.CityMapCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.chiefAvatarRes
import com.example.ui.components.escapeRiskColor
import com.example.ui.components.officerPortraitRes
import com.example.ui.components.vehicleImageRes
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
import com.example.ui.theme.SuccessGreenBright
import com.example.ui.theme.SuccessGreenSoft
import com.example.ui.theme.WarningOrange
import com.example.ui.theme.WarningOrangeSoft
import com.example.ui.viewmodel.GameUiState
import com.example.util.PersianFormat

@Composable
fun HomeScreen(
    state: GameUiState,
    onOpenMissions: () -> Unit,
    onOpenStation: () -> Unit,
    onOpenPrison: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenStats: () -> Unit,
    onScanNewCrime: () -> Unit,
    onMissionPinClick: (MissionEntity) -> Unit,
    onClaimMission: (Int) -> Unit,
    onTacticalDecision: (Int, TacticalAlertOption) -> Unit
) {
    val maxCapacity = GameEngine.maxOfficerCapacity(state.profile)
    val maxCells = GameEngine.maxPrisonCapacity(state.profile)
    val safetyScore = GameEngine.citySafetyScore(
        state.profile,
        state.officers.size,
        state.vehicles.size
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(IceBlueSurface)
            .testTag("home_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeStatTile(
                        title = "موجودی کلانتری",
                        value = PersianFormat.money(state.profile.money),
                        subtitle = "سطح ${PersianFormat.number(state.profile.level)} از ۲۰",
                        icon = Icons.Filled.MonetizationOn,
                        accent = BadgeGold,
                        modifier = Modifier.weight(1f)
                    )
                    HomeStatTile(
                        title = "مأموران آماده",
                        value = "${PersianFormat.number(state.freeOfficersCount)} از ${PersianFormat.number(state.officers.size)}",
                        subtitle = "ظرفیت کل: ${PersianFormat.number(maxCapacity)} نفر",
                        icon = Icons.Filled.LocalPolice,
                        accent = PoliceBlue,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeStatTile(
                        title = "مأموریت‌های فعال",
                        value = PersianFormat.number(state.activeMissions.size),
                        subtitle = "${PersianFormat.number(state.availableMissions.size)} گزارش در انتظار",
                        icon = Icons.Filled.NotificationsActive,
                        accent = if (state.readyToClaimCount > 0) SuccessGreen else AlertRed,
                        modifier = Modifier.weight(1f)
                    )
                    HomeStatTile(
                        title = "وضعیت کلانتری",
                        value = GameEngine.stationStatusLabel(state.profile),
                        subtitle = "امنیت شهر: ${PersianFormat.percent(safetyScore)}",
                        icon = Icons.Filled.Apartment,
                        accent = NavyPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Interactive 2D Live City Map with Landmarks & Live Escaping Criminals
        item {
            CityMapCard(
                availableMissions = state.availableMissions,
                activeMissions = state.activeMissions,
                nowMillis = state.nowMillis,
                safetyScore = safetyScore,
                prisonBreakoutActive = state.breakoutAlertsCount > 0 || state.prisonBreakoutPursuits.isNotEmpty(),
                onStationClick = onOpenStation,
                onPrisonAlertClick = onOpenPrison,
                onMissionPinClick = { mission ->
                    if (mission.status == MissionStatus.IN_PROGRESS.name && state.nowMillis >= mission.endTimeMillis) {
                        onClaimMission(mission.id)
                    } else {
                        onMissionPinClick(mission)
                    }
                }
            )
        }

        // Primary Command Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenMissions,
                    modifier = Modifier
                        .weight(1.25f)
                        .height(52.dp)
                        .testTag("home_view_missions_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PoliceBlue,
                        contentColor = PureWhite
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Assignment,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "مشاهده مأموریت‌ها",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                OutlinedButton(
                    onClick = onScanNewCrime,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("home_scan_crime_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, NavyPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Radar,
                        contentDescription = null,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "گزارش جدید",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        // Urgent Escaped Prisoner Hot-Pursuit Alert Banner (if any prisoner escaped onto the map!)
        val activePursuit = state.prisonBreakoutPursuits.firstOrNull()
        if (activePursuit != null) {
            item(key = "home_escaped_pursuit_banner") {
                val isChasing = activePursuit.status == MissionStatus.IN_PROGRESS.name
                Surface(
                    onClick = { onMissionPinClick(activePursuit) },
                    shape = RoundedCornerShape(18.dp),
                    color = AlertRed,
                    border = BorderStroke(1.5.dp, BadgeGold),
                    shadowElevation = 5.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_escaped_pursuit_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PureWhite.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DirectionsRun,
                                    contentDescription = "فراری از زندان",
                                    tint = PureWhite,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = activePursuit.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = PureWhite,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "در ${activePursuit.district} دیده شده • جایزه ${PersianFormat.money(activePursuit.rewardMoney)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BadgeGoldSoft
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = BadgeGold
                        ) {
                            Text(
                                text = if (isChasing) "در حال تعقیب" else "تعقیب فوری",
                                style = MaterialTheme.typography.labelMedium,
                                color = NavyDeep,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        }

        // City Prison Quick-Access Command Card
        item {
            val hasBreakout = state.breakoutAlertsCount > 0
            val hasReadyTransfer = state.readyTransfersCount > 0
            Surface(
                onClick = onOpenPrison,
                shape = RoundedCornerShape(18.dp),
                color = when {
                    hasBreakout -> AlertRedSoft
                    hasReadyTransfer -> SuccessGreenSoft.copy(alpha = 0.65f)
                    else -> PureWhite
                },
                border = BorderStroke(
                    width = if (hasBreakout || hasReadyTransfer) 1.5.dp else 1.dp,
                    color = when {
                        hasBreakout -> AlertRed
                        hasReadyTransfer -> SuccessGreen
                        else -> SlateBorder.copy(alpha = 0.65f)
                    }
                ),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_prison_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(
                                    when {
                                        hasBreakout -> AlertRed
                                        hasReadyTransfer -> SuccessGreen
                                        else -> NavyPrimary
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (hasBreakout) Icons.Filled.GppBad else Icons.Filled.Lock,
                                contentDescription = "زندان مرکزی شهر",
                                tint = PureWhite,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = if (hasBreakout) "آژیر قرمز در زندان مرکزی!" else "زندان و بازداشتگاه مرکزی",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (hasBreakout) AlertRed else NavyDeep,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when {
                                    hasBreakout -> "تلاش فرار در بند! برای مهار فوری وارد بخش زندان شوید."
                                    hasReadyTransfer -> "${PersianFormat.number(state.readyTransfersCount)} زندانی آماده تحویل به دادگاه و دریافت پاداش!"
                                    else -> "${PersianFormat.number(state.activeInmates.size)} از ${PersianFormat.number(maxCells)} سلول پر • امنیت ${PersianFormat.percent(GameEngine.prisonSecurityRating(state.profile))}"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (hasBreakout) NavyDeep else SlateMuted
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (hasBreakout) AlertRed else PoliceBlue
                    ) {
                        Text(
                            text = if (hasBreakout) "مهار فوری" else "ورود به زندان",
                            style = MaterialTheme.typography.labelMedium,
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }

        // Live Active Dispatches Strip (with deployed portraits, vehicle photo, & mid-mission radio alerts!)
        if (state.activeMissions.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "عملیات‌های در حال اجرا",
                    subtitle = "تایمر زنده و پیام‌های اضطراری بی‌سیم مأموران"
                )
            }
            items(state.activeMissions, key = { "home_active_${it.id}" }) { mission ->
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

        // Chief Character Profile Quick Card
        item {
            val trait = ChiefTrait.fromName(state.profile.chiefTrait)
            Surface(
                onClick = onOpenProfile,
                shape = RoundedCornerShape(18.dp),
                color = PureWhite,
                border = BorderStroke(1.dp, PoliceBlue.copy(alpha = 0.35f)),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_profile_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Image(
                            painter = painterResource(id = chiefAvatarRes(state.profile.chiefAvatarKey)),
                            contentDescription = state.profile.chiefName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(2.dp, BadgeGold, RoundedCornerShape(14.dp))
                        )
                        Column {
                            Text(
                                text = "پروفایل فرمانده: ${state.profile.chiefName}",
                                style = MaterialTheme.typography.titleSmall,
                                color = NavyDeep,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${trait.titleFa} • ${trait.badgeFa}",
                                style = MaterialTheme.typography.bodySmall,
                                color = PoliceBlue
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Filled.Badge,
                        contentDescription = "ویرایش پروفایل",
                        tint = PoliceBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Quick Stats & Career Card
        item {
            Surface(
                onClick = onOpenStats,
                shape = RoundedCornerShape(18.dp),
                color = PureWhite,
                border = BorderStroke(1.dp, SlateBorder.copy(alpha = 0.6f)),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_stats_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NavyPrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.BarChart,
                                contentDescription = "آمار عملکرد",
                                tint = NavyPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "آمار عملکرد و مسیر ۲۰ سطح",
                                style = MaterialTheme.typography.titleSmall,
                                color = NavyDeep
                            )
                            Text(
                                text = "${PersianFormat.number(state.profile.criminalsArrested)} دستگیری • ${PersianFormat.number(state.profile.criminalsEscaped)} فراری • ${PersianFormat.number(state.profile.missionsSucceeded)} موفق",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateMuted
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = BadgeGold,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(6.dp)) }
    }
}

@Composable
private fun HomeStatTile(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, SlateBorder.copy(alpha = 0.5f)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(13.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = SlateMuted
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = NavyDeep,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = SlateMuted
            )
        }
    }
}

@Composable
fun ActiveMissionCompactCard(
    mission: MissionEntity,
    officers: List<OfficerEntity>,
    vehicles: List<VehicleEntity>,
    playerMoney: Long,
    nowMillis: Long,
    onClaimClick: () -> Unit,
    onTacticalDecision: (TacticalAlertOption) -> Unit
) {
    val totalMs = (mission.endTimeMillis - mission.startTimeMillis).coerceAtLeast(1000L)
    val remainingMs = (mission.endTimeMillis - nowMillis).coerceAtLeast(0L)
    val progress = (1f - remainingMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)
    val isReady = remainingMs == 0L
    val hasAlert = mission.tacticalAlertActive && !isReady

    val deployedIds = mission.assignedOfficerIds.split(",").mapNotNull { it.trim().toIntOrNull() }
    val deployedOfficers = officers.filter { it.id in deployedIds }
    val deployedVehicle = mission.assignedVehicleId?.let { vid -> vehicles.firstOrNull { it.id == vid } }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = when {
            isReady -> SuccessGreenSoft.copy(alpha = 0.55f)
            hasAlert -> WarningOrangeSoft.copy(alpha = 0.55f)
            else -> PureWhite
        },
        border = BorderStroke(
            width = if (isReady || hasAlert) 1.5.dp else 1.dp,
            color = when {
                isReady -> SuccessGreen
                hasAlert -> AlertRed
                else -> SlateBorder.copy(alpha = 0.6f)
            }
        ),
        shadowElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_mission_card_${mission.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mission.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = NavyDeep,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${mission.district} • شانس ${PersianFormat.percent(mission.calculatedSuccessRate)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateMuted
                        )
                        Text(
                            text = "فرار ${PersianFormat.percent(mission.escapeRisk)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = escapeRiskColor(mission.escapeRisk),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (isReady) {
                    Button(
                        onClick = onClaimClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SuccessGreen,
                            contentColor = PureWhite
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("claim_mission_${mission.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "بررسی نتیجه", style = MaterialTheme.typography.labelLarge)
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BadgeGoldSoft
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Schedule,
                                contentDescription = null,
                                tint = NavyDeep,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = PersianFormat.duration((remainingMs + 999L) / 1000L),
                                style = MaterialTheme.typography.labelMedium,
                                color = NavyDeep,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Deployed Team Portraits + Vehicle Thumbnail Strip
            if (deployedOfficers.isNotEmpty() || deployedVehicle != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        deployedOfficers.take(4).forEach { officer ->
                            val spec = OfficerSpecialty.fromName(officer.specialty)
                            Image(
                                painter = painterResource(id = officerPortraitRes(officer.name, spec)),
                                contentDescription = officer.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, PoliceBlue, CircleShape)
                            )
                        }
                        Text(
                            text = "${PersianFormat.number(deployedOfficers.size)} مأمور اعزامی",
                            style = MaterialTheme.typography.labelSmall,
                            color = SlateMuted
                        )
                    }
                    if (deployedVehicle != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = deployedVehicle.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = NavyPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Image(
                                painter = painterResource(id = vehicleImageRes(deployedVehicle.modelKey)),
                                contentDescription = deployedVehicle.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .width(42.dp)
                                    .height(28.dp)
                                    .clip(RoundedCornerShape(7.dp))
                                    .border(1.dp, SlateBorder, RoundedCornerShape(7.dp))
                            )
                        }
                    }
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(50)),
                color = when {
                    isReady -> SuccessGreenBright
                    hasAlert -> AlertRed
                    else -> PoliceBlue
                },
                trackColor = SlateBorder.copy(alpha = 0.4f),
                strokeCap = StrokeCap.Round
            )

            // Interactive Mid-Mission Tactical Radio Emergency Panel!
            if (hasAlert) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PureWhite,
                    border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.55f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(11.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Radio,
                                contentDescription = "پیام اضطراری",
                                tint = AlertRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "پیام فوری بی‌سیم (در آستانه فرار مجرم!)",
                                style = MaterialTheme.typography.labelLarge,
                                color = AlertRed
                            )
                        }
                        Text(
                            text = mission.tacticalAlertText,
                            style = MaterialTheme.typography.bodySmall,
                            color = NavyDeep
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val hasVehicle = mission.assignedVehicleId != null
                            TacticalOptionButton(
                                option = TacticalAlertOption.BLOCK_EXITS,
                                enabled = true,
                                accent = NavyPrimary,
                                onClick = { onTacticalDecision(TacticalAlertOption.BLOCK_EXITS) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("tactical_block_${mission.id}")
                            )
                            TacticalOptionButton(
                                option = TacticalAlertOption.HOT_PURSUIT,
                                enabled = hasVehicle,
                                accent = PoliceBlue,
                                onClick = { onTacticalDecision(TacticalAlertOption.HOT_PURSUIT) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("tactical_pursuit_${mission.id}")
                            )
                            TacticalOptionButton(
                                option = TacticalAlertOption.RADAR_BACKUP,
                                enabled = playerMoney >= TacticalAlertOption.RADAR_BACKUP.costCoins,
                                accent = WarningOrange,
                                onClick = { onTacticalDecision(TacticalAlertOption.RADAR_BACKUP) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("tactical_radar_${mission.id}")
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TacticalOptionButton(
    option: TacticalAlertOption,
    enabled: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        color = if (enabled) accent else SlateBorder.copy(alpha = 0.45f),
        modifier = modifier.height(50.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = option.titleFa,
                style = MaterialTheme.typography.labelSmall,
                color = if (enabled) PureWhite else SlateMuted,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = when (option) {
                    TacticalAlertOption.BLOCK_EXITS -> "+۱۰٪ / +۴ث"
                    TacticalAlertOption.HOT_PURSUIT -> if (enabled) "+۱۶٪ سریع" else "بی‌خودرو"
                    TacticalAlertOption.RADAR_BACKUP -> "+۲۲٪ (۴۵ سکه)"
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (enabled) PureWhite.copy(alpha = 0.88f) else SlateMuted,
                maxLines = 1
            )
        }
    }
}
