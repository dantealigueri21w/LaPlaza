package pe.appmobile.laplaza.data.local

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
import pe.appmobile.laplaza.data.local.entity.BloqueContenidoEntity
import pe.appmobile.laplaza.data.local.entity.InsigniaEntity
import pe.appmobile.laplaza.data.local.entity.IntentoEntity
import pe.appmobile.laplaza.data.local.entity.PerfilEntity
import pe.appmobile.laplaza.data.local.entity.PregonEntity
import pe.appmobile.laplaza.data.local.entity.RachaEntity
import pe.appmobile.laplaza.data.local.entity.RinconEntity
import pe.appmobile.laplaza.data.local.entity.TemaEntity

/**
 * Pruebas de la capa Room contra una base en memoria (Robolectric, sin emulador).
 *
 * Room activa PRAGMA foreign_keys=ON por defecto, asi que un temaId o intentoId
 * "inventado" sin fila padre real fallaria con SQLiteConstraintException. Por eso
 * cada prueba que toca una entidad con foreign key crea primero una fila padre real
 * en vez de usar ids sueltos.
 *
 * @Config(sdk = [35]) fuerza el SDK simulado: el proyecto compila con targetSdk 37,
 * pero Robolectric 4.14 solo soporta hasta el 35 (sin esto, RobolectricTestRunner
 * falla al arrancar con "targetSdkVersion=37 > maxSdkVersion=35").
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppDatabaseTest {

    private lateinit var db: AppDatabase

    @Before
    fun crearBaseEnMemoria() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun cerrarBase() {
        db.close()
    }

    // ---------- PerfilDao ----------

    @Test
    fun `insertar y leer un perfil`() = runBlocking {
        db.perfilDao().insertar(PerfilEntity(alias = "Ana", avatarId = 1))

        val perfil = db.perfilDao().obtener().first()

        assertEquals("Ana", perfil?.alias)
    }

    @Test
    fun `actualizar perfil cambia sus preferencias de sonido y haptica`() = runBlocking {
        db.perfilDao().insertar(PerfilEntity(alias = "Luis", avatarId = 2))
        val guardado = db.perfilDao().obtener().first()!!

        db.perfilDao().actualizar(guardado.copy(sonidoActivado = false, hapticaActivada = false))

        val actualizado = db.perfilDao().obtener().first()
        assertEquals(false, actualizado?.sonidoActivado)
        assertEquals(false, actualizado?.hapticaActivada)
    }

    // ---------- RinconDao ----------

    @Test
    fun `insertarTodos siembra los ocho rincones y se leen todos ordenados`() = runBlocking {
        db.rinconDao().insertarTodos(rinconesDeEjemplo())

        val rincones = db.rinconDao().obtenerTodos().first()

        assertEquals(8, rincones.size)
        assertEquals("BALCON", rincones.first().id)
    }

    @Test
    fun `actualizarCompletado marca un rincon como completado`() = runBlocking {
        db.rinconDao().insertarTodos(rinconesDeEjemplo())

        db.rinconDao().actualizarCompletado("BALCON", true)

        assertEquals(true, db.rinconDao().obtenerPorId("BALCON")?.completado)
    }

    @Test
    fun `volver a sembrar rincones no borra un completado ya ganado`() = runBlocking {
        db.rinconDao().insertarTodos(rinconesDeEjemplo())
        db.rinconDao().actualizarCompletado("BALCON", true)

        db.rinconDao().insertarTodos(rinconesDeEjemplo()) // simula un reseed en un arranque posterior

        assertEquals(true, db.rinconDao().obtenerPorId("BALCON")?.completado)
    }

    // ---------- TemaDao + BloqueContenidoDao (encadenando foreign keys reales) ----------

    @Test
    fun `un tema referencia un rincon real y se lee por rinconId`() = runBlocking {
        db.rinconDao().insertarTodos(
            listOf(RinconEntity(id = "BALCON", nombre = "Balcón", descripcion = "El primer escenario", orden = 1))
        )
        db.temaDao().insertarTodos(
            listOf(TemaEntity(rinconId = "BALCON", titulo = "Preséntate a la plaza", dificultad = "FACIL", orden = 1))
        )

        val temas = db.temaDao().obtenerPorRincon("BALCON").first()

        assertEquals(1, temas.size)
        assertEquals("Preséntate a la plaza", temas.first().titulo)
    }

    @Test
    fun `los bloques de un tema se leen ordenados, referenciando un tema real`() = runBlocking {
        val temaId = crearTemaDePrueba(rinconId = "BALCON", titulo = "Preséntate a la plaza")

        db.bloqueContenidoDao().insertarTodos(
            listOf(
                BloqueContenidoEntity(temaId = temaId, franja = "CIERRE", texto = "Gracias, plaza", orden = 3),
                BloqueContenidoEntity(temaId = temaId, franja = "GANCHO", texto = "Buenos días, plaza", orden = 1),
                BloqueContenidoEntity(temaId = temaId, franja = "CUERPO", texto = "Me llamo...", orden = 2)
            )
        )

        val bloques = db.bloqueContenidoDao().obtenerPorTema(temaId)

        assertEquals(3, bloques.size)
        assertEquals("GANCHO", bloques[0].franja)
        assertEquals("CUERPO", bloques[1].franja)
        assertEquals("CIERRE", bloques[2].franja)
    }

    // ---------- IntentoDao ----------

    @Test
    fun `insertar un intento referenciando un tema real devuelve un id autogenerado`() = runBlocking {
        val temaId = crearTemaDePrueba(rinconId = "BALCON", titulo = "Tema A")

        val intentoId = db.intentoDao().insertar(intentoDeEjemplo(temaId, fechaEpochMs = 1000L))

        assertTrue(intentoId > 0)
    }

    @Test
    fun `obtenerRecientesPorTema ordena del intento mas nuevo al mas viejo`() = runBlocking {
        val temaId = crearTemaDePrueba(rinconId = "BALCON", titulo = "Tema A")
        // insertados fuera de orden cronologico a propósito
        db.intentoDao().insertar(intentoDeEjemplo(temaId, fechaEpochMs = 2000L))
        db.intentoDao().insertar(intentoDeEjemplo(temaId, fechaEpochMs = 5000L))
        db.intentoDao().insertar(intentoDeEjemplo(temaId, fechaEpochMs = 3000L))

        val recientes = db.intentoDao().obtenerRecientesPorTema(temaId, limite = 10)

        assertEquals(3, recientes.size)
        assertEquals(5000L, recientes[0].fechaEpochMs)
        assertEquals(3000L, recientes[1].fechaEpochMs)
        assertEquals(2000L, recientes[2].fechaEpochMs)
    }

    @Test
    fun `obtenerRecientesPorTema respeta el limite y no mezcla temas distintos`() = runBlocking {
        val temaA = crearTemaDePrueba(rinconId = "BALCON", titulo = "Tema A")
        val temaB = crearTemaDePrueba(rinconId = "KIOSCO", titulo = "Tema B")
        db.intentoDao().insertar(intentoDeEjemplo(temaA, fechaEpochMs = 1000L))
        db.intentoDao().insertar(intentoDeEjemplo(temaB, fechaEpochMs = 2000L))
        db.intentoDao().insertar(intentoDeEjemplo(temaB, fechaEpochMs = 3000L))
        db.intentoDao().insertar(intentoDeEjemplo(temaB, fechaEpochMs = 4000L))

        val recientes = db.intentoDao().obtenerRecientesPorTema(temaB, limite = 2)

        assertEquals(2, recientes.size)
        assertTrue(recientes.all { it.temaId == temaB })
        assertEquals(4000L, recientes[0].fechaEpochMs)
    }

    @Test
    fun `obtenerTodos de intentos se observa como Flow`() = runBlocking {
        val temaId = crearTemaDePrueba(rinconId = "BALCON", titulo = "Tema A")
        db.intentoDao().insertar(intentoDeEjemplo(temaId, fechaEpochMs = 1000L))
        db.intentoDao().insertar(intentoDeEjemplo(temaId, fechaEpochMs = 2000L))

        val todos = db.intentoDao().obtenerTodos().first()

        assertEquals(2, todos.size)
    }

    // ---------- PregonDao ----------

    @Test
    fun `insertar un pregon referenciando un intento real y leerlo`() = runBlocking {
        val temaId = crearTemaDePrueba(rinconId = "BALCON", titulo = "Tema A")
        val intentoId = db.intentoDao().insertar(intentoDeEjemplo(temaId, fechaEpochMs = 1000L))

        db.pregonDao().insertar(
            PregonEntity(
                intentoId = intentoId,
                titular = "Tu voz se escuchó en todo el Balcón",
                variableDestacada = "volumen",
                fechaEpochMs = 1000L
            )
        )

        val pregones = db.pregonDao().obtenerTodos().first()
        assertEquals(1, pregones.size)
        assertEquals("Tu voz se escuchó en todo el Balcón", pregones.first().titular)
    }

    @Test
    fun `el cuaderno de pregones lista del mas reciente al mas antiguo`() = runBlocking {
        val temaId = crearTemaDePrueba(rinconId = "BALCON", titulo = "Tema A")
        val intentoId = db.intentoDao().insertar(intentoDeEjemplo(temaId, fechaEpochMs = 1000L))
        db.pregonDao().insertar(
            PregonEntity(intentoId = intentoId, titular = "Pregón viejo", variableDestacada = "ritmo", fechaEpochMs = 1000L)
        )
        db.pregonDao().insertar(
            PregonEntity(intentoId = intentoId, titular = "Pregón nuevo", variableDestacada = "fluidez", fechaEpochMs = 9000L)
        )

        val pregones = db.pregonDao().obtenerTodos().first()

        assertEquals("Pregón nuevo", pregones[0].titular)
        assertEquals("Pregón viejo", pregones[1].titular)
    }

    // ---------- InsigniaDao ----------

    @Test
    fun `insertarTodas siembra insignias sin fecha de obtencion`() = runBlocking {
        db.insigniaDao().insertarTodas(
            listOf(InsigniaEntity(id = "PRIMER_PREGON", nombre = "Primer Pregón", descripcion = "Completa tu primer intento"))
        )

        val insignias = db.insigniaDao().obtenerTodas().first()

        assertEquals(1, insignias.size)
        assertNull(insignias.first().fechaObtenidaEpochMs)
    }

    @Test
    fun `marcarObtenida cambia fechaObtenidaEpochMs de null a un valor real`() = runBlocking {
        db.insigniaDao().insertarTodas(
            listOf(InsigniaEntity(id = "PRIMER_PREGON", nombre = "Primer Pregón", descripcion = "Completa tu primer intento"))
        )

        db.insigniaDao().marcarObtenida("PRIMER_PREGON", 123456L)

        val insignia = db.insigniaDao().obtenerTodas().first().first { it.id == "PRIMER_PREGON" }
        assertEquals(123456L, insignia.fechaObtenidaEpochMs)
    }

    // ---------- RachaDao ----------

    @Test
    fun `guardar racha con REPLACE actualiza la fila unica en vez de duplicarla`() = runBlocking {
        db.rachaDao().guardar(RachaEntity(diasSeguidos = 1, ultimoDiaEpoch = 100L))

        db.rachaDao().guardar(RachaEntity(diasSeguidos = 5, ultimoDiaEpoch = 500L))

        val racha = db.rachaDao().obtener().first()
        assertEquals(5, racha?.diasSeguidos)
        assertEquals(500L, racha?.ultimoDiaEpoch)
    }

    // ---------- helpers ----------

    private suspend fun crearTemaDePrueba(rinconId: String, titulo: String, orden: Int = 1): Long {
        db.rinconDao().insertarTodos(
            listOf(RinconEntity(id = rinconId, nombre = rinconId, descripcion = "rincon de prueba", orden = orden))
        )
        db.temaDao().insertarTodos(
            listOf(TemaEntity(rinconId = rinconId, titulo = titulo, dificultad = "FACIL", orden = orden))
        )
        return db.temaDao().obtenerPorRincon(rinconId).first().first { it.titulo == titulo }.id
    }

    private fun intentoDeEjemplo(temaId: Long, fechaEpochMs: Long): IntentoEntity = IntentoEntity(
        temaId = temaId,
        fechaEpochMs = fechaEpochMs,
        esRepaso = false,
        puntajeVolumen = 0.5f,
        puntajeEntonacion = 0.5f,
        puntajeRitmo = 0.5f,
        puntajeFluidez = 0.5f,
        puntajeCompuesto = 0.5f
    )

    private fun rinconesDeEjemplo(): List<RinconEntity> = listOf(
        RinconEntity(id = "BALCON", nombre = "Balcón", descripcion = "El primer escenario", orden = 1),
        RinconEntity(id = "KIOSCO", nombre = "Kiosco", descripcion = "Anuncios cortos", orden = 2),
        RinconEntity(id = "MOSTRADOR", nombre = "Mostrador", descripcion = "Pedir con buenas razones", orden = 3),
        RinconEntity(id = "JARDIN", nombre = "Jardín", descripcion = "Contar historias", orden = 4),
        RinconEntity(id = "FUENTE", nombre = "Fuente", descripcion = "Reflexionar en voz alta", orden = 5),
        RinconEntity(id = "MIRADOR", nombre = "Mirador", descripcion = "Ver el progreso", orden = 6),
        RinconEntity(id = "TARIMA_MAYOR", nombre = "Tarima Mayor", descripcion = "El gran escenario", orden = 7),
        RinconEntity(id = "LIBRE", nombre = "Libre", descripcion = "Práctica libre", orden = 8)
    )
}
