package pe.appmobile.laplaza.domain.model

enum class Franja { GANCHO, CUERPO, CIERRE }

data class BloqueDiscurso(val id: Long, val temaId: Long, val franja: Franja, val texto: String)

data class DiscursoArmado(val temaId: Long, val bloques: List<BloqueDiscurso>)

sealed class ResultadoValidacion {
    object Valido : ResultadoValidacion()
    data class Invalido(val motivo: String) : ResultadoValidacion()
}
