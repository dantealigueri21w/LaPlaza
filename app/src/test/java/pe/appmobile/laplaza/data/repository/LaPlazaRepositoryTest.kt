package pe.appmobile.laplaza.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import pe.appmobile.laplaza.data.local.AppDatabase
import pe.appmobile.laplaza.data.local.entity.RachaEntity
import pe.appmobile.laplaza.domain.model.Pregon

/**
 * Pruebas del repositorio contra una base Room real en memoria (Robolectric, sin emulador).
 *
 * @Config(sdk = [35]) fuerza el SDK simulado: el proyecto compila con targetSdk 37, pero
 * Robolectric 4.14 solo soporta hasta el 35 (mismo ajuste que AppDatabaseTest).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LaPlazaRepositoryTest {

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

    // ---------- sembrarSiEsNecesario ----------

    @Test
    fun `sembrarSiEsNecesario inserta los 7 rincones, 21 temas, 189 bloques y 12 insignias`() = runBlocking {
        repositorio.sembrarSiEsNecesario()

        val rincones = db.rinconDao().obtenerTodos().first()
        assertEquals(7, rincones.size)

        val temas = rincones.map { it.id }.flatMap { db.temaDao().obtenerPorRincon(it).first() }
        assertEquals(21, temas.size)

        var totalBloques = 0
        temas.forEach { tema -> totalBloques += db.bloqueContenidoDao().obtenerPorTema(tema.id).size }
        assertEquals(189, totalBloques)

        assertEquals(12, db.insigniaDao().obtenerTodas().first().size)
    }

    @Test
    fun `sembrarSiEsNecesario es idempotente, llamarlo dos veces no duplica nada`() = runBlocking {
        repositorio.sembrarSiEsNecesario()

        repositorio.sembrarSiEsNecesario() // simula un segundo arranque de la app

        val rincones = db.rinconDao().obtenerTodos().first()
        assertEquals(7, rincones.size)

        val temas = rincones.map { it.id }.flatMap { db.temaDao().obtenerPorRincon(it).first() }
        assertEquals(21, temas.size)

        var totalBloques = 0
        temas.forEach { tema -> totalBloques += db.bloqueContenidoDao().obtenerPorTema(tema.id).size }
        assertEquals(189, totalBloques)

        assertEquals(12, db.insigniaDao().obtenerTodas().first().size)
    }

    @Test
    fun `sembrar de nuevo no pisa un rincon que el usuario ya completo`() = runBlocking {
        repositorio.sembrarSiEsNecesario()
        repositorio.marcarRinconCompletado("BALCON")

        repositorio.sembrarSiEsNecesario() // segundo arranque: no debe tocar lo ya ganado

        assertEquals(true, db.rinconDao().obtenerPorId("BALCON")?.completado)
    }

    @Test
    fun `La Tarima Mayor queda sembrada como MEDIO-DIFICIL-DIFICIL, con sus 9 bloques cada tema`() = runBlocking {
        repositorio.sembrarSiEsNecesario()

        val temasTarima = db.temaDao().obtenerPorRincon("TARIMA_MAYOR").first().sortedBy { it.orden }
        assertEquals(listOf("MEDIO", "DIFICIL", "DIFICIL"), temasTarima.map { it.dificultad })

        temasTarima.forEach { tema ->
            val bloques = db.bloqueContenidoDao().obtenerPorTema(tema.id)
            assertEquals(9, bloques.size)
            assertEquals(3, bloques.count { it.franja == "GANCHO" })
            assertEquals(3, bloques.count { it.franja == "CUERPO" })
            assertEquals(3, bloques.count { it.franja == "CIERRE" })
        }
    }

    // ---------- sugerirRepaso: bisagra real Room -> MotorProgreso ----------

    @Test
    fun `sugerirRepaso elige el tema real con menor puntaje entre varios rincones`() = runBlocking {
        repositorio.sembrarSiEsNecesario()
        val temaBalcon = db.temaDao().obtenerPorRincon("BALCON").first().first { it.orden == 1 }
        val temaKiosco = db.temaDao().obtenerPorRincon("KIOSCO").first().first { it.orden == 1 }
        val temaMostrador = db.temaDao().obtenerPorRincon("MOSTRADOR").first().first { it.orden == 1 }

        // Tres intentos reales insertados vía el repositorio; Mostrador es claramente el peor.
        repositorio.registrarIntento(temaBalcon.id, false, 0.8f, 0.8f, 0.8f, 0.8f, 0.85f, fechaEpochMs = 1000L)
        repositorio.registrarIntento(temaKiosco.id, false, 0.7f, 0.7f, 0.7f, 0.7f, 0.72f, fechaEpochMs = 2000L)
        repositorio.registrarIntento(temaMostrador.id, false, 0.1f, 0.2f, 0.1f, 0.1f, 0.12f, fechaEpochMs = 3000L)

        val sugerencia = repositorio.sugerirRepaso()

        assertEquals(temaMostrador.id, sugerencia?.temaId)
        assertEquals(temaMostrador.titulo, sugerencia?.nombreTema)
    }

    @Test
    fun `sugerirRepaso cambia de tema sugerido cuando ese tema mejora y otro pasa a ser el peor`() = runBlocking {
        repositorio.sembrarSiEsNecesario()
        val temaBalcon = db.temaDao().obtenerPorRincon("BALCON").first().first { it.orden == 1 }
        val temaKiosco = db.temaDao().obtenerPorRincon("KIOSCO").first().first { it.orden == 1 }

        repositorio.registrarIntento(temaBalcon.id, false, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, fechaEpochMs = 1000L)
        repositorio.registrarIntento(temaKiosco.id, false, 0.9f, 0.9f, 0.9f, 0.9f, 0.9f, fechaEpochMs = 2000L)
        assertEquals(temaBalcon.id, repositorio.sugerirRepaso()?.temaId)

        // Un nuevo intento real y bueno en Balcon debe hacer que ahora Kiosco no aparezca
        // (ya no es el peor) y, tras un mal intento nuevo en Kiosco, que la sugerencia cambie.
        repositorio.registrarIntento(temaBalcon.id, true, 0.95f, 0.95f, 0.95f, 0.95f, 0.95f, fechaEpochMs = 3000L)
        repositorio.registrarIntento(temaKiosco.id, false, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, fechaEpochMs = 4000L)

        assertEquals(temaKiosco.id, repositorio.sugerirRepaso()?.temaId)
    }

    @Test
    fun `sugerirRepaso respeta el limite, dejando fuera intentos viejos`() = runBlocking {
        repositorio.sembrarSiEsNecesario()
        val temaBalcon = db.temaDao().obtenerPorRincon("BALCON").first().first { it.orden == 1 }
        val temaKiosco = db.temaDao().obtenerPorRincon("KIOSCO").first().first { it.orden == 1 }

        // Intento viejo y pesimo de Kiosco: fuera de la ventana con limite = 1.
        repositorio.registrarIntento(temaKiosco.id, false, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, fechaEpochMs = 100L)
        // Intento reciente y bueno de Balcon: el unico dentro de la ventana con limite = 1.
        repositorio.registrarIntento(temaBalcon.id, false, 0.9f, 0.9f, 0.9f, 0.9f, 0.9f, fechaEpochMs = 9000L)

        assertEquals(temaBalcon.id, repositorio.sugerirRepaso(limite = 1)?.temaId)
    }

    @Test
    fun `sugerirRepaso da null cuando todavia no hay intentos guardados`() = runBlocking {
        repositorio.sembrarSiEsNecesario()

        assertNull(repositorio.sugerirRepaso())
    }

    // ---------- Perfil ----------

    @Test
    fun `crearPerfil y obtenerPerfil devuelven el perfil guardado`() = runBlocking {
        repositorio.crearPerfil(alias = "Mateo", avatarId = 3)

        val perfil = repositorio.obtenerPerfil().first()

        assertEquals("Mateo", perfil?.alias)
        assertEquals(3, perfil?.avatarId)
    }

    @Test
    fun `actualizarPerfil cambia el alias de un perfil ya creado`() = runBlocking {
        repositorio.crearPerfil(alias = "Ana", avatarId = 1)
        val guardado = repositorio.obtenerPerfil().first()!!

        repositorio.actualizarPerfil(guardado.copy(alias = "Ana María"))

        assertEquals("Ana María", repositorio.obtenerPerfil().first()?.alias)
    }

    // ---------- Rincones y temas ----------

    @Test
    fun `marcarRinconCompletado marca completado en true`() = runBlocking {
        repositorio.sembrarSiEsNecesario()

        repositorio.marcarRinconCompletado("KIOSCO")

        assertEquals(true, db.rinconDao().obtenerPorId("KIOSCO")?.completado)
    }

    @Test
    fun `obtenerTema devuelve el tema real por su id autogenerado`() = runBlocking {
        repositorio.sembrarSiEsNecesario()
        val tema = db.temaDao().obtenerPorRincon("JARDIN").first().first { it.orden == 2 }

        val encontrado = repositorio.obtenerTema(tema.id)

        assertEquals("Reconoce a un amigo por algo que hizo bien", encontrado?.titulo)
    }

    // ---------- Bloques ----------

    @Test
    fun `obtenerBloquesDe devuelve los 9 bloques reales de un tema, con contenido verbatim`() = runBlocking {
        repositorio.sembrarSiEsNecesario()
        val tema = db.temaDao().obtenerPorRincon("BALCON").first().first { it.orden == 1 }

        val bloques = repositorio.obtenerBloquesDe(tema.id)

        assertEquals(9, bloques.size)
        assertEquals(3, bloques.count { it.franja == "GANCHO" })
        assertEquals(3, bloques.count { it.franja == "CUERPO" })
        assertEquals(3, bloques.count { it.franja == "CIERRE" })
        assertTrue(bloques.any { it.texto == "¡Hola, plaza! Soy nuevo por aquí y quiero que me conozcan." })
    }

    // ---------- Intentos ----------

    @Test
    fun `registrarIntento y obtenerIntentosRecientesDe devuelven los intentos de ese tema, mas nuevo primero`() = runBlocking {
        repositorio.sembrarSiEsNecesario()
        val tema = db.temaDao().obtenerPorRincon("BALCON").first().first { it.orden == 1 }

        repositorio.registrarIntento(tema.id, false, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, fechaEpochMs = 1000L)
        repositorio.registrarIntento(tema.id, true, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f, fechaEpochMs = 2000L)

        val recientes = repositorio.obtenerIntentosRecientesDe(tema.id)

        assertEquals(2, recientes.size)
        assertEquals(2000L, recientes.first().fechaEpochMs)
    }

    // ---------- Pregones ----------

    @Test
    fun `guardarPregon asocia el pregon al intento real y aparece en el cuaderno`() = runBlocking {
        repositorio.sembrarSiEsNecesario()
        val tema = db.temaDao().obtenerPorRincon("BALCON").first().first { it.orden == 1 }
        val intentoId = repositorio.registrarIntento(tema.id, false, 0.9f, 0.5f, 0.5f, 0.5f, 0.6f, fechaEpochMs = 1000L)

        repositorio.guardarPregon(
            intentoId = intentoId,
            pregon = Pregon(titular = "Tu voz se escuchó en todo El Balcón", variableDestacada = "volumen"),
            fechaEpochMs = 1000L
        )

        val pregones = repositorio.obtenerPregones().first()
        assertEquals(1, pregones.size)
        assertEquals(intentoId, pregones.first().intentoId)
        assertEquals("Tu voz se escuchó en todo El Balcón", pregones.first().titular)
        assertEquals("volumen", pregones.first().variableDestacada)
    }

    // ---------- Insignias ----------

    @Test
    fun `marcarInsigniaGanada cambia la fecha de obtencion de una insignia sembrada`() = runBlocking {
        repositorio.sembrarSiEsNecesario()

        repositorio.marcarInsigniaGanada("PRIMERA_VOZ", fechaEpochMs = 555L)

        val insignia = repositorio.obtenerInsignias().first().first { it.id == "PRIMERA_VOZ" }
        assertEquals(555L, insignia.fechaObtenidaEpochMs)
    }

    // ---------- Racha ----------

    @Test
    fun `actualizarRacha guarda y obtenerRacha lee la racha actual`() = runBlocking {
        repositorio.actualizarRacha(RachaEntity(diasSeguidos = 4, ultimoDiaEpoch = 20L))

        val racha = repositorio.obtenerRacha().first()

        assertEquals(4, racha?.diasSeguidos)
        assertEquals(20L, racha?.ultimoDiaEpoch)
    }
}
