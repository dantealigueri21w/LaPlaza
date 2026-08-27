package pe.appmobile.laplaza.ui

import android.os.Looper
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import pe.appmobile.laplaza.audio.ResultadoEnVivo
import pe.appmobile.laplaza.data.local.AppDatabase
import pe.appmobile.laplaza.data.repository.LaPlazaRepository
import pe.appmobile.laplaza.domain.model.DiscursoArmado
import pe.appmobile.laplaza.domain.model.NivelAtencion
import pe.appmobile.laplaza.domain.model.PuntajeAudiencia
import pe.appmobile.laplaza.domain.model.ResultadoAcustico

/**
 * Pruebas de LaPlazaViewModel contra una base Room real en memoria (mismo patron que
 * LaPlazaRepositoryTest). [esperarHasta] espera de verdad (reloj real, no un
 * TestDispatcher de tiempo virtual) a que el StateFlow refleje el resultado de una
 * corutina lanzada en viewModelScope -- Room ejecuta sus funciones suspend en su propio
 * executor, en un hilo real distinto al de la prueba, asi que hace falta esperar en
 * tiempo real (con una vuelta al looper de Robolectric por si acaso) en vez de asumir que
 * ya termino apenas se llama al metodo.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LaPlazaViewModelTest {

    private lateinit var db: AppDatabase
    private lateinit var repositorio: LaPlazaRepository

    @Before
    fun crearBaseYRepositorio() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repositorio = LaPlazaRepository(
            perfilDao = db.perfilDao(),
            rinconDao = db.rinconDao(),
            temaDao = db.temaDao(),
            bloqueContenidoDao = db.bloqueContenidoDao(),
            intentoDao = db.intentoDao(),
            pregonDao = db.pregonDao(),
            insigniaDao = db.insigniaDao(),
            rachaDao = db.rachaDao()
        )
    }

    @After
    fun cerrarBase() {
        db.close()
    }

    private fun esperarHasta(timeoutMs: Long = 5_000, condicion: () -> Boolean) {
        val limite = System.currentTimeMillis() + timeoutMs
        while (!condicion()) {
            check(System.currentTimeMillis() <= limite) { "Tiempo de espera agotado" }
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(10)
        }
    }

    @Test
    fun `al construirse siembra los 7 rincones y marca listoParaNavegar sin perfil creado`() {
        val viewModel = LaPlazaViewModel(repositorio)

        esperarHasta { viewModel.listoParaNavegar.value }

        assertNull(viewModel.perfil.value)
        assertEquals(7, viewModel.rincones.value.size)
    }

    @Test
    fun `crearPerfil guarda el perfil real y el StateFlow lo refleja`() {
        val viewModel = LaPlazaViewModel(repositorio)
        esperarHasta { viewModel.listoParaNavegar.value }

        runBlocking { viewModel.crearPerfil("Mateo", 3) }
        esperarHasta { viewModel.perfil.value != null }

        assertEquals("Mateo", viewModel.perfil.value?.alias)
        assertEquals(3, viewModel.perfil.value?.avatarId)
    }

    @Test
    fun `actualizarPerfil cambia sonidoActivado y el StateFlow lo refleja`() {
        val viewModel = LaPlazaViewModel(repositorio)
        esperarHasta { viewModel.listoParaNavegar.value }
        runBlocking { viewModel.crearPerfil("Ana", 1) }
        esperarHasta { viewModel.perfil.value != null }
        val actual = viewModel.perfil.value!!
        assertTrue(actual.sonidoActivado)

        runBlocking { viewModel.actualizarPerfil(actual.copy(sonidoActivado = false)) }
        esperarHasta { viewModel.perfil.value?.sonidoActivado == false }

        assertEquals(false, viewModel.perfil.value?.sonidoActivado)
    }

    @Test
    fun `un perfil ya existente antes de crear el ViewModel se refleja de inmediato`() {
        runBlocking { repositorio.crearPerfil("Luz", 5) }

        val viewModel = LaPlazaViewModel(repositorio)
        esperarHasta { viewModel.listoParaNavegar.value }

        assertEquals("Luz", viewModel.perfil.value?.alias)
    }

    private fun resultadoEnVivoDeMentira(puntajeCompuesto: Float = 0.5f) = ResultadoEnVivo(
        acustico = ResultadoAcustico(
            volumenPromedio = 0.6f,
            variacionEntonacionSemitonos = 3f,
            ritmoSilabasPorMinuto = 120f,
            pausas = emptyList(),
            duracionTotalMs = 3000L
        ),
        fluidez = 0.6f,
        puntaje = PuntajeAudiencia(puntajeCompuesto, NivelAtencion.ATENTA)
    )

    // ---------- finalizarDeclamacion ----------

    @Test
    fun `una declamacion real registra el intento, guarda el pregon en el Cuaderno y otorga Primera Voz`() {
        val viewModel = LaPlazaViewModel(repositorio)
        esperarHasta { viewModel.listoParaNavegar.value }
        val tema = runBlocking { viewModel.temasDe("BALCON") }.first { it.orden == 1 }
        val discurso = DiscursoArmado(temaId = tema.id, bloques = emptyList())

        val pregon = runBlocking {
            viewModel.finalizarDeclamacion(
                discurso = discurso,
                resultado = resultadoEnVivoDeMentira(),
                nombreTema = tema.titulo,
                nombreRincon = "El Balcón",
                esRepaso = false,
                viaRinconLibre = false
            )
        }

        esperarHasta { viewModel.pregones.value.isNotEmpty() }
        assertEquals(pregon.titular, viewModel.pregones.value.first().titular)

        val insignias = runBlocking { repositorio.obtenerInsignias().first() }
        assertTrue(insignias.first { it.id == "PRIMERA_VOZ" }.fechaObtenidaEpochMs != null)
    }

    @Test
    fun `una declamacion via Rincon Libre no entra al Cuaderno ni otorga Primera Voz, pero si devuelve un pregon para mostrar`() {
        val viewModel = LaPlazaViewModel(repositorio)
        esperarHasta { viewModel.listoParaNavegar.value }
        val tema = runBlocking { viewModel.temasDe("BALCON") }.first { it.orden == 1 }
        val discurso = DiscursoArmado(temaId = tema.id, bloques = emptyList())

        val pregon = runBlocking {
            viewModel.finalizarDeclamacion(
                discurso = discurso,
                resultado = resultadoEnVivoDeMentira(),
                nombreTema = tema.titulo,
                nombreRincon = "El Balcón",
                esRepaso = true,
                viaRinconLibre = true
            )
        }

        assertTrue(pregon.titular.isNotBlank())
        Thread.sleep(200)
        shadowOf(Looper.getMainLooper()).idle()
        assertTrue("el Cuaderno no debe recibir una entrada nueva via Rincon Libre", viewModel.pregones.value.isEmpty())

        val insignias = runBlocking { repositorio.obtenerInsignias().first() }
        assertFalse(insignias.first { it.id == "PRIMERA_VOZ" }.fechaObtenidaEpochMs != null)
    }
}
