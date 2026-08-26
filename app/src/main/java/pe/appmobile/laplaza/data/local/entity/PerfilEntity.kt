package pe.appmobile.laplaza.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "perfil")
data class PerfilEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alias: String,
    val avatarId: Int,
    val sonidoActivado: Boolean = true,
    val hapticaActivada: Boolean = true
)
