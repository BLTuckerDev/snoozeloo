package dev.bltucker.snoozeloo.common.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = SnoozelooBlue,
    secondary = SnoozelooBluePale,
    tertiary = SnoozelooBlack,
    background = SnoozelooWhite,
    surface = SnoozelooWhite,
    onPrimary = SnoozelooWhite,
    onSecondary = SnoozelooBlack,
    onTertiary = SnoozelooWhite,
    onBackground = SnoozelooBlack,
    onSurface = SnoozelooBlack,
)

@Composable
fun SnoozelooTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}