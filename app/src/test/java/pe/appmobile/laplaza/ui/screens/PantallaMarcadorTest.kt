package pe.appmobile.laplaza.ui.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import pe.appmobile.laplaza.ui.theme.LaPlazaTheme

/**
 * Pruebas del destino marcador que usan, por ahora, los 7 rincones + Rincon Libre + el
 * Cuaderno de Pregones: que muestre el titulo real que le pasan (por ejemplo el nombre
 * real de un rincon, que viene de Room) y que "Volver" funcione.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PantallaMarcadorTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `compone mostrando el titulo real que recibe y volver dispara onVolver`() {
        var seVolvio = false
        compose.setContent {
            LaPlazaTheme {
                PantallaMarcador(titulo = "El Balcón", onVolver = { seVolvio = true })
            }
        }
        compose.waitForIdle()

        compose.onNodeWithText("El Balcón").assertExists()

        compose.onNodeWithText("Volver").performClick()
        compose.waitForIdle()

        assertTrue(seVolvio)
    }
}
