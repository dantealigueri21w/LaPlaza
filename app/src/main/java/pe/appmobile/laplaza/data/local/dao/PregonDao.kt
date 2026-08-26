package pe.appmobile.laplaza.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pe.appmobile.laplaza.data.local.entity.PregonEntity

@Dao
interface PregonDao {

    @Insert
    suspend fun insertar(pregon: PregonEntity): Long

    /** El "Cuaderno de Pregones": lista coleccionable, del titular mas reciente al mas antiguo. */
    @Query("SELECT * FROM pregon ORDER BY fechaEpochMs DESC")
    fun obtenerTodos(): Flow<List<PregonEntity>>
}
