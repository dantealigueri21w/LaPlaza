package pe.appmobile.laplaza.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** [franja] guarda el nombre del enum Franja del dominio como String: GANCHO, CUERPO, CIERRE. */
@Entity(
    tableName = "bloque_contenido",
    foreignKeys = [ForeignKey(
        entity = TemaEntity::class,
        parentColumns = ["id"],
        childColumns = ["temaId"]
    )],
    indices = [Index("temaId")]
)
data class BloqueContenidoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val temaId: Long,
    val franja: String,
    val texto: String,
    val orden: Int
)
