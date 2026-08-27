package pe.appmobile.laplaza.ui.components

import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
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

/**
 * Pruebas de BotonDePlaza: que componga sin reventar, que el tap responda, y sobre
 * todo que el objetivo de toque real (medido, no a ojo) sea de al menos 120dp -- la
 * regla de accesibilidad que la auditoria del equipo encontro ausente en 15 de 17 apps
 * hermanas.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class BotonDePlazaTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `BotonDePlaza compone sin reventar y responde al tap`() {
        var veces = 0
        compose.setContent {
            LaPlazaTheme {
                BotonDePlaza(label = "Hablar", onClick = { veces++ })
            }
        }
        compose.waitForIdle()

        val boton = compose.onNodeWithContentDescription("Hablar")
        boton.assertIsDisplayed()
        boton.performClick()
        compose.waitForIdle()

        assertEquals(1, veces)
    }

    @Test
    fun `BotonDePlaza deshabilitado no dispara onClick al tocarlo`() {
        var veces = 0
        compose.setContent {
            LaPlazaTheme {
                BotonDePlaza(label = "Hablar", onClick = { veces++ }, enabled = false)
            }
        }
        compose.waitForIdle()

        compose.onNodeWithContentDescription("Hablar").performClick()
        compose.waitForIdle()

        assertEquals(0, veces)
    }

    @Test
    fun `el objetivo de toque medido es de al menos 120dp de ancho y alto`() {
        compose.setContent {
            LaPlazaTheme {
                BotonDePlaza(label = "Hablar", onClick = {})
            }
        }
        compose.waitForIdle()

        compose.onNodeWithContentDescription("Hablar")
            .assertWidthIsAtLeast(120.dp)
            .assertHeightIsAtLeast(120.dp)
    }
}
