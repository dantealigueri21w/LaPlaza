package pe.appmobile.laplaza.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "intento",
    foreignKeys = [ForeignKey(
        entity = TemaEntity::class,
        parentColumns = ["id"],
        childColumns = ["temaId"]
    )],
    indices = [Index("temaId")]
)
data class IntentoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val temaId: Long,
    val fechaEpochMs: Long,
    val esRepaso: Boolean,
    val puntajeVolumen: Float,
    val puntajeEntonacion: Float,
    val puntajeRitmo: Float,
    val puntajeFluidez: Float,
    val puntajeCompuesto: Float
)
