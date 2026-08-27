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
import pe.appmobile.laplaza.data.local.entity.TemaEntity
import pe.appmobile.laplaza.domain.model.SugerenciaRepaso
import pe.appmobile.laplaza.ui.theme.LaPlazaTheme

/**
 * Pruebas de TemasDeRinconScreen con datos falsos reales -- funcion pura de datos +
 * callbacks, sin ViewModel ni Room (mismo patron que HomeScreenTest). Cubre: que
 * componga los 3 temas de un rincon real y sean tocables de verdad (una interaccion
 * real, no solo "no revienta"), y que la sugerencia de repaso de Rincon Libre aparezca
 * con su motivo real y tambien sea tocable.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w480dp-h1200dp")
class TemasDeRinconScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private fun temasDeMentira() = listOf(
        TemaEntity(id = 10L, rinconId = "BALCON", titulo = "Preséntate a la plaza", dificultad = "FACIL", orden = 1),
        TemaEntity(id = 11L, rinconId = "BALCON", titulo = "Cuenta tu comida favorita", dificultad = "MEDIO", orden = 2),
        TemaEntity(id = 12L, rinconId = "BALCON", titulo = "Convence de tu mejor cualidad", dificultad = "DIFICIL", orden = 3)
    )

    @Test
    fun `compone con 3 temas falsos y sugerencia nula, mostrando titulo y dificultad reales`() {
        compose.setContent {
            LaPlazaTheme {
                TemasDeRinconScreen(
                    tituloRincon = "El Balcón",
                    temas = temasDeMentira(),
                    sugerenciaRepaso = null,
                    onSeleccionarTema = {},
                    onVolver = {}
                )
            }
        }
        compose.waitForIdle()

        compose.onNodeWithText("El Balcón").assertExists()
        compose.onNodeWithContentDescription("Preséntate a la plaza, dificultad FACIL").assertExists()
        compose.onNodeWithContentDescription("Cuenta tu comida favorita, dificultad MEDIO").assertExists()
        compose.onNodeWithContentDescription("Convence de tu mejor cualidad, dificultad DIFICIL").assertExists()
    }

    @Test
    fun `tocar una tarjeta de tema dispara onSeleccionarTema con el id real de ese tema`() {
        var idSeleccionado: Long? = null
        compose.setContent {
            LaPlazaTheme {
                TemasDeRinconScreen(
                    tituloRincon = "El Balcón",
                    temas = temasDeMentira(),
                    sugerenciaRepaso = null,
                    onSeleccionarTema = { idSeleccionado = it },
                    onVolver = {}
                )
            }
        }
        compose.waitForIdle()

        compose.onNodeWithContentDescription("Cuenta tu comida favorita, dificultad MEDIO").performClick()
        compose.waitForIdle()

        assertEquals(11L, idSeleccionado)
    }

    @Test
    fun `con sugerencia de repaso no nula se muestra su motivo real y tambien es tocable`() {
        var idSeleccionado: Long? = null
        val sugerencia = SugerenciaRepaso(temaId = 99L, nombreTema = "El Kiosco", motivo = "Te falta practicar El Kiosco")
        compose.setContent {
            LaPlazaTheme {
                TemasDeRinconScreen(
                    tituloRincon = "Rincón Libre",
                    temas = temasDeMentira(),
                    sugerenciaRepaso = sugerencia,
                    onSeleccionarTema = { idSeleccionado = it },
                    onVolver = {}
                )
            }
        }
        compose.waitForIdle()

        compose.onNodeWithText("Te falta practicar El Kiosco").assertExists()

        compose.onNodeWithContentDescription("Chirri te sugiere: Te falta practicar El Kiosco").performClick()
        compose.waitForIdle()

        assertEquals(99L, idSeleccionado)
    }

    @Test
    fun `volver dispara onVolver`() {
        var seVolvio = false
        compose.setContent {
            LaPlazaTheme {
                TemasDeRinconScreen(
                    tituloRincon = "El Balcón",
                    temas = temasDeMentira(),
                    sugerenciaRepaso = null,
                    onSeleccionarTema = {},
                    onVolver = { seVolvio = true }
                )
            }
        }
        compose.waitForIdle()

        compose.onNodeWithContentDescription("Volver").performClick()
        compose.waitForIdle()

        assertTrue(seVolvio)
    }
}
