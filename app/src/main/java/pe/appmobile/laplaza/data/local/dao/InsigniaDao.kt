package pe.appmobile.laplaza.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pe.appmobile.laplaza.data.local.entity.InsigniaEntity

@Dao
interface InsigniaDao {

    /** IGNORE a proposito: el id es fijo por insignia. Sembrar de nuevo no debe borrar
     * la fechaObtenidaEpochMs de una insignia que el usuario ya se gano. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarTodas(insignias: List<InsigniaEntity>)

    @Query("SELECT * FROM insignia ORDER BY id")
    fun obtenerTodas(): Flow<List<InsigniaEntity>>

    /** Marca una insignia como recien ganada. */
    @Query("UPDATE insignia SET fechaObtenidaEpochMs = :fechaEpochMs WHERE id = :id")
    suspend fun marcarObtenida(id: String, fechaEpochMs: Long)
}
