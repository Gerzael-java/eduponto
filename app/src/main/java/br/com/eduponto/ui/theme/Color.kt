package br.com.eduponto.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Identidade: fundo grafite, verde-claro para ações principais, verde-escuro para destaques,
// âmbar para atenção e vermelho para erros e ações destrutivas.
val EsquemaEscuro = darkColorScheme(
    primary = Color(0xFF6FD4A0),
    onPrimary = Color(0xFF00391F),
    primaryContainer = Color(0xFF1F5A3E),
    onPrimaryContainer = Color(0xFFB8F3D2),
    secondary = Color(0xFFF2BE6B),
    onSecondary = Color(0xFF462B00),
    secondaryContainer = Color(0xFF653F00),
    onSecondaryContainer = Color(0xFFFFDDAF),
    tertiary = Color(0xFFA9C8F8),
    onTertiary = Color(0xFF0A305D),
    tertiaryContainer = Color(0xFF274776),
    onTertiaryContainer = Color(0xFFD5E3FF),
    background = Color(0xFF111413),
    onBackground = Color(0xFFE2E6E3),
    surface = Color(0xFF111413),
    onSurface = Color(0xFFE2E6E3),
    surfaceVariant = Color(0xFF3F4943),
    onSurfaceVariant = Color(0xFFBFC9C3),
    surfaceContainerLowest = Color(0xFF0C0F0E),
    surfaceContainerLow = Color(0xFF161A18),
    surfaceContainer = Color(0xFF1A1F1D),
    surfaceContainerHigh = Color(0xFF242925),
    surfaceContainerHighest = Color(0xFF2E332F),
    outline = Color(0xFF89938D),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

val EsquemaClaro = lightColorScheme(
    primary = Color(0xFF1F6B47),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB8F3D2),
    onPrimaryContainer = Color(0xFF002111),
    secondary = Color(0xFF8A5A00),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDDAF),
    onSecondaryContainer = Color(0xFF2C1900),
    tertiary = Color(0xFF3F5F8A),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD5E3FF),
    onTertiaryContainer = Color(0xFF001B3C),
    background = Color(0xFFF5F8F6),
    onBackground = Color(0xFF191C1B),
    surface = Color(0xFFF5F8F6),
    onSurface = Color(0xFF191C1B),
    surfaceVariant = Color(0xFFDCE5DF),
    onSurfaceVariant = Color(0xFF404944),
    outline = Color(0xFF707974),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)
