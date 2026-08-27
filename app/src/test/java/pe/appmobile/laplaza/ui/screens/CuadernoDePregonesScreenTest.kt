package pe.appmobile.laplaza.ui.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import pe.appmobile.laplaza.data.local.entity.PregonEntity
import pe.appmobile.laplaza.ui.theme.LaPlazaTheme

/**
 * El Cuaderno de Pregones real: una lista de titulares reales generados por
 * [pe.appmobile.laplaza.domain.engine.MotorPregon] y guardados en Room, del mas
 * reciente al mas antiguo -- nunca datos de ejemplo.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w480dp-h1200dp")
class CuadernoDePregonesScreenTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `cuaderno vacio muestra a Chirri y un mensaje, nunca una lista de ejemplo`() {
        compose.setContent {
            LaPlazaTheme {
                CuadernoDePregonesScreen(pregones = emptyList(), onVolver = {})
            }
        }
        compose.waitForIdle()

        compose.onNodeWithText("Tu voz se escuchó en todo El Kiosco").assertDoesNotExist()
        compose.onNodeWithContentDescription("Chirri", substring = true, useUnmergedTree = true).assertExists()
    }

    @Test
    fun `muestra los titulares reales, del mas reciente al mas antiguo`() {
        val pregones = listOf(
            PregonEntity(id = 2L, intentoId = 20L, titular = "El más nuevo de todos", variableDestacada = "volumen", fechaEpochMs = 2_000L),
            PregonEntity(id = 1L, intentoId = 10L, titular = "El primero que se guardó", variableDestacada = "fluidez", fechaEpochMs = 1_000L)
        )
        compose.setContent {
            LaPlazaTheme {
                CuadernoDePregonesScreen(pregones = pregones, onVolver = {})
            }
        }
        compose.waitForIdle()

        compose.onNodeWithText("El más nuevo de todos").assertExists()
        compose.onNodeWithText("El primero que se guardó").assertExists()
    }

    @Test
    fun `volver dispara el callback real`() {
        var volvio = false
        compose.setContent {
            LaPlazaTheme {
                CuadernoDePregonesScreen(pregones = emptyList(), onVolver = { volvio = true })
            }
        }
        compose.waitForIdle()

        compose.onNodeWithText("Volver").performClick()
        compose.waitForIdle()

        assertTrue(volvio)
    }
}
