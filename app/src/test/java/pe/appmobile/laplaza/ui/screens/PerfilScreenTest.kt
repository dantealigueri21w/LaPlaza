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
import pe.appmobile.laplaza.data.local.entity.InsigniaEntity
import pe.appmobile.laplaza.data.local.entity.PerfilEntity
import pe.appmobile.laplaza.data.seed.SemillaInsignias
import pe.appmobile.laplaza.ui.theme.LaPlazaTheme

/**
 * Pruebas de PerfilScreen: que componga con un perfil real ya creado, que "Volver" no
 * intente guardar nada, y que cambiar de avatar y guardar dispare [onGuardar] con el
 * avatarId realmente elegido (no el original).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w480dp-h1200dp")
class PerfilScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val perfilDeMentira = PerfilEntity(id = 1, alias = "Mateo", avatarId = 1)

    @Test
    fun `compone mostrando el alias y el avatar reales del perfil`() {
        compose.setContent {
            LaPlazaTheme {
                PerfilScreen(perfil = perfilDeMentira, onGuardar = { _, _ -> }, onVolver = {})
            }
        }
        compose.waitForIdle()

        compose.onNodeWithText("Mateo").assertExists()
        compose.onNodeWithContentDescription("Avatar 1, elegido").assertExists()
    }

    @Test
    fun `volver no dispara onGuardar`() {
        var seGuardo = false
        compose.setContent {
            LaPlazaTheme {
                PerfilScreen(perfil = perfilDeMentira, onGuardar = { _, _ -> seGuardo = true }, onVolver = {})
            }
        }
        compose.waitForIdle()

        compose.onNodeWithText("Volver").performClick()
        compose.waitForIdle()

        assertTrue(!seGuardo)
    }

    @Test
    fun `elegir otro avatar y guardar dispara onGuardar con el avatar nuevo`() {
        var avatarGuardado: Int? = null
        compose.setContent {
            LaPlazaTheme {
                PerfilScreen(
                    perfil = perfilDeMentira,
                    onGuardar = { _, avatarId -> avatarGuardado = avatarId },
                    onVolver = {}
                )
            }
        }
        compose.waitForIdle()

        compose.onNodeWithContentDescription("Avatar 6").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Guardar").performClick()
        compose.waitForIdle()

        assertEquals(6, avatarGuardado)
    }

    // ---------- Mis Insignias ----------

    private fun insigniasDeMentira(ganadas: Set<String> = emptySet()) = SemillaInsignias.insignias.map { semilla ->
        semilla.copy(fechaObtenidaEpochMs = if (semilla.id in ganadas) 1000L else null)
    }

    @Test
    fun `muestra las 12 insignias reales de la ficha, con su nombre`() {
        compose.setContent {
            LaPlazaTheme {
                PerfilScreen(
                    perfil = perfilDeMentira,
                    insignias = insigniasDeMentira(),
                    onGuardar = { _, _ -> },
                    onVolver = {}
                )
            }
        }
        compose.waitForIdle()

        SemillaInsignias.insignias.forEach { insignia ->
            compose.onNodeWithText(insignia.nombre).assertExists()
        }
    }

    @Test
    fun `una insignia ganada y una por ganar tienen descripciones distintas, no solo distinto color`() {
        compose.setContent {
            LaPlazaTheme {
                PerfilScreen(
                    perfil = perfilDeMentira,
                    insignias = insigniasDeMentira(ganadas = setOf("PRIMERA_VOZ")),
                    onGuardar = { _, _ -> },
                    onVolver = {}
                )
            }
        }
        compose.waitForIdle()

        compose.onNodeWithContentDescription("Primera Voz, ganada", substring = true).assertExists()
        compose.onNodeWithContentDescription("Gancho que Prende, todavía por ganar", substring = true).assertExists()
    }
}
