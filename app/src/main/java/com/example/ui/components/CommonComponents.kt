package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.MissionEntity
import com.example.data.local.OfficerEntity
import com.example.data.local.PlayerProfileEntity
import com.example.data.local.VehicleEntity
import com.example.data.model.CrimeType
import com.example.data.model.OfficerSpecialty
import com.example.data.repository.MissionResolutionResult
import com.example.domain.GameCatalog
import com.example.domain.GameEngine
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedSoft
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
import com.example.ui.theme.WarningOrange
import com.example.ui.theme.WarningOrangeSoft
import com.example.util.PersianFormat

@Composable
fun CommandTopBar(
    profile: PlayerProfileEntity,
    isStatsSelected: Boolean,
    isPrisonSelected: Boolean = false,
    isProfileSelected: Boolean = false,
    detainedCount: Int = 0,
    prisonBreakoutActive: Boolean = false,
    onProfileClick: () -> Unit = {},
    onPrisonClick: () -> Unit = {},
    onStatsClick: () -> Unit
) {
    val xpNeeded = GameCatalog.xpRequiredForLevel(profile.level)
    val xpProgress = if (profile.level >= GameCatalog.MAX_PLAYER_LEVEL || xpNeeded <= 0) {
        1f
    } else {
        (profile.xp.toFloat() / xpNeeded.toFloat()).coerceIn(0f, 1f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(NavyDeep, NavyPrimary, NavyElevated)
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    onClick = onProfileClick,
                    shape = RoundedCornerShape(14.dp),
                    color = if (isProfileSelected) PoliceBlue.copy(alpha = 0.45f) else Color.Transparent,
                    border = if (isProfileSelected) BorderStroke(1.dp, SkyAccent) else null,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("topbar_profile_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        Box(modifier = Modifier.size(44.dp)) {
                            Image(
                                painter = painterResource(id = chiefAvatarRes(profile.chiefAvatarKey)),
                                contentDescription = "پروفایل ${profile.chiefName}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(13.dp))
                                    .border(1.8.dp, BadgeGold, RoundedCornerShape(13.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(19.dp)
                                    .clip(CircleShape)
                                    .background(BadgeGold)
                                    .border(1.dp, NavyDeep, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = PersianFormat.number(profile.level),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NavyDeep,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                        Column {
                            Text(
                                text = profile.chiefName.ifBlank { GameCatalog.rankTitleForLevel(profile.level) },
                                style = MaterialTheme.typography.titleSmall,
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${profile.chiefCallsign} • ${GameCatalog.rankTitleForLevel(profile.level)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SkyAccent,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = PureWhite.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, BadgeGold.copy(alpha = 0.55f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MonetizationOn,
                                contentDescription = "موجودی پول",
                                tint = BadgeGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = PersianFormat.number(profile.money),
                                style = MaterialTheme.typography.labelLarge,
                                color = PureWhite,
                                modifier = Modifier.testTag("topbar_money_text")
                            )
                        }
                    }

                    Surface(
                        onClick = onPrisonClick,
                        shape = RoundedCornerShape(12.dp),
                        color = when {
                            prisonBreakoutActive -> AlertRed
                            isPrisonSelected -> PoliceBlue
                            else -> PureWhite.copy(alpha = 0.14f)
                        },
                        border = if (prisonBreakoutActive) BorderStroke(1.dp, BadgeGold) else null,
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("topbar_prison_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "زندان مرکزی",
                                tint = PureWhite,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = PersianFormat.number(detainedCount),
                                style = MaterialTheme.typography.labelMedium,
                                color = PureWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        onClick = onStatsClick,
                        shape = RoundedCornerShape(12.dp),
                        color = if (isStatsSelected) PoliceBlue else PureWhite.copy(alpha = 0.14f),
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("topbar_stats_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.BarChart,
                                contentDescription = "صفحه آمار و رتبه",
                                tint = PureWhite,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { xpProgress },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                    color = SkyAccent,
                    trackColor = PureWhite.copy(alpha = 0.16f),
                    strokeCap = StrokeCap.Round
                )
                Text(
                    text = if (profile.level >= GameCatalog.MAX_PLAYER_LEVEL) {
                        "حداکثر سطح"
                    } else {
                        "${PersianFormat.number(profile.xp)} / ${PersianFormat.number(xpNeeded)} تجربه"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = PureWhite.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
fun DifficultyStars(difficulty: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= difficulty) Icons.Filled.Star else Icons.Filled.StarOutline,
                contentDescription = null,
                tint = if (i <= difficulty) BadgeGold else SlateBorder,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun DispatchMissionDialog(
    mission: MissionEntity,
    profile: PlayerProfileEntity,
    officers: List<OfficerEntity>,
    vehicles: List<VehicleEntity>,
    onDismiss: () -> Unit,
    onConfirmDispatch: (List<Int>, Int?) -> Unit
) {
    val crimeType = CrimeType.fromName(mission.crimeType)
    val availableOfficers = remember(officers) { officers.filter { !it.isBusy } }
    val availableVehicles = remember(vehicles) { vehicles.filter { !it.isBusy } }

    var selectedOfficerIds by remember(mission.id) {
        val sorted = availableOfficers.sortedByDescending {
            val matchBonus = if (OfficerSpecialty.fromName(it.specialty) == crimeType.preferredSpecialty) 100 else 0
            matchBonus + GameEngine.officerPower(it)
        }
        mutableStateOf(sorted.take(mission.requiredOfficers).map { it.id }.toSet())
    }

    var selectedVehicleId by remember(mission.id) {
        mutableStateOf(availableVehicles.maxByOrNull { GameEngine.vehiclePowerBonus(it) }?.id)
    }

    val chosenOfficers = availableOfficers.filter { it.id in selectedOfficerIds }
    val chosenVehicle = availableVehicles.firstOrNull { it.id == selectedVehicleId }

    val preview = remember(chosenOfficers, chosenVehicle, profile, mission) {
        GameEngine.calculateMissionPreview(mission, chosenOfficers, chosenVehicle, profile)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 18.dp)
                    .testTag("dispatch_dialog"),
                shape = RoundedCornerShape(24.dp),
                color = PureWhite,
                shadowElevation = 12.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (mission.isFugitivePursuit) AlertRedSoft else PoliceBlue.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (mission.isFugitivePursuit) Icons.Filled.DirectionsRun else crimeIcon(crimeType),
                                    contentDescription = mission.title,
                                    tint = if (mission.isFugitivePursuit) AlertRed else PoliceBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = mission.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SlateInk,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${mission.district} • تخصص: ${crimeType.preferredSpecialty.titleFa}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SlateMuted
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "بستن",
                                tint = SlateMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = mission.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateMuted
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Live 4-Metric Tactical Telemetry Strip
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = NavyPrimary
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 11.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            PreviewMetric(
                                label = "موفقیت",
                                value = PersianFormat.percent(preview.successRate),
                                valueColor = successRateColor(preview.successRate)
                            )
                            PreviewMetric(
                                label = "خطر فرار",
                                value = PersianFormat.percent(preview.escapeRisk),
                                valueColor = escapeRiskColor(preview.escapeRisk)
                            )
                            PreviewMetric(
                                label = "زمان",
                                value = PersianFormat.duration(preview.finalDurationSeconds.toLong()),
                                valueColor = SkyAccent
                            )
                            PreviewMetric(
                                label = "پاداش",
                                value = PersianFormat.number(preview.finalRewardMoney),
                                valueColor = BadgeGold
                            )
                        }
                    }

                    // Tactical Warnings when risk of escape is elevated
                    if (preview.tacticalWarnings.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = WarningOrangeSoft,
                            border = BorderStroke(1.dp, WarningOrange.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ReportProblem,
                                    contentDescription = "هشدار تاکتیکی",
                                    tint = WarningOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = preview.tacticalWarnings.first(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NavyDeep
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Scrollable Officer Portraits + Vehicle Photos Picker
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item(key = "header_officers") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "انتخاب مأموران (پیشنهادی: ${PersianFormat.number(mission.requiredOfficers)} نفر)",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = SlateInk
                                )
                                Text(
                                    text = "انتخاب‌شده: ${PersianFormat.number(chosenOfficers.size)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = PoliceBlue
                                )
                            }
                        }

                        if (availableOfficers.isEmpty()) {
                            item(key = "empty_officers") {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = AlertRedSoft,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "در حال حاضر هیچ مأمور آزادی در کلانتری نیست! منتظر بازگشت نیروها بمانید یا نیروی جدید استخدام کنید.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AlertRed,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        } else {
                            items(availableOfficers, key = { "officer_${it.id}" }) { officer ->
                                val isSelected = officer.id in selectedOfficerIds
                                val spec = OfficerSpecialty.fromName(officer.specialty)
                                val isMatch = spec == crimeType.preferredSpecialty
                                Surface(
                                    onClick = {
                                        selectedOfficerIds = if (isSelected) {
                                            selectedOfficerIds - officer.id
                                        } else {
                                            selectedOfficerIds + officer.id
                                        }
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) PoliceBlue.copy(alpha = 0.10f) else IceBlueSurface,
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) PoliceBlue else SlateBorder.copy(alpha = 0.6f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("select_officer_${officer.id}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Image(
                                                painter = painterResource(id = officerPortraitRes(officer.name, spec)),
                                                contentDescription = officer.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(46.dp)
                                                    .clip(CircleShape)
                                                    .border(
                                                        width = if (isSelected) 2.dp else 1.dp,
                                                        color = if (isSelected) PoliceBlue else SlateBorder,
                                                        shape = CircleShape
                                                    )
                                            )
                                            Column {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                                ) {
                                                    Text(
                                                        text = officer.name,
                                                        style = MaterialTheme.typography.titleSmall,
                                                        color = SlateInk
                                                    )
                                                    if (isMatch) {
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = SuccessGreenSoft
                                                        ) {
                                                            Text(
                                                                text = "+۲۸٪ تخصص",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = SuccessGreen,
                                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                Text(
                                                    text = "${spec.titleFa} • سطح ${PersianFormat.number(officer.level)} • قدرت ${PersianFormat.number(GameEngine.officerPower(officer))}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = SlateMuted
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Filled.LocalPolice,
                                            contentDescription = null,
                                            tint = if (isSelected) PoliceBlue else SlateBorder,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }

                        item(key = "header_vehicles") {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (crimeType.needsFastVehicle) {
                                    "انتخاب خودروی تعقیب (بسیار مهم برای جلوگیری از فرار!)"
                                } else {
                                    "انتخاب خودروی پشتیبانی (اختیاری)"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                color = if (crimeType.needsFastVehicle && chosenVehicle == null) AlertRed else SlateInk
                            )
                        }

                        if (availableVehicles.isEmpty()) {
                            item(key = "empty_vehicles") {
                                Text(
                                    text = "تمام خودروهای گاراژ در مأموریت هستند (اعزام پیاده همچنان ممکن است، اما خطر فرار سارق بالاتر می‌رود).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SlateMuted
                                )
                            }
                        } else {
                            items(availableVehicles, key = { "vehicle_${it.id}" }) { vehicle ->
                                val isSelected = selectedVehicleId == vehicle.id
                                Surface(
                                    onClick = {
                                        selectedVehicleId = if (isSelected) null else vehicle.id
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) BadgeGoldSoft.copy(alpha = 0.7f) else IceBlueSurface,
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) BadgeGold else SlateBorder.copy(alpha = 0.6f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("select_vehicle_${vehicle.id}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Image(
                                                painter = painterResource(id = vehicleImageRes(vehicle.modelKey)),
                                                contentDescription = vehicle.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .width(64.dp)
                                                    .height(46.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (isSelected) BadgeGold else SlateBorder,
                                                        shape = RoundedCornerShape(10.dp)
                                                    )
                                            )
                                            Column {
                                                Text(
                                                    text = "${vehicle.name} (سطح ${PersianFormat.number(vehicle.level)})",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = SlateInk
                                                )
                                                Text(
                                                    text = "سرعت -${PersianFormat.percent(GameEngine.vehicleSpeedBonus(vehicle))} • قدرت +${PersianFormat.number(GameEngine.vehiclePowerBonus(vehicle))}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = SlateMuted
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Filled.DirectionsCar,
                                            contentDescription = null,
                                            tint = if (isSelected) BadgeGold else SlateBorder,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onConfirmDispatch(chosenOfficers.map { it.id }, chosenVehicle?.id) },
                        enabled = chosenOfficers.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("confirm_dispatch_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PoliceBlue,
                            contentColor = PureWhite
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = null,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (chosenOfficers.isEmpty()) {
                                "حداقل یک مأمور انتخاب کنید"
                            } else {
                                "اعزام ${PersianFormat.number(chosenOfficers.size)} مأمور به مأموریت"
                            },
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewMetric(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = PureWhite.copy(alpha = 0.72f)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = valueColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MissionResultDialog(
    result: MissionResolutionResult,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = PureWhite,
                shadowElevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mission_result_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val iconBg = when {
                        result.isSuccess -> SuccessGreenSoft
                        result.suspectEscaped -> AlertRedSoft
                        else -> WarningOrangeSoft
                    }
                    val iconTint = when {
                        result.isSuccess -> SuccessGreen
                        result.suspectEscaped -> AlertRed
                        else -> WarningOrange
                    }
                    val headerIcon = when {
                        result.isSuccess -> Icons.Filled.Verified
                        result.suspectEscaped -> Icons.Filled.DirectionsRun
                        else -> Icons.Filled.Warning
                    }

                    Box(
                        modifier = Modifier
                            .size(66.dp)
                            .clip(CircleShape)
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = headerIcon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = when {
                            result.isSuccess -> "مأموریت موفق!"
                            result.suspectEscaped -> "مجرم فرار کرد!"
                            else -> "مأموریت ناتمام"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        color = iconTint,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "${result.missionTitle} • ${result.district}",
                        style = MaterialTheme.typography.titleSmall,
                        color = SlateInk
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = result.summaryMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateMuted
                    )

                    // Specific Escape Reason Report Card
                    if (result.suspectEscaped && result.escapeReason.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = AlertRedSoft,
                            border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ReportProblem,
                                        contentDescription = "دلیل فرار",
                                        tint = AlertRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "علت فرار مجرم (گزارش میدانی):",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = AlertRed
                                    )
                                }
                                Text(
                                    text = result.escapeReason,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NavyDeep
                                )
                            }
                        }
                    }

                    // Fugitive Pursuit Alert if spawned
                    if (result.fugitiveSpawned) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = WarningOrangeSoft,
                            border = BorderStroke(1.dp, WarningOrange),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "ردیابی فوری: دوربین‌های شهر مجرم فراری را در منطقه دیگری شناسایی کردند! مأموریت «تعقیب فراری» با جایزه +۴۵٪ به نقشه اضافه شد.",
                                style = MaterialTheme.typography.bodySmall,
                                color = NavyDeep,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(11.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Rewards Summary Box
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = IceBlueSurface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ResultStatRow("پاداش نقدی", "+${PersianFormat.money(result.earnedMoney)}", PoliceBlue)
                            ResultStatRow("امتیاز تجربه (XP)", "+${PersianFormat.number(result.earnedXp)} تجربه", NavyPrimary)
                            if (result.suspectsArrested > 0) {
                                ResultStatRow("مجرمان دستگیرشده", "${PersianFormat.number(result.suspectsArrested)} نفر", SuccessGreen)
                            }
                            if (result.inmatesBooked > 0) {
                                ResultStatRow("انتقال به سلول زندان", "${PersianFormat.number(result.inmatesBooked)} زندانی", WarningOrange)
                            }
                            if (result.dailyBonusAwarded > 0) {
                                HorizontalDivider(color = SlateBorder.copy(alpha = 0.5f))
                                ResultStatRow(
                                    "پاداش پایان روز خدمتی",
                                    "+${PersianFormat.money(result.dailyBonusAwarded)}",
                                    BadgeGold
                                )
                            }
                        }
                    }

                    if (result.leveledUp) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = BadgeGoldSoft,
                            border = BorderStroke(1.5.dp, BadgeGold),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.EmojiEvents,
                                    contentDescription = "ارتقای سطح",
                                    tint = BadgeGold,
                                    modifier = Modifier.size(30.dp)
                                )
                                Column {
                                    Text(
                                        text = "تبریک! ارتقا به سطح ${PersianFormat.number(result.newLevel)}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = NavyDeep
                                    )
                                    Text(
                                        text = "رتبه جدید: ${GameCatalog.rankTitleForLevel(result.newLevel)}\n${GameCatalog.milestoneFor(result.newLevel).unlockDescriptionFa}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SlateInk
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("close_result_dialog_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary, contentColor = PureWhite)
                    ) {
                        Text(text = "ادامه فرماندهی", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultStatRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = SlateMuted)
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = valueColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = NavyDeep
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateMuted
                )
            }
        }
        if (trailing != null) {
            trailing()
        }
    }
}
