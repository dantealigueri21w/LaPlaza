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

    /** Insercion de uno solo: el sembrado necesita el id autogenerado real de CADA tema
     * (id = 0 antes de insertar) para poder insertar sus 9 bloques de contenido con el
     * temaId correcto, algo que insertarTodos (bulk, sin retorno) no permite. */
    @Insert
    suspend fun insertar(tema: TemaEntity): Long

    @Query("SELECT * FROM tema WHERE rinconId = :rinconId ORDER BY orden")
    fun obtenerPorRincon(rinconId: String): Flow<List<TemaEntity>>

    @Query("SELECT * FROM tema WHERE id = :id")
    suspend fun obtenerPorId(id: Long): TemaEntity?

    /** Los 21 temas de los 7 rincones juntos, para Rincon Libre: ahi el nino puede elegir
     * cualquier tema ya visto en cualquier rincon, no solo los de uno. */
    @Query("SELECT * FROM tema ORDER BY rinconId, orden")
    fun obtenerTodos(): Flow<List<TemaEntity>>
}
