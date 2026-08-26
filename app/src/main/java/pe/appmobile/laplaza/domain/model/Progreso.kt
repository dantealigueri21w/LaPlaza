package pe.appmobile.laplaza.domain.model

enum class IdRincon { BALCON, KIOSCO, MOSTRADOR, JARDIN, FUENTE, MIRADOR, TARIMA_MAYOR, LIBRE }

data class SugerenciaRepaso(val temaId: Long, val nombreTema: String, val motivo: String)
