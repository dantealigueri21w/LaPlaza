package pe.appmobile.laplaza.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import pe.appmobile.laplaza.data.local.dao.BloqueContenidoDao
import pe.appmobile.laplaza.data.local.dao.InsigniaDao
import pe.appmobile.laplaza.data.local.dao.IntentoDao
import pe.appmobile.laplaza.data.local.dao.PerfilDao
import pe.appmobile.laplaza.data.local.dao.PregonDao
import pe.appmobile.laplaza.data.local.dao.RachaDao
import pe.appmobile.laplaza.data.local.dao.RinconDao
import pe.appmobile.laplaza.data.local.dao.TemaDao
import pe.appmobile.laplaza.data.local.entity.BloqueContenidoEntity
import pe.appmobile.laplaza.data.local.entity.InsigniaEntity
import pe.appmobile.laplaza.data.local.entity.IntentoEntity
import pe.appmobile.laplaza.data.local.entity.PerfilEntity
import pe.appmobile.laplaza.data.local.entity.PregonEntity
import pe.appmobile.laplaza.data.local.entity.RachaEntity
import pe.appmobile.laplaza.data.local.entity.RinconEntity
import pe.appmobile.laplaza.data.local.entity.TemaEntity
import pe.appmobile.laplaza.data.seed.SemillaBloques
import pe.appmobile.laplaza.data.seed.SemillaInsignias
import pe.appmobile.laplaza.data.seed.SemillaRincones
import pe.appmobile.laplaza.data.seed.SemillaTemas
import pe.appmobile.laplaza.domain.engine.MotorProgreso
import pe.appmobile.laplaza.domain.model.Pregon
import pe.appmobile.laplaza.domain.model.SugerenciaRepaso

/**
 * Punto único de acceso a los datos de La Plaza. Envuelve los 8 DAOs de Room y, cuando
 * corresponde, traduce sus filas a los tipos que esperan los motores de dominio (por
 * ejemplo [MotorProgreso.sugerirRepaso]) en vez de duplicar esa lógica aquí.
 *
 * Construcción manual, sin framework de inyección de dependencias (este proyecto no usa
 * Hilt ni Koin): quien arme la app (Application/Activity) crea el AppDatabase y pasa sus
 * 8 DAOs al constructor.
 */
