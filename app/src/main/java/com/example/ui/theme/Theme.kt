package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val SampurnaLightBlueRedTheme = lightColorScheme(
    primary = SacredVermilion,
    onPrimary = Color.White,
    primaryContainer = VermilionContainer,
    onPrimaryContainer = OnVermilionContainer,
    secondary = TempleGold,
    onSecondary = Color.White,
    secondaryContainer = TempleGoldContainer,
    onSecondaryContainer = OnTempleGoldContainer,
    tertiary = DeepCrimsonText,
    onTertiary = Color.White,
    tertiaryContainer = LightBlueTinted,
    onTertiaryContainer = DeepCrimsonText,
    background = LightBlueBackground,
    onBackground = DeepCrimsonText,
    surface = LightBlueSurface,
    onSurface = DeepCrimsonText,
    surfaceVariant = LightBlueTinted,
    onSurfaceVariant = MutedCrimsonText,
    outline = TempleGold,
    outlineVariant = LightBlueBorder
)

val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp)
)

@Composable
fun SampurnaAstroTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SampurnaLightBlueRedTheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
