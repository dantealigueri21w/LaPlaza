package pe.appmobile.laplaza.domain.engine

import pe.appmobile.laplaza.domain.model.IdRincon
import pe.appmobile.laplaza.domain.model.SugerenciaRepaso

object MotorProgreso {

    private val SEIS_PREVIOS = setOf(
        IdRincon.BALCON, IdRincon.KIOSCO, IdRincon.MOSTRADOR,
        IdRincon.JARDIN, IdRincon.FUENTE, IdRincon.MIRADOR
    )

    fun rinconesDesbloqueados(completados: Set<IdRincon>): Set<IdRincon> {
        val desbloqueados = mutableSetOf(IdRincon.BALCON, IdRincon.LIBRE)
        if (IdRincon.BALCON in completados) desbloqueados.add(IdRincon.KIOSCO)
        if (IdRincon.KIOSCO in completados) desbloqueados.add(IdRincon.MOSTRADOR)
        if (IdRincon.MOSTRADOR in completados) {
            desbloqueados.add(IdRincon.JARDIN)
            desbloqueados.add(IdRincon.FUENTE)
        }
        if (IdRincon.JARDIN in completados || IdRincon.FUENTE in completados) {
            desbloqueados.add(IdRincon.MIRADOR)
        }
        if (completados.containsAll(SEIS_PREVIOS)) {
            desbloqueados.add(IdRincon.TARIMA_MAYOR)
        }
        return desbloqueados
    }

    fun calcularRacha(fechasConIntentoEpochDia: List<Long>, hoyEpochDia: Long): Int {
        if (fechasConIntentoEpochDia.isEmpty()) return 0
        val diasUnicos = fechasConIntentoEpochDia.toSortedSet(compareByDescending { it })
        var racha = 0
        var diaEsperado = hoyEpochDia
        for (dia in diasUnicos) {
            if (dia == diaEsperado) {
                racha++
                diaEsperado--
            } else {
                break
            }
        }
        return racha
    }

    fun sugerirRepaso(intentosRecientes: List<Triple<Long, String, Float>>): SugerenciaRepaso? {
        val peor = intentosRecientes.minByOrNull { it.third } ?: return null
        return SugerenciaRepaso(
            temaId = peor.first,
            nombreTema = peor.second,
            motivo = if (peor.third < 0.4f) {
                "la última vez se apagó la voz a mitad de camino"
            } else {
                "todavía puede sonar más firme"
            }
        )
    }
}
