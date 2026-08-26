package pe.appmobile.laplaza.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Fila unica (id fijo = 1): la racha de dias seguidos practicando en la plaza. */
@Entity(tableName = "racha")
data class RachaEntity(
    @PrimaryKey val id: Int = 1,
    val diasSeguidos: Int,
    val ultimoDiaEpoch: Long
)
