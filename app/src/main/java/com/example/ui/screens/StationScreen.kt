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
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.EquipmentType
import com.example.domain.GameCatalog
import com.example.domain.GameEngine
import com.example.ui.components.SectionHeader
import com.example.ui.components.equipmentIcon
import com.example.ui.theme.BadgeGold
import com.example.ui.theme.IceBlueSurface
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PoliceBlue
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyAccent
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateMuted
import com.example.ui.viewmodel.GameUiState
import com.example.util.PersianFormat

@Composable
fun StationScreen(
    state: GameUiState,
    onUpgradeBuilding: () -> Unit,
    onUpgradeCapacity: () -> Unit,
    onUpgradeEquipment: (EquipmentType) -> Unit
) {
    val profile = state.profile
    val maxCapacity = GameEngine.maxOfficerCapacity(profile)
    val buildingCost = GameEngine.buildingUpgradeCost(profile.stationBuildingLevel)
    val capacityCost = GameEngine.capacityUpgradeCost(profile.capacityLevel)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(IceBlueSurface)
            .testTag("station_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Illustrated Police Station Hero Banner
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                shadowElevation = 5.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(178.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_precinct_hq_1795700031592),
                        contentDescription = "ساختمان ستاد کلانتری",
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
                                        NavyDeep.copy(alpha = 0.55f),
                                        NavyDeep.copy(alpha = 0.92f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BadgeGold
                        ) {
                            Text(
                                text = "ساختمان سطح ${PersianFormat.number(profile.stationBuildingLevel)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = NavyDeep,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = GameEngine.stationStatusLabel(profile),
                            style = MaterialTheme.typography.headlineSmall,
                            color = PureWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "ظرفیت نیرو: ${PersianFormat.number(state.officers.size)} از ${PersianFormat.number(maxCapacity)} نفر • پاداش ساختمان +${PersianFormat.percent((profile.stationBuildingLevel - 1) * 8)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SkyAccent
                        )
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = "توسعه ساختمان و ظرفیت کلانتری",
                subtitle = "افزایش درآمد، قدرت عمومی و تعداد مأموران قابل استخدام"
            )
        }

        // Building Upgrade Card
        item {
            StationUpgradeCard(
                title = "ارتقای ساختمان کلانتری",
                description = "هر سطح ساختمان +۸٪ به پاداش تمام مأموریت‌ها، +۶ به قدرت تیم و بودجه پایان روز اضافه می‌کند.",
                currentLevel = profile.stationBuildingLevel,
                maxLevel = GameCatalog.MAX_BUILDING_LEVEL,
                cost = buildingCost,
                canAfford = profile.money >= buildingCost,
                icon = Icons.Filled.Apartment,
                accent = NavyPrimary,
                testTag = "upgrade_building_button",
                onUpgrade = onUpgradeBuilding
            )
        }

        // Capacity Upgrade Card
        item {
            StationUpgradeCard(
                title = "افزایش ظرفیت نیروها",
                description = "هر ارتقا ۲ جایگاه جدید برای استخدام پلیس‌های بیشتر باز می‌کند (ظرفیت فعلی: ${PersianFormat.number(maxCapacity)} نفر).",
                currentLevel = profile.capacityLevel,
                maxLevel = GameCatalog.MAX_CAPACITY_LEVEL,
                cost = capacityCost,
                canAfford = profile.money >= capacityCost,
                icon = Icons.Filled.GroupAdd,
                accent = PoliceBlue,
                testTag = "upgrade_capacity_button",
                onUpgrade = onUpgradeCapacity
            )
        }

        item {
            SectionHeader(
                title = "ارتقای تجهیزات هوشمند",
                subtitle = "تجهیزات مدرن مستقیماً روی سرعت، درآمد و احتمال موفقیت اثر دارند"
            )
        }

        items(EquipmentType.entries, key = { it.name }) { equip ->
            val currentLevel = GameEngine.equipmentLevel(profile, equip)
            val cost = GameEngine.equipmentUpgradeCost(equip, currentLevel)
            StationUpgradeCard(
                title = equip.titleFa,
                description = equip.effectDescFa,
                currentLevel = currentLevel,
                maxLevel = equip.maxLevel,
                cost = cost,
                canAfford = profile.money >= cost,
                icon = equipmentIcon(equip),
                accent = PoliceBlue,
                testTag = "upgrade_equip_${equip.name.lowercase()}",
                onUpgrade = { onUpgradeEquipment(equip) }
            )
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun StationUpgradeCard(
    title: String,
    description: String,
    currentLevel: Int,
    maxLevel: Int,
    cost: Long,
    canAfford: Boolean,
    icon: ImageVector,
    accent: Color,
    testTag: String,
    onUpgrade: () -> Unit
) {
    val isMax = currentLevel >= maxLevel
    val progress = (currentLevel.toFloat() / maxLevel.coerceAtLeast(1)).coerceIn(0f, 1f)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, SlateBorder.copy(alpha = 0.55f)),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
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
                            .size(44.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(accent.copy(alpha = 0.13f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = accent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = NavyDeep,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "سطح ${PersianFormat.number(currentLevel)} از ${PersianFormat.number(maxLevel)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = PoliceBlue
                        )
                    }
                }
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = SlateMuted
            )

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50)),
                color = accent,
                trackColor = SlateBorder.copy(alpha = 0.35f),
                strokeCap = StrokeCap.Round
            )

            Button(
                onClick = onUpgrade,
                enabled = !isMax && canAfford,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag(testTag),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = PureWhite
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowUpward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isMax) {
                        "به حداکثر سطح رسیده است"
                    } else {
                        "ارتقا به سطح ${PersianFormat.number(currentLevel + 1)} • ${PersianFormat.money(cost)}"
                    },
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
