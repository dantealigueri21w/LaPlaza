package pe.appmobile.laplaza.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** [fechaObtenidaEpochMs] null significa que todavia no se gano esta insignia. */
@Entity(tableName = "insignia")
data class InsigniaEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val descripcion: String,
    val fechaObtenidaEpochMs: Long? = null
)
