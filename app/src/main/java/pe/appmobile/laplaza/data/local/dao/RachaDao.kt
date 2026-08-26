package pe.appmobile.laplaza.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pe.appmobile.laplaza.data.local.entity.RachaEntity

@Dao
interface RachaDao {

    @Query("SELECT * FROM racha WHERE id = 1")
    fun obtener(): Flow<RachaEntity?>

    /** Upsert real: id siempre es 1, asi que REPLACE actualiza la unica fila en vez
     * de fallar por choque de clave primaria. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(racha: RachaEntity)
}
