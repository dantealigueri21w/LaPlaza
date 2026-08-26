package pe.appmobile.laplaza.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** El "Cuaderno de Pregones": un titular coleccionable generado a partir de un intento. */
@Entity(
    tableName = "pregon",
    foreignKeys = [ForeignKey(
        entity = IntentoEntity::class,
        parentColumns = ["id"],
        childColumns = ["intentoId"]
    )],
    indices = [Index("intentoId")]
)
data class PregonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val intentoId: Long,
    val titular: String,
    val variableDestacada: String,
    val fechaEpochMs: Long
)
