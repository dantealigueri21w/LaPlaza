package pe.appmobile.laplaza.domain.engine

import pe.appmobile.laplaza.domain.model.Pausa

object MotorFluidez {
    private const val PAUSA_LIMPIA_MIN_MS = 200L
    private const val PAUSA_LIMPIA_MAX_MS = 900L
    private const val VENTANA_TITUBEO_MS = 1500L

    fun calcularFluidez(pausas: List<Pausa>, duracionTotalMs: Long): Float {
        if (duracionTotalMs <= 0L) return 0f
        if (pausas.isEmpty()) return 1f

        val grupos = agruparPausasCercanas(pausas, VENTANA_TITUBEO_MS)
        var penalizacion = 0f
        for (grupo in grupos) {
            if (grupo.size == 1) {
                val pausa = grupo[0]
                if (pausa.duracionMs !in PAUSA_LIMPIA_MIN_MS..PAUSA_LIMPIA_MAX_MS) {
                    penalizacion += 0.05f
                }
            } else {
                penalizacion += 0.15f * (grupo.size - 1)
            }
        }
        return (1f - penalizacion).coerceIn(0f, 1f)
    }

    fun agruparPausasCercanas(pausas: List<Pausa>, ventanaMs: Long): List<List<Pausa>> {
        if (pausas.isEmpty()) return emptyList()
        val ordenadas = pausas.sortedBy { it.inicioMs }
        val grupos = mutableListOf(mutableListOf(ordenadas[0]))
        for (i in 1 until ordenadas.size) {
            val anterior = grupos.last().last()
            val actual = ordenadas[i]
            val distancia = actual.inicioMs - (anterior.inicioMs + anterior.duracionMs)
            if (distancia <= ventanaMs) {
                grupos.last().add(actual)
            } else {
                grupos.add(mutableListOf(actual))
            }
        }
        return grupos
    }
}
