package pe.appmobile.laplaza.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pe.appmobile.laplaza.data.local.entity.PerfilEntity

@Dao
interface PerfilDao {

    @Insert
    suspend fun insertar(perfil: PerfilEntity): Long

    /** Solo existe un perfil en la practica; si por algun motivo hubiera mas de uno,
     * se queda con el mas reciente. */
    @Query("SELECT * FROM perfil ORDER BY id DESC LIMIT 1")
    fun obtener(): Flow<PerfilEntity?>

    @Update
    suspend fun actualizar(perfil: PerfilEntity)
}
