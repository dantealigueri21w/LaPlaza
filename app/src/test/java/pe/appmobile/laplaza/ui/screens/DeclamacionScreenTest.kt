package pe.appmobile.laplaza.ui.screens

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.down
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.up
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import pe.appmobile.laplaza.audio.EstadoCapturaEnVivo
import pe.appmobile.laplaza.audio.ResultadoEnVivo
import pe.appmobile.laplaza.domain.model.BloqueDiscurso
import pe.appmobile.laplaza.domain.model.DiscursoArmado
import pe.appmobile.laplaza.domain.model.Franja
import pe.appmobile.laplaza.domain.model.NivelAtencion
import pe.appmobile.laplaza.domain.model.Pregon
import pe.appmobile.laplaza.domain.model.PuntajeAudiencia
import pe.appmobile.laplaza.domain.model.ResultadoAcustico
import pe.appmobile.laplaza.ui.theme.LaPlazaTheme

/**
 * Pruebas de [ContenidoDeclamacion] -- la parte pura de la pantalla de declamacion, sin
 * Context ni AudioRecord real (ver el comentario grande de esa funcion). Cubren los 5
 * momentos reales del ciclo: el discurso se lee, "Hablar" arranca la captura, el ensayo
 * silencioso responde a un toque real (no solo existe en pantalla), el indicador en vivo
 * aparece mientras se escucha, y el pregon real + Chirri + "Continuar" cierran el ciclo.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w480dp-h1200dp")
class DeclamacionScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val temaId = 11L

    private fun discursoDeMentira() = DiscursoArmado(
        temaId = temaId,
        bloques = listOf(
            BloqueDiscurso(id = 1L, temaId = temaId, franja = Franja.GANCHO, texto = "Vengan a escuchar mi pregon"),
            BloqueDiscurso(id = 2L, temaId = temaId, franja = Franja.CUERPO, texto = "Esta es la parte central de mi historia"),
            BloqueDiscurso(id = 3L, temaId = temaId, franja = Franja.CIERRE, texto = "Y asi termina mi pregon de hoy")
        )
    )

    private fun resultadoEnVivoDeMentira(nivel: NivelAtencion = NivelAtencion.ATENTA) = ResultadoEnVivo(
        acustico = ResultadoAcustico(
            volumenPromedio = 0.6f,
            variacionEntonacionSemitonos = 3f,
            ritmoSilabasPorMinuto = 120f,
            pausas = emptyList(),
            duracionTotalMs = 3000L
        ),
        fluidez = 0.8f,
        puntaje = PuntajeAudiencia(0.6f, nivel)
    )

    @Test
    fun `muestra el discurso armado real, en orden gancho cuerpo cierre`() {
        compose.setContent {
            LaPlazaTheme {
                ContenidoDeclamacion(
                    discurso = discursoDeMentira(),
                    estadoPermiso = EstadoPermisoMicrofono.Concedido,
                    estadoCaptura = EstadoCapturaEnVivo.SinIniciar,
                    onSolicitarPermiso = {},
                    onEmpezar = {},
                    onTerminar = {},
                    onPresionarEnsayo = {},
                    onSoltarEnsayo = {},
                    onContinuar = {},
                    resultado = null
                )
            }
        }
        compose.waitForIdle()

        compose.onNodeWithText("Vengan a escuchar mi pregon").assertExists()
        compose.onNodeWithText("Esta es la parte central de mi historia").assertExists()
        compose.onNodeWithText("Y asi termina mi pregon de hoy").assertExists()
    }

    @Test
    fun `boton Hablar existe cuando no empezo y el permiso esta concedido, y arranca la captura`() {
        var empezoLaCaptura = false
        compose.setContent {
            LaPlazaTheme {
                ContenidoDeclamacion(
                    discurso = discursoDeMentira(),
                    estadoPermiso = EstadoPermisoMicrofono.Concedido,
                    estadoCaptura = EstadoCapturaEnVivo.SinIniciar,
                    onSolicitarPermiso = {},
                    onEmpezar = { empezoLaCaptura = true },
                    onTerminar = {},
                    onPresionarEnsayo = {},
                    onSoltarEnsayo = {},
                    onContinuar = {},
                    resultado = null
                )
            }
        }
        compose.waitForIdle()

        compose.onNodeWithText("Hablar").assertExists()
        compose.onNodeWithText("Hablar").performClick()
        compose.waitForIdle()

        assertTrue(empezoLaCaptura)
    }

    @Test
    fun `con el permiso denegado aparece el ensayo silencioso y presionar-soltar llama a los callbacks reales`() {
        var presiono = false
        var solto = false
        compose.setContent {
            LaPlazaTheme {
                ContenidoDeclamacion(
                    discurso = discursoDeMentira(),
                    estadoPermiso = EstadoPermisoMicrofono.Denegado,
                    estadoCaptura = EstadoCapturaEnVivo.SinIniciar,
                    onSolicitarPermiso = {},
                    onEmpezar = {},
                    onTerminar = {},
                    onPresionarEnsayo = { presiono = true },
                    onSoltarEnsayo = { solto = true },
                    onContinuar = {},
                    resultado = null
                )
            }
        }
        compose.waitForIdle()

        // No existe "Hablar": el permiso esta denegado, la unica accion es el circulo
        // de sostener.
        compose.onNodeWithText("Hablar").assertDoesNotExist()

        val nodoEnsayo = compose.onNodeWithContentDescription(
            "Mantén presionado mientras hablas, para practicar sin micrófono"
        )
        nodoEnsayo.assertExists()

        nodoEnsayo.performTouchInput { down(Offset(width / 2f, height / 2f)) }
        compose.waitForIdle()
        assertTrue("onPresionarEnsayo debia dispararse al bajar el dedo", presiono)
        assertFalse("onSoltarEnsayo NO debia dispararse todavia", solto)

        nodoEnsayo.performTouchInput { up() }
        compose.waitForIdle()
        assertTrue("onSoltarEnsayo debia dispararse al levantar el dedo", solto)
    }

    @Test
    fun `mientras escucha se muestra el indicador en vivo y el boton Terminar`() {
        compose.setContent {
            LaPlazaTheme {
                ContenidoDeclamacion(
                    discurso = discursoDeMentira(),
                    estadoPermiso = EstadoPermisoMicrofono.Concedido,
                    estadoCaptura = EstadoCapturaEnVivo.Escuchando(
                        umbralCalibrado = 0.02f,
                        resultado = resultadoEnVivoDeMentira(NivelAtencion.ATENTA)
                    ),
                    onSolicitarPermiso = {},
                    onEmpezar = {},
                    onTerminar = {},
                    onPresionarEnsayo = {},
                    onSoltarEnsayo = {},
                    onContinuar = {},
                    resultado = null
                )
            }
        }
        compose.waitForIdle()

        compose.onNodeWithContentDescription("La plaza te está escuchando con atención").assertExists()
        compose.onNodeWithText("Terminar").assertExists()
    }

    @Test
    fun `al terminar se muestra el pregon real, Chirri, y Continuar vuelve a la plaza`() {
        var continuo = false
        val pregonReal = Pregon(titular = "Tu voz se escucho en toda la plaza", variableDestacada = "volumen")
        compose.setContent {
            LaPlazaTheme {
                ContenidoDeclamacion(
                    discurso = discursoDeMentira(),
                    estadoPermiso = EstadoPermisoMicrofono.Concedido,
                    estadoCaptura = EstadoCapturaEnVivo.Detenido(
                        umbralCalibrado = 0.02f,
                        resultado = resultadoEnVivoDeMentira(NivelAtencion.ENTUSIASMADA)
                    ),
                    onSolicitarPermiso = {},
                    onEmpezar = {},
                    onTerminar = {},
                    onPresionarEnsayo = {},
                    onSoltarEnsayo = {},
                    onContinuar = { continuo = true },
                    resultado = pregonReal
                )
            }
        }
        compose.waitForIdle()

        compose.onNodeWithText("Tu voz se escucho en toda la plaza").assertExists()
        compose.onNodeWithText("Continuar").assertExists()
        compose.onNodeWithText("Continuar").performClick()
        compose.waitForIdle()

        assertTrue(continuo)
    }
}
