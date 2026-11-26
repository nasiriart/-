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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.OfficerEntity
import com.example.data.model.OfficerSpecialty
import com.example.domain.GameCatalog
import com.example.domain.GameEngine
import com.example.domain.RecruitTemplate
import com.example.ui.components.SectionHeader
import com.example.ui.components.officerPortraitRes
import com.example.ui.components.specialtyColor
import com.example.ui.components.specialtyIcon
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
fun OfficersScreen(
    state: GameUiState,
    onHireOfficer: (RecruitTemplate) -> Unit,
    onUpgradeOfficer: (Int) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val maxCap = GameEngine.maxOfficerCapacity(state.profile)
    val capacityProgress = (state.officers.size.toFloat() / maxCap.coerceAtLeast(1)).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IceBlueSurface)
            .testTag("officers_screen")
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = PureWhite,
            border = BorderStroke(1.dp, SlateBorder.copy(alpha = 0.55f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
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
                    Text(
                        text = "ظرفیت پرسنل کلانتری",
                        style = MaterialTheme.typography.titleSmall,
                        color = NavyDeep
                    )
                    Text(
                        text = "${PersianFormat.number(state.officers.size)} از ${PersianFormat.number(maxCap)} مأمور",
                        style = MaterialTheme.typography.labelLarge,
                        color = PoliceBlue
                    )
                }
                LinearProgressIndicator(
                    progress = { capacityProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(50)),
                    color = if (state.officers.size >= maxCap) BadgeGold else PoliceBlue,
                    trackColor = SlateBorder.copy(alpha = 0.35f),
                    strokeCap = StrokeCap.Round
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilterChip(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                label = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("نیروهای من (${PersianFormat.number(state.officers.size)})")
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("tab_my_officers"),
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NavyPrimary,
                    selectedLabelColor = PureWhite,
                    containerColor = PureWhite,
                    labelColor = SlateInk
                )
            )
            FilterChip(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                label = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("استخدام نیروی جدید")
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("tab_hire_officers"),
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NavyPrimary,
                    selectedLabelColor = PureWhite,
                    containerColor = PureWhite,
                    labelColor = SlateInk
                )
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (selectedTab == 0) {
                item(key = "roster_header") {
                    SectionHeader(
                        title = "پرسنل فعال کلانتری",
                        subtitle = "با ارتقای هر مأمور، قدرت تیم بالا رفته و احتمال فرار مجرم کاهش می‌یابد"
                    )
                }
                items(state.officers, key = { "my_officer_${it.id}" }) { officer ->
                    OfficerRosterCard(
                        officer = officer,
                        playerMoney = state.profile.money,
                        onUpgrade = { onUpgradeOfficer(officer.id) }
                    )
                }
            } else {
                item(key = "recruit_header") {
                    SectionHeader(
                        title = "مرکز استخدام نیروهای حرفه‌ای",
                        subtitle = "با افزایش سطح فرمانده، افسران متخصص‌تر آزاد می‌شوند"
                    )
                }
                items(GameCatalog.recruitTemplates, key = { "rec_${it.id}" }) { template ->
                    RecruitCandidateCard(
                        template = template,
                        playerLevel = state.profile.level,
                        playerMoney = state.profile.money,
                        hasCapacity = state.officers.size < maxCap,
                        onHire = { onHireOfficer(template) }
                    )
                }
            }
            item(key = "officers_spacer") { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun OfficerRosterCard(
    officer: OfficerEntity,
    playerMoney: Long,
    onUpgrade: () -> Unit
) {
    val spec = OfficerSpecialty.fromName(officer.specialty)
    val accent = specialtyColor(spec)
    val power = GameEngine.officerPower(officer)
    val isMax = officer.level >= GameCatalog.MAX_OFFICER_LEVEL
    val upgradeCost = GameEngine.officerUpgradeCost(officer)
    val canUpgrade = !officer.isBusy && !isMax && playerMoney >= upgradeCost

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, SlateBorder.copy(alpha = 0.55f)),
        shadowElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("officer_card_${officer.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Framed Officer Portrait Photo with Level Badge
                    Box(modifier = Modifier.size(64.dp)) {
                        Image(
                            painter = painterResource(id = officerPortraitRes(officer.name, spec)),
                            contentDescription = officer.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                                .border(2.dp, accent, RoundedCornerShape(16.dp))
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(BadgeGold)
                                .border(1.5.dp, PureWhite, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = PersianFormat.number(officer.level),
                                style = MaterialTheme.typography.labelSmall,
                                color = NavyDeep,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = officer.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = NavyDeep,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = specialtyIcon(spec),
                                contentDescription = spec.titleFa,
                                tint = accent,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "${spec.titleFa} • سطح ${PersianFormat.number(officer.level)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateMuted
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (officer.isBusy) BadgeGoldSoft else SuccessGreenSoft
                ) {
                    Text(
                        text = if (officer.isBusy) "در مأموریت" else "آماده اعزام",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (officer.isBusy) NavyDeep else SuccessGreen,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(IceBlueSurface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = PoliceBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "قدرت عملیاتی: ${PersianFormat.number(power)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = SlateInk
                    )
                }
                Text(
                    text = "مأموریت موفق: ${PersianFormat.number(officer.missionsCompleted)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = SlateMuted
                )
            }

            Button(
                onClick = onUpgrade,
                enabled = canUpgrade,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("upgrade_officer_${officer.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PoliceBlue,
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
                    text = when {
                        isMax -> "حداکثر سطح"
                        officer.isBusy -> "منتظر بازگشت از مأموریت"
                        else -> "ارتقا به سطح ${PersianFormat.number(officer.level + 1)} (+۱۴ قدرت) • ${PersianFormat.money(upgradeCost)}"
                    },
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun RecruitCandidateCard(
    template: RecruitTemplate,
    playerLevel: Int,
    playerMoney: Long,
    hasCapacity: Boolean,
    onHire: () -> Unit
) {
    val isUnlocked = playerLevel >= template.unlockLevel
    val accent = specialtyColor(template.specialty)
    val canHire = isUnlocked && hasCapacity && playerMoney >= template.hireCost

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, if (isUnlocked) SlateBorder.copy(alpha = 0.6f) else SlateBorder.copy(alpha = 0.35f)),
        shadowElevation = if (isUnlocked) 2.dp else 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recruit_card_${template.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(modifier = Modifier.size(62.dp)) {
                        Image(
                            painter = painterResource(id = officerPortraitRes(template.name, template.specialty)),
                            contentDescription = template.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                                .alpha(if (isUnlocked) 1f else 0.45f)
                                .border(
                                    width = 1.5.dp,
                                    color = if (isUnlocked) accent else SlateBorder,
                                    shape = RoundedCornerShape(16.dp)
                                )
                        )
                        if (!isUnlocked) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(NavyDeep.copy(alpha = 0.45f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = "قفل شده",
                                    tint = PureWhite,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = template.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isUnlocked) NavyDeep else SlateMuted,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${template.specialty.titleFa} • قدرت پایه ${PersianFormat.number(template.basePower)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateMuted
                        )
                    }
                }
                if (!isUnlocked) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = IceBlueSurface
                    ) {
                        Text(
                            text = "سطح ${PersianFormat.number(template.unlockLevel)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = NavyPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Text(
                text = template.bioFa,
                style = MaterialTheme.typography.bodySmall,
                color = SlateMuted
            )

            Button(
                onClick = onHire,
                enabled = canHire,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("hire_button_${template.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NavyPrimary,
                    contentColor = PureWhite
                )
            ) {
                Icon(
                    imageVector = if (isUnlocked) Icons.Filled.PersonAdd else Icons.Filled.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when {
                        !isUnlocked -> "باز شدن در سطح ${PersianFormat.number(template.unlockLevel)}"
                        !hasCapacity -> "ظرفیت کلانتری تکمیل است"
                        else -> "استخدام نیرو • ${PersianFormat.money(template.hireCost)}"
                    },
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
