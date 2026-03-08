package com.atsighi.tutor.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val HausaIndigo = Color(0xFF3F51B5)
val HausaDeepIndigo = Color(0xFF283593)
val HausaSoftBlue = Color(0xFFE3F2FD)
val EcoLeafGreen = Color(0xFF4CAF50)
val TeacherWarmWhite = Color(0xFFF5F5F5)
val AcademicGold = Color(0xFFFFD700)

private val LightColorScheme = lightColorScheme(
    primary = HausaIndigo,
    secondary = EcoLeafGreen,
    tertiary = AcademicGold,
    background = TeacherWarmWhite,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
)

@Composable
fun HausaTutorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        // Simple dark mode implementation
        darkColorScheme(primary = HausaIndigo, secondary = EcoLeafGreen)
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = androidx.compose.material3.Typography(), // Assume standard Typography is defined
        content = content
    )
}
