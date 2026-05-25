package com.weatherdrive.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val WeatherDriveColorScheme = darkColorScheme(
    primary = Purple80,
    onPrimary = DarkBackground,
    primaryContainer = Purple40,
    onPrimaryContainer = PurpleLight,
    secondary = Purple60,
    onSecondary = DarkBackground,
    secondaryContainer = DarkCardHighlight,
    onSecondaryContainer = TextWhite,
    tertiary = OrangeWarm,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = TextWhite,
    surface = DarkSurface,
    onSurface = TextWhite,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextGray,
    error = RedError,
    onError = DarkBackground,
    outline = TextMuted
)

private val WeatherDriveShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun WeatherDriveTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WeatherDriveColorScheme,
        typography = WeatherDriveTypography,
        shapes = WeatherDriveShapes,
        content = content
    )
}
