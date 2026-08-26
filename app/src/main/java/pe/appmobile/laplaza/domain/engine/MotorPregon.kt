package pe.appmobile.laplaza.domain.engine

import pe.appmobile.laplaza.domain.model.DatosIntento
import pe.appmobile.laplaza.domain.model.Pregon

object MotorPregon {

    fun generar(datos: DatosIntento): Pregon {
        val variableDestacada = variableMasFuerte(datos)
        val titular = when (variableDestacada) {
            "volumen" -> "Tu voz se escuchó en todo ${datos.nombreRincon} durante \"${datos.nombreTema}\""
            "entonacion" -> "Le diste vida con la voz a \"${datos.nombreTema}\" en ${datos.nombreRincon}"
            "fluidez" -> "Dijiste \"${datos.nombreTema}\" sin cortes, de principio a fin"
            else -> "Mantuviste un buen ritmo contando \"${datos.nombreTema}\" en ${datos.nombreRincon}"
        }
        return Pregon(titular = titular, variableDestacada = variableDestacada)
    }

    internal fun variableMasFuerte(datos: DatosIntento): String {
        val puntajes = linkedMapOf(
            "volumen" to datos.puntajeVolumen,
            "entonacion" to datos.puntajeEntonacion,
            "ritmo" to datos.puntajeRitmo,
            "fluidez" to datos.puntajeFluidez
        )
        return puntajes.maxByOrNull { it.value }?.key ?: "ritmo"
    }
}
