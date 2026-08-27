package pe.appmobile.laplaza.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Verifica el mapeo exacto de la paleta de la ficha a los slots de Material3: blanco
 * solo sobre los colores saturados (primario/secundario), indigo profundo solo sobre
 * los colores claros (fondo/acento) -- la regla fija de 24-LA-PLAZA.md, seccion Paleta.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LaPlazaThemeTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `LaPlazaTheme mapea cada color de la ficha al slot de Material3 correcto`() {
        var esquema: androidx.compose.material3.ColorScheme? = null
        compose.setContent {
            LaPlazaTheme {
                esquema = MaterialTheme.colorScheme
            }
        }
        compose.waitForIdle()

        val colores = requireNotNull(esquema)
        assertEquals(RosaBerenjena, colores.primary)
        assertEquals(Color.White, colores.onPrimary)
        assertEquals(VioletaAtardecer, colores.secondary)
        assertEquals(Color.White, colores.onSecondary)
        assertEquals(AmbarFarol, colores.tertiary)
        assertEquals(IndigoProfundo, colores.onTertiary)
        assertEquals(BlancoRosado, colores.background)
        assertEquals(IndigoProfundo, colores.onBackground)
        assertEquals(BlancoRosado, colores.surface)
        assertEquals(IndigoProfundo, colores.onSurface)
    }
}
