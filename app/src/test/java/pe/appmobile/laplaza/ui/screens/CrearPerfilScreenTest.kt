package pe.appmobile.laplaza.ui.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import pe.appmobile.laplaza.ui.theme.LaPlazaTheme

/**
 * Pruebas de CrearPerfilScreen: que componga con datos reales de UI (sin ViewModel, la
 * pantalla es una funcion pura de callbacks -- ver el propio composable), que el boton de
 * confirmar este deshabilitado sin alias, y que confirmar con datos reales dispare
 * [onCrear] con el alias y el avatarId realmente elegidos.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w480dp-h1200dp")
class CrearPerfilScreenTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `compone sin reventar y no crea perfil si no se eligio un alias`() {
        var alias: String? = null
        compose.setContent {
            LaPlazaTheme {
                CrearPerfilScreen(onCrear = { a, _ -> alias = a })
            }
        }
        compose.waitForIdle()

        compose.onNodeWithText("¡Listo, vamos a la plaza!").performClick()
        compose.waitForIdle()

        assertNull(alias)
    }

    @Test
    fun `escribir un alias y elegir un avatar y confirmar dispara onCrear con esos datos reales`() {
        var aliasCreado: String? = null
        var avatarCreado: Int? = null
        compose.setContent {
            LaPlazaTheme {
                CrearPerfilScreen(onCrear = { a, id -> aliasCreado = a; avatarCreado = id })
            }
        }
        compose.waitForIdle()

        compose.onNodeWithTag(TAG_CAMPO_ALIAS).performTextInput("Mateo")
        compose.onNodeWithContentDescription("Avatar 3").performClick()
        compose.onNodeWithText("¡Listo, vamos a la plaza!").performClick()
        compose.waitForIdle()

        assertEquals("Mateo", aliasCreado)
        assertEquals(3, avatarCreado)
    }
}
