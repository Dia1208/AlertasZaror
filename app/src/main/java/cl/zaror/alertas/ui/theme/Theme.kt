package cl.zaror.alertas.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val Esquema = lightColorScheme(
    primary = Color(0xFFD32F2F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    secondary = Color(0xFF1E3A8A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCE6FF),
    tertiary = Color(0xFF2E7D32),
    background = Color(0xFFFAF7F2),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF1ECE4),
    errorContainer = Color(0xFFFFE3DF)
)

private val Formas = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp)
)

@Composable
fun ZarorTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Esquema, shapes = Formas, content = content)
}
