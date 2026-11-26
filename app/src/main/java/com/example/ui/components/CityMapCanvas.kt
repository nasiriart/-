package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.MissionEntity
import com.example.data.model.CrimeType
import com.example.data.model.MissionStatus
import com.example.ui.theme.AlertRed
import com.example.ui.theme.BadgeGold
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyElevated
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PoliceBlue
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyAccent
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenBright
import com.example.ui.theme.WarningOrange
import com.example.util.PersianFormat
import kotlin.math.roundToInt

enum class CityMapTheme2D(val labelFa: String) {
    TACTICAL_NIGHT("شبانه"),
    CLEAN_DAY("روشن")
}

private data class CityLandmark(
    val id: String,
    val labelFa: String,
    val xFrac: Float,
    val yFrac: Float,
    val icon: ImageVector,
    val accentColor: Color
)

// Fixed 2D city landmarks so the metropolis feels alive and instantly readable
private val cityLandmarks = listOf(
    CityLandmark("bank", "بانک", 0.14f, 0.16f, Icons.Filled.AccountBalance, Color(0xFFF59E0B)),
    CityLandmark("jewelry", "جواهری", 0.50f, 0.14f, Icons.Filled.Diamond, Color(0xFF06B6D4)),
    CityLandmark("shops", "مغازه‌ها", 0.14f, 0.45f, Icons.Filled.Storefront, Color(0xFFEC4899)),
    CityLandmark("park", "پارک", 0.17f, 0.72f, Icons.Filled.Park, Color(0xFF10B981)),
    CityLandmark("dealership", "ماشین‌فروشی", 0.85f, 0.71f, Icons.Filled.DirectionsCar, Color(0xFF8B5CF6))
)

// Street loop waypoints used to animate escaping criminals live along actual 2D city roads
private val escapeRoadWaypoints = listOf(
    Offset(0.84f, 0.27f), // Outside Prison Gate
    Offset(0.76f, 0.27f), // North-East Junction
    Offset(0.76f, 0.52f), // Eastern Highway
    Offset(0.50f, 0.52f), // Central Plaza Bypass
    Offset(0.24f, 0.52f), // Shops Avenue
    Offset(0.24f, 0.77f), // Park Boulevard
    Offset(0.76f, 0.77f), // Car Dealership Avenue
    Offset(0.76f, 0.27f)  // Back North Loop
)

private fun sampleEscapeRoute(progress: Float): Pair<Offset, Offset> {
    val segments = escapeRoadWaypoints.size - 1
    val scaled = (progress % 1f) * segments
    val idx = scaled.toInt().coerceIn(0, segments - 1)
    val localT = scaled - idx
    val start = escapeRoadWaypoints[idx]
    val end = escapeRoadWaypoints[idx + 1]
    val current = Offset(
        x = start.x + (end.x - start.x) * localT,
        y = start.y + (end.y - start.y) * localT
    )
    return start to current
}

