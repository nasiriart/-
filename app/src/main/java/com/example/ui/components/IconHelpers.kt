package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.AirportShuttle
import androidx.compose.material.icons.filled.CarCrash
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.R
import com.example.data.model.ChiefTrait
import com.example.data.model.CrimeType
import com.example.data.model.EquipmentType
import com.example.data.model.OfficerSpecialty
import com.example.ui.theme.AlertRed
import com.example.ui.theme.BadgeGold
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PoliceBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningOrange

fun specialtyIcon(specialty: OfficerSpecialty): ImageVector = when (specialty) {
    OfficerSpecialty.PATROL -> Icons.Filled.LocalPolice
    OfficerSpecialty.TRAFFIC -> Icons.Filled.Traffic
    OfficerSpecialty.DETECTIVE -> Icons.Filled.ManageSearch
    OfficerSpecialty.TACTICAL -> Icons.Filled.Shield
    OfficerSpecialty.NEGOTIATOR -> Icons.Filled.Forum
}

fun specialtyColor(specialty: OfficerSpecialty): Color = when (specialty) {
    OfficerSpecialty.PATROL -> PoliceBlue
    OfficerSpecialty.TRAFFIC -> WarningOrange
    OfficerSpecialty.DETECTIVE -> NavyPrimary
    OfficerSpecialty.TACTICAL -> AlertRed
    OfficerSpecialty.NEGOTIATOR -> SuccessGreen
}

@DrawableRes
fun officerPortraitRes(name: String, specialty: OfficerSpecialty): Int {
    val femaleKeywords = listOf("سارا", "مریم", "نازنین", "لیلا", "الهام", "پرستو")
    if (femaleKeywords.any { name.contains(it) }) {
        return R.drawable.img_officer_female_1795702045887
    }
    return when (specialty) {
        OfficerSpecialty.TACTICAL -> R.drawable.img_officer_tactical_1795702047436
        OfficerSpecialty.DETECTIVE, OfficerSpecialty.NEGOTIATOR -> R.drawable.img_officer_detective_1795702122400
        OfficerSpecialty.PATROL, OfficerSpecialty.TRAFFIC -> R.drawable.img_officer_patrol_1795702046369
    }
}

@DrawableRes
fun chiefAvatarRes(avatarKey: String): Int = when (avatarKey) {
    "patrol" -> R.drawable.img_officer_patrol_1795702046369
    "female" -> R.drawable.img_officer_female_1795702045887
    "detective" -> R.drawable.img_officer_detective_1795702122400
    else -> R.drawable.img_officer_tactical_1795702047436
}

fun chiefTraitIcon(trait: ChiefTrait): ImageVector = when (trait) {
    ChiefTrait.TACTICAL_LEADER -> Icons.Filled.Shield
    ChiefTrait.DETECTIVE_MIND -> Icons.Filled.ManageSearch
    ChiefTrait.PURSUIT_ACE -> Icons.Filled.Speed
    ChiefTrait.CITY_ECONOMIST -> Icons.Filled.AccountBalance
}

@DrawableRes
fun vehicleImageRes(modelKey: String): Int = when (modelKey) {
    "moto_patrol" -> R.drawable.img_vehicle_moto_1795702048622
    "sedan_cruiser", "traffic_van" -> R.drawable.img_vehicle_sedan_1795702123596
    "suv_patrol", "turbo_interceptor", "mobile_hq" -> R.drawable.img_vehicle_suv_1795702126009
    "police_heli" -> R.drawable.img_vehicle_heli_1795702125243
    else -> R.drawable.img_vehicle_sedan_1795702123596
}

fun crimeIcon(crimeType: CrimeType): ImageVector = when (crimeType) {
    CrimeType.SHOP_THEFT -> Icons.Filled.Storefront
    CrimeType.PUBLIC_DISTURBANCE -> Icons.Filled.NotificationsActive
    CrimeType.TRAFFIC_ACCIDENT -> Icons.Filled.CarCrash
    CrimeType.MISSING_PERSON -> Icons.Filled.PersonSearch
    CrimeType.STREET_FIGHT -> Icons.Filled.Groups
    CrimeType.CAR_THEFT -> Icons.Filled.DirectionsCar
    CrimeType.CRIMINAL_PURSUIT -> Icons.Filled.Speed
    CrimeType.HOSTAGE_RESCUE -> Icons.Filled.VerifiedUser
    CrimeType.JEWELRY_HEIST -> Icons.Filled.AccountBalance
    CrimeType.HIGHWAY_CRISIS -> Icons.Filled.Warning
    CrimeType.CITYWIDE_OPERATION -> Icons.Filled.Security
}

fun equipmentIcon(type: EquipmentType): ImageVector = when (type) {
    EquipmentType.RADIO -> Icons.Filled.Radio
    EquipmentType.RADAR -> Icons.Filled.Radar
    EquipmentType.SAFETY_GEAR -> Icons.Filled.HealthAndSafety
    EquipmentType.COMMAND_CENTER -> Icons.Filled.GppGood
}

fun vehicleIcon(modelKey: String): ImageVector = when (modelKey) {
    "moto_patrol" -> Icons.Filled.TwoWheeler
    "sedan_cruiser" -> Icons.Filled.DirectionsCar
    "traffic_van" -> Icons.Filled.Traffic
    "suv_patrol" -> Icons.Filled.LocalPolice
    "turbo_interceptor" -> Icons.Filled.Speed
    "mobile_hq" -> Icons.Filled.AirportShuttle
    "police_heli" -> Icons.Filled.AirplanemodeActive
    else -> Icons.Filled.DirectionsCar
}

fun successRateColor(rate: Int): Color = when {
    rate >= 78 -> SuccessGreen
    rate >= 52 -> BadgeGold
    else -> AlertRed
}

fun escapeRiskColor(risk: Int): Color = when {
    risk <= 20 -> SuccessGreen
    risk <= 42 -> BadgeGold
    else -> AlertRed
}
