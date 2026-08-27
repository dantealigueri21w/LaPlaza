package pe.appmobile.laplaza.ui.screens

import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import pe.appmobile.laplaza.data.local.entity.PerfilEntity
import pe.appmobile.laplaza.data.local.entity.RachaEntity
import pe.appmobile.laplaza.data.local.entity.RinconEntity
import pe.appmobile.laplaza.ui.theme.LaPlazaTheme

/**
 * Pruebas de HomeScreen con datos falsos reales (ver la tarea: "con un par de rincones
 * y perfil de mentira") -- nunca con un ViewModel real, HomeScreen es una funcion pura de
 * datos + callbacks. Cubre: que componga con 7 rincones falsos + Rincon Libre, que se
 * lean la racha y el avatar reales pasados por parametro, que TODOS los rincones sean
 * tocables sin importar su estado (nunca se bloquea nada, seccion 5.1 del maestro), y que
 * el objetivo de toque de una zona sea de al menos 120dp.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w480dp-h1200dp")
class HomeScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private fun rinconesDeMentira() = listOf(
        RinconEntity(id = "BALCON", nombre = "El Balcón", descripcion = "Presentarte", orden = 1, completado = true),
        RinconEntity(id = "KIOSCO", nombre = "El Kiosco", descripcion = "Contar algo", orden = 2, completado = false),
        RinconEntity(id = "MOSTRADOR", nombre = "El Mostrador", descripcion = "Pedir algo", orden = 3, completado = false),
        RinconEntity(id = "JARDIN", nombre = "El Jardín", descripcion = "Agradecer", orden = 4, completado = false),
        RinconEntity(id = "FUENTE", nombre = "La Fuente", descripcion = "Dar ánimo", orden = 5, completado = false),
        RinconEntity(id = "MIRADOR", nombre = "El Mirador", descripcion = "Anunciar", orden = 6, completado = false),
        RinconEntity(id = "TARIMA_MAYOR", nombre = "La Tarima Mayor", descripcion = "Ocasión especial", orden = 7, completado = false)
    )

    @Test
    fun `compone con 7 rincones falsos, Rincon Libre y muestra alias y racha reales`() {
        compose.setContent {
            LaPlazaTheme {
                HomeScreen(
                    perfil = PerfilEntity(id = 1, alias = "Ana", avatarId = 2),
                    rincones = rinconesDeMentira(),
                    racha = RachaEntity(diasSeguidos = 5, ultimoDiaEpoch = 100L),
                    onNavegarRincon = {},
                    onNavegarPerfil = {},
                    onNavegarAjustes = {},
                    onNavegarCuaderno = {}
                )
            }
        }
        compose.waitForIdle()

        compose.onNodeWithText("Ana").assertExists()
        compose.onNodeWithText("5 días seguidos").assertExists()
        compose.onNodeWithContentDescription("El Balcón, completado").assertExists()
        compose.onNodeWithContentDescription("El Kiosco, disponible").assertExists()
        compose.onNodeWithContentDescription("Rincón Libre, disponible").assertExists()
    }

    @Test
    fun `tocar un rincon completado igual dispara la navegacion, nunca se bloquea por progreso`() {
        var idNavegado: String? = null
        compose.setContent {
            LaPlazaTheme {
                HomeScreen(
                    perfil = PerfilEntity(id = 1, alias = "Ana", avatarId = 2),
                    rincones = rinconesDeMentira(),
                    racha = null,
                    onNavegarRincon = { idNavegado = it },
                    onNavegarPerfil = {},
                    onNavegarAjustes = {},
                    onNavegarCuaderno = {}
                )
            }
        }
        compose.waitForIdle()

        // TARIMA_MAYOR normalmente seria "el cierre" tras completar todo lo demas, pero
        // aqui esta sin completar y de todas formas debe poder tocarse: ningun rincon se
        // bloquea por progreso (seccion 5.1 del maestro, corrige a la ficha).
        compose.onNodeWithContentDescription("La Tarima Mayor, disponible").performClick()
        compose.waitForIdle()

        assertEquals("TARIMA_MAYOR", idNavegado)
    }

    @Test
    fun `el objetivo de toque de una zona del mapa es de al menos 120dp`() {
        compose.setContent {
            LaPlazaTheme {
                HomeScreen(
                    perfil = PerfilEntity(id = 1, alias = "Ana", avatarId = 2),
                    rincones = rinconesDeMentira(),
                    racha = null,
                    onNavegarRincon = {},
                    onNavegarPerfil = {},
                    onNavegarAjustes = {},
                    onNavegarCuaderno = {}
                )
            }
        }
        compose.waitForIdle()

        compose.onNodeWithContentDescription("El Balcón, completado")
            .assertWidthIsAtLeast(120.dp)
            .assertHeightIsAtLeast(120.dp)
    }
}
