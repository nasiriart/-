package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private val ColorContainerBlue = androidx.compose.ui.graphics.Color(0xFFDBEAFE)

private val PoliceLightColorScheme = lightColorScheme(
    primary = PoliceBlue,
    onPrimary = PureWhite,
    primaryContainer = ColorContainerBlue,
    onPrimaryContainer = NavyPrimary,
    secondary = NavyPrimary,
    onSecondary = PureWhite,
    secondaryContainer = IceBlueVariant,
    onSecondaryContainer = NavyDeep,
    tertiary = BadgeGold,
    onTertiary = NavyDeep,
    tertiaryContainer = BadgeGoldSoft,
    onTertiaryContainer = NavyDeep,
    background = IceBlueSurface,
    onBackground = SlateInk,
    surface = PureWhite,
    onSurface = SlateInk,
    surfaceVariant = IceBlueVariant,
    onSurfaceVariant = SlateMuted,
    outline = SlateBorder,
    error = AlertRed,
    onError = PureWhite,
    errorContainer = AlertRedSoft,
    onErrorContainer = AlertRed
)

private val PoliceShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun PoliceChiefTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = PoliceLightColorScheme,
            typography = Typography,
            shapes = PoliceShapes,
            content = content
        )
    }
}
