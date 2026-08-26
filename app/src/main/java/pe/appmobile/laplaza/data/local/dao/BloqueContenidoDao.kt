package pe.appmobile.laplaza.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import pe.appmobile.laplaza.data.local.entity.BloqueContenidoEntity

@Dao
interface BloqueContenidoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarTodos(bloques: List<BloqueContenidoEntity>)

    /** Los bloques de un tema son contenido estatico ya armado (gancho, cuerpo, cierre);
     * no hace falta observarlos como Flow, se leen una vez para armar el discurso. */
    @Query("SELECT * FROM bloque_contenido WHERE temaId = :temaId ORDER BY orden")
    suspend fun obtenerPorTema(temaId: Long): List<BloqueContenidoEntity>
}
