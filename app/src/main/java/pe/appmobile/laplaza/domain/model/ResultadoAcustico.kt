package pe.appmobile.laplaza.domain.model

data class ResultadoAcustico(
    val volumenPromedio: Float,
    val variacionEntonacionSemitonos: Float,
    val ritmoSilabasPorMinuto: Float,
    val pausas: List<Pausa>,
    val duracionTotalMs: Long
)

enum class NivelAtencion { DISTRAIDA, ATENTA, ENTUSIASMADA }

data class PuntajeAudiencia(
    val puntajeCompuesto: Float,
    val nivel: NivelAtencion
)
