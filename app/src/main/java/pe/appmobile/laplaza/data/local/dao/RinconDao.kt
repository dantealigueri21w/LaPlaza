package pe.appmobile.laplaza.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pe.appmobile.laplaza.data.local.entity.RinconEntity

@Dao
interface RinconDao {

    /** IGNORE a proposito: el id es fijo (nombre del enum), y sembrar de nuevo en cada
     * arranque no debe pisar el "completado" que ya gano el usuario. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarTodos(rincones: List<RinconEntity>)

    @Query("SELECT * FROM rincon ORDER BY orden")
    fun obtenerTodos(): Flow<List<RinconEntity>>

    @Query("SELECT * FROM rincon WHERE id = :id")
    suspend fun obtenerPorId(id: String): RinconEntity?

    @Query("UPDATE rincon SET completado = :completado WHERE id = :id")
    suspend fun actualizarCompletado(id: String, completado: Boolean)
}
