package pe.appmobile.laplaza.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Un solo esquema claro: la ficha no define un tema oscuro. Blanco solo va sobre los
 * dos colores saturados (primario/secundario) e indigo profundo solo sobre los dos
 * colores claros (fondo/acento) -- es la propia tabla de contraste de la ficha: indigo
 * sobre primario o secundario da ~2.1-2.6:1 (no pasa 4.5:1), mientras que blanco sobre
 * esos mismos colores si pasa (5.55:1 y 5.79:1) e indigo sobre fondo o acento tambien
 * pasa (11.1:1 y 5.99:1). Por eso onPrimary/onSecondary son blancos y onTertiary es
 * indigo, nunca al reves.
 */
private val EsquemaClaro = lightColorScheme(
    primary = RosaBerenjena,
    onPrimary = Color.White,
    secondary = VioletaAtardecer,
    onSecondary = Color.White,
    tertiary = AmbarFarol,
    onTertiary = IndigoProfundo,
    background = BlancoRosado,
    onBackground = IndigoProfundo,
    surface = BlancoRosado,
    onSurface = IndigoProfundo
)

/** Tamanos explicitos en sp: nunca un tamano de texto sin unidad de accesibilidad real. */
private val TipografiaLaPlaza = Typography(
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 26.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 17.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 15.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
)

@Composable
fun LaPlazaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaClaro,
        typography = TipografiaLaPlaza,
        content = content
    )
}