@Composable
fun CityMapCard(
    availableMissions: List<MissionEntity>,
    activeMissions: List<MissionEntity>,
    nowMillis: Long,
    safetyScore: Int = 80,
    prisonBreakoutActive: Boolean = false,
    onStationClick: (() -> Unit)? = null,
    onPrisonAlertClick: (() -> Unit)? = null,
    onMissionPinClick: (MissionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var mapTheme by remember { mutableStateOf(CityMapTheme2D.TACTICAL_NIGHT) }
    var isFullscreenOpen by remember { mutableStateOf(false) }

    val allMapMissions = availableMissions + activeMissions
    val liveFugitivesCount = allMapMissions.count { it.isFugitivePursuit }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("city_map_card"),
        shape = RoundedCornerShape(22.dp),
        color = NavyDeep,
        border = BorderStroke(1.dp, NavyElevated),
        shadowElevation = 6.dp
    ) {
        Column {
            // Crisp 2D Map Header Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyPrimary)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PoliceBlue.copy(alpha = 0.28f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Map,
                            contentDescription = "نقشه زنده ۲ بعدی شهر",
                            tint = SkyAccent,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "نقشه زنده شهر",
                            style = MaterialTheme.typography.titleSmall,
                            color = PureWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (liveFugitivesCount > 0) {
                                "${PersianFormat.number(liveFugitivesCount)} فراری در حال حرکت زنده • امنیت ${PersianFormat.percent(safetyScore)}"
                            } else {
                                "${PersianFormat.number(allMapMissions.size)} موقعیت • امنیت ${PersianFormat.percent(safetyScore)}"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (liveFugitivesCount > 0) BadgeGold else SkyAccent
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    CityMapTheme2D.entries.forEach { theme ->
                        val active = mapTheme == theme
                        Surface(
                            onClick = { mapTheme = theme },
                            shape = RoundedCornerShape(8.dp),
                            color = if (active) SkyAccent else PureWhite.copy(alpha = 0.12f),
                            modifier = Modifier.testTag("map_theme_${theme.name.lowercase()}")
                        ) {
                            Text(
                                text = theme.labelFa,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (active) NavyDeep else PureWhite.copy(alpha = 0.88f),
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Surface(
                        onClick = { isFullscreenOpen = true },
                        shape = RoundedCornerShape(8.dp),
                        color = PureWhite.copy(alpha = 0.14f),
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("map_fullscreen_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.OpenInFull,
                                contentDescription = "بزرگ‌نمایی نقشه شهر",
                                tint = PureWhite,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            // Crisp Top-Down 2D Live City Viewport
            TwoDimensionalCityViewport(
                missions = allMapMissions,
                nowMillis = nowMillis,
                mapTheme = mapTheme,
                prisonBreakoutActive = prisonBreakoutActive,
                onStationClick = onStationClick,
                onPrisonClick = onPrisonAlertClick,
                onMissionPinClick = onMissionPinClick,
                viewportHeight = 290.dp,
                showMissionLabels = false
            )

            // Legend Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyElevated)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MapLegendDot(color = PoliceBlue, label = "کلانتری")
                MapLegendDot(color = AlertRed, label = "جرم")
                MapLegendDot(color = WarningOrange, label = "فراری زنده")
                MapLegendDot(color = BadgeGold, label = "اعزام")
                MapLegendDot(color = SuccessGreenBright, label = "آماده")
            }
        }
    }

    if (isFullscreenOpen) {
        Fullscreen2DMapDialog(
            missions = allMapMissions,
            nowMillis = nowMillis,
            mapTheme = mapTheme,
            onThemeChange = { mapTheme = it },
            safetyScore = safetyScore,
            prisonBreakoutActive = prisonBreakoutActive,
            onStationClick = {
                isFullscreenOpen = false
                onStationClick?.invoke()
            },
            onPrisonClick = {
                isFullscreenOpen = false
                onPrisonAlertClick?.invoke()
            },
            onMissionPinClick = { mission ->
                isFullscreenOpen = false
                onMissionPinClick(mission)
            },
            onDismiss = { isFullscreenOpen = false }
        )
    }
}

@Composable
private fun TwoDimensionalCityViewport(
    missions: List<MissionEntity>,
    nowMillis: Long,
    mapTheme: CityMapTheme2D,
    prisonBreakoutActive: Boolean,
    onStationClick: (() -> Unit)?,
    onPrisonClick: (() -> Unit)?,
    onMissionPinClick: (MissionEntity) -> Unit,
    viewportHeight: Dp,
    showMissionLabels: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "city_2d_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(880, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pin_pulse"
    )
    val radarWave by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_wave"
    )
    val radarAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_angle"
    )
    val patrolA by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(7800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "patrol_a"
    )
    val patrolB by infiniteTransition.animateFloat(
        initialValue = 0.14f,
        targetValue = 0.86f,
        animationSpec = infiniteRepeatable(
            animation = tween(6400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "patrol_b"
    )
    // Smooth live movement for escaping criminals along 2D city roads
    val fugitiveLoop by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fugitive_loop"
    )
    val dispatchCarT by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dispatch_car"
    )
    val sirenBlink by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "siren_blink"
    )

    val isNight = mapTheme == CityMapTheme2D.TACTICAL_NIGHT

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(viewportHeight)
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val bgTop = if (isNight) Color(0xFF0C1C36) else Color(0xFFEAF2FB)
            val bgBottom = if (isNight) Color(0xFF12284B) else Color(0xFFDBE8F6)
            val waterColor = if (isNight) Color(0xFF0B3566) else Color(0xFF93C5FD)
            val parkFill = if (isNight) Color(0xFF124743) else Color(0xFFB5E7C7)
            val parkStroke = if (isNight) Color(0xFF1F7A68) else Color(0xFF6CC996)
            val treeColor = if (isNight) Color(0xFF1B6957) else Color(0xFF45B378)
            val blockColor = if (isNight) Color(0xFF183359) else Color(0xFFFFFFFF)
            val blockBorder = if (isNight) Color(0xFF284B7D) else Color(0xFFCBD9EC)
            val secondaryRoad = if (isNight) Color(0xFF264978) else Color(0xFFC3D5EC)
            val mainAvenue = if (isNight) Color(0xFF3161A0) else Color(0xFF94B6E2)
            val roadDashColor = if (isNight) Color(0x6693C5FD) else Color(0xCCFFFFFF)

            // 1. Clean 2D Ground
            drawRect(brush = Brush.verticalGradient(colors = listOf(bgTop, bgBottom)))

            // 2. Subtle Coordinate Grid
            val gridTint = if (isNight) SkyAccent.copy(alpha = 0.05f) else PoliceBlue.copy(alpha = 0.06f)
            for (i in 1..7) {
                drawLine(gridTint, Offset(w * (i / 8f), 0f), Offset(w * (i / 8f), h), strokeWidth = 1f)
            }
            for (j in 1..5) {
                drawLine(gridTint, Offset(0f, h * (j / 6f)), Offset(w, h * (j / 6f)), strokeWidth = 1f)
            }

            // 3. Southern Harbor / River Band
            val riverPath = Path().apply {
                moveTo(0f, h * 0.88f)
                quadraticTo(w * 0.34f, h * 0.82f, w * 0.66f, h * 0.90f)
                quadraticTo(w * 0.86f, h * 0.95f, w, h * 0.86f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(riverPath, color = waterColor)

            // 4. Dedicated City Park Zone (Bottom-Left) with Trees & Pond!
            val parkTopLeft = Offset(w * 0.04f, h * 0.59f)
            val parkSize = Size(w * 0.24f, h * 0.24f)
            drawRoundRect(
                color = parkFill,
                topLeft = parkTopLeft,
                size = parkSize,
                cornerRadius = CornerRadius(18f, 18f)
            )
            drawRoundRect(
                color = parkStroke,
                topLeft = parkTopLeft,
                size = parkSize,
                cornerRadius = CornerRadius(18f, 18f),
                style = Stroke(width = 1.6f)
            )
            // Park pond & trees
            drawOval(
                color = waterColor,
                topLeft = Offset(w * 0.07f, h * 0.63f),
                size = Size(w * 0.07f, h * 0.06f)
            )
            listOf(
                Offset(0.22f, 0.64f),
                Offset(0.08f, 0.77f),
                Offset(0.24f, 0.78f),
                Offset(0.11f, 0.62f)
            ).forEach { treeFrac ->
                drawCircle(color = treeColor, radius = 6.5f, center = Offset(w * treeFrac.x, h * treeFrac.y))
            }

            // 5. Dedicated Landmark Lots: Bank (Top-Left), Jewelry (Top-Center), Prison (Top-Right), Shops (Mid-Left), Car Dealership (Bottom-Right)
            // Bank Plaza
            drawRoundRect(
                color = blockColor,
                topLeft = Offset(w * 0.04f, h * 0.06f),
                size = Size(w * 0.20f, h * 0.17f),
                cornerRadius = CornerRadius(12f, 12f)
            )
            drawRoundRect(
                color = BadgeGold.copy(alpha = 0.55f),
                topLeft = Offset(w * 0.04f, h * 0.06f),
                size = Size(w * 0.20f, h * 0.17f),
                cornerRadius = CornerRadius(12f, 12f),
                style = Stroke(width = 1.6f)
            )

            // Jewelry District Block
            drawRoundRect(
                color = blockColor,
                topLeft = Offset(w * 0.36f, h * 0.05f),
                size = Size(w * 0.28f, h * 0.16f),
                cornerRadius = CornerRadius(12f, 12f)
            )
            drawRoundRect(
                color = SkyAccent.copy(alpha = 0.55f),
                topLeft = Offset(w * 0.36f, h * 0.05f),
                size = Size(w * 0.28f, h * 0.16f),
                cornerRadius = CornerRadius(12f, 12f),
                style = Stroke(width = 1.5f)
            )

            // Fortified Prison Compound (Top-Right) with Security Perimeter Fence!
            val prisonTl = Offset(w * 0.75f, h * 0.05f)
            val prisonSz = Size(w * 0.21f, h * 0.18f)
            drawRoundRect(
                color = if (isNight) Color(0xFF1F2937) else Color(0xFFE2E8F0),
                topLeft = prisonTl,
                size = prisonSz,
                cornerRadius = CornerRadius(10f, 10f)
            )
            drawRoundRect(
                color = if (prisonBreakoutActive) AlertRed else Color(0xFF64748B),
                topLeft = prisonTl,
                size = prisonSz,
                cornerRadius = CornerRadius(10f, 10f),
                style = Stroke(
                    width = if (prisonBreakoutActive) 2.5f else 1.8f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f), 0f)
                )
            )

            // Shops & Market Strip (Mid-Left)
            drawRoundRect(
                color = blockColor,
                topLeft = Offset(w * 0.04f, h * 0.33f),
                size = Size(w * 0.19f, h * 0.19f),
                cornerRadius = CornerRadius(12f, 12f)
            )
            drawRoundRect(
                color = Color(0xFFEC4899).copy(alpha = 0.5f),
                topLeft = Offset(w * 0.04f, h * 0.33f),
                size = Size(w * 0.19f, h * 0.19f),
                cornerRadius = CornerRadius(12f, 12f),
                style = Stroke(width = 1.5f)
            )

            // Car Dealership Lot (Bottom-Right) with Showroom Bays!
            val dealerTl = Offset(w * 0.75f, h * 0.59f)
            val dealerSz = Size(w * 0.21f, h * 0.20f)
            drawRoundRect(
                color = blockColor,
                topLeft = dealerTl,
                size = dealerSz,
                cornerRadius = CornerRadius(12f, 12f)
            )
            drawRoundRect(
                color = Color(0xFF8B5CF6).copy(alpha = 0.55f),
                topLeft = dealerTl,
                size = dealerSz,
                cornerRadius = CornerRadius(12f, 12f),
                style = Stroke(width = 1.5f)
            )

            // Additional Urban Residential/Commercial Blocks
            val fillerBlocks = listOf(
                Offset(0.32f, 0.33f) to Size(0.11f, 0.13f),
                Offset(0.58f, 0.33f) to Size(0.14f, 0.14f),
                Offset(0.80f, 0.33f) to Size(0.14f, 0.15f),
                Offset(0.36f, 0.60f) to Size(0.15f, 0.13f),
                Offset(0.56f, 0.60f) to Size(0.14f, 0.13f)
            )
            fillerBlocks.forEach { (pos, sz) ->
                val tl = Offset(w * pos.x, h * pos.y)
                val rectSz = Size(w * sz.width, h * sz.height)
                drawRoundRect(color = blockColor, topLeft = tl, size = rectSz, cornerRadius = CornerRadius(9f, 9f))
                drawRoundRect(
                    color = blockBorder,
                    topLeft = tl,
                    size = rectSz,
                    cornerRadius = CornerRadius(9f, 9f),
                    style = Stroke(width = 1f)
                )
            }

            // 6. Clean 2D Street Network & Main Boulevards
            val horizontalY = listOf(0.27f, 0.52f, 0.77f)
            val verticalX = listOf(0.24f, 0.50f, 0.76f)

            horizontalY.forEach { yf ->
                val isCentral = yf == 0.52f
                drawLine(
                    color = if (isCentral) mainAvenue else secondaryRoad,
                    start = Offset(0f, h * yf),
                    end = Offset(w, h * yf),
                    strokeWidth = if (isCentral) 11f else 7.5f
                )
            }
            verticalX.forEach { xf ->
                val isCentral = xf == 0.50f
                drawLine(
                    color = if (isCentral) mainAvenue else secondaryRoad,
                    start = Offset(w * xf, 0f),
                    end = Offset(w * xf, h),
                    strokeWidth = if (isCentral) 11f else 7.5f
                )
            }

            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(11f, 9f), 0f)
            drawLine(roadDashColor, Offset(0f, h * 0.52f), Offset(w, h * 0.52f), strokeWidth = 1.5f, pathEffect = dashEffect)
            drawLine(roadDashColor, Offset(w * 0.50f, 0f), Offset(w * 0.50f, h), strokeWidth = 1.5f, pathEffect = dashEffect)

            // Central Police HQ Plaza Compound
            val hqCenter = Offset(w * 0.50f, h * 0.52f)
            drawCircle(color = PoliceBlue.copy(alpha = 0.25f), radius = 26f, center = hqCenter)
            drawCircle(color = PoliceBlue, radius = 20f, center = hqCenter, style = Stroke(width = 2f))

            // 7. Smooth 2D Radar Sweep & Expanding Ring from Police Station
            val radarRadius = minOf(w, h) * 0.42f
            val radarTint = if (isNight) SkyAccent else PoliceBlue
            drawCircle(
                color = radarTint.copy(alpha = (1f - radarWave) * 0.34f),
                radius = radarRadius * (0.12f + 0.92f * radarWave),
                center = hqCenter,
                style = Stroke(width = 2.2f)
            )
            drawArc(
                brush = Brush.radialGradient(
                    colors = listOf(radarTint.copy(alpha = 0.20f), Color.Transparent),
                    center = hqCenter,
                    radius = radarRadius
                ),
                startAngle = radarAngle,
                sweepAngle = 38f,
                useCenter = true,
                topLeft = Offset(hqCenter.x - radarRadius, hqCenter.y - radarRadius),
                size = Size(radarRadius * 2f, radarRadius * 2f)
            )

            // 8. Live Escape Trails for Moving Fugitives & Active Police Dispatch Routes!
            val sirenA = if (sirenBlink > 0.5f) AlertRed else SkyAccent
            val sirenB = if (sirenBlink > 0.5f) SkyAccent else AlertRed

            missions.forEach { mission ->
                val status = MissionStatus.fromName(mission.status)
                val targetFrac = if (mission.isFugitivePursuit) {
                    val phase = (fugitiveLoop + (mission.id * 0.23f)) % 1f
                    val (segStart, curPos) = sampleEscapeRoute(phase)
                    // Draw glowing dashed crimson escape trail behind the live fleeing criminal!
                    val trailStartPx = Offset(w * segStart.x, h * segStart.y)
                    val curPx = Offset(w * curPos.x, h * curPos.y)
                    drawLine(
                        color = AlertRed.copy(alpha = 0.85f),
                        start = trailStartPx,
                        end = curPx,
                        strokeWidth = 3.5f,
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(9f, 6f), 0f)
                    )
                    // Target lock-on radar ring around moving fugitive
                    drawCircle(
                        color = WarningOrange.copy(alpha = 0.45f),
                        radius = 22f * pulseScale,
                        center = curPx,
                        style = Stroke(width = 2f)
                    )
                    curPos
                } else {
                    Offset(mission.mapX, mission.mapY)
                }

                // If Police Officers are currently dispatched to this mission, draw live police pursuit line & moving cruiser!
                if (status == MissionStatus.IN_PROGRESS && nowMillis < mission.endTimeMillis) {
                    val targetPx = Offset(w * targetFrac.x, h * targetFrac.y)
                    drawLine(
                        color = SkyAccent.copy(alpha = 0.78f),
                        start = hqCenter,
                        end = targetPx,
                        strokeWidth = 2.6f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                    )
                    val carPx = Offset(
                        x = hqCenter.x + (targetPx.x - hqCenter.x) * dispatchCarT,
                        y = hqCenter.y + (targetPx.y - hqCenter.y) * dispatchCarT
                    )
                    drawCircle(color = sirenA.copy(alpha = 0.42f), radius = 14f, center = carPx)
                    drawCircle(color = PureWhite, radius = 5f, center = carPx)
                    drawCircle(color = PoliceBlue, radius = 3.3f, center = carPx)
                }
            }

            // 9. Ambient City Police Patrol Cars driving along avenues
            val cruiser1 = Offset(w * patrolA, h * 0.52f)
            val cruiser2 = Offset(w * 0.24f, h * (1f - patrolB))
            val cruiser3 = Offset(w * 0.76f, h * patrolB)

            listOf(cruiser1 to sirenA, cruiser2 to sirenB, cruiser3 to sirenA).forEach { (pos, flash) ->
                drawCircle(color = flash.copy(alpha = 0.28f), radius = 12f, center = pos)
                drawCircle(color = PureWhite, radius = 4.2f, center = pos)
                drawCircle(color = PoliceBlue, radius = 2.8f, center = pos)
            }
        }

        // Labeled 2D City Landmarks (Bank, Jewelry, Shops, Park, Car Dealership)
        cityLandmarks.forEach { landmark ->
            CityLandmarkBadge(
                labelFa = landmark.labelFa,
                icon = landmark.icon,
                accentColor = landmark.accentColor,
                isNight = isNight,
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (widthPx * landmark.xFrac).roundToInt() - 28.dp.roundToPx(),
                            y = (heightPx * landmark.yFrac).roundToInt() - 22.dp.roundToPx()
                        )
                    }
                    .testTag("map_landmark_${landmark.id}")
            )
        }

        // Clearly Labeled Police Station HQ Marker (کلانتری) — Tappable!
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (widthPx * 0.50f).roundToInt() - 26.dp.roundToPx(),
                        y = (heightPx * 0.52f).roundToInt() - 24.dp.roundToPx()
                    )
                }
                .clickable(enabled = onStationClick != null) { onStationClick?.invoke() }
                .testTag("map_station_marker")
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(PoliceBlue)
                    .border(2.2.dp, PureWhite, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalPolice,
                    contentDescription = "کلانتری مرکزی",
                    tint = PureWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = PoliceBlue,
                border = BorderStroke(0.8.dp, PureWhite.copy(alpha = 0.85f))
            ) {
                Text(
                    text = "کلانتری",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = PureWhite,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                )
            }
        }

        // Clearly Labeled City Prison Marker (زندان) — Tappable!
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (widthPx * 0.86f).roundToInt() - 26.dp.roundToPx(),
                        y = (heightPx * 0.14f).roundToInt() - 22.dp.roundToPx()
                    )
                }
                .clickable(enabled = onPrisonClick != null) { onPrisonClick?.invoke() }
                .testTag("map_prison_marker")
        ) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                if (prisonBreakoutActive) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(AlertRed.copy(alpha = 0.42f))
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (prisonBreakoutActive) AlertRed else Color(0xFF1E293B))
                        .border(
                            width = 2.dp,
                            color = if (prisonBreakoutActive) BadgeGold else SkyAccent,
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (prisonBreakoutActive) Icons.Filled.GppBad else Icons.Filled.Lock,
                        contentDescription = "زندان مرکزی شهر",
                        tint = PureWhite,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (prisonBreakoutActive) AlertRed else NavyDeep.copy(alpha = 0.92f),
                border = BorderStroke(
                    0.8.dp,
                    if (prisonBreakoutActive) BadgeGold else SkyAccent.copy(alpha = 0.7f)
                )
            ) {
                Text(
                    text = if (prisonBreakoutActive) "زندان (آژیر!)" else "زندان",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = PureWhite,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                )
            }
        }

        // Interactive Mission Pins & LIVE MOVING Escaping Criminals!
        missions.forEach { mission ->
            val status = MissionStatus.fromName(mission.status)
            val isFinished = status == MissionStatus.IN_PROGRESS && nowMillis >= mission.endTimeMillis
            val hasRadioAlert = status == MissionStatus.IN_PROGRESS && mission.tacticalAlertActive && !isFinished
            val isLiveFugitive = mission.isFugitivePursuit

            val livePos = if (isLiveFugitive) {
                val phase = (fugitiveLoop + (mission.id * 0.23f)) % 1f
                sampleEscapeRoute(phase).second
            } else {
                Offset(mission.mapX, mission.mapY)
            }

            val pinColor = when {
                isFinished -> SuccessGreenBright
                hasRadioAlert -> AlertRed
                isLiveFugitive -> WarningOrange
                status == MissionStatus.IN_PROGRESS -> BadgeGold
                else -> AlertRed
            }
            val pinIcon = when {
                isFinished -> Icons.Filled.CheckCircle
                isLiveFugitive -> Icons.Filled.DirectionsRun
                status == MissionStatus.IN_PROGRESS -> Icons.Filled.Schedule
                else -> crimeIcon(CrimeType.fromName(mission.crimeType))
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (widthPx * livePos.x).roundToInt() - 26.dp.roundToPx(),
                            y = (heightPx * livePos.y).roundToInt() - 24.dp.roundToPx()
                        )
                    }
                    .clickable { onMissionPinClick(mission) }
                    .testTag("map_pin_${mission.id}")
            ) {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (status == MissionStatus.AVAILABLE || isFinished || hasRadioAlert || isLiveFugitive) {
                        Box(
                            modifier = Modifier
                                .size(if (isLiveFugitive || hasRadioAlert) 44.dp else 38.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(pinColor.copy(alpha = 0.38f))
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(if (isLiveFugitive) 34.dp else 30.dp)
                            .clip(CircleShape)
                            .background(pinColor)
                            .border(
                                width = if (isLiveFugitive) 2.5.dp else 2.dp,
                                color = if (isLiveFugitive) BadgeGold else PureWhite,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = pinIcon,
                            contentDescription = mission.title,
                            tint = PureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                if (isLiveFugitive || hasRadioAlert || showMissionLabels) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isLiveFugitive) AlertRed else NavyDeep.copy(alpha = 0.88f),
                        border = BorderStroke(0.9.dp, if (isLiveFugitive) BadgeGold else pinColor)
                    ) {
                        Text(
                            text = when {
                                isLiveFugitive && mission.title.startsWith("فراری از زندان:") -> "فراری زندان!"
                                isLiveFugitive -> "در حال فرار!"
                                else -> mission.district
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                            color = PureWhite,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CityLandmarkBadge(
    labelFa: String,
    icon: ImageVector,
    accentColor: Color,
    isNight: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isNight) NavyDeep.copy(alpha = 0.9f) else PureWhite.copy(alpha = 0.95f))
                .border(1.3.dp, accentColor, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = labelFa,
                tint = accentColor,
                modifier = Modifier.size(15.dp)
            )
        }
        Surface(
            shape = RoundedCornerShape(5.dp),
            color = if (isNight) NavyDeep.copy(alpha = 0.82f) else PureWhite.copy(alpha = 0.9f),
            border = BorderStroke(0.7.dp, accentColor.copy(alpha = 0.65f))
        ) {
            Text(
                text = labelFa,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                color = if (isNight) PureWhite else NavyDeep,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
            )
        }
    }
}

@Composable
private fun Fullscreen2DMapDialog(
    missions: List<MissionEntity>,
    nowMillis: Long,
    mapTheme: CityMapTheme2D,
    onThemeChange: (CityMapTheme2D) -> Unit,
    safetyScore: Int,
    prisonBreakoutActive: Boolean,
    onStationClick: () -> Unit,
    onPrisonClick: () -> Unit,
    onMissionPinClick: (MissionEntity) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 20.dp)
                    .testTag("fullscreen_map_dialog"),
                shape = RoundedCornerShape(22.dp),
                color = NavyDeep,
                border = BorderStroke(1.2.dp, SkyAccent.copy(alpha = 0.45f)),
                shadowElevation = 14.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NavyPrimary)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Map,
                                contentDescription = null,
                                tint = SkyAccent,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = "نقشه کامل زنده شهر",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = PureWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "امنیت ${PersianFormat.percent(safetyScore)} • کلانتری، زندان، بانک، جواهری، پارک، مغازه‌ها و ماشین‌فروشی",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SkyAccent,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "بستن نقشه بزرگ",
                                tint = PureWhite
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NavyDeep)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CityMapTheme2D.entries.forEach { theme ->
                            val active = mapTheme == theme
                            Surface(
                                onClick = { onThemeChange(theme) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (active) SkyAccent else PureWhite.copy(alpha = 0.12f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "نمای ${theme.labelFa}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (active) NavyDeep else PureWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    TwoDimensionalCityViewport(
                        missions = missions,
                        nowMillis = nowMillis,
                        mapTheme = mapTheme,
                        prisonBreakoutActive = prisonBreakoutActive,
                        onStationClick = onStationClick,
                        onPrisonClick = onPrisonClick,
                        onMissionPinClick = onMissionPinClick,
                        viewportHeight = 430.dp,
                        showMissionLabels = true
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NavyElevated)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MapLegendDot(color = PoliceBlue, label = "کلانتری")
                        MapLegendDot(color = AlertRed, label = "جرم")
                        MapLegendDot(color = WarningOrange, label = "فراری زنده")
                        MapLegendDot(color = BadgeGold, label = "اعزام")
                        MapLegendDot(color = SuccessGreenBright, label = "آماده")
                    }
                }
            }
        }
    }
}

@Composable
private fun MapLegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = PureWhite.copy(alpha = 0.9f)
        )
    }
}
