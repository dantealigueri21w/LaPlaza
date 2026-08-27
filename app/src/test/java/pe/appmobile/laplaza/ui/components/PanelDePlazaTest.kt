package pe.appmobile.laplaza.ui.components

import androidx.compose.material3.Text
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
 * Prueba de humo de PanelDePlaza: que componga sin reventar con la forma irregular de
 * tablon/placa (GenericShape con Path real, no un Card con RoundedCornerShape) y que su
 * contenido se muestre.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PanelDePlazaTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `PanelDePlaza compone sin reventar y muestra su contenido`() {
        compose.setContent {
            LaPlazaTheme {
                PanelDePlaza {
                    Text("Un discurso de ejemplo")
                }
            }
        }
        compose.waitForIdle()

        compose.onNodeWithText("Un discurso de ejemplo").assertExists().assertIsDisplayed()
    }
}
