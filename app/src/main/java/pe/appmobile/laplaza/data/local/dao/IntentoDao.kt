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
}
