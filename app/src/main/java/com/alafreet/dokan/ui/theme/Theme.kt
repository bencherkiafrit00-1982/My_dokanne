package com.alafreet.dokan.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = DokanPurple,
    onPrimary = Color.White,
    secondary = DokanOrange,
    background = DokanBackground,
    surface = DokanSurface,
    onSurface = DokanOnSurface,
    error = DokanRed
)

private val DarkColors = darkColorScheme(
    primary = DokanPurple,
    secondary = DokanOrange,
    error = DokanRed
)

@Composable
fun DokanAlafreetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = DokanTypography,
        content = content
    )
}
