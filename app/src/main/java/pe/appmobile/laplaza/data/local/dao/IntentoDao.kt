package pe.appmobile.laplaza.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pe.appmobile.laplaza.data.local.entity.IntentoEntity

@Dao
interface IntentoDao {

    @Insert
    suspend fun insertar(intento: IntentoEntity): Long

    @Query("SELECT * FROM intento ORDER BY fechaEpochMs DESC")
    fun obtenerTodos(): Flow<List<IntentoEntity>>

    /** Consulta cruda para el repositorio: los ultimos [limite] intentos de UN tema,
     * del mas nuevo al mas viejo. El repositorio es quien arma, a partir de esto (y del
     * nombre del tema), el List<Triple<Long, String, Float>> que espera
     * MotorProgreso.sugerirRepaso — este DAO no conoce esa forma. */
    @Query("SELECT * FROM intento WHERE temaId = :temaId ORDER BY fechaEpochMs DESC LIMIT :limite")
    suspend fun obtenerRecientesPorTema(temaId: Long, limite: Int): List<IntentoEntity>

    /** Version "para toda la plaza" de la consulta anterior: los ultimos [limite] intentos
     * de CUALQUIER tema, con el titulo real via JOIN con tema (para no inventar el nombre
     * en el repositorio). Es la fuente real de MotorProgreso.sugerirRepaso: el repositorio
     * solo traduce cada fila a Triple(temaId, titulo, puntajeCompuesto). */
    @Query(
        """
        SELECT intento.temaId AS temaId, tema.titulo AS titulo, intento.puntajeCompuesto AS puntajeCompuesto
        FROM intento
        INNER JOIN tema ON tema.id = intento.temaId
        ORDER BY intento.fechaEpochMs DESC
        LIMIT :limite
        """
    )
    suspend fun obtenerRecientesConTema(limite: Int): List<IntentoConTema>

    /** Todo el historial de intentos, con el rinconId real del tema (via JOIN) -- la
     * fuente real de [pe.appmobile.laplaza.domain.engine.MotorInsignias.insigniasGanadas].
     * Sin limite ni orden: el motor evalua el conjunto completo cada vez, no le importa
     * el orden. */
    @Query(
        """
        SELECT intento.temaId AS temaId, tema.rinconId AS rinconId, intento.viaRinconLibre AS viaRinconLibre,
               intento.puntajeVolumen AS puntajeVolumen, intento.puntajeCompuesto AS puntajeCompuesto,
               intento.puntajeFluidez AS puntajeFluidez
        FROM intento
        INNER JOIN tema ON tema.id = intento.temaId
        """
    )
    suspend fun obtenerTodosParaInsignias(): List<IntentoParaInsigniasFila>
}

/** Proyeccion cruda de [obtenerTodosParaInsignias]: el repositorio la traduce a
 * [pe.appmobile.laplaza.domain.model.IntentoParaInsignias] (el motor de dominio no
 * conoce esta clase, vive en data/, no en domain/ -- seccion 8 del maestro). */
data class IntentoParaInsigniasFila(
    val temaId: Long,
    val rinconId: String,
    val viaRinconLibre: Boolean,
    val puntajeVolumen: Float,
    val puntajeCompuesto: Float,
    val puntajeFluidez: Float
)

/** Proyeccion de la consulta anterior: exactamente los tres campos que
 * MotorProgreso.sugerirRepaso necesita, ya con el titulo real del tema. */
data class IntentoConTema(
    val temaId: Long,
    val titulo: String,
    val puntajeCompuesto: Float
)
