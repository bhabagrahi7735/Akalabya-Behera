package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.example.data.preferences.AppPalette
import com.example.data.preferences.JournalFont
import com.example.data.preferences.ThemeMode

// ==========================================
// 1. FOREST SANCTUARY
// ==========================================
private val ForestDarkColorScheme = darkColorScheme(
    primary = SageDarkPrimary,
    onPrimary = SageDarkOnPrimary,
    primaryContainer = SageDarkPrimaryContainer,
    onPrimaryContainer = SageDarkOnPrimaryContainer,
    secondary = TerracottaDarkAccent,
    onSecondary = Color.White,
    secondaryContainer = TerracottaDarkContainer,
    onSecondaryContainer = Color(0xFFFFDBCF),
    tertiary = Color(0xFFC4D4E0),
    onTertiary = Color(0xFF1E323D),
    background = StoneDark,
    onBackground = Color(0xFFE6E8EA),
    surface = StoneSurface,
    onSurface = Color(0xFFE6E8EA),
    surfaceVariant = StoneSurfaceVariant,
    onSurfaceVariant = Color(0xFFBAC2CB),
    outline = StoneOutline,
    outlineVariant = StoneOutlineVariant
)

private val ForestLightColorScheme = lightColorScheme(
    primary = EarthPrimary,
    onPrimary = EarthOnPrimary,
    primaryContainer = EarthPrimaryContainer,
    onPrimaryContainer = EarthOnPrimaryContainer,
    secondary = TerracottaAccent,
    onSecondary = Color.White,
    secondaryContainer = TerracottaContainer,
    onSecondaryContainer = Color(0xFF5D1D0F),
    tertiary = Color(0xFF386663),
    onTertiary = Color.White,
    background = SandParchment,
    onBackground = Color(0xFF22252A),
    surface = SandSurface,
    onSurface = Color(0xFF22252A),
    surfaceVariant = SandSurfaceVariant,
    onSurfaceVariant = Color(0xFF6B7280),
    outline = EarthOutline,
    outlineVariant = EarthOutlineVariant
)

// ==========================================
// 2. MIDNIGHT AMBER
// ==========================================
private val AmberDarkColorScheme = darkColorScheme(
    primary = AmberDarkPrimary,
    onPrimary = AmberDarkOnPrimary,
    primaryContainer = AmberDarkPrimaryContainer,
    onPrimaryContainer = AmberDarkOnPrimaryContainer,
    secondary = AmberDarkSecondary,
    onSecondary = Color.White,
    secondaryContainer = AmberDarkSecondaryContainer,
    onSecondaryContainer = Color(0xFFFFDBCC),
    tertiary = Color(0xFFE2C48D),
    onTertiary = Color(0xFF3F2E05),
    background = AmberDarkBackground,
    onBackground = Color(0xFFEFEBE4),
    surface = AmberDarkSurface,
    onSurface = Color(0xFFEFEBE4),
    surfaceVariant = AmberDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCEC5B6),
    outline = AmberDarkOutline,
    outlineVariant = AmberDarkOutlineVariant
)

private val AmberLightColorScheme = lightColorScheme(
    primary = AmberPrimary,
    onPrimary = AmberOnPrimary,
    primaryContainer = AmberPrimaryContainer,
    onPrimaryContainer = AmberOnPrimaryContainer,
    secondary = AmberSecondary,
    onSecondary = Color.White,
    secondaryContainer = AmberSecondaryContainer,
    onSecondaryContainer = Color(0xFF381A00),
    tertiary = Color(0xFF755B29),
    onTertiary = Color.White,
    background = AmberParchment,
    onBackground = Color(0xFF231B0F),
    surface = AmberSurface,
    onSurface = Color(0xFF231B0F),
    surfaceVariant = AmberSurfaceVariant,
    onSurfaceVariant = Color(0xFF6D6353),
    outline = AmberOutline,
    outlineVariant = AmberOutlineVariant
)

// ==========================================
// 3. NORDIC INDIGO
// ==========================================
private val IndigoDarkColorScheme = darkColorScheme(
    primary = IndigoDarkPrimary,
    onPrimary = IndigoDarkOnPrimary,
    primaryContainer = IndigoDarkPrimaryContainer,
    onPrimaryContainer = IndigoDarkOnPrimaryContainer,
    secondary = IndigoDarkSecondary,
    onSecondary = Color.White,
    secondaryContainer = IndigoDarkSecondaryContainer,
    onSecondaryContainer = Color(0xFFD6EEF8),
    tertiary = Color(0xFF90C2E7),
    onTertiary = Color(0xFF0F314D),
    background = IndigoDarkBackground,
    onBackground = Color(0xFFE5EBF2),
    surface = IndigoDarkSurface,
    onSurface = Color(0xFFE5EBF2),
    surfaceVariant = IndigoDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFBCC7D5),
    outline = IndigoDarkOutline,
    outlineVariant = IndigoDarkOutlineVariant
)

private val IndigoLightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = IndigoOnPrimary,
    primaryContainer = IndigoPrimaryContainer,
    onPrimaryContainer = IndigoOnPrimaryContainer,
    secondary = IndigoSecondary,
    onSecondary = Color.White,
    secondaryContainer = IndigoSecondaryContainer,
    onSecondaryContainer = Color(0xFF0B2B3E),
    tertiary = Color(0xFF2B5975),
    onTertiary = Color.White,
    background = IndigoParchment,
    onBackground = Color(0xFF161F2B),
    surface = IndigoSurface,
    onSurface = Color(0xFF161F2B),
    surfaceVariant = IndigoSurfaceVariant,
    onSurfaceVariant = Color(0xFF5D6979),
    outline = IndigoOutline,
    outlineVariant = IndigoOutlineVariant
)

