package pe.appmobile.laplaza.domain.engine

import pe.appmobile.laplaza.domain.model.DatosIntento
import pe.appmobile.laplaza.domain.model.Pregon

object MotorPregon {

    /** Por debajo de este umbral, ninguna de las 4 variables refleja una senal real de
     * voz: no se detecto hablar durante la declamacion (silencio total o casi total).
     * Sin este chequeo, un intento asi caia en un empate de 0,0,0,0 -- las 4 variables se
     * derivan de RMS/F0 sobre ventanas de voz real, y sin ninguna ventana de voz las 4
     * quedan exactamente en 0 -- y variableMasFuerte elegia "volumen" solo por ser la
     * primera clave del mapa, generando un pregon falso ("Tu voz se escucho en todo...")
     * para un intento donde no se dijo nada. Bug real, encontrado por Rodrigo en su
     * celular incluso despues de la correccion de MotorFluidez (esa corrigio que el
     * silencio no valiera fluidez alta, pero dejo expuesto este empate en 0 con las otras
     * tres variables).
     */
    private const val UMBRAL_SENAL_MINIMA = 0.05f

    fun generar(datos: DatosIntento): Pregon {
        if (!huboVozReal(datos)) {
            return Pregon(
                titular = "No te escuchamos esta vez en ${datos.nombreRincon}. ¿Lo intentamos de nuevo?",
                variableDestacada = "silencio"
            )
        }
        val variableDestacada = variableMasFuerte(datos)
        val titular = when (variableDestacada) {
            "volumen" -> "Tu voz se escuchó en todo ${datos.nombreRincon} durante \"${datos.nombreTema}\""
            "entonacion" -> "Le diste vida con la voz a \"${datos.nombreTema}\" en ${datos.nombreRincon}"
            "fluidez" -> "Dijiste \"${datos.nombreTema}\" sin cortes, de principio a fin"
            else -> "Mantuviste un buen ritmo contando \"${datos.nombreTema}\" en ${datos.nombreRincon}"
        }
        return Pregon(titular = titular, variableDestacada = variableDestacada)
    }

    /** true si al menos una de las 4 variables muestra una senal real de voz -- los
     * "altos y bajos" del volumen y la entonacion, y su reflejo en ritmo y fluidez --, no
     * solo un empate en cero. */
    internal fun huboVozReal(datos: DatosIntento): Boolean {
        val maximo = maxOf(datos.puntajeVolumen, datos.puntajeEntonacion, datos.puntajeRitmo, datos.puntajeFluidez)
        return maximo >= UMBRAL_SENAL_MINIMA
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
