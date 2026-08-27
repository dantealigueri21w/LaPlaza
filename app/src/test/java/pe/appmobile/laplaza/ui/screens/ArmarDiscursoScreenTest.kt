package pe.appmobile.laplaza.ui.screens

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import pe.appmobile.laplaza.data.local.entity.BloqueContenidoEntity
import pe.appmobile.laplaza.domain.engine.MotorDiscurso
import pe.appmobile.laplaza.domain.model.DiscursoArmado
import pe.appmobile.laplaza.domain.model.ResultadoValidacion
import pe.appmobile.laplaza.ui.theme.LaPlazaTheme

/**
 * Pruebas de ArmarDiscursoScreen -- el tablero real de armado del discurso, con
 * interacciones reales de toque en Robolectric (no solo "compone sin reventar"). Cubre:
 * que "Declamar" no exista hasta tener una seleccion en las 3 franjas, que elegir un
 * bloque distinto DENTRO de la misma franja reemplace la seleccion anterior (no la
 * sume), y que el DiscursoArmado que sale de onDeclamar pase MotorDiscurso.validar de
 * verdad -- el mismo motor de dominio que ya prueba MotorDiscursoTest, no una copia de
 * la regla aqui.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w480dp-h1200dp")
class ArmarDiscursoScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val temaId = 7L

    private fun bloquesDeMentira() = listOf(
        BloqueContenidoEntity(id = 1L, temaId = temaId, franja = "GANCHO", texto = "Gancho uno", orden = 1),
        BloqueContenidoEntity(id = 2L, temaId = temaId, franja = "GANCHO", texto = "Gancho dos", orden = 2),
        BloqueContenidoEntity(id = 3L, temaId = temaId, franja = "GANCHO", texto = "Gancho tres", orden = 3),
        BloqueContenidoEntity(id = 4L, temaId = temaId, franja = "CUERPO", texto = "Cuerpo uno", orden = 1),
        BloqueContenidoEntity(id = 5L, temaId = temaId, franja = "CUERPO", texto = "Cuerpo dos", orden = 2),
        BloqueContenidoEntity(id = 6L, temaId = temaId, franja = "CUERPO", texto = "Cuerpo tres", orden = 3),
        BloqueContenidoEntity(id = 7L, temaId = temaId, franja = "CIERRE", texto = "Cierre uno", orden = 1),
        BloqueContenidoEntity(id = 8L, temaId = temaId, franja = "CIERRE", texto = "Cierre dos", orden = 2),
        BloqueContenidoEntity(id = 9L, temaId = temaId, franja = "CIERRE", texto = "Cierre tres", orden = 3)
    )

    @Test
    fun `Declamar no existe todavia si falta seleccion en alguna franja`() {
        compose.setContent {
            LaPlazaTheme {
                ArmarDiscursoScreen(
                    tituloTema = "Preséntate a la plaza",
                    bloques = bloquesDeMentira(),
                    onDeclamar = {},
                    onVolver = {}
                )
            }
        }
        compose.waitForIdle()

        compose.onAllNodesWithText("Declamar").assertCountEquals(0)

        compose.onNodeWithText("Gancho uno").performClick()
        compose.waitForIdle()
        compose.onAllNodesWithText("Declamar").assertCountEquals(0)

        compose.onNodeWithText("Cuerpo uno").performClick()
        compose.waitForIdle()
        compose.onAllNodesWithText("Declamar").assertCountEquals(0)
    }

    @Test
    fun `elegir un bloque distinto en la misma franja reemplaza la seleccion, no la suma`() {
        var discursoRecibido: DiscursoArmado? = null
        compose.setContent {
            LaPlazaTheme {
                ArmarDiscursoScreen(
                    tituloTema = "Preséntate a la plaza",
                    bloques = bloquesDeMentira(),
                    onDeclamar = { discursoRecibido = it },
                    onVolver = {}
                )
            }
        }
        compose.waitForIdle()

        compose.onNodeWithText("Gancho uno").performClick()
        compose.waitForIdle()
        // Reemplaza la seleccion anterior en GANCHO: si sumara en vez de reemplazar,
        // el discurso final tendria 2 bloques de GANCHO y MotorDiscurso.validar lo
        // rechazaria (ver el assert final de esta misma prueba).
        compose.onNodeWithText("Gancho dos").performClick()
        compose.waitForIdle()

        compose.onNodeWithText("Cuerpo uno").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Cierre uno").performClick()
        compose.waitForIdle()

        compose.onNodeWithText("Declamar").assertExists()
        compose.onNodeWithText("Declamar").performClick()
        compose.waitForIdle()

        val discurso = discursoRecibido
        requireNotNull(discurso)
        assertEquals(3, discurso.bloques.size)
        assertEquals(listOf(2L, 4L, 7L), discurso.bloques.map { it.id })
        assertEquals(ResultadoValidacion.Valido, MotorDiscurso.validar(discurso))
    }

    @Test
    fun `seleccionar un bloque por cada franja habilita Declamar y produce un discurso valido de verdad`() {
        var discursoRecibido: DiscursoArmado? = null
        compose.setContent {
            LaPlazaTheme {
                ArmarDiscursoScreen(
                    tituloTema = "Preséntate a la plaza",
                    bloques = bloquesDeMentira(),
                    onDeclamar = { discursoRecibido = it },
                    onVolver = {}
                )
            }
        }
        compose.waitForIdle()

        compose.onAllNodesWithText("Declamar").assertCountEquals(0)

        compose.onNodeWithText("Gancho tres").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Cuerpo dos").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Cierre uno").performClick()
        compose.waitForIdle()

        compose.onNodeWithText("Declamar").assertExists()
        compose.onNodeWithText("Declamar").performClick()
        compose.waitForIdle()

        val discurso = discursoRecibido
        requireNotNull(discurso)
        assertEquals(temaId, discurso.temaId)
        assertEquals(listOf(3L, 5L, 7L), discurso.bloques.map { it.id })
        assertEquals(ResultadoValidacion.Valido, MotorDiscurso.validar(discurso))
    }
}
