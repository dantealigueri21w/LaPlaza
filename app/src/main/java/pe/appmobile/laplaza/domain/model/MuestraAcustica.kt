package pe.appmobile.laplaza.domain.model

data class MuestraAcustica(
    val rms: Float,
    val f0Hz: Float,
    val esVoz: Boolean
)

data class Pausa(val inicioMs: Long, val duracionMs: Long)
