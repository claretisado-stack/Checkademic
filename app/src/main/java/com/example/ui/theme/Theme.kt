package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val CheckademicLightColorScheme = lightColorScheme(
    primary = CobaltInk,
    onPrimary = Color.White,
    primaryContainer = CobaltContainer,
    onPrimaryContainer = OnCobaltContainer,
    secondary = AmberTerracotta,
    onSecondary = Color.White,
    secondaryContainer = AmberContainer,
    onSecondaryContainer = OnAmberContainer,
    tertiary = SageTeal,
    onTertiary = Color.White,
    tertiaryContainer = MintContainer,
    onTertiaryContainer = OnMintContainer,
    error = CrimsonOverdue,
    onError = Color.White,
    errorContainer = RoseContainer,
    onErrorContainer = OnRoseContainer,
    background = IvoryPaper,
    onBackground = InkText,
    surface = CrispAlabaster,
    onSurface = InkText,
    surfaceVariant = SoftParchment,
    onSurfaceVariant = SlateMuted,
    outline = WarmDivider,
    outlineVariant = WarmDivider,
)

private val CheckademicDarkColorScheme = darkColorScheme(
    primary = SkyCobalt,
    onPrimary = Color(0xFF0A2463),
    primaryContainer = DarkCobaltContainer,
    onPrimaryContainer = OnDarkCobaltContainer,
    secondary = WarmGold,
    onSecondary = Color(0xFF422006),
    secondaryContainer = DarkAmberContainer,
    onSecondaryContainer = OnDarkAmberContainer,
    tertiary = BrightMint,
    onTertiary = Color(0xFF042F2E),
    tertiaryContainer = DarkMintContainer,
    onTertiaryContainer = OnDarkMintContainer,
    error = CoralError,
    onError = Color(0xFF450A0A),
    errorContainer = DarkRoseContainer,
    onErrorContainer = OnDarkRoseContainer,
    background = MidnightSlate,
    onBackground = CrispOffWhite,
    surface = CharcoalCard,
    onSurface = CrispOffWhite,
    surfaceVariant = ElevatedCharcoal,
    onSurfaceVariant = CoolGrayMuted,
    outline = DarkDivider,
    outlineVariant = DarkDivider,
)

val CheckademicShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun CheckademicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) CheckademicDarkColorScheme else CheckademicLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CheckademicTypography,
        shapes = CheckademicShapes,
        content = content
    )
}
