package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.VehicleEntity
import com.example.domain.GameCatalog
import com.example.domain.GameEngine
import com.example.domain.VehicleTemplate
import com.example.ui.components.SectionHeader
import com.example.ui.components.vehicleImageRes
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
import com.example.ui.viewmodel.GameUiState
import com.example.util.PersianFormat

@Composable
fun GarageScreen(
    state: GameUiState,
    onBuyVehicle: (VehicleTemplate) -> Unit,
    onUpgradeVehicle: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(IceBlueSurface)
            .testTag("garage_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "garage_owned_header") {
            SectionHeader(
                title = "ناوگان خودروهای کلانتری",
                subtitle = "خودروهای آزاد: ${PersianFormat.number(state.freeVehiclesCount)} از ${PersianFormat.number(state.vehicles.size)} دستگاه"
            )
        }

        items(state.vehicles, key = { "owned_${it.id}" }) { vehicle ->
            OwnedVehicleCard(
                vehicle = vehicle,
                playerMoney = state.profile.money,
                onUpgrade = { onUpgradeVehicle(vehicle.id) }
            )
        }

        item(key = "garage_store_header") {
            Spacer(modifier = Modifier.height(6.dp))
            SectionHeader(
                title = "نمایشگاه خرید خودروی پلیس",
                subtitle = "خودروهای سریع‌تر زمان رسیدن را کم کرده و مانع فرار سارقان می‌شوند!"
            )
        }

        items(GameCatalog.vehicleTemplates, key = { "store_${it.modelKey}" }) { template ->
            VehicleStoreCard(
                template = template,
                playerLevel = state.profile.level,
                playerMoney = state.profile.money,
                ownedCount = state.vehicles.count { it.modelKey == template.modelKey },
                onBuy = { onBuyVehicle(template) }
            )
        }

        item(key = "garage_spacer") { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun OwnedVehicleCard(
    vehicle: VehicleEntity,
    playerMoney: Long,
    onUpgrade: () -> Unit
) {
    val speed = GameEngine.vehicleSpeedBonus(vehicle)
    val power = GameEngine.vehiclePowerBonus(vehicle)
    val isMax = vehicle.level >= GameCatalog.MAX_VEHICLE_LEVEL
    val cost = GameEngine.vehicleUpgradeCost(vehicle)
    val canUpgrade = !vehicle.isBusy && !isMax && playerMoney >= cost

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, SlateBorder.copy(alpha = 0.6f)),
        shadowElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("owned_vehicle_${vehicle.id}")
    ) {
        Column {
            // Vehicle Showcase Image Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(148.dp)
            ) {
                Image(
                    painter = painterResource(id = vehicleImageRes(vehicle.modelKey)),
                    contentDescription = vehicle.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    NavyDeep.copy(alpha = 0.78f)
                                )
                            )
                        )
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (vehicle.isBusy) BadgeGold else SuccessGreen,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                ) {
                    Text(
                        text = if (vehicle.isBusy) "در مأموریت" else "آماده حرکت",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (vehicle.isBusy) NavyDeep else PureWhite,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = NavyDeep.copy(alpha = 0.85f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "سطح ${PersianFormat.number(vehicle.level)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = BadgeGold,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = vehicle.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = PureWhite,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }

            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(IceBlueSurface)
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Speed,
                            contentDescription = null,
                            tint = PoliceBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "کاهش زمان و فرار: ${PersianFormat.percent(speed)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = SlateInk
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = null,
                            tint = NavyPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "قدرت: +${PersianFormat.number(power)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = SlateInk
                        )
                    }
                }

                Button(
                    onClick = onUpgrade,
                    enabled = canUpgrade,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("upgrade_vehicle_${vehicle.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PoliceBlue, contentColor = PureWhite)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowUpward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when {
                            isMax -> "حداکثر سطح ارتقا"
                            vehicle.isBusy -> "منتظر بازگشت خودرو"
                            else -> "ارتقای موتور و تجهیزات • ${PersianFormat.money(cost)}"
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleStoreCard(
    template: VehicleTemplate,
    playerLevel: Int,
    playerMoney: Long,
    ownedCount: Int,
    onBuy: () -> Unit
) {
    val isUnlocked = playerLevel >= template.unlockLevel
    val canBuy = isUnlocked && playerMoney >= template.cost

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, SlateBorder.copy(alpha = 0.55f)),
        shadowElevation = if (isUnlocked) 3.dp else 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("store_vehicle_${template.modelKey}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(142.dp)
            ) {
                Image(
                    painter = painterResource(id = vehicleImageRes(template.modelKey)),
                    contentDescription = template.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(if (isUnlocked) 1f else 0.45f)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    NavyDeep.copy(alpha = if (isUnlocked) 0.72f else 0.85f)
                                )
                            )
                        )
                )
                if (!isUnlocked) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NavyDeep.copy(alpha = 0.86f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "قفل شده",
                            tint = BadgeGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "باز شدن در سطح ${PersianFormat.number(template.unlockLevel)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = PureWhite
                        )
                    }
                }
                if (ownedCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PoliceBlue,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "${PersianFormat.number(ownedCount)} در گاراژ",
                            style = MaterialTheme.typography.labelSmall,
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = template.subtitleFa,
                        style = MaterialTheme.typography.bodySmall,
                        color = PureWhite.copy(alpha = 0.85f)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "سرعت: -${PersianFormat.percent(template.baseSpeedBonus)} زمان",
                        style = MaterialTheme.typography.labelMedium,
                        color = PoliceBlue
                    )
                    Text(
                        text = "قدرت پشتیبانی: +${PersianFormat.number(template.basePowerBonus)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = NavyPrimary
                    )
                }

                Button(
                    onClick = onBuy,
                    enabled = canBuy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("buy_vehicle_${template.modelKey}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary, contentColor = PureWhite)
                ) {
                    Icon(
                        imageVector = if (isUnlocked) Icons.Filled.AddShoppingCart else Icons.Filled.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isUnlocked) {
                            "خرید خودرو • ${PersianFormat.money(template.cost)}"
                        } else {
                            "باز شدن در سطح ${PersianFormat.number(template.unlockLevel)}"
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
