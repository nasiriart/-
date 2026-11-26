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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.PlayerProfileEntity
import com.example.data.model.ChiefAvatarOption
import com.example.data.model.ChiefTrait
import com.example.domain.GameCatalog
import com.example.ui.components.SectionHeader
import com.example.ui.components.chiefAvatarRes
import com.example.ui.components.chiefTraitIcon
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
import com.example.util.PersianFormat

private val suggestedChiefNames = listOf(
    "سرهنگ آرش راد",
    "سرگرد سارا نادری",
    "سروان پارسا شهاب",
    "فرمانده کیانوش ملکی"
)

private val suggestedCallsigns = listOf(
    "عقاب ۱",
    "تندر",
    "شاهین",
    "شیر شهر"
)

@Composable
fun ProfileScreen(
    profile: PlayerProfileEntity,
    isOnboarding: Boolean,
    onSaveProfile: (name: String, callsign: String, avatarKey: String, trait: ChiefTrait, bio: String) -> Unit
) {
    var nameInput by rememberSaveable(profile.chiefName, isOnboarding) {
        mutableStateOf(if (isOnboarding && !profile.profileConfigured) "" else profile.chiefName)
    }
    var callsignInput by rememberSaveable(profile.chiefCallsign, isOnboarding) {
        mutableStateOf(profile.chiefCallsign)
    }
    var bioInput by rememberSaveable(profile.chiefBio, isOnboarding) {
        mutableStateOf(profile.chiefBio)
    }
    var selectedAvatarKey by rememberSaveable(profile.chiefAvatarKey, isOnboarding) {
        mutableStateOf(profile.chiefAvatarKey)
    }
    var selectedTraitName by rememberSaveable(profile.chiefTrait, isOnboarding) {
        mutableStateOf(profile.chiefTrait)
    }

    val selectedTrait = ChiefTrait.fromName(selectedTraitName)
    val previewName = nameInput.trim().ifBlank { "فرمانده کلانتری" }
    val previewCallsign = callsignInput.trim().ifBlank { "عقاب ۱" }
    val canSave = nameInput.trim().length >= 2

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(IceBlueSurface)
            .then(
                if (isOnboarding) Modifier.statusBarsPadding().navigationBarsPadding() else Modifier
            )
            .testTag(if (isOnboarding) "character_creation_screen" else "profile_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (isOnboarding) {
            item(key = "onboarding_title") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "ساخت شخصیت و پروفایل رئیس پلیس",
                        style = MaterialTheme.typography.headlineSmall,
                        color = NavyDeep,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "چهره، نام، کد بی‌سیم و تخصص فرماندهی خود را انتخاب کنید تا هویت اختصاصی شما در کلانتری ثبت شود.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateMuted
                    )
                }
            }
        }

        // Official Police Chief ID Card (Live Preview)
        item(key = "chief_id_card") {
            Surface(
                shape = RoundedCornerShape(22.dp),
                shadowElevation = 6.dp,
                border = BorderStroke(1.5.dp, BadgeGold.copy(alpha = 0.75f)),
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
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Badge,
                                    contentDescription = null,
                                    tint = BadgeGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "کارت شناسایی رسمی فرماندهی پلیس",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = BadgeGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = PureWhite.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = "سطح ${PersianFormat.number(profile.level)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SkyAccent,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Image(
                                painter = painterResource(id = chiefAvatarRes(selectedAvatarKey)),
                                contentDescription = previewName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .border(2.5.dp, BadgeGold, RoundedCornerShape(18.dp))
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = previewName,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = PureWhite,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${GameCatalog.rankTitleForLevel(profile.level)} • کد بی‌سیم: $previewCallsign",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SkyAccent
                                )
                                Text(
                                    text = bioInput.trim().ifBlank { "حافظ امنیت و آرامش شهر" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PureWhite.copy(alpha = 0.8f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Active Commander Trait Strip
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PureWhite.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, SkyAccent.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = chiefTraitIcon(selectedTrait),
                                    contentDescription = selectedTrait.titleFa,
                                    tint = BadgeGold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "ویژگی شخصیت: ${selectedTrait.titleFa}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = PureWhite
                                    )
                                    Text(
                                        text = selectedTrait.badgeFa,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SkyAccent
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Career Medals (Shown when viewing existing Profile inside game)
        if (!isOnboarding) {
            item(key = "chief_medals") {
                SectionHeader(
                    title = "نشان‌ها و افتخارات فرمانده",
                    subtitle = "دستاوردهای ثبت‌شده در پرونده خدمتی شما"
                )
            }
            item(key = "chief_medals_row") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileMedalChip(
                        title = "نگهبان شهر",
                        unlocked = profile.missionsSucceeded >= 3,
                        detail = "${PersianFormat.number(profile.missionsSucceeded)}/۳ مأموریت",
                        modifier = Modifier.weight(1f)
                    )
                    ProfileMedalChip(
                        title = "رئیس زندان",
                        unlocked = profile.inmatesTransferred >= 2,
                        detail = "${PersianFormat.number(profile.inmatesTransferred)}/۲ تحویل",
                        modifier = Modifier.weight(1f)
                    )
                    ProfileMedalChip(
                        title = "فرمانده ارشد",
                        unlocked = profile.level >= 5,
                        detail = "سطح ${PersianFormat.number(profile.level)}/۵",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Step 1: Avatar Selection
        item(key = "avatar_header") {
            SectionHeader(
                title = "۱. انتخاب چهره شخصیت فرمانده",
                subtitle = "تصویر پروفایل شما در نوار فرماندهی و کارت شناسایی نمایش داده می‌شود"
            )
        }
        item(key = "avatar_grid") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ChiefAvatarOption.entries.forEach { option ->
                    val isSelected = selectedAvatarKey == option.key
                    Surface(
                        onClick = { selectedAvatarKey = option.key },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) PoliceBlue.copy(alpha = 0.12f) else PureWhite,
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) PoliceBlue else SlateBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("avatar_option_${option.key}")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box {
                                Image(
                                    painter = painterResource(id = chiefAvatarRes(option.key)),
                                    contentDescription = option.titleFa,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .border(
                                            width = if (isSelected) 2.5.dp else 1.dp,
                                            color = if (isSelected) BadgeGold else SlateBorder,
                                            shape = CircleShape
                                        )
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "انتخاب‌شده",
                                        tint = PoliceBlue,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .align(Alignment.BottomEnd)
                                            .clip(CircleShape)
                                            .background(PureWhite)
                                    )
                                }
                            }
                            Text(
                                text = option.titleFa,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) NavyDeep else SlateMuted,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Step 2: Name, Callsign & Bio Inputs
        item(key = "identity_inputs") {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = PureWhite,
                border = BorderStroke(1.dp, SlateBorder.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "۲. مشخصات شخصیت شما",
                        style = MaterialTheme.typography.titleSmall,
                        color = NavyDeep,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { if (it.length <= 26) nameInput = it },
                        label = { Text("نام شخصیت فرمانده") },
                        placeholder = { Text("مثلاً: سرهنگ پارسا راد") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = PoliceBlue)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PoliceBlue,
                            unfocusedBorderColor = SlateBorder
                        ),
                        shape = RoundedCornerShape(13.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name_input")
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(suggestedChiefNames, key = { "sug_name_$it" }) { suggestion ->
                            Surface(
                                onClick = { nameInput = suggestion },
                                shape = RoundedCornerShape(50),
                                color = IceBlueSurface,
                                border = BorderStroke(1.dp, SlateBorder.copy(alpha = 0.7f))
                            ) {
                                Text(
                                    text = suggestion,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NavyPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = callsignInput,
                        onValueChange = { if (it.length <= 16) callsignInput = it },
                        label = { Text("کد بی‌سیم یا لقب فرماندهی") },
                        placeholder = { Text("مثلاً: عقاب ۱") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Filled.Radio, contentDescription = null, tint = NavyPrimary)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PoliceBlue,
                            unfocusedBorderColor = SlateBorder
                        ),
                        shape = RoundedCornerShape(13.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_callsign_input")
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(suggestedCallsigns, key = { "sug_call_$it" }) { call ->
                            Surface(
                                onClick = { callsignInput = call },
                                shape = RoundedCornerShape(50),
                                color = BadgeGoldSoft.copy(alpha = 0.65f),
                                border = BorderStroke(1.dp, BadgeGold.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = call,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NavyDeep,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = bioInput,
                        onValueChange = { if (it.length <= 54) bioInput = it },
                        label = { Text("شعار فرماندهی (اختیاری)") },
                        placeholder = { Text("مثلاً: حافظ امنیت و آرامش شهر") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Filled.Shield, contentDescription = null, tint = BadgeGold)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PoliceBlue,
                            unfocusedBorderColor = SlateBorder
                        ),
                        shape = RoundedCornerShape(13.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_bio_input")
                    )
                }
            }
        }

        // Step 3: Commander Archetype / Trait Picker
        item(key = "trait_header") {
            SectionHeader(
                title = "۳. انتخاب تخصص و سبک فرماندهی",
                subtitle = "هر شخصیت مزیت واقعی و دائمی در مأموریت‌ها و زندان به شما می‌دهد"
            )
        }
        items(ChiefTrait.entries, key = { "trait_${it.name}" }) { trait ->
            val isSelected = selectedTrait == trait
            Surface(
                onClick = { selectedTraitName = trait.name },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) PoliceBlue.copy(alpha = 0.10f) else PureWhite,
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) PoliceBlue else SlateBorder.copy(alpha = 0.65f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("trait_option_${trait.name.lowercase()}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) PoliceBlue else NavyPrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = chiefTraitIcon(trait),
                            contentDescription = trait.titleFa,
                            tint = if (isSelected) PureWhite else NavyPrimary,
                            modifier = Modifier.size(23.dp)
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = trait.titleFa,
                                style = MaterialTheme.typography.titleSmall,
                                color = NavyDeep,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = BadgeGoldSoft
                            ) {
                                Text(
                                    text = trait.badgeFa,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NavyDeep,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = trait.bonusDescFa,
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateMuted
                        )
                    }
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = "انتخاب‌شده",
                            tint = PoliceBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // Save Profile Button
        item(key = "save_profile_cta") {
            Button(
                onClick = {
                    onSaveProfile(
                        nameInput,
                        callsignInput,
                        selectedAvatarKey,
                        selectedTrait,
                        bioInput
                    )
                },
                enabled = canSave,
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PoliceBlue,
                    contentColor = PureWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_profile_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Save,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isOnboarding) {
                        "ساخت شخصیت و ورود به مرکز فرماندهی"
                    } else {
                        "ذخیره تغییرات پروفایل و شخصیت"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            if (!canSave) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "برای ادامه، یک نام (حداقل ۲ حرف) وارد کنید یا از نام‌های پیشنهادی انتخاب نمایید.",
                    style = MaterialTheme.typography.labelSmall,
                    color = SlateMuted
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ProfileMedalChip(
    title: String,
    unlocked: Boolean,
    detail: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (unlocked) SuccessGreenSoft.copy(alpha = 0.7f) else PureWhite,
        border = BorderStroke(
            1.dp,
            if (unlocked) SuccessGreen else SlateBorder.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.EmojiEvents,
                contentDescription = title,
                tint = if (unlocked) BadgeGold else SlateBorder,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = if (unlocked) NavyDeep else SlateMuted,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.labelSmall,
                color = if (unlocked) SuccessGreen else SlateMuted,
                maxLines = 1
            )
        }
    }
}
