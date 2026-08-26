package pe.appmobile.laplaza.domain.engine

import pe.appmobile.laplaza.domain.model.DiscursoArmado
import pe.appmobile.laplaza.domain.model.Franja
import pe.appmobile.laplaza.domain.model.ResultadoValidacion

object MotorDiscurso {
    private val ordenEsperado = listOf(Franja.GANCHO, Franja.CUERPO, Franja.CIERRE)

    fun validar(discurso: DiscursoArmado): ResultadoValidacion {
        if (discurso.bloques.isEmpty()) {
            return ResultadoValidacion.Invalido("El discurso está vacío")
        }
        if (discurso.bloques.size != ordenEsperado.size) {
            return ResultadoValidacion.Invalido("Un discurso necesita gancho, cuerpo y cierre")
        }
        for ((indice, franjaEsperada) in ordenEsperado.withIndex()) {
            if (discurso.bloques[indice].franja != franjaEsperada) {
                return ResultadoValidacion.Invalido("El bloque ${indice + 1} debería ser $franjaEsperada")
            }
        }
        val bloqueDeOtroTema = discurso.bloques.firstOrNull { it.temaId != discurso.temaId }
        if (bloqueDeOtroTema != null) {
            return ResultadoValidacion.Invalido("Hay un bloque que no pertenece a este tema")
        }
        return ResultadoValidacion.Valido
    }
}
