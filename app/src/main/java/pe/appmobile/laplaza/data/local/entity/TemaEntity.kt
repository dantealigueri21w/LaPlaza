package pe.appmobile.laplaza.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tema",
    foreignKeys = [ForeignKey(
        entity = RinconEntity::class,
        parentColumns = ["id"],
        childColumns = ["rinconId"]
    )],
    indices = [Index("rinconId")]
)
data class TemaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rinconId: String,
    val titulo: String,
    val dificultad: String,
    val orden: Int
)
