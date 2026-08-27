package pe.appmobile.laplaza.ui.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import pe.appmobile.laplaza.ui.theme.LaPlazaTheme

/** Pruebas de AjustesScreen: que componga, que tocar el switch de sonido dispare
 * [onCambiarSonido] con el valor real invertido, y que "Volver" no lo dispare. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AjustesScreenTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `compone mostrando el estado real del sonido`() {
        compose.setContent {
            LaPlazaTheme {
                AjustesScreen(sonidoActivado = true, onCambiarSonido = {}, onVolver = {})
            }
        }
        compose.waitForIdle()

        compose.onNodeWithText("Ajustes").assertExists()
        compose.onNodeWithText("Sonido").assertExists()
    }

    @Test
    fun `tocar el switch de sonido dispara onCambiarSonido con el valor invertido`() {
        var nuevoValor: Boolean? = null
        compose.setContent {
            LaPlazaTheme {
                AjustesScreen(sonidoActivado = true, onCambiarSonido = { nuevoValor = it }, onVolver = {})
            }
        }
        compose.waitForIdle()

        compose.onNodeWithContentDescription("Sonido").performClick()
        compose.waitForIdle()

        assertEquals(false, nuevoValor)
    }

    @Test
    fun `volver no cambia el sonido`() {
        var seToco = false
        compose.setContent {
            LaPlazaTheme {
                AjustesScreen(sonidoActivado = true, onCambiarSonido = { seToco = true }, onVolver = {})
            }
        }
        compose.waitForIdle()

        compose.onNodeWithText("Volver").performClick()
        compose.waitForIdle()

        assertTrue(!seToco)
    }
}