// ==========================================
// 4. ROSEWOOD & CASHMERE
// ==========================================
private val RosewoodDarkColorScheme = darkColorScheme(
    primary = RosewoodDarkPrimary,
    onPrimary = RosewoodDarkOnPrimary,
    primaryContainer = RosewoodDarkPrimaryContainer,
    onPrimaryContainer = RosewoodDarkOnPrimaryContainer,
    secondary = RosewoodDarkSecondary,
    onSecondary = Color.White,
    secondaryContainer = RosewoodDarkSecondaryContainer,
    onSecondaryContainer = Color(0xFFFFD9E2),
    tertiary = Color(0xFFE0AFC0),
    onTertiary = Color(0xFF451929),
    background = RosewoodDarkBackground,
    onBackground = Color(0xFFF3E7EB),
    surface = RosewoodDarkSurface,
    onSurface = Color(0xFFF3E7EB),
    surfaceVariant = RosewoodDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFD3C1C7),
    outline = RosewoodDarkOutline,
    outlineVariant = RosewoodDarkOutlineVariant
)

private val RosewoodLightColorScheme = lightColorScheme(
    primary = RosewoodPrimary,
    onPrimary = RosewoodOnPrimary,
    primaryContainer = RosewoodPrimaryContainer,
    onPrimaryContainer = RosewoodOnPrimaryContainer,
    secondary = RosewoodSecondary,
    onSecondary = Color.White,
    secondaryContainer = RosewoodSecondaryContainer,
    onSecondaryContainer = Color(0xFF420E22),
    tertiary = Color(0xFF7E3F56),
    onTertiary = Color.White,
    background = RosewoodParchment,
    onBackground = Color(0xFF26191E),
    surface = RosewoodSurface,
    onSurface = Color(0xFF26191E),
    surfaceVariant = RosewoodSurfaceVariant,
    onSurfaceVariant = Color(0xFF705F66),
    outline = RosewoodOutline,
    outlineVariant = RosewoodOutlineVariant
)

// ==========================================
// 5. KYOTO MATCHA
// ==========================================
private val MatchaDarkColorScheme = darkColorScheme(
    primary = MatchaDarkPrimary,
    onPrimary = MatchaDarkOnPrimary,
    primaryContainer = MatchaDarkPrimaryContainer,
    onPrimaryContainer = MatchaDarkOnPrimaryContainer,
    secondary = MatchaDarkSecondary,
    onSecondary = Color.White,
    secondaryContainer = MatchaDarkSecondaryContainer,
    onSecondaryContainer = Color(0xFFF8E9C4),
    tertiary = Color(0xFFC0D29E),
    onTertiary = Color(0xFF243513),
    background = MatchaDarkBackground,
    onBackground = Color(0xFFEBEFE6),
    surface = MatchaDarkSurface,
    onSurface = Color(0xFFEBEFE6),
    surfaceVariant = MatchaDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFC5CEBF),
    outline = MatchaDarkOutline,
    outlineVariant = MatchaDarkOutlineVariant
)

private val MatchaLightColorScheme = lightColorScheme(
    primary = MatchaPrimary,
    onPrimary = MatchaOnPrimary,
    primaryContainer = MatchaPrimaryContainer,
    onPrimaryContainer = MatchaOnPrimaryContainer,
    secondary = MatchaSecondary,
    onSecondary = Color.White,
    secondaryContainer = MatchaSecondaryContainer,
    onSecondaryContainer = Color(0xFF2F240A),
    tertiary = Color(0xFF4C6B37),
    onTertiary = Color.White,
    background = MatchaParchment,
    onBackground = Color(0xFF1D241A),
    surface = MatchaSurface,
    onSurface = Color(0xFF1D241A),
    surfaceVariant = MatchaSurfaceVariant,
    onSurfaceVariant = Color(0xFF646E5E),
    outline = MatchaOutline,
    outlineVariant = MatchaOutlineVariant
)

fun getPaletteColorScheme(palette: AppPalette, isDark: Boolean): ColorScheme {
    return when (palette) {
        AppPalette.FOREST_SANCTUARY -> if (isDark) ForestDarkColorScheme else ForestLightColorScheme
        AppPalette.MIDNIGHT_AMBER -> if (isDark) AmberDarkColorScheme else AmberLightColorScheme
        AppPalette.NORDIC_INDIGO -> if (isDark) IndigoDarkColorScheme else IndigoLightColorScheme
        AppPalette.ROSEWOOD_CASHMERE -> if (isDark) RosewoodDarkColorScheme else RosewoodLightColorScheme
        AppPalette.KYOTO_MATCHA -> if (isDark) MatchaDarkColorScheme else MatchaLightColorScheme
    }
}

@Composable
fun AkalabyaTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    journalFont: JournalFont = JournalFont.SERIF,
    appPalette: AppPalette = AppPalette.FOREST_SANCTUARY,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val colorScheme = getPaletteColorScheme(appPalette, isDark)

    val fontFamily = when (journalFont) {
        JournalFont.SERIF -> FontFamily.Serif
        JournalFont.SANS_SERIF -> FontFamily.SansSerif
        JournalFont.MONOSPACE -> FontFamily.Monospace
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = createTypography(fontFamily),
        content = content
    )
}
