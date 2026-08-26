package pe.appmobile.laplaza.domain.engine

import pe.appmobile.laplaza.domain.model.NivelAtencion
import pe.appmobile.laplaza.domain.model.PuntajeAudiencia
import pe.appmobile.laplaza.domain.model.ResultadoAcustico

object MotorPuntajeAudiencia {
    private const val RITMO_IDEAL_MIN = 90f
    private const val RITMO_IDEAL_MAX = 160f
    private const val VARIACION_RICA_SEMITONOS = 6f

    fun calcularPuntajeVentana(volumen: Float, variacionEntonacion: Float, esVoz: Boolean): Float {
        if (!esVoz) return 0f
        val puntajeVolumen = volumen.coerceIn(0f, 1f)
        val puntajeEntonacion = (variacionEntonacion / VARIACION_RICA_SEMITONOS).coerceIn(0f, 1f)
        return (puntajeVolumen * 0.6f + puntajeEntonacion * 0.4f).coerceIn(0f, 1f)
    }

    fun calcularPuntajeRitmo(ritmoSilabasPorMinuto: Float): Float {
        if (ritmoSilabasPorMinuto <= 0f) return 0f
        return when {
            ritmoSilabasPorMinuto < RITMO_IDEAL_MIN -> (ritmoSilabasPorMinuto / RITMO_IDEAL_MIN).coerceIn(0f, 1f)
            ritmoSilabasPorMinuto > RITMO_IDEAL_MAX -> (RITMO_IDEAL_MAX / ritmoSilabasPorMinuto).coerceIn(0f, 1f)
            else -> 1f
        }
    }

    fun calcularPuntajeCompuesto(resultado: ResultadoAcustico, fluidez: Float): PuntajeAudiencia {
        val puntajeVolumen = resultado.volumenPromedio.coerceIn(0f, 1f)
        val puntajeEntonacion = (resultado.variacionEntonacionSemitonos / VARIACION_RICA_SEMITONOS).coerceIn(0f, 1f)
        val puntajeRitmo = calcularPuntajeRitmo(resultado.ritmoSilabasPorMinuto)
        val compuesto = (puntajeVolumen * 0.3f + puntajeEntonacion * 0.25f + puntajeRitmo * 0.2f + fluidez.coerceIn(0f, 1f) * 0.25f)
            .coerceIn(0f, 1f)
        val nivel = when {
            compuesto < 0.4f -> NivelAtencion.DISTRAIDA
            compuesto < 0.75f -> NivelAtencion.ATENTA
            else -> NivelAtencion.ENTUSIASMADA
        }
        return PuntajeAudiencia(compuesto, nivel)
    }
}
