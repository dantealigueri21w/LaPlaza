package pe.appmobile.laplaza.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import pe.appmobile.laplaza.ui.theme.LaPlazaTheme

/**
 * Prueba de humo de GloboDeDialogo: que componga sin reventar con la cola apuntando
 * hacia cualquiera de los dos lados, y que el texto de Chirri se muestre -- reemplaza a
 * AlertDialog, asi que no hay dialogo del sistema que probar aqui, solo la forma propia.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class GloboDeDialogoTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `GloboDeDialogo compone sin reventar con la cola a cualquiera de los dos lados`() {
        compose.setContent {
            LaPlazaTheme {
                Column {
                    GloboDeDialogo(texto = "Respira conmigo. Uno... dos... listo.", colaHaciaIzquierda = true)
                    GloboDeDialogo(texto = "¡Lo lograste!", colaHaciaIzquierda = false)
                }
            }
        }
        compose.waitForIdle()

        compose.onNodeWithText("Respira conmigo. Uno... dos... listo.").assertExists().assertIsDisplayed()
        compose.onNodeWithText("¡Lo lograste!").assertExists().assertIsDisplayed()
    }
}