class LaPlazaRepository(
    private val perfilDao: PerfilDao,
    private val rinconDao: RinconDao,
    private val temaDao: TemaDao,
    private val bloqueContenidoDao: BloqueContenidoDao,
    private val intentoDao: IntentoDao,
    private val pregonDao: PregonDao,
    private val insigniaDao: InsigniaDao,
    private val rachaDao: RachaDao
) {

    // ---------- Semilla ----------

    /**
     * Siembra los 7 rincones, los 21 temas (con sus 189 bloques de contenido) y las 12
     * insignias en el primer arranque de la app.
     *
     * Idempotente a propósito: si `rincon` ya tiene filas, no hace nada más. Esa es la
     * única compuerta -no basta con IGNORE en cada insert, porque TemaEntity.id es
     * autogenerado: reinsertar temas en cada arranque duplicaría los 21 temas (y sus 189
     * bloques) al no chocar nunca contra una primary key ya usada.
     */
    suspend fun sembrarSiEsNecesario() {
        val yaHaySemilla = rinconDao.obtenerTodos().first().isNotEmpty()
        if (yaHaySemilla) return

        rinconDao.insertarTodos(SemillaRincones.rincones)

        SemillaTemas.temas.forEach { temaSemilla ->
            val temaId = temaDao.insertar(
                TemaEntity(
                    rinconId = temaSemilla.rinconId,
                    titulo = temaSemilla.titulo,
                    dificultad = temaSemilla.dificultad,
                    orden = temaSemilla.orden
                )
            )
            val bloques = SemillaBloques.porTema.getValue(temaSemilla.clave).map { bloqueSemilla ->
                BloqueContenidoEntity(
                    temaId = temaId,
                    franja = bloqueSemilla.franja,
                    texto = bloqueSemilla.texto,
                    orden = bloqueSemilla.orden
                )
            }
            bloqueContenidoDao.insertarTodos(bloques)
        }

        insigniaDao.insertarTodas(SemillaInsignias.insignias)
    }

    // ---------- Perfil ----------

    fun obtenerPerfil(): Flow<PerfilEntity?> = perfilDao.obtener()

    suspend fun crearPerfil(alias: String, avatarId: Int): Long =
        perfilDao.insertar(PerfilEntity(alias = alias, avatarId = avatarId))

    suspend fun actualizarPerfil(perfil: PerfilEntity) = perfilDao.actualizar(perfil)

    // ---------- Rincones ----------

    fun obtenerRincones(): Flow<List<RinconEntity>> = rinconDao.obtenerTodos()

    suspend fun marcarRinconCompletado(id: String) = rinconDao.actualizarCompletado(id, true)

    // ---------- Temas ----------

    fun obtenerTemasDe(rinconId: String): Flow<List<TemaEntity>> = temaDao.obtenerPorRincon(rinconId)

    /** Los 21 temas de los 7 rincones juntos, para Rincon Libre (ver [obtenerTemasDe]). */
    fun obtenerTodosLosTemas(): Flow<List<TemaEntity>> = temaDao.obtenerTodos()

    suspend fun obtenerTema(id: Long): TemaEntity? = temaDao.obtenerPorId(id)

    // ---------- Bloques de contenido ----------

    /** Contenido estático ya armado (gancho/cuerpo/cierre) de un tema, ordenado dentro
     * de cada franja: se lee una vez para construir el tablero de armado del discurso. */
    suspend fun obtenerBloquesDe(temaId: Long): List<BloqueContenidoEntity> =
        bloqueContenidoDao.obtenerPorTema(temaId)

    // ---------- Intentos ----------

    /**
     * Registra una declamación real y devuelve el id autogenerado del intento: lo
     * necesita [guardarPregon] para asociar el pregón que Chirri genera con el intento
     * del que salió.
     */
    suspend fun registrarIntento(
        temaId: Long,
        esRepaso: Boolean,
        puntajeVolumen: Float,
        puntajeEntonacion: Float,
        puntajeRitmo: Float,
        puntajeFluidez: Float,
        puntajeCompuesto: Float,
        fechaEpochMs: Long = System.currentTimeMillis()
    ): Long = intentoDao.insertar(
        IntentoEntity(
            temaId = temaId,
            fechaEpochMs = fechaEpochMs,
            esRepaso = esRepaso,
            puntajeVolumen = puntajeVolumen,
            puntajeEntonacion = puntajeEntonacion,
            puntajeRitmo = puntajeRitmo,
            puntajeFluidez = puntajeFluidez,
            puntajeCompuesto = puntajeCompuesto
        )
    )

    suspend fun obtenerIntentosRecientesDe(temaId: Long, limite: Int = 10): List<IntentoEntity> =
        intentoDao.obtenerRecientesPorTema(temaId, limite)

    // ---------- Progreso: sugerencia de repaso ----------

    /**
     * Bisagra entre Room y [MotorProgreso.sugerirRepaso]: lee los últimos [limite]
     * intentos de CUALQUIER tema (con su título real, vía JOIN en el DAO), arma el
     * `List<Triple<temaId, tituloTema, puntajeCompuesto>>` que el motor de dominio espera,
     * y devuelve su sugerencia real -nunca un tema inventado. Si todavía no hay intentos
     * guardados, el motor recibe una lista vacía y ya sabe devolver null en ese caso.
     */
    suspend fun sugerirRepaso(limite: Int = 20): SugerenciaRepaso? {
        val paraElMotor = intentoDao.obtenerRecientesConTema(limite)
            .map { Triple(it.temaId, it.titulo, it.puntajeCompuesto) }
        return MotorProgreso.sugerirRepaso(paraElMotor)
    }

    // ---------- Pregones (Cuaderno de Pregones) ----------

    /** Guarda un pregón ya generado por MotorPregon.generar, asociado al intento real
     * del que salió. */
    suspend fun guardarPregon(
        intentoId: Long,
        pregon: Pregon,
        fechaEpochMs: Long = System.currentTimeMillis()
    ): Long = pregonDao.insertar(
        PregonEntity(
            intentoId = intentoId,
            titular = pregon.titular,
            variableDestacada = pregon.variableDestacada,
            fechaEpochMs = fechaEpochMs
        )
    )

    fun obtenerPregones(): Flow<List<PregonEntity>> = pregonDao.obtenerTodos()

    // ---------- Insignias ----------

    fun obtenerInsignias(): Flow<List<InsigniaEntity>> = insigniaDao.obtenerTodas()

    suspend fun marcarInsigniaGanada(id: String, fechaEpochMs: Long = System.currentTimeMillis()) =
        insigniaDao.marcarObtenida(id, fechaEpochMs)

    // ---------- Racha ----------

    fun obtenerRacha(): Flow<RachaEntity?> = rachaDao.obtener()

    suspend fun actualizarRacha(racha: RachaEntity) = rachaDao.guardar(racha)
}
