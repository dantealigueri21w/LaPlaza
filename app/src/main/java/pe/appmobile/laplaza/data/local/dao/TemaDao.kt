package pe.appmobile.laplaza.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pe.appmobile.laplaza.data.local.entity.TemaEntity

@Dao
interface TemaDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarTodos(temas: List<TemaEntity>)

    @Query("SELECT * FROM tema WHERE rinconId = :rinconId ORDER BY orden")
    fun obtenerPorRincon(rinconId: String): Flow<List<TemaEntity>>

    @Query("SELECT * FROM tema WHERE id = :id")
    suspend fun obtenerPorId(id: Long): TemaEntity?
}
