package pe.appmobile.laplaza.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** [id] son los nombres del enum IdRincon del dominio como String: BALCON, KIOSCO,
 * MOSTRADOR, JARDIN, FUENTE, MIRADOR, TARIMA_MAYOR, LIBRE. */
@Entity(tableName = "rincon")
data class RinconEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val descripcion: String,
    val orden: Int,
    val completado: Boolean = false
)
