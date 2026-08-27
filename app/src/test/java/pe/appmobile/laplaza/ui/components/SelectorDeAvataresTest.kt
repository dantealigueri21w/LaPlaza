package pe.appmobile.laplaza.ui.components

import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import pe.appmobile.laplaza.ui.theme.LaPlazaTheme

/** Pruebas del selector de 8 avatares en cuadricula manual (chunked(4), nunca
 * LazyVerticalGrid -- seccion 7.1 punto 6 del maestro). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w480dp-h1200dp")
class SelectorDeAvataresTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `marca el avatar elegido en su descripcion de accesibilidad`() {
        compose.setContent {
            LaPlazaTheme {
                SelectorDeAvatares(seleccionado = 2, onSeleccionar = {})
            }
        }
        compose.waitForIdle()

        compose.onNodeWithContentDescription("Avatar 2, elegido").assertExists()
        compose.onNodeWithContentDescription("Avatar 5").assertExists()
    }

    @Test
    fun `tocar otro avatar dispara onSeleccionar con el id real tocado`() {
        var elegido: Int? = null
        compose.setContent {
            LaPlazaTheme {
                SelectorDeAvatares(seleccionado = 1, onSeleccionar = { elegido = it })
            }
        }
        compose.waitForIdle()

        compose.onNodeWithContentDescription("Avatar 7").performClick()
        compose.waitForIdle()

        assertEquals(7, elegido)
    }

    @Test
    fun `cada avatar tocable mide al menos 120dp`() {
        compose.setContent {
            LaPlazaTheme {
                SelectorDeAvatares(seleccionado = 1, onSeleccionar = {})
            }
        }
        compose.waitForIdle()

        compose.onNodeWithContentDescription("Avatar 1, elegido")
            .assertWidthIsAtLeast(120.dp)
            .assertHeightIsAtLeast(120.dp)
    }
}
